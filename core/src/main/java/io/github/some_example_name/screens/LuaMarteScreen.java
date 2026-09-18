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
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.MarsEnemy;
import io.github.some_example_name.entities.MarsOre;
import io.github.some_example_name.entities.MarsPortal;
import io.github.some_example_name.entities.MarsPortalStrike;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.entities.SupremeAlienBoss;
import io.github.some_example_name.managers.AssetManager;

/** Mars phase: Lua clone plus mining, upgraded weapon, Martians and Supreme Alien boss. */
public class LuaMarteScreen extends ScreenAdapter {

    private enum MarsMission {
        COLLECT_ORE,
        REFINE_ORE,
        UPGRADE_WEAPON,
        DEFEAT_SUPREME_ALIEN,
        GO_TO_NEXT
    }

    private enum BossAttack {
        COOLDOWN,
        SCREAM,
        PORTAL,
        MARTIAN_WAVE
    }

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float WORLD_WIDTH = 3000f;
    private static final float WORLD_HEIGHT = 2000f;
    private static final float PLAYER_SPAWN_X = 400f;
    private static final float PLAYER_SPAWN_Y = 400f;
    private static final float BASE_X = 500f;
    private static final float BASE_Y = 390f;
    private static final float BASE_WIDTH = 240f;
    private static final float BASE_HEIGHT = 160f;
    private static final float MINE_X = 1980f;
    private static final float MINE_Y = 920f;
    private static final float MINE_WIDTH = BASE_WIDTH;
    private static final float MINE_HEIGHT = BASE_HEIGHT;
    private static final float TILE_SIZE = 128f;
    private static final int REQUIRED_ORE = 5;
    private static final float NORMAL_FIRE_INTERVAL = 0.18f;
    private static final float UPGRADED_FIRE_INTERVAL = 0.09f;
    private static final float BURST_SPREAD = 0.045f;
    private static final float BOSS_COOLDOWN = 3f;
    private static final float SCREAM_DURATION = 3f;
    private static final float SCREAM_PULL_SPEED = 180f;
    private static final int NORMAL_MARTIANS = 5;
    private static final int MARTIANS_PER_WAVE = 10;
    private static final float MARTIAN_RESPAWN_INTERVAL = 3f;
    private static final float KEY_SIZE = 76f;
    private static final float BOSS_DEATH_DELAY = 1f;

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ScreenViewport hudViewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont hudFont;
    private final AssetManager assets;
    private final Player player;
    private final PlayerStats stats;
    private final PauseMenu pauseMenu;

    private final Array<LuaItem> resources = new Array<>();
    private final Array<MarsOre> ores = new Array<>();
    private final Array<Laser> lasers = new Array<>();
    private final Array<MarsEnemy> martians = new Array<>();
    private final Array<MarsPortalStrike> portalStrikes = new Array<>();
    private final Rectangle greenKeyHitbox = new Rectangle();

    private MarsMission mission = MarsMission.COLLECT_ORE;
    private SupremeAlienBoss supremeAlien;
    private MarsPortal marsPortal;
    private BossAttack bossAttack = BossAttack.COOLDOWN;

    private int rawOreCount;
    private int refinedOreCount;
    private boolean weaponUpgraded;
    private boolean portalSpawned;
    private boolean portalUnlocked;
    private boolean portalEntryArmed;
    private boolean greenKeyVisible;
    private boolean greenKeyCollected;
    private boolean bossDeathSequenceStarted;

    private float fireTimer;
    private float bossAttackTimer;
    private float bossDeathTimer;
    private float martianSpawnTimer;
    private float messageTimer;
    private float screamTimer;
    private int nextBossAttackIndex;
    private String missionMessage = "";
    private boolean changingScreen;
    private boolean disposed;

