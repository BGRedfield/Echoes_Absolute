package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.entities.TitaBoss;
import io.github.some_example_name.entities.TitaBossProjectile;
import io.github.some_example_name.managers.AssetManager;

/**
 * Titan phase:
 * three mini-castles give red/blue/yellow crystals and three altars unlock
 * the final castle containing CR7.
 */
public class titascreen extends ScreenAdapter {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float WORLD_WIDTH = 3000f;
    private static final float WORLD_HEIGHT = 2000f;

    private static final float BASE_X = 1320f;
    private static final float BASE_Y = 910f;
    private static final float BASE_WIDTH = 260f;
    private static final float BASE_HEIGHT = 175f;

    private static final float CASTLE_WIDTH = 360f;
    private static final float CASTLE_HEIGHT = 280f;

    private static final float TOP_CASTLE_X = 1320f;
    private static final float TOP_CASTLE_Y = 1650f;

    private static final float BOTTOM_CASTLE_X = 1320f;
    private static final float BOTTOM_CASTLE_Y = 80f;

    private static final float LEFT_CASTLE_X = 210f;
    private static final float LEFT_CASTLE_Y = 860f;

    private static final float FINAL_CASTLE_X = 2280f;
    private static final float FINAL_CASTLE_Y = 860f;

    private static final float TILE_SIZE = 128f;
    private static final float FIRE_INTERVAL = 0.12f;
    private static final float BASE_RECOVERY_INTERVAL = 1f;

    private static final float ARENA_MIN_X = 760f;
    private static final float ARENA_MIN_Y = 520f;
    private static final float ARENA_MAX_X = 2240f;
    private static final float ARENA_MAX_Y = 1480f;

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ScreenViewport hudViewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final AssetManager assets;
    private final Player player;
    private final PlayerStats stats;
    private final PauseMenu pauseMenu;

    private final Array<Laser> lasers = new Array<>();
    private final Array<TitaBossProjectile> bossProjectiles = new Array<>();

    private final Rectangle[] castleDoors = {
            new Rectangle(TOP_CASTLE_X + CASTLE_WIDTH / 2f - 45f, TOP_CASTLE_Y - 10f, 90f, 55f),
            new Rectangle(BOTTOM_CASTLE_X + CASTLE_WIDTH / 2f - 45f, BOTTOM_CASTLE_Y - 10f, 90f, 55f),
            new Rectangle(LEFT_CASTLE_X + CASTLE_WIDTH - 10f, LEFT_CASTLE_Y + CASTLE_HEIGHT / 2f - 45f, 55f, 90f),
            new Rectangle(FINAL_CASTLE_X + CASTLE_WIDTH / 2f - 55f, FINAL_CASTLE_Y - 10f, 110f, 60f)
    };

    private final Rectangle[] altars = {
            new Rectangle(1780f, 865f, 105f, 115f),
            new Rectangle(1940f, 865f, 105f, 115f),
            new Rectangle(2100f, 865f, 105f, 115f)
    };

    private final boolean[] crystalOwned = new boolean[3];
    private final boolean[] crystalPlaced = new boolean[3];
    private final boolean[] bossDefeated = new boolean[4];
    private final TitaBoss[] bosses = new TitaBoss[4];
    private final Rectangle[] crystalDrops = new Rectangle[3];

    private int currentCastle = -1;
    private boolean insideCastle;
    private boolean changingScreen;
    private boolean disposed;
    private boolean finalCastleUnlocked;

    private float fireTimer;
    private float baseRecoveryTimer;
    private float messageTimer;
    private String message = "";

    public titascreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        hudViewport = new ScreenViewport();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        assets = new AssetManager();
        assets.load();
        player = new Player(BASE_X + 120f, BASE_Y + 50f);
        stats = new PlayerStats();
        pauseMenu = new PauseMenu(game);

        for (int i = 0; i < crystalDrops.length; i++) {
            crystalDrops[i] = new Rectangle();
        }

