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
import io.github.some_example_name.managers.AssetManager;
import io.github.some_example_name.managers.SaveManager;

/**
 * Calisto:
 * corredor de evolução com 5 entidades de luz e um boss final de 3 fases.
 */
public class CalistoScreen extends ScreenAdapter {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float WORLD_WIDTH = 3900f;
    private static final float WORLD_HEIGHT = 1600f;

    private static final float CORRIDOR_MIN_X = 120f;
    private static final float CORRIDOR_MAX_X = 2400f;
    private static final float CORRIDOR_MIN_Y = 360f;
    private static final float CORRIDOR_MAX_Y = 1040f;

    private static final float GATE_X = 2280f;
    private static final float GATE_Y = 500f;
    private static final float GATE_WIDTH = 120f;
    private static final float GATE_HEIGHT = 320f;

    private static final float BOSS_ARENA_MIN_X = 2420f;
    private static final float BOSS_ARENA_MAX_X = 3780f;
    private static final float BOSS_ARENA_MIN_Y = 90f;
    private static final float BOSS_ARENA_MAX_Y = 1510f;

    private static final float BOSS_X = 3360f;
    private static final float BOSS_Y = 660f;
    private static final float BOSS_SIZE = 280f;
    private static final float BOSS_MAX_HEALTH = 18000f;

    private static final float[] ANGEL_X = {
            520f, 870f, 1220f, 1570f, 1920f
    };
    private static final float ANGEL_Y = 650f;
    private static final float ANGEL_SIZE = 100f;

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

    private final Rectangle gate = new Rectangle(
            GATE_X, GATE_Y, GATE_WIDTH, GATE_HEIGHT
    );
    private final Rectangle bossHitbox = new Rectangle(
            BOSS_X, BOSS_Y, BOSS_SIZE, BOSS_SIZE
    );

    private final Rectangle finalKeyHitbox = new Rectangle();
    private final Rectangle finalPortalHitbox = new Rectangle();

    private static final float FINAL_KEY_SIZE = 82f;
    private static final float FINAL_PORTAL_SIZE = 170f;
    private final Rectangle[] angels = new Rectangle[5];
    private final boolean[] angelBlessings = new boolean[5];
    private final Array<Laser> lasers = new Array<>();
    private final Array<CalistoProjectile> projectiles = new Array<>();

    private boolean inBossArena;
    private boolean changingScreen;
    private boolean disposed;

    private boolean tripleShot;
    private float fireInterval = 0.14f;
    private float laserDamage = 10f;
    private float fireTimer;
    private float shieldMultiplier = 1f;

    private float bossHealth = BOSS_MAX_HEALTH;
    private boolean bossDefeated;
    private boolean finalKeySpawned;
    private boolean finalKeyCollected;
    private boolean finalPortalActive;
    private int bossPhase = 1;
    private int bossAttackIndex;
    private int lastDisplayedPhase;
    private float bossAttackTimer = 1.4f;

    private String message = "";
    private float messageTimer;

    public CalistoScreen(Game game) {
        this(game, null);
    }

    public CalistoScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        hudViewport = new ScreenViewport();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        assets = new AssetManager();
        assets.load();

        player = new Player(210f, 620f);
        stats = new PlayerStats();
        pauseMenu = new PauseMenu(game);

        for (int i = 0; i < angels.length; i++) {
            angels[i] = new Rectangle(
                    ANGEL_X[i] - ANGEL_SIZE / 2f,
                    ANGEL_Y - ANGEL_SIZE / 2f,
                    ANGEL_SIZE,
                    ANGEL_SIZE
            );
        }

        if (saveData != null) {
            applySave(saveData);
            showMessage("SAVE CARREGADO: corredor de Calisto.");
        } else {
            showMessage("CALISTO: converse com as 5 entidades de luz antes do portão.");
        }