    public LuaMarteScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        hudViewport = new ScreenViewport();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudFont = new BitmapFont();
        assets = new AssetManager();
        assets.load();
        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        stats = new PlayerStats();
        pauseMenu = new PauseMenu(game);
        createMarsResources();
        createMarsOres();
        createStartingMartians();
        showMessage("MISSÃO MARTE: colete 5 minérios verdes e leve-os até a mina para refinar.");
        updateCamera();
    }

    private void createMarsResources() {
        addResource(LuaItem.Type.FOOD, 780f, 620f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1120f, 430f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1680f, 560f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 2240f, 420f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 720f, 1510f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1900f, 1560f, 56f, 56f);
        addResource(LuaItem.Type.O2_TANK, 1050f, 920f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2050f, 1320f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 1450f, 560f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2640f, 1150f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 560f, 1180f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2400f, 1540f, 46f, 125f);
    }

    private void addResource(LuaItem.Type type, float x, float y, float width, float height) {
        resources.add(new LuaItem(type, x, y, width, height));
    }

    private void createMarsOres() {
        ores.add(new MarsOre(900f, 800f));
        ores.add(new MarsOre(1320f, 1260f));
        ores.add(new MarsOre(1740f, 650f));
        ores.add(new MarsOre(2220f, 1200f));
        ores.add(new MarsOre(700f, 1550f));
        ores.add(new MarsOre(2520f, 500f));
        ores.add(new MarsOre(1120f, 1750f));
        ores.add(new MarsOre(2320f, 760f));
    }

    private void createStartingMartians() {
        for (int i = 0; i < NORMAL_MARTIANS; i++) {
            spawnMartianFarFromPlayer();
        }
        martianSpawnTimer = MARTIAN_RESPAWN_INTERVAL;
    }

    private void spawnMartianFarFromPlayer() {
        for (int attempt = 0; attempt < 30; attempt++) {
            float x = MathUtils.random(120f, WORLD_WIDTH - 180f);
            float y = MathUtils.random(120f, WORLD_HEIGHT - 180f);
            float dx = x - player.getCenterX();
            float dy = y - player.getCenterY();
            if (dx * dx + dy * dy > 500f * 500f) {
                martians.add(new MarsEnemy(x, y));
                return;
            }
        }
        martians.add(new MarsEnemy(WORLD_WIDTH - 220f, WORLD_HEIGHT - 220f));
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
        collectResources();
        collectOres();
        handleMissionInteraction();
        updateMartians(delta);
        updateShooting(delta);
        updateLasers(delta);
        updateBoss(delta);
        updatePortalStrikes(delta);
        handleExitPortal();
        stats.update(delta);

        if (stats.isDead()) {
            changingScreen = true;
            DeathCause cause = stats.getDeathCause();
            dispose();
            game.setScreen(new GameOverScreen(game, cause));
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return false;
        }

        updateCamera();
        return true;
    }

    private void collectResources() {
        for (int i = resources.size - 1; i >= 0; i--) {
            LuaItem item = resources.get(i);
            if (!player.getHitbox().overlaps(item.getHitbox())) continue;
            if (item.getType() == LuaItem.Type.FOOD) {
                stats.eatFood();
                showMessage("Comida coletada: fome e vida restauradas.");
            } else if (item.getType() == LuaItem.Type.O2_TANK) {
                stats.addOxygen(20f);
                showMessage("O2 coletado: oxigênio restaurado.");
            }
            resources.removeIndex(i);
        }
    }

    private void collectOres() {
        for (int i = ores.size - 1; i >= 0; i--) {
            MarsOre ore = ores.get(i);
            if (!player.getHitbox().overlaps(ore.getHitbox())) continue;
            rawOreCount++;
            ores.removeIndex(i);
            if (mission == MarsMission.COLLECT_ORE) {
                if (rawOreCount >= REQUIRED_ORE) {
                    mission = MarsMission.REFINE_ORE;
                    showMessage("5 minérios coletados! Vá até a mina e pressione E para refinar.");
                } else {
                    showMessage("Minério verde coletado: " + rawOreCount + "/" + REQUIRED_ORE + ".");
                }
            }
        }
    }

    private void handleMissionInteraction() {
        Rectangle mine = new Rectangle(MINE_X, MINE_Y, MINE_WIDTH, MINE_HEIGHT);
        Rectangle base = new Rectangle(BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);

        if (mission == MarsMission.REFINE_ORE && rawOreCount >= REQUIRED_ORE
                && player.getHitbox().overlaps(mine)
                && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            refinedOreCount = rawOreCount;
            rawOreCount = 0;
            mission = MarsMission.UPGRADE_WEAPON;
            showMessage("Minérios refinados! Volte à base e pressione E para melhorar a arma.");
            return;
        }

        if (mission == MarsMission.UPGRADE_WEAPON && refinedOreCount >= REQUIRED_ORE
                && player.getHitbox().overlaps(base)
                && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            weaponUpgraded = true;
            mission = MarsMission.DEFEAT_SUPREME_ALIEN;
            spawnSupremeAlien();
            showMessage("ARMA APRIMORADA! O ALIEN SUPREMO APARECEU! Derrote-o.");
        }
    }

    private void updateShooting(float delta) {
        fireTimer -= delta;
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || fireTimer > 0f) return;

        Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseWorld);
        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) return;
        dx /= length;
        dy /= length;

        if (weaponUpgraded) {
            addLaserWithAngle(dx, dy, BURST_SPREAD);
            addLaserWithAngle(dx, dy, -BURST_SPREAD);
            fireTimer = UPGRADED_FIRE_INTERVAL;
        } else {
            lasers.add(new Laser(player.getCenterX(), player.getCenterY(), dx, dy));
            fireTimer = NORMAL_FIRE_INTERVAL;
        }
    }

    private void addLaserWithAngle(float dx, float dy, float angle) {
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float rotatedX = dx * cos - dy * sin;
        float rotatedY = dx * sin + dy * cos;
        lasers.add(new Laser(player.getCenterX(), player.getCenterY(), rotatedX, rotatedY));
    }

    private void updateLasers(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);
            boolean hit = false;

            for (int j = martians.size - 1; j >= 0; j--) {
                MarsEnemy enemy = martians.get(j);
                if (!enemy.isDead() && laser.getHitbox().overlaps(enemy.getHitbox())) {
                    enemy.takeDamage(10f);
                    hit = true;
                    break;
                }
            }

            if (!hit && mission == MarsMission.DEFEAT_SUPREME_ALIEN
                    && supremeAlien != null && !supremeAlien.isDead()
                    && laser.getHitbox().overlaps(supremeAlien.getHitbox())) {
                supremeAlien.takeDamage(10f);
                hit = true;
                if (supremeAlien.isDead()) beginBossDeathSequence();
            }

            if (hit || laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) lasers.removeIndex(i);
        }
    }

    private void updateMartians(float delta) {
        for (int i = martians.size - 1; i >= 0; i--) {
            MarsEnemy enemy = martians.get(i);
            enemy.update(delta, player.getCenterX(), player.getCenterY(), stats);
            if (enemy.isDead()) martians.removeIndex(i);
        }

        if (supremeAlien == null && mission != MarsMission.GO_TO_NEXT && martians.size < NORMAL_MARTIANS) {
            martianSpawnTimer -= delta;
            if (martianSpawnTimer <= 0f) {
                spawnMartianFarFromPlayer();
                martianSpawnTimer = MARTIAN_RESPAWN_INTERVAL;
            }
        }

        if (supremeAlien != null && !supremeAlien.isDead()
                && bossAttack == BossAttack.MARTIAN_WAVE
                && martians.size == 0 && !supremeAlien.isBarrierActive()) {
            startBossCooldown("Os 10 marcianos foram derrotados. O Alien Supremo prepara outro ataque.");
        }
    }

    private void spawnSupremeAlien() {
        if (supremeAlien != null) return;
        martians.clear();
        supremeAlien = new SupremeAlienBoss(2250f, 1350f);
        bossAttack = BossAttack.COOLDOWN;
        bossAttackTimer = BOSS_COOLDOWN;
        nextBossAttackIndex = 0;
    }

    private void updateBoss(float delta) {
        if (supremeAlien == null) return;

        if (supremeAlien.isDead()) {
            if (!bossDeathSequenceStarted) beginBossDeathSequence();
            bossDeathTimer -= delta;
            if (bossDeathTimer <= 0f && !portalSpawned) spawnMarsExit();
            return;
        }

        supremeAlien.update(delta, player.getCenterX(), player.getCenterY(), WORLD_WIDTH, WORLD_HEIGHT);
        bossAttackTimer -= delta;

        switch (bossAttack) {
            case COOLDOWN:
                if (bossAttackTimer <= 0f) beginNextBossAttack();
                break;
            case SCREAM:
                updateScream(delta);
                break;
            case PORTAL:
                if (portalStrikes.size == 0) startBossCooldown("O ataque de portal acabou.");
                break;
            case MARTIAN_WAVE:
                break;
            default:
                break;
        }
    }

    private void beginNextBossAttack() {
        if (nextBossAttackIndex == 0) beginScream();
        else if (nextBossAttackIndex == 1) beginPortalAttack();
        else beginMartianWave();
        nextBossAttackIndex = (nextBossAttackIndex + 1) % 3;
    }

    private void beginScream() {
        bossAttack = BossAttack.SCREAM;
        screamTimer = 0f;
        showMessage("GRITO: ondas sonoras escuras estão puxando você!");
    }

    private void updateScream(float delta) {
        screamTimer += delta;
        float dx = supremeAlien.getCenterX() - player.getCenterX();
        float dy = supremeAlien.getCenterY() - player.getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0.001f) {
            dx /= distance;
            dy /= distance;
            player.getHitbox().x += dx * SCREAM_PULL_SPEED * delta;
            player.getHitbox().y += dy * SCREAM_PULL_SPEED * delta;
            clampPlayerToWorld();
        }
        if (screamTimer >= SCREAM_DURATION) startBossCooldown("O grito acabou.");
    }

    private void beginPortalAttack() {
        bossAttack = BossAttack.PORTAL;
        portalStrikes.clear();
        portalStrikes.add(new MarsPortalStrike(
                supremeAlien.getCenterX(),
                supremeAlien.getCenterY(),
                player.getCenterX(),
                player.getCenterY()
        ));
        showMessage("PORTAL: o Alien Supremo lançou um portal em você!");
    }

    private void beginMartianWave() {
        bossAttack = BossAttack.MARTIAN_WAVE;
        martians.clear();
        float centerX = supremeAlien.getCenterX();
        float centerY = supremeAlien.getCenterY();

        for (int i = 0; i < MARTIANS_PER_WAVE; i++) {
            float angle = MathUtils.PI2 * i / MARTIANS_PER_WAVE;
            float radius = 520f;
            float x = MathUtils.clamp(centerX + MathUtils.cos(angle) * radius, 80f, WORLD_WIDTH - 140f);
            float y = MathUtils.clamp(centerY + MathUtils.sin(angle) * radius, 80f, WORLD_HEIGHT - 140f);
            martians.add(new MarsEnemy(x, y));
        }

        supremeAlien.activateBarrier();
        showMessage("10 MARCIANOS! A barreira está ativa: cause 250 de dano para quebrá-la.");
    }

    private void startBossCooldown(String message) {
        bossAttack = BossAttack.COOLDOWN;
        bossAttackTimer = BOSS_COOLDOWN;
        showMessage(message);
    }

    private void updatePortalStrikes(float delta) {
        for (int i = portalStrikes.size - 1; i >= 0; i--) {
            MarsPortalStrike strike = portalStrikes.get(i);
            strike.update(delta);
            if (strike.shouldDamagePlayer() && player.getHitbox().overlaps(strike.getHitbox())) {
                stats.damage(MarsPortalStrike.DAMAGE, DeathCause.UNKNOWN);
            }
            if (strike.isFinished()) portalStrikes.removeIndex(i);
        }
    }

    private void beginBossDeathSequence() {
        if (bossDeathSequenceStarted) return;
        bossDeathSequenceStarted = true;
        bossDeathTimer = BOSS_DEATH_DELAY;
        mission = MarsMission.GO_TO_NEXT;
        martians.clear();
        portalStrikes.clear();
        showMessage("ALIEN SUPREMO DERROTADO! Uma chave verde apareceu.");
    }

    private void spawnMarsExit() {
        portalSpawned = true;
        portalUnlocked = false;
        portalEntryArmed = false;
        greenKeyVisible = true;
        greenKeyCollected = false;
        greenKeyHitbox.set(
                supremeAlien.getCenterX() - KEY_SIZE / 2f,
                supremeAlien.getCenterY() - KEY_SIZE / 2f,
                KEY_SIZE,
                KEY_SIZE
        );
        marsPortal = new MarsPortal(2580f, 1530f);
        showMessage("CHAVE VERDE: toque nela para coletar e siga até o portal.");
    }

    private void handleExitPortal() {
        if (!portalSpawned || marsPortal == null || mission != MarsMission.GO_TO_NEXT) return;

        if (greenKeyVisible && player.getHitbox().overlaps(greenKeyHitbox)) {
            greenKeyVisible = false;
            greenKeyCollected = true;
            showMessage("CHAVE VERDE COLETADA! Vá ao portal e pressione E.");
        }

        boolean atPortal = player.getHitbox().overlaps(marsPortal.getHitbox());
        if (!portalUnlocked) {
            if (!greenKeyCollected) return;
            if (atPortal && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                portalUnlocked = true;
                portalEntryArmed = false;
                showMessage("PORTAL ABERTO! Saia e entre novamente para concluir Marte.");
            }
            return;
        }

        if (!atPortal) {
            portalEntryArmed = true;
            return;
        }

        if (portalEntryArmed) {
            float health = stats.getHealth();
            float hunger = stats.getHunger();
            float oxygen = stats.getOxygen();
            changingScreen = true;
            dispose();
            game.setScreen(new MarsLevelStatusScreen(game, health, hunger, oxygen));
        }
    }

    private void clampPlayerToWorld() {
        Rectangle box = player.getHitbox();
        box.x = MathUtils.clamp(box.x, 0f, WORLD_WIDTH - box.width);
        box.y = MathUtils.clamp(box.y, 0f, WORLD_HEIGHT - box.height);
    }

    private void updateCamera() {
        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;
        float cameraX = MathUtils.clamp(player.getCenterX(), halfViewportWidth, WORLD_WIDTH - halfViewportWidth);
        float cameraY = MathUtils.clamp(player.getCenterY(), halfViewportHeight, WORLD_HEIGHT - halfViewportHeight);
        camera.position.set(cameraX, cameraY, 0f);
        camera.update();
    }

    private void drawMarsFloor() {
        Texture tile = assets.getMarsTileTexture();
        for (float x = 0f; x < WORLD_WIDTH; x += TILE_SIZE) {
            for (float y = 0f; y < WORLD_HEIGHT; y += TILE_SIZE) {
                float width = Math.min(TILE_SIZE, WORLD_WIDTH - x);
                float height = Math.min(TILE_SIZE, WORLD_HEIGHT - y);
                batch.draw(tile, x, y, width, height);
            }
        }
    }

    private void drawResources() {
        for (LuaItem item : resources) {
            Texture texture = item.getType() == LuaItem.Type.FOOD
                    ? assets.getFoodTexture()
                    : assets.getO2TankTexture();
            batch.draw(texture, item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
    }

    private void drawWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMarsFloor();
        batch.draw(assets.getLunarBaseTexture(), BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);
        batch.draw(assets.getMineTexture(), MINE_X, MINE_Y, MINE_WIDTH, MINE_HEIGHT);
        drawResources();

        Texture oreTexture = assets.getOreTexture();
        for (MarsOre ore : ores) batch.draw(oreTexture, ore.getX(), ore.getY(), ore.getWidth(), ore.getHeight());

        Texture alienTexture = assets.getAlienTexture();
        for (MarsEnemy enemy : martians) batch.draw(alienTexture, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());

        if (supremeAlien != null && !supremeAlien.isDead()) {
            Texture bossTexture = assets.getBossMarsTexture();
            batch.draw(bossTexture, supremeAlien.getX(), supremeAlien.getY(), supremeAlien.getWidth(), supremeAlien.getHeight());
        }

        Texture portalTexture = assets.getPortalTexture();
        for (MarsPortalStrike strike : portalStrikes) {
            batch.draw(portalTexture, strike.getX(), strike.getY(), strike.getWidth(), strike.getHeight());
        }

        if (marsPortal != null && portalSpawned) {
            batch.draw(portalTexture, marsPortal.getX(), marsPortal.getY(), marsPortal.getWidth(), marsPortal.getHeight());
        }

        if (greenKeyVisible) {
            Texture keyTexture = assets.getGreenKeyTexture();
            batch.draw(keyTexture, greenKeyHitbox.x, greenKeyHitbox.y, greenKeyHitbox.width, greenKeyHitbox.height);
        }

        Texture laserTexture = assets.getLaserTexture();
        for (Laser laser : lasers) batch.draw(laserTexture, laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());

        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(playerTexture, player.getX(), player.getY(), player.getWidth(), player.getHeight());
        batch.end();
    }

    private void drawBossEffects() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (bossAttack == BossAttack.SCREAM && supremeAlien != null && !supremeAlien.isDead()) {
            for (int i = 0; i < 5; i++) {
                float radius = 80f + screamTimer * 180f + i * 70f;
                float alpha = Math.max(0.03f, 0.18f - i * 0.028f);
                shapeRenderer.setColor(new Color(0f, 0f, 0f, alpha));
                shapeRenderer.circle(supremeAlien.getCenterX(), supremeAlien.getCenterY(), radius);
            }
        }

        if (supremeAlien != null && supremeAlien.isBarrierActive()) {
            float ratio = MathUtils.clamp(supremeAlien.getBarrierHealth() / SupremeAlienBoss.BARRIER_MAX_HEALTH, 0f, 1f);
            float radius = supremeAlien.getWidth() * 1.15f + 24f * ratio;
            shapeRenderer.setColor(new Color(0.05f, 0.95f, 0.45f, 0.24f));
            shapeRenderer.circle(supremeAlien.getCenterX(), supremeAlien.getCenterY(), radius);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawArrow() {
        if (!portalSpawned) return;
        float targetX;
        float targetY;
        if (greenKeyVisible) {
            targetX = greenKeyHitbox.x + greenKeyHitbox.width / 2f;
            targetY = greenKeyHitbox.y + greenKeyHitbox.height / 2f;
        } else if (marsPortal != null && !portalUnlocked) {
            targetX = marsPortal.getX() + marsPortal.getWidth() / 2f;
            targetY = marsPortal.getY() + marsPortal.getHeight() / 2f;
        } else {
            return;
        }

        Vector3 targetScreen = new Vector3(targetX, targetY, 0f);
        Vector3 playerScreen = new Vector3(player.getCenterX(), player.getCenterY(), 0f);
        camera.project(targetScreen);
        camera.project(playerScreen);

        float dx = targetScreen.x - playerScreen.x;
        float dy = targetScreen.y - playerScreen.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) return;
        dx /= length;
        dy /= length;

        float centerX = hudViewport.getWorldWidth() / 2f;
        float centerY = hudViewport.getWorldHeight() - 140f;
        float tipX = centerX + dx * 50f;
        float tipY = centerY + dy * 50f;
        float baseX = centerX - dx * 12f;
        float baseY = centerY - dy * 12f;
        float perpX = -dy;
        float perpY = dx;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rectLine(centerX - dx * 15f, centerY - dy * 15f, baseX, baseY, 8f);
        shapeRenderer.triangle(tipX, tipY, baseX + perpX * 18f, baseY + perpY * 18f, baseX - perpX * 18f, baseY - perpY * 18f);
        shapeRenderer.end();
    }

    private void drawBar(float x, float y, float width, float height, float value, float maxValue, Color color) {
        shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.08f, 0.92f));
        shapeRenderer.rect(x, y, width, height);
        float percent = MathUtils.clamp(value / maxValue, 0f, 1f);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawBossHealthBar() {
        if (supremeAlien == null || supremeAlien.isDead()) return;

        float worldWidth = hudViewport.getWorldWidth();
        float barWidth = Math.min(900f, worldWidth - 120f);
        float barHeight = 26f;
        float barX = (worldWidth - barWidth) / 2f;
        float barY = 34f;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawBar(barX, barY, barWidth, barHeight, supremeAlien.getHealth(), SupremeAlienBoss.MAX_HEALTH, Color.GREEN);
        if (supremeAlien.isBarrierActive()) {
            drawBar(barX, barY, barWidth, barHeight, supremeAlien.getBarrierHealth(), SupremeAlienBoss.BARRIER_MAX_HEALTH, Color.CYAN);
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        hudFont.getData().setScale(1.15f);
        hudFont.setColor(Color.WHITE);
        String bossName = "ALIEN SUPREMO";
        GlyphLayout nameLayout = new GlyphLayout(hudFont, bossName);
        hudFont.draw(batch, bossName, worldWidth / 2f - nameLayout.width / 2f, barY + barHeight + 28f);
        hudFont.getData().setScale(0.9f);
        String hpText = supremeAlien.isBarrierActive()
                ? String.format("BARREIRA %.0f/250 | HP %.0f/%.0f", supremeAlien.getBarrierHealth(), supremeAlien.getHealth(), SupremeAlienBoss.MAX_HEALTH)
                : String.format("HP %.0f/%.0f", supremeAlien.getHealth(), SupremeAlienBoss.MAX_HEALTH);
        GlyphLayout hpLayout = new GlyphLayout(hudFont, hpText);
        hudFont.draw(batch, hpText, worldWidth / 2f - hpLayout.width / 2f, barY - 8f);
        batch.end();
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

        drawArrow();
        drawBossHealthBar();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        hudFont.getData().setScale(1.05f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, String.format("HP: %.0f / 100", stats.getHealth()), x + 10f, firstY + 20f);
        hudFont.draw(batch, String.format("FOME: %.0f / 100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        hudFont.draw(batch, String.format("O2: %.0f / 100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);

        hudFont.getData().setScale(1f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "MISSÃO MARTE", 28f, hudViewport.getWorldHeight() - 205f);
        hudFont.getData().setScale(0.92f);
        hudFont.setColor(Color.LIGHT_GRAY);

        String objective;
        switch (mission) {
            case COLLECT_ORE:
                objective = "Colete minérios verdes: " + rawOreCount + "/" + REQUIRED_ORE;
                break;
            case REFINE_ORE:
                objective = "Vá à mina e pressione E para refinar os minérios";
                break;
            case UPGRADE_WEAPON:
                objective = "Volte à base e pressione E para melhorar a arma";
                break;
            case DEFEAT_SUPREME_ALIEN:
                objective = "Derrote o Alien Supremo: 5000 HP";
                break;
            case GO_TO_NEXT:
                if (greenKeyVisible) objective = "Colete a chave verde tocando nela";
                else if (!portalUnlocked) objective = "Vá ao portal e pressione E para usar a chave verde";
                else if (!portalEntryArmed) objective = "Portal aberto: saia e entre novamente";
                else objective = "Entre no portal para concluir Marte";
                break;
            default:
                objective = "";
                break;
        }
        hudFont.draw(batch, objective, 28f, hudViewport.getWorldHeight() - 235f);

        hudFont.setColor(Color.ORANGE);
        hudFont.draw(batch, "MINA", MINE_X, MINE_Y + MINE_HEIGHT + 24f);
        hudFont.setColor(Color.GREEN);
        hudFont.draw(batch, weaponUpgraded ? "ARMA: APRIMORADA | 2 tiros | cadência 2x" : "ARMA: padrão | 1 tiro", 28f, hudViewport.getWorldHeight() - 280f);

        if (greenKeyVisible) {
            hudFont.setColor(Color.GREEN);
            hudFont.draw(batch, "CHAVE VERDE", hudViewport.getWorldWidth() - 210f, 62f);
        }

        if (messageTimer > 0f) {
            hudFont.getData().setScale(1f);
            hudFont.setColor(Color.WHITE);
            GlyphLayout layout = new GlyphLayout(hudFont, missionMessage);
            hudFont.draw(batch, missionMessage, hudViewport.getWorldWidth() / 2f - layout.width / 2f, 96f);
        }

        hudFont.getData().setScale(0.86f);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(batch, "WASD / SETAS = mover | Mouse = mirar + segurar para atirar | E = interagir | ESC = pausar", 28f, 18f);
        batch.end();
        messageTimer = Math.max(0f, messageTimer - Gdx.graphics.getDeltaTime());
    }

    private void showMessage(String message) {
        missionMessage = message;
        messageTimer = 4f;
    }

    @Override
    public void render(float delta) {
        if (changingScreen) return;

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
            if (!update(delta)) return;
        }

        ScreenUtils.clear(0.72f, 0.25f, 0.06f, 1f);
        drawWorld();
        drawBossEffects();
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
        if (disposed) return;
        disposed = true;
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
        pauseMenu.dispose();
    }
}

/** Compatibility class used by LuaScreen and MarsIntroScreen. */
class MarteScreen extends LuaMarteScreen {
    public MarteScreen(Game game) {
        super(game);
    }
}