        showMessage("TITÃ: derrote os 3 chefes, pegue os cristais e abra o castelo final.");
        updateCamera();
    }

    @Override
    public void show() {
        changingScreen = false;
        viewport.apply(true);
        hudViewport.apply(true);
        updateCamera();
    }

    private boolean update(float delta) {
        delta = Math.min(delta, 0.05f);

        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);

        if (insideCastle) {
            clampPlayerToArena();

            TitaBoss boss = bosses[currentCastle];
            if (boss != null && !boss.isDead()) {
                boss.update(
                        delta,
                        player.getCenterX(),
                        player.getCenterY(),
                        stats,
                        ARENA_MIN_X,
                        ARENA_MIN_Y,
                        ARENA_MAX_X,
                        ARENA_MAX_Y
                );

                if (boss.canShoot()) {
                    spawnBossProjectiles(boss);
                    boss.resetAttackTimer();
                }
            }

            updateBossProjectiles(delta);
            updateLasers(delta);
            handleCastleCrystalPickup();

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)
                    && playerNearArenaDoor()
                    && (boss == null || boss.isDead())) {
                leaveCastle();
            }
        } else {
            handleExteriorInteractions();
            updateLasers(delta);
            recoverAtBase(delta);
        }

        stats.update(delta);

        if (stats.isDead()) {
            changingScreen = true;
            DeathCause cause = stats.getDeathCause();
            dispose();
            game.setScreen(new GameOverScreen(game, cause, 3));
            return false;
        }

        updateCamera();
        return true;
    }

    private void recoverAtBase(float delta) {
        Rectangle base = new Rectangle(BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);
        if (!player.getHitbox().overlaps(base)) {
            baseRecoveryTimer = 0f;
            return;
        }

        baseRecoveryTimer += delta;
        while (baseRecoveryTimer >= BASE_RECOVERY_INTERVAL) {
            baseRecoveryTimer -= BASE_RECOVERY_INTERVAL;
            stats.addOxygen(5f);
            stats.addHunger(5f);
        }
    }

    private void handleExteriorInteractions() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (tryUseAltar()) {
                return;
            }

            for (int i = 0; i < castleDoors.length; i++) {
                if (!player.getHitbox().overlaps(castleDoors[i])) {
                    continue;
                }

                if (i == 3 && !finalCastleUnlocked) {
                    showMessage("O castelo final está trancado. Coloque os 3 cristais nos altares.");
                    return;
                }

                enterCastle(i);
                return;
            }
        }
    }

    private boolean tryUseAltar() {
        for (int i = 0; i < altars.length; i++) {
            if (!player.getHitbox().overlaps(altars[i])) {
                continue;
            }

            if (crystalPlaced[i]) {
                showMessage("Este altar já recebeu seu cristal.");
                return true;
            }

            if (!crystalOwned[i]) {
                showMessage("Você ainda não possui o cristal desta cor.");
                return true;
            }

            crystalOwned[i] = false;
            crystalPlaced[i] = true;
            showMessage("Cristal colocado no altar " + (i + 1) + "/3.");

            if (crystalPlaced[0] && crystalPlaced[1] && crystalPlaced[2]) {
                finalCastleUnlocked = true;
                showMessage("OS 3 CRISTAIS ATIVARAM O CASTELO FINAL! Vá até a grande porta.");
            }
            return true;
        }

        return false;
    }

    private void enterCastle(int castleIndex) {
        currentCastle = castleIndex;
        insideCastle = true;

        player.getHitbox().set(
                (ARENA_MIN_X + ARENA_MAX_X) / 2f - player.getWidth() / 2f,
                ARENA_MIN_Y + 140f,
                player.getWidth(),
                player.getHeight()
        );

        bossProjectiles.clear();
        lasers.clear();

        if (bosses[castleIndex] == null) {
            TitaBoss.Type type;
            switch (castleIndex) {
                case 0:
                    type = TitaBoss.Type.OBAMA;
                    break;
                case 1:
                    type = TitaBoss.Type.AUTHENTIC_GAMES;
                    break;
                case 2:
                    type = TitaBoss.Type.VERITY;
                    break;
                case 3:
                default:
                    type = TitaBoss.Type.CR7;
                    break;
            }

            bosses[castleIndex] = new TitaBoss(
                    type,
                    (ARENA_MIN_X + ARENA_MAX_X) / 2f - 75f,
                    ARENA_MIN_Y + 470f
            );
        }

        String title = bosses[castleIndex].getName();
        if (bosses[castleIndex].isDead()) {
            showMessage(title + " já foi derrotado. Volte pela porta.");
        } else {
            showMessage("Você entrou no castelo: " + title + ". Derrote o chefe!");
        }
    }

    private void leaveCastle() {
        insideCastle = false;
        int index = currentCastle;
        currentCastle = -1;
        bossProjectiles.clear();
        lasers.clear();

        switch (index) {
            case 0:
                player.getHitbox().set(TOP_CASTLE_X + CASTLE_WIDTH / 2f - player.getWidth() / 2f,
                        TOP_CASTLE_Y - 105f, player.getWidth(), player.getHeight());
                break;
            case 1:
                player.getHitbox().set(BOTTOM_CASTLE_X + CASTLE_WIDTH / 2f - player.getWidth() / 2f,
                        BOTTOM_CASTLE_Y + CASTLE_HEIGHT + 30f, player.getWidth(), player.getHeight());
                break;
            case 2:
                player.getHitbox().set(LEFT_CASTLE_X + CASTLE_WIDTH + 65f,
                        LEFT_CASTLE_Y + CASTLE_HEIGHT / 2f - player.getHeight() / 2f,
                        player.getWidth(), player.getHeight());
                break;
            case 3:
            default:
                player.getHitbox().set(FINAL_CASTLE_X + CASTLE_WIDTH / 2f - player.getWidth() / 2f,
                        FINAL_CASTLE_Y - 110f, player.getWidth(), player.getHeight());
                break;
        }

        showMessage("Você saiu do castelo.");
    }

    private boolean playerNearArenaDoor() {
        Rectangle door = new Rectangle(
                ARENA_MIN_X + 480f,
                ARENA_MIN_Y + 5f,
                320f,
                100f
        );
        return player.getHitbox().overlaps(door);
    }

    private void handleCastleCrystalPickup() {
        if (currentCastle < 0 || currentCastle > 2 || bosses[currentCastle] == null) {
            return;
        }

        TitaBoss boss = bosses[currentCastle];
        if (!boss.isDead() || crystalOwned[currentCastle] || crystalPlaced[currentCastle]) {
            return;
        }

        Rectangle drop = crystalDrops[currentCastle];

        if (drop.width <= 0f) {
            drop.set(boss.getCenterX() - 32f, boss.getCenterY() - 32f, 64f, 64f);
        }

        if (player.getHitbox().overlaps(drop)) {
            crystalOwned[currentCastle] = true;
            showMessage("CRISTAL " + crystalName(currentCastle) + " COLETADO! Volte para a superfície.");
        }
    }

    private void spawnBossProjectiles(TitaBoss boss) {
        int count = boss.getProjectileCount();
        float damage = boss.getProjectileDamage();
        float centerX = boss.getCenterX();
        float centerY = boss.getCenterY();

        for (int i = 0; i < count; i++) {
            float angle = MathUtils.PI2 * i / count;
            bossProjectiles.add(new TitaBossProjectile(
                    centerX,
                    centerY,
                    MathUtils.cos(angle),
                    MathUtils.sin(angle),
                    damage
            ));
        }
    }

    private void updateBossProjectiles(float delta) {
        for (int i = bossProjectiles.size - 1; i >= 0; i--) {
            TitaBossProjectile projectile = bossProjectiles.get(i);
            projectile.update(delta);

            if (projectile.hitsPlayer(player)) {
                stats.damage(projectile.getDamage(), DeathCause.UNKNOWN);
                bossProjectiles.removeIndex(i);
                continue;
            }

            if (projectile.isOutside(WORLD_WIDTH, WORLD_HEIGHT)) {
                bossProjectiles.removeIndex(i);
            }
        }
    }

    private void updateLasers(float delta) {
        fireTimer -= delta;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && fireTimer <= 0f) {
            Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(mouseWorld);

            float dx = mouseWorld.x - player.getCenterX();
            float dy = mouseWorld.y - player.getCenterY();
            float length = (float) Math.sqrt(dx * dx + dy * dy);

            if (length > 0.001f) {
                dx /= length;
                dy /= length;

                lasers.add(new Laser(player.getCenterX(), player.getCenterY(), dx, dy));
                fireTimer = FIRE_INTERVAL;
            }
        }

        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);
            boolean hit = false;

            if (insideCastle && currentCastle >= 0 && currentCastle < bosses.length) {
                TitaBoss boss = bosses[currentCastle];
                if (boss != null && !boss.isDead()
                        && laser.getHitbox().overlaps(boss.getHitbox())) {
                    boss.takeDamage(10f);
                    hit = true;

                    if (boss.isDead()) {
                        bossDefeated[currentCastle] = true;

                        if (currentCastle <= 2) {
                            crystalDrops[currentCastle].set(
                                    boss.getCenterX() - 32f,
                                    boss.getCenterY() - 32f,
                                    64f,
                                    64f
                            );
                            showMessage(boss.getName() + " derrotado! O cristal " + crystalName(currentCastle) + " caiu.");
                        } else {
                            bossProjectiles.clear();
                            showMessage("CR7 DERROTADO! TITÃ FOI CONCLUÍDO.");
                            changingScreen = true;
                            dispose();
                            game.setScreen(new VictoryScreen(game));
                            return;
                        }
                    }
                }
            }

            if (hit || laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                lasers.removeIndex(i);
            }
        }
    }

    private String crystalName(int index) {
        switch (index) {
            case 0:
                return "VERMELHO";
            case 1:
                return "AZUL";
            case 2:
                return "AMARELO";
            default:
                return "DESCONHECIDO";
        }
    }

    private void drawWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (insideCastle) {
            drawArenaFloor();
            drawBoss();
        } else {
            drawExteriorFloor();
            drawExteriorBuildings();
        }

        drawLasers();
        batch.end();

        if (insideCastle) {
            drawArenaEffects();
        }
    }

    private void drawArenaEffects() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.78f, 0.78f, 0.80f, 1f));
        shapeRenderer.rect(ARENA_MIN_X, ARENA_MIN_Y,
                ARENA_MAX_X - ARENA_MIN_X,
                ARENA_MAX_Y - ARENA_MIN_Y);
        shapeRenderer.end();

        drawBossProjectiles();
    }

    private void drawExteriorFloor() {
        Texture tile = assets.getTitaTileTexture();

        for (float x = 0f; x < WORLD_WIDTH; x += TILE_SIZE) {
            for (float y = 0f; y < WORLD_HEIGHT; y += TILE_SIZE) {
                float width = Math.min(TILE_SIZE, WORLD_WIDTH - x);
                float height = Math.min(TILE_SIZE, WORLD_HEIGHT - y);
                batch.draw(tile, x, y, width, height);
            }
        }
    }

    private void drawArenaFloor() {
        Texture tile = assets.getTitaInteriorTileTexture();

        for (float x = ARENA_MIN_X; x < ARENA_MAX_X; x += TILE_SIZE) {
            for (float y = ARENA_MIN_Y; y < ARENA_MAX_Y; y += TILE_SIZE) {
                batch.draw(
                        tile,
                        x,
                        y,
                        Math.min(TILE_SIZE, ARENA_MAX_X - x),
                        Math.min(TILE_SIZE, ARENA_MAX_Y - y)
                );
            }
        }
    }

    private void drawExteriorBuildings() {
        drawCastle(TOP_CASTLE_X, TOP_CASTLE_Y, "CASTELO OBAMA");
        drawCastle(BOTTOM_CASTLE_X, BOTTOM_CASTLE_Y, "CASTELO AUTHENTIC");
        drawCastle(LEFT_CASTLE_X, LEFT_CASTLE_Y, "CASTELO VERITY");
        drawFinalCastle();

        batch.draw(assets.getLunarBaseTexture(), BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);

        for (int i = 0; i < altars.length; i++) {
            batch.draw(assets.getTitaAltarTexture(), altars[i].x, altars[i].y,
                    altars[i].width, altars[i].height);

            if (crystalPlaced[i]) {
                Texture crystal = getCrystalTexture(i);
                batch.draw(crystal, altars[i].x + 22f, altars[i].y + 30f, 60f, 60f);
            }
        }
    }

    private void drawCastle(float x, float y, String label) {
        batch.draw(assets.getTitaCastleTexture(), x, y, CASTLE_WIDTH, CASTLE_HEIGHT);
        batch.draw(assets.getTitaDoorTexture(),
                x + CASTLE_WIDTH / 2f - 45f,
                y - 10f,
                90f,
                75f);
    }

    private void drawFinalCastle() {
        float width = 500f;
        float height = 390f;
        batch.draw(assets.getTitaFinalCastleTexture(), FINAL_CASTLE_X, FINAL_CASTLE_Y, width, height);

        if (finalCastleUnlocked) {
            batch.draw(
                    assets.getTitaDoorTexture(),
                    FINAL_CASTLE_X + width / 2f - 55f,
                    FINAL_CASTLE_Y - 10f,
                    110f,
                    80f
            );
        }
    }

    private void drawBoss() {
        if (currentCastle < 0 || bosses[currentCastle] == null || bosses[currentCastle].isDead()) {
            return;
        }

        Texture texture = getBossTexture(currentCastle);
        TitaBoss boss = bosses[currentCastle];
        batch.draw(texture, boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight());
    }

    private Texture getBossTexture(int index) {
        switch (index) {
            case 0:
                return assets.getObamaTexture();
            case 1:
                return assets.getAuthenticGamesTexture();
            case 2:
                return assets.getVerityTexture();
            case 3:
            default:
                return assets.getCr7Texture();
        }
    }

    private Texture getCrystalTexture(int index) {
        switch (index) {
            case 0:
                return assets.getRedCrystalTexture();
            case 1:
                return assets.getBlueCrystalTexture();
            case 2:
            default:
                return assets.getYellowCrystalTexture();
        }
    }

    private void drawLasers() {
        Texture texture = assets.getLaserTexture();
        for (Laser laser : lasers) {
            batch.draw(texture, laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
        }

        if (!insideCastle) {
            return;
        }

        if (currentCastle >= 0 && currentCastle <= 2
                && bosses[currentCastle] != null
                && bosses[currentCastle].isDead()
                && !crystalOwned[currentCastle]
                && !crystalPlaced[currentCastle]) {
            Texture crystal = getCrystalTexture(currentCastle);
            Rectangle drop = crystalDrops[currentCastle];
            batch.draw(crystal, drop.x, drop.y, drop.width, drop.height);
        }
    }

    private void drawBossProjectiles() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.9f, 0.20f, 0.20f, 1f));

        for (TitaBossProjectile projectile : bossProjectiles) {
            shapeRenderer.circle(
                    projectile.getX() + projectile.getSize() / 2f,
                    projectile.getY() + projectile.getSize() / 2f,
                    projectile.getSize() / 2f
            );
        }

        shapeRenderer.end();
    }

    private void drawHud() {
        hudViewport.apply(false);

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float x = 28f;
        float width = 330f;
        float height = 28f;
        float firstY = hudViewport.getWorldHeight() - 52f;
        float gap = 48f;

        drawBar(x, firstY, width, height, stats.getHealth(), PlayerStats.MAX_HEALTH, Color.RED);
        drawBar(x, firstY - gap, width, height, stats.getHunger(), PlayerStats.MAX_HUNGER, Color.ORANGE);
        drawBar(x, firstY - gap * 2f, width, height, stats.getOxygen(), PlayerStats.MAX_OXYGEN, Color.CYAN);

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        font.getData().setScale(1.02f);
        font.setColor(Color.WHITE);
        font.draw(batch, String.format("HP %.0f/100", stats.getHealth()), x + 10f, firstY + 20f);
        font.draw(batch, String.format("FOME %.0f/100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        font.draw(batch, String.format("O2 %.0f/100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);

        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);
        font.draw(batch, "TITÃ // VERMELHO-ESCURO", 28f, hudViewport.getWorldHeight() - 205f);

        font.getData().setScale(0.90f);
        font.setColor(Color.LIGHT_GRAY);

        if (insideCastle && currentCastle >= 0) {
            TitaBoss boss = bosses[currentCastle];
            font.draw(batch, "CASTELO: " + boss.getName(), 28f, hudViewport.getWorldHeight() - 238f);
            if (boss.isDead()) {
                font.draw(batch, "Chefe derrotado. Vá à porta inferior e pressione E.", 28f,
                        hudViewport.getWorldHeight() - 270f);
            } else {
                font.draw(batch, "Derrote o chefe | Clique/segure = atirar", 28f,
                        hudViewport.getWorldHeight() - 270f);
            }
        } else {
            font.draw(batch, "Base: recupera +5 O2 e +5 fome por segundo", 28f,
                    hudViewport.getWorldHeight() - 238f);

            font.draw(batch,
                    "Cristais: "
                            + (crystalPlaced[0] ? "VERMELHO " : crystalOwned[0] ? "[VERMELHO] " : "- ")
                            + (crystalPlaced[1] ? "AZUL " : crystalOwned[1] ? "[AZUL] " : "- ")
                            + (crystalPlaced[2] ? "AMARELO" : crystalOwned[2] ? "[AMARELO]" : "-"),
                    28f,
                    hudViewport.getWorldHeight() - 270f);

            if (finalCastleUnlocked) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "CASTELO FINAL ABERTO! Derrote CR7.", 28f,
                        hudViewport.getWorldHeight() - 302f);
            } else {
                font.draw(batch, "E = entrar nos mini castelos | E = colocar cristais nos altares", 28f,
                        hudViewport.getWorldHeight() - 302f);
            }
        }

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.86f);
        font.draw(batch, "WASD/SETAS = mover | Mouse = mirar + atirar | E = entrar/interagir | ESC = pausar",
                28f, 18f);

        if (messageTimer > 0f) {
            font.getData().setScale(1.0f);
            font.setColor(Color.WHITE);
            GlyphLayout layout = new GlyphLayout(font, message);
            font.draw(batch, message, hudViewport.getWorldWidth() / 2f - layout.width / 2f, 92f);
        }

        if (insideCastle && currentCastle >= 0) {
            TitaBoss boss = bosses[currentCastle];
            if (boss != null && !boss.isDead()) {
                drawBossHealthText();
            }
        }

        batch.end();

        if (insideCastle && currentCastle >= 0) {
            drawBossHealthBar();
        }

        messageTimer = Math.max(0f, messageTimer - Gdx.graphics.getDeltaTime());
    }

    private void drawBossHealthText() {
        // Text is drawn in drawBossHealthBar to avoid mixing projections.
    }

    private void drawBossHealthBar() {
        TitaBoss boss = bosses[currentCastle];
        float width = hudViewport.getWorldWidth();
        float barWidth = Math.min(850f, width - 120f);
        float barHeight = 26f;
        float x = (width - barWidth) / 2f;
        float y = 36f;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawBar(x, y, barWidth, barHeight, boss.getHealth(), boss.getMaxHealth(), Color.RED);

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);

        String name = boss.getName();
        GlyphLayout nameLayout = new GlyphLayout(font, name);
        font.draw(batch, name, width / 2f - nameLayout.width / 2f, y + 58f);

        font.getData().setScale(0.90f);
        String hp = String.format("HP %.0f/%.0f", boss.getHealth(), boss.getMaxHealth());
        GlyphLayout hpLayout = new GlyphLayout(font, hp);
        font.draw(batch, hp, width / 2f - hpLayout.width / 2f, y - 10f);

        batch.end();
    }

    private void drawBar(float x, float y, float width, float height,
                         float value, float maxValue, Color color) {
        shapeRenderer.setColor(new Color(0.07f, 0.07f, 0.08f, 0.94f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * MathUtils.clamp(value / maxValue, 0f, 1f), height);
    }

    private void clampPlayerToArena() {
        Rectangle box = player.getHitbox();
        box.x = MathUtils.clamp(box.x, ARENA_MIN_X + 30f, ARENA_MAX_X - box.width - 30f);
        box.y = MathUtils.clamp(box.y, ARENA_MIN_Y + 30f, ARENA_MAX_Y - box.height - 30f);
    }

    private void updateCamera() {
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float targetX;
        float targetY;

        if (insideCastle) {
            targetX = MathUtils.clamp(player.getCenterX(), halfWidth, WORLD_WIDTH - halfWidth);
            targetY = MathUtils.clamp(player.getCenterY(), halfHeight, WORLD_HEIGHT - halfHeight);
        } else {
            targetX = MathUtils.clamp(player.getCenterX(), halfWidth, WORLD_WIDTH - halfWidth);
            targetY = MathUtils.clamp(player.getCenterY(), halfHeight, WORLD_HEIGHT - halfHeight);
        }

        camera.position.set(targetX, targetY, 0f);
        camera.update();
    }

    private Texture getCrystalTextureForHud(int index) {
        return getCrystalTexture(index);
    }

    private void showMessage(String text) {
        message = text;
        messageTimer = 4f;
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        PauseMenu.Action pauseAction = pauseMenu.handleInput();

        if (pauseAction == PauseMenu.Action.PHASES) {
            changingScreen = true;
            dispose();
            game.setScreen(new PhaseSelectScreen(game));
            return;
        }

        if (pauseAction == PauseMenu.Action.MENU) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (!pauseMenu.isOpen()) {
            if (!update(delta)) {
                return;
            }
        }

        ScreenUtils.clear(0.22f, 0.015f, 0.025f, 1f);
        drawWorld();
        drawHud();
        pauseMenu.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        pauseMenu.resize(width, height);
        updateCamera();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        assets.dispose();
        pauseMenu.dispose();
    }
}
