package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import io.github.some_example_name.managers.SaveManager;
import io.github.some_example_name.systems.BossStorm;

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
    private static final float PORTAL_ROTATION_SPEED = 120f;
    private static final float LASER_RENDER_LENGTH = 42f;

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
    private final QuestLog questLog;

    private final Array<LuaItem> resources = new Array<>();
    private final Array<MarsOre> ores = new Array<>();
    private final Array<Laser> lasers = new Array<>();
    private final Array<MarsEnemy> martians = new Array<>();
    private final Array<MarsPortalStrike> portalStrikes = new Array<>();
    private final Rectangle greenKeyHitbox = new Rectangle();
    private final BossStorm bossStorm = new BossStorm();

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
    private float portalRotationDegrees;
    private float bossAttackTimer;
    private float martianSpawnTimer;
    private float baseRecoveryTimer;
    private float messageTimer;
    private float screamTimer;
    private float collectibleFloatTime;
    private int nextBossAttackIndex;
    private String missionMessage = "";
    private boolean changingScreen;
    private boolean disposed;

    public LuaMarteScreen(Game game) {
        this(game, null);
    }

    public LuaMarteScreen(Game game, SaveManager.SaveData saveData) {
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
        questLog = new QuestLog();
        createMarsResources();
        createMarsOres();
        createStartingMartians();

        if (saveData != null) {
            applySave(saveData);
            showMessage("SAVE CARREGADO: retomando Marte.");
        } else {
            showMessage("MISSÃO MARTE: colete 5 minérios verdes e leve-os até a mina para refinar.");
        }

        updateCamera();
    }

    private void applySave(SaveManager.SaveData data) {
        player.getHitbox().set(data.playerX, data.playerY, player.getWidth(), player.getHeight());
        stats.setHealth(data.health);
        stats.setHunger(data.hunger);
        stats.setOxygen(data.oxygen);

        mission = MarsMission.values()[MathUtils.clamp(data.marsMission, 0, MarsMission.values().length - 1)];
        rawOreCount = Math.max(0, data.rawOreCount);
        refinedOreCount = Math.max(0, data.refinedOreCount);
        weaponUpgraded = data.weaponUpgraded;

        portalSpawned = data.marsPortalSpawned;
        portalUnlocked = data.marsPortalUnlocked;
        portalEntryArmed = data.marsPortalEntryArmed;
        greenKeyVisible = data.greenKeyVisible;
        greenKeyCollected = data.greenKeyCollected;

        if (data.supremeAlienExists) {
            supremeAlien = new SupremeAlienBoss(2250f, 1350f);
            supremeAlien.setHealth(data.supremeAlienHealth);
            bossDeathSequenceStarted = supremeAlien.isDead();
        }

        if (portalSpawned) {
            marsPortal = new MarsPortal(2580f, 1530f);
        }

        if (portalSpawned && greenKeyCollected) {
            portalUnlocked = true;
            portalEntryArmed = true;
        }

        if (supremeAlien != null && greenKeyVisible && !greenKeyCollected) {
            greenKeyHitbox.set(
                    supremeAlien.getCenterX() - KEY_SIZE / 2f,
                    supremeAlien.getCenterY() - KEY_SIZE / 2f,
                    KEY_SIZE,
                    KEY_SIZE
            );
        }

        if (bossDeathSequenceStarted) {
            if (data.marsStormRemaining < 0f) {
                bossStorm.restorePending(
                        -data.marsStormRemaining,
                        2580f,
                        1530f
                );
            } else if (data.marsStormRemaining > 0f) {
                bossStorm.restore(data.marsStormRemaining, 2580f, 1530f);
            }
        }
    }

    private void saveGame() {
        saveGame(SaveManager.getActiveSlot());
    }

    private void saveGame(int slot) {
        SaveManager.SaveData data = new SaveManager.SaveData();
        data.phase = SaveManager.Phase.MARTE;
        data.playerX = player.getX();
        data.playerY = player.getY();
        data.health = stats.getHealth();
        data.hunger = stats.getHunger();
        data.oxygen = stats.getOxygen();

        data.marsMission = mission.ordinal();
        data.rawOreCount = rawOreCount;
        data.refinedOreCount = refinedOreCount;
        data.weaponUpgraded = weaponUpgraded;
        data.marsPortalSpawned = portalSpawned;
        data.marsPortalUnlocked = portalUnlocked;
        data.marsPortalEntryArmed = portalEntryArmed;
        data.greenKeyVisible = greenKeyVisible;
        data.greenKeyCollected = greenKeyCollected;
        data.supremeAlienExists = supremeAlien != null;
        data.supremeAlienHealth = supremeAlien == null
                ? SupremeAlienBoss.MAX_HEALTH
                : supremeAlien.getHealth();
        data.marsStormRemaining = bossStorm.isPending()
                ? -bossStorm.getDelayRemaining()
                : bossStorm.getRemaining();

        SaveManager.save(data);
        showMessage("JOGO SALVO! O save permanece mesmo fechando o jogo.");
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
        collectibleFloatTime += delta;
        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        if (portalSpawned) {
            portalRotationDegrees -= PORTAL_ROTATION_SPEED * delta;
        }
        collectResources();
        collectOres();
        handleMissionInteraction();
        recoverAtLunarBase(delta);
        updateMartians(delta);
        updateShooting(delta);
        updateLasers(delta);
        updateBoss(delta);
        bossStorm.update(delta, stats, player.getCenterX(), player.getCenterY());
        updatePortalStrikes(delta);
        handleExitPortal();

        Rectangle lunarBase = new Rectangle(
                BASE_X,
                BASE_Y,
                BASE_WIDTH,
                BASE_HEIGHT
        );
        if (!player.getHitbox().overlaps(lunarBase)) {
            stats.update(delta);
        }

        if (stats.isDead()) {
            changingScreen = true;
            DeathCause cause = stats.getDeathCause();
            dispose();
            game.setScreen(new GameOverScreen(game, cause));
            return false;
        }

        updateCamera();
        return true;
    }

    private void recoverAtLunarBase(float delta) {
        Rectangle base = new Rectangle(BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);

        if (!player.getHitbox().overlaps(base)) {
            baseRecoveryTimer = 0f;
            return;
        }

        baseRecoveryTimer += delta;
        while (baseRecoveryTimer >= 1f) {
            baseRecoveryTimer -= 1f;
            stats.addOxygen(10f);
            stats.addHunger(10f);
        }
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
        mission = MarsMission.GO_TO_NEXT;
        martians.clear();
        portalStrikes.clear();
        bossStorm.restore(0f, 2580f, 1530f);

        // Igual à Lua: a chave nasce imediatamente no local do boss.
        // O portal só aparece quando a chave é coletada.
        greenKeyVisible = true;
        greenKeyCollected = false;
        portalSpawned = false;
        portalUnlocked = false;
        portalEntryArmed = false;

        greenKeyHitbox.set(
                supremeAlien.getCenterX() - KEY_SIZE / 2f,
                supremeAlien.getCenterY() - KEY_SIZE / 2f,
                KEY_SIZE,
                KEY_SIZE
        );

        showMessage(
                "ALIEN SUPREMO DERROTADO! PEGUE A CHAVE VERDE NO LOCAL DO BOSS. "
                        + "A TEMPESTADE COMEÇA QUANDO A CHAVE FOR COLETADA!"
        );
    }

    private void spawnMarsExit() {
        portalSpawned = true;
        portalUnlocked = true;
        portalEntryArmed = true;
        marsPortal = new MarsPortal(2580f, 1530f);
    }

    private void handleExitPortal() {
        if (mission != MarsMission.GO_TO_NEXT) return;

        // Igual à Lua: tocar na chave faz a tempestade começar e revela o portal.
        if (greenKeyVisible
                && !greenKeyCollected
                && player.getHitbox().overlaps(greenKeyHitbox)) {

            greenKeyVisible = false;
            greenKeyCollected = true;

            bossStorm.startImmediate(2580f, 1530f);
            spawnMarsExit();
            saveGame();

            showMessage("CHAVE VERDE COLETADA! CORRA PARA O PORTAL!");
        }

        if (!portalSpawned || marsPortal == null || !portalUnlocked) return;

        if (player.getHitbox().overlaps(marsPortal.getHitbox())) {
            float health = stats.getHealth();
            float hunger = stats.getHunger();
            float oxygen = stats.getOxygen();
            changingScreen = true;
            saveGame();
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
        int index = 0;
        for (LuaItem item : resources) {
            Texture texture = item.getType() == LuaItem.Type.FOOD
                    ? assets.getFoodTexture()
                    : assets.getO2TankTexture();

            float floatOffset = MathUtils.sin(
                    collectibleFloatTime * 2.2f + index * 0.85f
            ) * 8f;

            batch.draw(
                    texture,
                    item.getX(),
                    item.getY() + floatOffset,
                    item.getWidth(),
                    item.getHeight()
            );

            index++;
        }
    }

    private void drawStormEffect() {
        if (!bossStorm.isActive()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        bossStorm.drawWorld(shapeRenderer, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMarsFloor();
        batch.draw(assets.getLunarBaseTexture(), BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);
        batch.draw(assets.getMineTexture(), MINE_X, MINE_Y, MINE_WIDTH, MINE_HEIGHT);
        drawResources();

        Texture oreTexture = assets.getOreTexture();
        int oreIndex = 0;
        for (MarsOre ore : ores) {
            float floatOffset = MathUtils.sin(
                    collectibleFloatTime * 2.2f + (oreIndex + 6) * 0.85f
            ) * 8f;
            batch.draw(
                    oreTexture,
                    ore.getX(),
                    ore.getY() + floatOffset,
                    ore.getWidth(),
                    ore.getHeight()
            );
            oreIndex++;
        }

        Texture alienTexture = assets.getAlienTexture();
        for (MarsEnemy enemy : martians) {
            drawTextureFacingPlayer(
                    alienTexture,
                    enemy.getX(),
                    enemy.getY(),
                    enemy.getWidth(),
                    enemy.getHeight(),
                    enemy.getCenterX(),
                    enemy.getCenterY()
            );
        }

        if (supremeAlien != null && !supremeAlien.isDead()) {
            Texture bossTexture = assets.getBossMarsTexture();
            drawTextureFacingPlayer(
                    bossTexture,
                    supremeAlien.getX(),
                    supremeAlien.getY(),
                    supremeAlien.getWidth(),
                    supremeAlien.getHeight(),
                    supremeAlien.getCenterX(),
                    supremeAlien.getCenterY()
            );
        }

        Texture portalTexture = assets.getPortalTexture();
        for (MarsPortalStrike strike : portalStrikes) {
            batch.draw(portalTexture, strike.getX(), strike.getY(), strike.getWidth(), strike.getHeight());
        }

        if (marsPortal != null && portalSpawned) {
            TextureRegion portalRegion = createCenteredSquareRegion(assets.getPortalTitaTexture());
            float portalSize = Math.min(marsPortal.getWidth(), marsPortal.getHeight());
            batch.draw(
                    portalRegion,
                    marsPortal.getX(),
                    marsPortal.getY(),
                    portalSize / 2f,
                    portalSize / 2f,
                    portalSize,
                    portalSize,
                    1f,
                    1f,
                    portalRotationDegrees
            );
        }

        if (greenKeyVisible) {
            Texture keyTexture = assets.getGreenKeyTexture();
            float keyFloatOffset = MathUtils.sin(collectibleFloatTime * 2.2f + 1.7f) * 8f;
            batch.draw(
                    keyTexture,
                    greenKeyHitbox.x,
                    greenKeyHitbox.y + keyFloatOffset,
                    greenKeyHitbox.width,
                    greenKeyHitbox.height
            );
        }

        drawPlayerLasers();

        drawPlayerFacingMouse();
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

        for (MarsPortalStrike strike : portalStrikes) {
            if (!strike.isExploding()) {
                continue;
            }

            float progress = strike.getExplosionProgress();
            float centerX = strike.getX() + strike.getWidth() / 2f;
            float centerY = strike.getY() + strike.getHeight() / 2f;
            float outerRadius = 70f + 130f * progress;
            float alpha = Math.max(0.04f, 0.35f * (1f - progress));

            shapeRenderer.setColor(new Color(0.05f, 0.45f, 1f, alpha));
            shapeRenderer.circle(centerX, centerY, outerRadius);

            shapeRenderer.setColor(new Color(0.40f, 0.82f, 1f, Math.max(0.05f, 0.50f * (1f - progress))));
            shapeRenderer.circle(centerX, centerY, 38f + 55f * progress);
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

    private void drawPlayerLasers() {
        Texture laserTexture = assets.getLaserTexture();
        TextureRegion laserRegion = new TextureRegion(laserTexture);

        float aspect = laserTexture.getWidth()
                / (float) Math.max(1, laserTexture.getHeight());

        float width = aspect >= 1f
                ? LASER_RENDER_LENGTH
                : LASER_RENDER_LENGTH * aspect;
        float height = aspect >= 1f
                ? LASER_RENDER_LENGTH / Math.max(aspect, 0.001f)
                : LASER_RENDER_LENGTH;

        for (Laser laser : lasers) {
            float angle = MathUtils.atan2(
                    laser.getDirectionY(),
                    laser.getDirectionX()
            ) * MathUtils.radiansToDegrees;

            if (laserTexture.getHeight() > laserTexture.getWidth()) {
                angle -= 90f;
            }

            batch.draw(
                    laserRegion,
                    laser.getHitbox().x + laser.getWidth() / 2f - width / 2f,
                    laser.getHitbox().y + laser.getHeight() / 2f - height / 2f,
                    width / 2f,
                    height / 2f,
                    width,
                    height,
                    1f,
                    1f,
                    angle
            );
        }
    }

    private void drawPlayerFacingMouse() {
        Vector3 mouseWorld = new Vector3(
                Gdx.input.getX(),
                Gdx.input.getY(),
                0f
        );
        camera.unproject(mouseWorld);

        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        float width = player.getWidth();
        float height = player.getHeight();

        TextureRegion playerRegion = new TextureRegion(assets.getPlayerTexture());

        batch.draw(
                playerRegion,
                player.getX(),
                player.getY(),
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                angle
        );
    }

    private TextureRegion createCenteredSquareRegion(Texture texture) {
        int side = Math.min(texture.getWidth(), texture.getHeight());
        int x = (texture.getWidth() - side) / 2;
        int y = (texture.getHeight() - side) / 2;
        return new TextureRegion(texture, x, y, side, side);
    }

    private void drawTextureFacingPlayer(
            Texture texture,
            float x,
            float y,
            float width,
            float height,
            float targetX,
            float targetY
    ) {
        float dx = player.getCenterX() - targetX;
        float dy = player.getCenterY() - targetY;
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        TextureRegion region = new TextureRegion(texture);

        batch.draw(
                region,
                x,
                y,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                angle
        );
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
        hudFont.draw(batch, String.format("SACIAÇÃO: %.0f / 100", stats.getHunger()), x + 10f, firstY - gap + 20f);
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

        if (bossStorm.isActive()) {
            hudFont.setColor(Color.valueOf("D98CFF"));
            hudFont.getData().setScale(1.0f);
            hudFont.draw(
                    batch,
                    String.format("TEMPESTADE ROXA: %.1fs | -10 HP/s | PORTAL FECHADO",
                            bossStorm.getRemaining()),
                    28f,
                    hudViewport.getWorldHeight() - 318f
            );
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                && !pauseMenu.isOpen()) {
            saveGame();
            changingScreen = true;
            dispose();
            game.setScreen(new FastTravelScreen(game, SaveManager.load()));
            return;
        }

        boolean questWasOpen = questLog.isOpen();

        if (!questWasOpen
                && !pauseMenu.isOpen()
                && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            saveGame();
            questLog.open();
        }

        if (questWasOpen) {
            questLog.handleInput();
        }

        PauseMenu.Action pauseAction = (questWasOpen || questLog.isOpen())
                ? PauseMenu.Action.NONE
                : pauseMenu.handleInput();

        if (pauseAction == PauseMenu.Action.SAVE_SLOT_1) {
            saveGame(1);
            return;
        }

        if (pauseAction == PauseMenu.Action.SAVE_SLOT_2) {
            saveGame(2);
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

        if (!pauseMenu.isOpen() && !questWasOpen && !questLog.isOpen()) {
            if (!update(delta)) return;
        }

        ScreenUtils.clear(0.72f, 0.25f, 0.06f, 1f);
        drawWorld();
        drawBossEffects();
        drawStormEffect();
        drawHud();
        pauseMenu.render();
        questLog.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        pauseMenu.resize(width, height);
        questLog.resize(width, height);
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
        questLog.dispose();
    }
}

/** Compatibility class used by LuaScreen and MarsIntroScreen. */
class MarteScreen extends LuaMarteScreen {
    public MarteScreen(Game game) {
        super(game);
    }
}