        updateCamera();
    }

    private void applySave(SaveManager.SaveData data) {
        player.getHitbox().set(data.playerX, data.playerY,
                player.getWidth(), player.getHeight());
        stats.setHealth(data.health);
        stats.setHunger(data.hunger);
        stats.setOxygen(data.oxygen);

        for (int i = 0; i < 5; i++) {
            angelBlessings[i] = data.calistoAngelBlessings[i];
        }

        bossHealth = MathUtils.clamp(
                data.calistoBossHealth,
                0f,
                BOSS_MAX_HEALTH
        );
        bossDefeated = data.calistoBossDefeated;
        finalKeySpawned = data.calistoFinalKeySpawned || bossDefeated;
        finalKeyCollected = data.calistoFinalKeyCollected;
        finalPortalActive = finalKeyCollected;
        bossPhase = MathUtils.clamp(data.calistoBossPhase, 1, 3);

        if (finalKeySpawned && !finalKeyCollected) {
            spawnFinalKey();
        }
        if (finalPortalActive) {
            activateFinalPortal();
        }

        updateUpgradesFromBlessings();
        stats.setSurvivalNeedsDisabled(angelBlessings[2]);
    }

    private void updateUpgradesFromBlessings() {
        tripleShot = angelBlessings[0];
        fireInterval = angelBlessings[1] ? 0.065f : 0.14f;
        shieldMultiplier = angelBlessings[3] ? 0.65f : 1f;
        laserDamage = angelBlessings[4] ? 18f : 10f;
    }

    private void saveGame() {
        SaveManager.SaveData data = new SaveManager.SaveData();
        data.phase = SaveManager.Phase.CALISTO;
        data.playerX = player.getX();
        data.playerY = player.getY();
        data.health = stats.getHealth();
        data.hunger = stats.getHunger();
        data.oxygen = stats.getOxygen();

        for (int i = 0; i < 5; i++) {
            data.calistoAngelBlessings[i] = angelBlessings[i];
        }

        data.calistoBossHealth = bossHealth;
        data.calistoBossDefeated = bossDefeated;
        data.calistoBossPhase = bossPhase;
        data.calistoFinalKeySpawned = finalKeySpawned;
        data.calistoFinalKeyCollected = finalKeyCollected;

        SaveManager.save(data);
        showMessage("JOGO SALVO! Você poderá carregar Calisto pelo menu.");
    }

    private boolean allBlessingsCollected() {
        for (boolean blessing : angelBlessings) {
            if (!blessing) {
                return false;
            }
        }
        return true;
    }

    private void update(float delta) {
        delta = Math.min(delta, 0.05f);

        if (!inBossArena) {
            player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
            clampPlayerToCorridor();
            handleCorridorInteractions();
        } else {
            player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
            clampPlayerToBossArena();
            updateBoss(delta);
        }

        updateLasers(delta);
        updateProjectiles(delta);

        stats.update(delta);

        if (stats.isDead()) {
            changingScreen = true;
            DeathCause cause = stats.getDeathCause();
            dispose();
            game.setScreen(new GameOverScreen(game, cause, 4));
            return;
        }

        if (inBossArena && bossDefeated) {
            handleFinalSequence();
        }

        updateCamera();
        messageTimer = Math.max(0f, messageTimer - delta);
    }

    private void handleCorridorInteractions() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            return;
        }

        for (int i = 0; i < angels.length; i++) {
            if (!angelBlessings[i] && player.getHitbox().overlaps(angels[i])) {
                receiveBlessing(i);
                return;
            }
        }

        if (player.getHitbox().overlaps(gate)) {
            if (!allBlessingsCollected()) {
                showMessage("O portão está fechado. As 5 entidades ainda não entregaram suas bênçãos.");
                return;
            }

            inBossArena = true;
            projectiles.clear();
            lasers.clear();
            player.getHitbox().set(
                    BOSS_ARENA_MIN_X + 150f,
                    BOSS_ARENA_MIN_Y + 520f,
                    player.getWidth(),
                    player.getHeight()
            );
            bossAttackTimer = 0.9f;
            bossAttackIndex = 0;
            showMessage("PORTÃO PRINCIPAL ABERTO. O BOSS FINAL DE CALISTO SURGIU.");
        }
    }

    private void receiveBlessing(int index) {
        angelBlessings[index] = true;
        updateUpgradesFromBlessings();

        stats.heal(25f);
        stats.addOxygen(25f);
        stats.addHunger(25f);
        stats.setSurvivalNeedsDisabled(angelBlessings[2]);

        switch (index) {
            case 0:
                showMessage("ANJO I: BÊNÇÃO DO CAMINHO — TIRO TRIPLO DESBLOQUEADO.");
                break;
            case 1:
                showMessage("ANJO II: PULSO DA LUZ — CADÊNCIA MUITO MAIS RÁPIDA.");
                break;
            case 2:
                showMessage("ANJO III: SACIAÇÃO — FOME e O2 não diminuem mais.");
                break;
            case 3:
                showMessage("ANJO IV: MANTO DE LUZ — dano recebido reduzido em 35%.");
                break;
            case 4:
            default:
                showMessage("ANJO V: LANÇA CELESTE — dano do laser aumentado.");
                break;
        }
    }

    private void updateLasers(float delta) {
        fireTimer -= delta;

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || fireTimer > 0f) {
            updateLaserMovement(delta);
            return;
        }

        Vector3 mouseWorld = new Vector3(
                Gdx.input.getX(),
                Gdx.input.getY(),
                0f
        );
        camera.unproject(mouseWorld);

        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length <= 0.001f) {
            return;
        }

        dx /= length;
        dy /= length;

        if (tripleShot) {
            addLaser(dx, dy, 0.11f);
            addLaser(dx, dy, 0f);
            addLaser(dx, dy, -0.11f);
        } else {
            addLaser(dx, dy, 0f);
        }

        fireTimer = fireInterval;
        updateLaserMovement(delta);
    }

    private void addLaser(float dx, float dy, float angle) {
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float rx = dx * cos - dy * sin;
        float ry = dx * sin + dy * cos;

        lasers.add(new Laser(
                player.getCenterX(),
                player.getCenterY(),
                rx,
                ry
        ));
    }

    private void updateLaserMovement(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);

            if (inBossArena
                    && !bossDefeated
                    && laser.getHitbox().overlaps(bossHitbox)) {
                bossHealth = Math.max(0f, bossHealth - laserDamage);

                if (bossHealth <= 0f) {
                    bossDefeated = true;
                    projectiles.clear();
                    finalKeySpawned = false;
                    finalKeyCollected = false;
                    finalPortalActive = false;
                    showMessage("BOSS FINAL DERROTADO! A Chave de Luz vai surgir...");
                    saveGame();
                }

                lasers.removeIndex(i);
                continue;
            }

            if (laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                lasers.removeIndex(i);
            }
        }
    }

    private void handleFinalSequence() {
        if (!finalKeySpawned) {
            spawnFinalKey();
            return;
        }

        if (!finalKeyCollected
                && player.getHitbox().overlaps(finalKeyHitbox)) {
            finalKeyCollected = true;
            finalPortalActive = true;
            activateFinalPortal();
            showMessage("CHAVE DE LUZ COLETADA! A passagem final foi aberta.");
            saveGame();
            return;
        }

        if (finalPortalActive
                && player.getHitbox().overlaps(finalPortalHitbox)) {
            changingScreen = true;
            dispose();
            game.setScreen(new CalistoInfoScreen(game));
        }
    }

    private void spawnFinalKey() {
        finalKeySpawned = true;
        finalKeyCollected = false;
        finalPortalActive = false;

        finalKeyHitbox.set(
                BOSS_X + BOSS_SIZE / 2f - FINAL_KEY_SIZE / 2f,
                BOSS_Y - 150f,
                FINAL_KEY_SIZE,
                FINAL_KEY_SIZE
        );

        showMessage("A CHAVE DE LUZ surgiu. Encoste nela para abrir o caminho final.");
    }

    private void activateFinalPortal() {
        finalPortalHitbox.set(
                BOSS_ARENA_MIN_X + 95f,
                BOSS_ARENA_MIN_Y + (BOSS_ARENA_MAX_Y - BOSS_ARENA_MIN_Y) / 2f
                        - FINAL_PORTAL_SIZE / 2f,
                FINAL_PORTAL_SIZE,
                FINAL_PORTAL_SIZE
        );
    }

    private void updateBoss(float delta) {
        if (bossDefeated) {
            return;
        }

        int newPhase;
        if (bossHealth > BOSS_MAX_HEALTH * 0.66f) {
            newPhase = 1;
        } else if (bossHealth > BOSS_MAX_HEALTH * 0.33f) {
            newPhase = 2;
        } else {
            newPhase = 3;
        }

        if (newPhase != bossPhase) {
            bossPhase = newPhase;
            bossAttackIndex = 0;
            bossAttackTimer = 0.35f;
            projectiles.clear();
            showMessage("FASE " + bossPhase + " DO BOSS FINAL — A LUZ FICOU MAIS FORTE.");
        }

        bossAttackTimer -= delta;

        if (bossAttackTimer <= 0f) {
            launchBossAttack(bossAttackIndex);
            bossAttackIndex = (bossAttackIndex + 1) % 3;
            bossAttackTimer = getBossAttackCooldown();
        }
    }

    private float getBossAttackCooldown() {
        switch (bossPhase) {
            case 1:
                return 1.45f;
            case 2:
                return 0.95f;
            case 3:
            default:
                return 0.62f;
        }
    }

    private float getBossDamage() {
        switch (bossPhase) {
            case 1:
                return 10f;
            case 2:
                return 18f;
            case 3:
            default:
                return 28f;
        }
    }

    private float getBossSpeed() {
        switch (bossPhase) {
            case 1:
                return 390f;
            case 2:
                return 500f;
            case 3:
            default:
                return 620f;
        }
    }

    private void launchBossAttack(int attack) {
        switch (attack) {
            case 0:
                launchRadialBurst();
                break;
            case 1:
                launchTargetedFan();
                break;
            case 2:
            default:
                launchCrossStorm();
                break;
        }
    }

    private void launchRadialBurst() {
        int count = 8 + bossPhase * 4;
        float speed = getBossSpeed();
        float damage = getBossDamage();

        for (int i = 0; i < count; i++) {
            float angle = MathUtils.PI2 * i / count;
            projectiles.add(new CalistoProjectile(
                    bossHitbox.x + bossHitbox.width / 2f,
                    bossHitbox.y + bossHitbox.height / 2f,
                    MathUtils.cos(angle),
                    MathUtils.sin(angle),
                    speed,
                    damage,
                    24f
            ));
        }

        showMessage("BOSS — ATAQUE 1/3: EXPLOSÃO RADIAL.");
    }

    private void launchTargetedFan() {
        float baseAngle = MathUtils.atan2(
                player.getCenterY() - bossHitbox.getY() - bossHitbox.height / 2f,
                player.getCenterX() - bossHitbox.getX() - bossHitbox.width / 2f
        );

        int count = 3 + bossPhase * 2;
        float spread = 0.22f + bossPhase * 0.07f;

        for (int i = 0; i < count; i++) {
            float center = (count - 1) / 2f;
            float angle = baseAngle + (i - center) * spread;
            projectiles.add(new CalistoProjectile(
                    bossHitbox.x + bossHitbox.width / 2f,
                    bossHitbox.y + bossHitbox.height / 2f,
                    MathUtils.cos(angle),
                    MathUtils.sin(angle),
                    getBossSpeed() + 80f,
                    getBossDamage() + 4f,
                    22f
            ));
        }

        showMessage("BOSS — ATAQUE 2/3: LEQUE DE LUZ.");
    }

    private void launchCrossStorm() {
        int count = 6 + bossPhase * 4;
        float speed = getBossSpeed() + 40f;
        float damage = getBossDamage();

        for (int i = 0; i < count; i++) {
            float y = CORRIDOR_MIN_Y + 70f
                    + (CORRIDOR_MAX_Y - CORRIDOR_MIN_Y - 140f) * i / Math.max(1f, count - 1f);

            projectiles.add(new CalistoProjectile(
                    BOSS_X - 30f,
                    y,
                    -1f,
                    0f,
                    speed,
                    damage,
                    20f
            ));

            if (bossPhase >= 2) {
                projectiles.add(new CalistoProjectile(
                        BOSS_X + BOSS_SIZE + 30f,
                        y,
                        1f,
                        0f,
                        speed,
                        damage,
                        20f
                ));
            }

            if (bossPhase >= 3) {
                float x = GATE_X + 220f
                        + (BOSS_X - GATE_X - 300f) * i / Math.max(1f, count - 1f);

                projectiles.add(new CalistoProjectile(
                        x,
                        CORRIDOR_MAX_Y + 80f,
                        0f,
                        -1f,
                        speed,
                        damage,
                        20f
                ));
            }
        }

        showMessage("BOSS — ATAQUE 3/3: TEMPESTADE CRUZADA.");
    }

    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            CalistoProjectile projectile = projectiles.get(i);
            projectile.update(delta);

            if (projectile.hits(player.getHitbox())) {
                damagePlayer(projectile.damage);
                projectiles.removeIndex(i);
                continue;
            }

            if (projectile.isOutside(WORLD_WIDTH, WORLD_HEIGHT)) {
                projectiles.removeIndex(i);
            }
        }
    }

    private void damagePlayer(float amount) {
        stats.damage(amount * shieldMultiplier, DeathCause.UNKNOWN);
    }

    private void clampPlayerToCorridor() {
        Rectangle box = player.getHitbox();
        box.x = MathUtils.clamp(
                box.x,
                CORRIDOR_MIN_X,
                CORRIDOR_MAX_X - box.width
        );
        box.y = MathUtils.clamp(
                box.y,
                CORRIDOR_MIN_Y,
                CORRIDOR_MAX_Y - box.height
        );
    }

    private void clampPlayerToBossArena() {
        Rectangle box = player.getHitbox();
        box.x = MathUtils.clamp(
                box.x,
                BOSS_ARENA_MIN_X + 35f,
                BOSS_ARENA_MAX_X - box.width - 35f
        );
        box.y = MathUtils.clamp(
                box.y,
                BOSS_ARENA_MIN_Y + 35f,
                BOSS_ARENA_MAX_Y - box.height - 35f
        );
    }

    private void drawWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawFloor();
        if (!inBossArena) {
            // Player and corridor entities are drawn above the floor.
        }

        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(
                playerTexture,
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        for (Laser laser : lasers) {
            batch.draw(
                    assets.getLaserTexture(),
                    laser.getX(),
                    laser.getY(),
                    laser.getWidth(),
                    laser.getHeight()
            );
        }

        batch.end();

        drawEnvironmentShapes();
    }

    private void drawFloor() {
        float tile = 128f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(0.04f, 0.05f, 0.10f, 1f));
        shapeRenderer.rect(
                0f, 0f, WORLD_WIDTH, WORLD_HEIGHT
        );

        // Corredor medieval: pedra escura, carpete central, paredes e janelas.
        shapeRenderer.setColor(new Color(0.12f, 0.10f, 0.09f, 1f));
        shapeRenderer.rect(
                CORRIDOR_MIN_X,
                CORRIDOR_MIN_Y,
                CORRIDOR_MAX_X - CORRIDOR_MIN_X,
                CORRIDOR_MAX_Y - CORRIDOR_MIN_Y
        );

        shapeRenderer.setColor(new Color(0.23f, 0.18f, 0.15f, 1f));
        shapeRenderer.rect(
                CORRIDOR_MIN_X,
                CORRIDOR_MIN_Y + 20f,
                CORRIDOR_MAX_X - CORRIDOR_MIN_X,
                40f
        );
        shapeRenderer.rect(
                CORRIDOR_MIN_X,
                CORRIDOR_MAX_Y - 60f,
                CORRIDOR_MAX_X - CORRIDOR_MIN_X,
                40f
        );

        // Carpete vermelho medieval.
        shapeRenderer.setColor(new Color(0.36f, 0.03f, 0.05f, 1f));
        shapeRenderer.rect(
                CORRIDOR_MIN_X + 120f,
                CORRIDOR_MIN_Y + 120f,
                CORRIDOR_MAX_X - CORRIDOR_MIN_X - 240f,
                CORRIDOR_MAX_Y - CORRIDOR_MIN_Y - 240f
        );

        shapeRenderer.setColor(new Color(0.62f, 0.38f, 0.15f, 1f));
        shapeRenderer.rect(
                CORRIDOR_MIN_X + 120f,
                CORRIDOR_MIN_Y + 120f,
                18f,
                CORRIDOR_MAX_Y - CORRIDOR_MIN_Y - 240f
        );
        shapeRenderer.rect(
                CORRIDOR_MAX_X - 138f,
                CORRIDOR_MIN_Y + 120f,
                18f,
                CORRIDOR_MAX_Y - CORRIDOR_MIN_Y - 240f
        );

        // Janelas nas duas paredes laterais.
        for (int i = 0; i < 6; i++) {
            float wx = CORRIDOR_MIN_X + 230f + i * 330f;

            shapeRenderer.setColor(new Color(0.09f, 0.12f, 0.20f, 1f));
            shapeRenderer.rect(wx, CORRIDOR_MAX_Y - 95f, 120f, 58f);
            shapeRenderer.rect(wx, CORRIDOR_MIN_Y + 37f, 120f, 58f);

            shapeRenderer.setColor(new Color(0.55f, 0.78f, 1f, 0.75f));
            shapeRenderer.rect(wx + 8f, CORRIDOR_MAX_Y - 87f, 104f, 42f);
            shapeRenderer.rect(wx + 8f, CORRIDOR_MIN_Y + 45f, 104f, 42f);

            shapeRenderer.setColor(new Color(0.30f, 0.23f, 0.16f, 1f));
            shapeRenderer.rect(wx + 56f, CORRIDOR_MAX_Y - 87f, 6f, 42f);
            shapeRenderer.rect(wx + 56f, CORRIDOR_MIN_Y + 45f, 6f, 42f);
        }

        // Grande sala final medieval.
        shapeRenderer.setColor(new Color(0.08f, 0.075f, 0.10f, 1f));
        shapeRenderer.rect(
                BOSS_ARENA_MIN_X,
                BOSS_ARENA_MIN_Y,
                BOSS_ARENA_MAX_X - BOSS_ARENA_MIN_X,
                BOSS_ARENA_MAX_Y - BOSS_ARENA_MIN_Y
        );

        shapeRenderer.setColor(new Color(0.16f, 0.12f, 0.12f, 1f));
        shapeRenderer.rect(
                BOSS_ARENA_MIN_X + 70f,
                BOSS_ARENA_MIN_Y + 70f,
                BOSS_ARENA_MAX_X - BOSS_ARENA_MIN_X - 140f,
                BOSS_ARENA_MAX_Y - BOSS_ARENA_MIN_Y - 140f
        );

        shapeRenderer.setColor(new Color(0.25f, 0.04f, 0.05f, 1f));
        shapeRenderer.rect(
                BOSS_ARENA_MIN_X + 170f,
                BOSS_ARENA_MIN_Y + 160f,
                BOSS_ARENA_MAX_X - BOSS_ARENA_MIN_X - 340f,
                BOSS_ARENA_MAX_Y - BOSS_ARENA_MIN_Y - 320f
        );

        shapeRenderer.end();
    }

    private void drawEnvironmentShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Portão
        if (!inBossArena) {
            shapeRenderer.setColor(
                    allBlessingsCollected()
                            ? new Color(0.80f, 0.85f, 1f, 1f)
                            : new Color(0.25f, 0.26f, 0.33f, 1f)
            );
            shapeRenderer.rect(
                    gate.x,
                    gate.y,
                    gate.width,
                    gate.height
            );
        }

        // Cinco entidades de luz.
        for (int i = 0; i < angels.length; i++) {
            float cx = angels[i].x + angels[i].width / 2f;
            float cy = angels[i].y + angels[i].height / 2f;

            if (angelBlessings[i]) {
                shapeRenderer.setColor(new Color(0.65f, 0.85f, 1f, 0.28f));
                shapeRenderer.circle(cx, cy, 62f);
                continue;
            }

            shapeRenderer.setColor(new Color(0.75f, 0.90f, 1f, 0.30f));
            shapeRenderer.circle(cx, cy, 58f);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(cx, cy, 35f);
            shapeRenderer.setColor(new Color(0.55f, 0.75f, 1f, 1f));
            shapeRenderer.rect(cx - 9f, cy - 60f, 18f, 120f);
            shapeRenderer.rect(cx - 60f, cy - 9f, 120f, 18f);
        }

        // Chave e saída do final.
        if (inBossArena && bossDefeated && finalKeySpawned && !finalKeyCollected) {
            float cx = finalKeyHitbox.x + finalKeyHitbox.width / 2f;
            float cy = finalKeyHitbox.y + finalKeyHitbox.height / 2f;
            shapeRenderer.setColor(new Color(1f, 0.85f, 0.1f, 0.28f));
            shapeRenderer.circle(cx, cy, 70f);
        }

        if (inBossArena && finalPortalActive) {
            float cx = finalPortalHitbox.x + finalPortalHitbox.width / 2f;
            float cy = finalPortalHitbox.y + finalPortalHitbox.height / 2f;
            shapeRenderer.setColor(new Color(1f, 0.85f, 0.1f, 0.18f));
            shapeRenderer.circle(cx, cy, 115f);
        }

        // Boss final.
        if (inBossArena && !bossDefeated) {
            float cx = bossHitbox.x + bossHitbox.width / 2f;
            float cy = bossHitbox.y + bossHitbox.height / 2f;
            float glow = 160f + bossPhase * 30f;

            shapeRenderer.setColor(new Color(1f, 0.84f, 0.35f, 0.15f));
            shapeRenderer.circle(cx, cy, glow);

            shapeRenderer.setColor(new Color(1f, 0.95f, 0.76f, 1f));
            shapeRenderer.rect(
                    bossHitbox.x,
                    bossHitbox.y,
                    bossHitbox.width,
                    bossHitbox.height
            );

            shapeRenderer.setColor(new Color(0.75f, 0.62f, 0.10f, 1f));
            shapeRenderer.rect(
                    bossHitbox.x + 35f,
                    bossHitbox.y + 35f,
                    bossHitbox.width - 70f,
                    bossHitbox.height - 70f
            );

            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(cx - 45f, cy + 35f, 15f);
            shapeRenderer.circle(cx + 45f, cy + 35f, 15f);
        }

        for (CalistoProjectile projectile : projectiles) {
            shapeRenderer.setColor(new Color(0.75f, 0.90f, 1f, 1f));
            shapeRenderer.circle(
                    projectile.hitbox.x + projectile.hitbox.width / 2f,
                    projectile.hitbox.y + projectile.hitbox.height / 2f,
                    projectile.hitbox.width / 2f
            );
        }

        shapeRenderer.end();
    }

    private void drawHud() {
        hudViewport.apply(false);

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float width = hudViewport.getWorldWidth();
        float x = 28f;
        float y = hudViewport.getWorldHeight() - 50f;
        float barWidth = 320f;

        drawBar(x, y, barWidth, 24f,
                stats.getHealth(), PlayerStats.MAX_HEALTH, Color.RED);
        drawBar(x, y - 40f, barWidth, 24f,
                stats.getHunger(), PlayerStats.MAX_HUNGER, Color.ORANGE);
        drawBar(x, y - 80f, barWidth, 24f,
                stats.getOxygen(), PlayerStats.MAX_OXYGEN, Color.CYAN);

        if (inBossArena) {
            drawBar(
                    width / 2f - 360f,
                    30f,
                    720f,
                    30f,
                    bossHealth,
                    BOSS_MAX_HEALTH,
                    Color.YELLOW
            );
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.15f);
        font.draw(batch, "CALISTO // CORREDOR DA EVOLUÇÃO", 28f,
                hudViewport.getWorldHeight() - 160f);

        font.getData().setScale(0.95f);
        font.draw(batch, String.format("HP %.0f/100", stats.getHealth()), x + 8f, y + 18f);
        font.draw(batch, String.format("FOME %.0f/100", stats.getHunger()), x + 8f, y - 22f);
        font.draw(batch, String.format("O2 %.0f/100", stats.getOxygen()), x + 8f, y - 62f);

        int blessings = 0;
        for (boolean blessing : angelBlessings) {
            if (blessing) blessings++;
        }

        font.getData().setScale(1.0f);
        font.setColor(Color.CYAN);
        font.draw(batch, "BÊNÇÃOS: " + blessings + "/5", 28f,
                hudViewport.getWorldHeight() - 190f);

        if (!inBossArena) {
            font.setColor(Color.LIGHT_GRAY);
            font.getData().setScale(0.88f);
            font.draw(batch,
                    allBlessingsCollected()
                            ? "As 5 bênçãos estão completas. E no portão principal."
                            : "E perto de cada entidade de luz para receber um upgrade.",
                    28f,
                    hudViewport.getWorldHeight() - 222f);

            if (stats.areSurvivalNeedsDisabled()) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "SACIAÇÃO: FOME e O2 DESATIVADOS", 28f,
                        hudViewport.getWorldHeight() - 250f);
            }
        } else {
            font.setColor(Color.ORANGE);
            font.getData().setScale(1.0f);
            font.draw(batch,
                    bossDefeated
                            ? "BOSS FINAL DERROTADO"
                            : "BOSS FINAL — FASE " + bossPhase + "/3 — ATAQUE " + (bossAttackIndex + 1) + "/3",
                    28f,
                    hudViewport.getWorldHeight() - 222f);

            font.getData().setScale(0.86f);
            font.setColor(Color.LIGHT_GRAY);

            if (bossDefeated && !finalKeyCollected) {
                font.draw(batch,
                        "A CHAVE DE LUZ surgiu perto do boss. Encoste nela.",
                        28f,
                        hudViewport.getWorldHeight() - 250f);
            } else if (finalPortalActive) {
                font.draw(batch,
                        "PORTAL FINAL ABERTO — entre nele para concluir a jornada.",
                        28f,
                        hudViewport.getWorldHeight() - 250f);
            } else {
                font.draw(batch,
                        "FASE 1: 8-12 projéteis | FASE 2: mais velocidade | FASE 3: tempestade máxima",
                        28f,
                        hudViewport.getWorldHeight() - 250f);
            }
        }

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.84f);
        font.draw(batch,
                "WASD/SETAS = mover | Mouse = atirar | E = interagir | ESC = pausar",
                28f,
                18f);

        if (messageTimer > 0f) {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.0f);
            GlyphLayout layout = new GlyphLayout(font, message);
            font.draw(batch,
                    message,
                    hudViewport.getWorldWidth() / 2f - layout.width / 2f,
                    92f
            );
        }

        batch.end();
    }

    private void drawBar(
            float x,
            float y,
            float width,
            float height,
            float value,
            float max,
            Color color
    ) {
        shapeRenderer.setColor(new Color(0.05f, 0.05f, 0.07f, 0.95f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(
                x,
                y,
                width * MathUtils.clamp(value / max, 0f, 1f),
                height
        );
    }

    private void updateCamera() {
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float targetX = MathUtils.clamp(
                player.getCenterX(),
                halfWidth,
                WORLD_WIDTH - halfWidth
        );
        float targetY = MathUtils.clamp(
                player.getCenterY(),
                halfHeight,
                WORLD_HEIGHT - halfHeight
        );

        camera.position.set(targetX, targetY, 0f);
        camera.update();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                && !pauseMenu.isOpen()) {
            saveGame();
            changingScreen = true;
            dispose();
            game.setScreen(new FastTravelScreen(game, SaveManager.load()));
            return;
        }

        PauseMenu.Action pauseAction = pauseMenu.handleInput();

        if (pauseAction == PauseMenu.Action.SAVE) {
            saveGame();
            return;
        }

        if (pauseAction == PauseMenu.Action.PHASES) {
            saveGame();
            changingScreen = true;
            dispose();
            game.setScreen(new PhaseSelectScreen(game));
            return;
        }

        if (pauseAction == PauseMenu.Action.MENU) {
            saveGame();
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (!pauseMenu.isOpen()) {
            update(delta);

            // A troca de Screen pode acontecer dentro de update().
            // Nesse caso a Screen atual já pode ter sido descartada.
            // Nunca continue desenhando com SpriteBatch/ShapeRenderer destruídos.
            if (changingScreen) {
                return;
            }
        }

        ScreenUtils.clear(0.015f, 0.025f, 0.07f, 1f);
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

    private static class CalistoProjectile {
        private final Rectangle hitbox;
        private final float dx;
        private final float dy;
        private final float speed;
        private final float damage;

        private CalistoProjectile(
                float x,
                float y,
                float dx,
                float dy,
                float speed,
                float damage,
                float size
        ) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.001f) {
                length = 1f;
            }

            this.dx = dx / length;
            this.dy = dy / length;
            this.speed = speed;
            this.damage = damage;
            this.hitbox = new Rectangle(
                    x - size / 2f,
                    y - size / 2f,
                    size,
                    size
            );
        }

        private void update(float delta) {
            hitbox.x += dx * speed * delta;
            hitbox.y += dy * speed * delta;
        }

        private boolean hits(Rectangle target) {
            return hitbox.overlaps(target);
        }

        private boolean isOutside(float worldWidth, float worldHeight) {
            return hitbox.x + hitbox.width < 0f
                    || hitbox.y + hitbox.height < 0f
                    || hitbox.x > worldWidth
                    || hitbox.y > worldHeight;
        }
    }
}
