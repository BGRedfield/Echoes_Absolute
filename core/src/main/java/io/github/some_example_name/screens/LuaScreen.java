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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.AmericanEnemy;
import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.EnemyBullet;
import io.github.some_example_name.entities.ExplosionEffect;
import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.LuaMission;
import io.github.some_example_name.entities.MarsPortal;
import io.github.some_example_name.entities.MissileWarning;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.entities.RifleWeapon;
import io.github.some_example_name.entities.TrumpBoss;
import io.github.some_example_name.entities.TrumpMissile;
import io.github.some_example_name.managers.AssetManager;
import io.github.some_example_name.managers.SaveManager;
import io.github.some_example_name.systems.BossStorm;

/** Complete Lua phase: mission, enemies, Trump boss, weapons and portal to Mars. */
public class LuaScreen extends ScreenAdapter {

    private enum TrumpAttackPhase {
        COOLDOWN,
        MISSILE_WARNING,
        MISSILE_TRAVEL,
        RIFLE_BARRIER,
        AMERICAN_WAVE
    }

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float WORLD_WIDTH = 3000f;
    private static final float WORLD_HEIGHT = 2000f;
    private static final float PLAYER_SPAWN_X = 400f;
    private static final float PLAYER_SPAWN_Y = 400f;
    private static final float LUNAR_BASE_X = 500f;
    private static final float LUNAR_BASE_Y = 390f;
    private static final float LUNAR_BASE_WIDTH = 240f;
    private static final float LUNAR_BASE_HEIGHT = 160f;
    private static final float TILE_SIZE = 128f;
    private static final int REQUIRED_ICE = 5;
    private static final float PLAYER_SHOT_DAMAGE = 10f;
    private static final float ENEMY_BULLET_DAMAGE = 10f;
    private static final float BOSS_ATTACK_COOLDOWN = 5f;
    private static final float MISSILE_WARNING_TIME = 2f;
    private static final float MESSAGE_DURATION = 4f;
    private static final float BOSS_EXPLOSION_DURATION = 1.25f;
    private static final float RIFLE_ORBIT_RADIUS = 410f;
    private static final float RIFLE_ORBIT_SPEED = 0.55f;
    private static final float RIFLE_OUTWARD_ROTATION_OFFSET = 0f;
    private static final int MAX_ACTIVE_LUA_AMERICANS = 3;
    private static final float AMERICAN_RESPAWN_INTERVAL = 2.5f;
    private static final float RED_KEY_SIZE = 76f;

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
    private final Array<Laser> lasers = new Array<>();
    private final Array<LuaItem> luaItems = new Array<>();
    private final Array<AmericanEnemy> americans = new Array<>();
    private final Array<EnemyBullet> americanBullets = new Array<>();
    private final Array<EnemyBullet> rifleBullets = new Array<>();
    private final Array<TrumpMissile> trumpMissiles = new Array<>();
    private final Array<MissileWarning> missileWarnings = new Array<>();
    private final Array<RifleWeapon> rifleWeapons = new Array<>();
    private final Array<ExplosionEffect> missileExplosions = new Array<>();
    private final BossStorm bossStorm = new BossStorm();

    private LuaMission mission = LuaMission.COLLECT_ICE;
    private TrumpBoss trumpBoss;
    private TrumpAttackPhase trumpAttackPhase;
    private TrumpAttackPhase nextTrumpAttack;
    private MarsPortal marsPortal;
    private final Rectangle redKeyHitbox = new Rectangle();

    private float bossPhaseTimer;
    private float bossCooldownTimer;
    private float messageTimer;
    private float bossExplosionTimer;
    private float rifleOrbitAngle;
    private float americanSpawnTimer;
    private float baseRecoveryTimer;
    private boolean portalSpawned;
    private boolean portalUnlocked;
    private boolean portalEntryArmed;
    private boolean redKeyVisible;
    private boolean redKeyCollected;
    private boolean bossDeathSequenceStarted;
    private int bossAttackCycle;
    private String missionMessage = "";
    private boolean screenChanged;

    public LuaScreen(Game game) {
        this(game, null);
    }

    public LuaScreen(Game game, SaveManager.SaveData saveData) {
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
        createLuaResources();
        createStartingAmericans();

        if (saveData != null) {
            applySave(saveData);
            showMessage("SAVE CARREGADO: retomando a Lua.");
        } else {
            showMessage("MISSÃO LUA: colete 5 gelos e leve os 5 até a base lunar.");
        }

        updateCamera();
    }

    private void applySave(SaveManager.SaveData data) {
        player.getHitbox().set(data.playerX, data.playerY, player.getWidth(), player.getHeight());
        stats.setHealth(data.health);
        stats.setHunger(data.hunger);
        stats.setOxygen(data.oxygen);

        mission = LuaMission.values()[MathUtils.clamp(data.luaMission, 0, LuaMission.values().length - 1)];

        while (stats.getIceCollected() < data.iceCollected) {
            stats.collectIce();
        }

        portalSpawned = data.luaPortalSpawned;
        portalUnlocked = data.luaPortalUnlocked;
        portalEntryArmed = data.luaPortalEntryArmed;
        redKeyVisible = data.redKeyVisible;
        redKeyCollected = data.redKeyCollected;

        if (data.trumpBossExists) {
            trumpBoss = new TrumpBoss(1240f, 850f);
            trumpBoss.setHealth(data.trumpBossHealth);
            bossDeathSequenceStarted = trumpBoss.isDead();
        }

        if (portalSpawned) {
            marsPortal = new MarsPortal(2580f, 1530f);
            if (trumpBoss != null) {
                redKeyHitbox.set(
                        trumpBoss.getCenterX() - RED_KEY_SIZE / 2f,
                        trumpBoss.getCenterY() - RED_KEY_SIZE / 2f,
                        RED_KEY_SIZE,
                        RED_KEY_SIZE
                );
            }
        }

        if (!portalSpawned && bossDeathSequenceStarted) {
            float remaining = data.luaStormRemaining > 0f
                    ? data.luaStormRemaining
                    : BossStorm.DURATION;
            bossStorm.restore(remaining, 2580f, 1530f);
        }
    }

    private void saveGame() {
        saveGame(SaveManager.getActiveSlot());
    }

    private void saveGame(int slot) {
        SaveManager.SaveData data = new SaveManager.SaveData();
        data.phase = SaveManager.Phase.LUA;
        data.playerX = player.getX();
        data.playerY = player.getY();
        data.health = stats.getHealth();
        data.hunger = stats.getHunger();
        data.oxygen = stats.getOxygen();

        data.luaMission = mission.ordinal();
        data.iceCollected = stats.getIceCollected();
        data.luaPortalSpawned = portalSpawned;
        data.luaPortalUnlocked = portalUnlocked;
        data.luaPortalEntryArmed = portalEntryArmed;
        data.redKeyVisible = redKeyVisible;
        data.redKeyCollected = redKeyCollected;
        data.trumpBossExists = trumpBoss != null;
        data.trumpBossHealth = trumpBoss == null ? TrumpBoss.MAX_HEALTH : trumpBoss.getHealth();
        data.luaStormRemaining = bossStorm.getRemaining();

        SaveManager.save(data);
        showMessage("JOGO SALVO! O save permanece mesmo fechando o jogo.");
    }

    private void createLuaResources() {
        addItem(LuaItem.Type.FOOD, 780f, 620f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1120f, 430f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1680f, 560f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 2240f, 420f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 720f, 1510f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1900f, 1560f, 56f, 56f);
        addItem(LuaItem.Type.O2_TANK, 1050f, 920f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2050f, 1320f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 1450f, 560f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2640f, 1150f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 560f, 1180f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2400f, 1540f, 46f, 125f);
        addItem(LuaItem.Type.ICE, 880f, 820f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 1350f, 1300f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 2380f, 760f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 1680f, 1480f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 620f, 1600f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 2520f, 480f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 1130f, 1780f, 74f, 74f);
        addItem(LuaItem.Type.ICE, 2180f, 1160f, 74f, 74f);
    }

    private void addItem(LuaItem.Type type, float x, float y, float width, float height) {
        luaItems.add(new LuaItem(type, x, y, width, height));
    }

    private void createStartingAmericans() {
        americans.add(new AmericanEnemy(2380f, 1540f));
        americans.add(new AmericanEnemy(2200f, 520f));
        americans.add(new AmericanEnemy(1750f, 1700f));
        americanSpawnTimer = AMERICAN_RESPAWN_INTERVAL;
    }

    @Override
    public void show() {
        screenChanged = false;
        viewport.apply(true);
        hudViewport.apply(true);
        updateCamera();
    }

    private boolean update(float delta) {
        delta = Math.min(delta, 0.05f);
        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        collectItems();
        handleMissionInteraction();
        recoverAtLunarBase(delta);
        updateAmericans(delta);
        updatePlayerShooting();
        updatePlayerLasers(delta);
        updateMissiles(delta);
        updateMissileExplosions(delta);
        updateRifleBullets(delta);
        updateAmericanBullets(delta);
        updateBoss(delta);
        bossStorm.update(delta, stats);
        handleMarsPortalInteraction();
        stats.update(delta);
        if (stats.isDead()) { openGameOver(); return false; }
        if (mission == LuaMission.GO_TO_MARS && portalSpawned && portalUnlocked && portalEntryArmed
                && marsPortal != null && player.getHitbox().overlaps(marsPortal.getHitbox())) {
            screenChanged = true;
            float health = stats.getHealth();
            float hunger = stats.getHunger();
            float oxygen = stats.getOxygen();
            dispose();
            game.setScreen(new LuaLevelStatusScreen(game, health, hunger, oxygen, REQUIRED_ICE));
            return false;
        }
        updateCamera();
        return true;
    }

    private void collectItems() {
        for (int i = luaItems.size - 1; i >= 0; i--) {
            LuaItem item = luaItems.get(i);
            if (!player.getHitbox().overlaps(item.getHitbox())) continue;
            switch (item.getType()) {
                case FOOD: stats.eatFood(); showMessage("Comida coletada: fome e vida restauradas."); break;
                case O2_TANK: stats.addOxygen(20f); showMessage("O2 coletado: oxigênio restaurado."); break;
                case ICE:
                    stats.collectIce();
                    if (mission == LuaMission.COLLECT_ICE) {
                        if (stats.getIceCollected() >= REQUIRED_ICE) {
                            mission = LuaMission.MELT_ICE;
                            showMessage("5 gelos coletados! Leve os 5 à base e pressione E para derreter e beber a água.");
                        } else {
                            showMessage("Gelo coletado: " + stats.getIceCollected() + "/" + REQUIRED_ICE + ".");
                        }
                    }
                    break;
                default: break;
            }
            luaItems.removeIndex(i);
        }
    }

    private void recoverAtLunarBase(float delta) {
        Rectangle base = new Rectangle(
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

        if (!player.getHitbox().overlaps(base)) {
            baseRecoveryTimer = 0f;
            return;
        }

        baseRecoveryTimer += delta;
        while (baseRecoveryTimer >= 1f) {
            baseRecoveryTimer -= 1f;
            stats.addOxygen(5f);
            stats.addHunger(5f);
        }
    }

    private void handleMissionInteraction() {
        if (mission != LuaMission.MELT_ICE) return;
        Rectangle base = new Rectangle(LUNAR_BASE_X, LUNAR_BASE_Y, LUNAR_BASE_WIDTH, LUNAR_BASE_HEIGHT);
        if (!player.getHitbox().overlaps(base)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && stats.consumeIce(REQUIRED_ICE)) {
            stats.drinkWater();
            spawnTrumpBoss();
            showMessage("Os 5 gelos foram derretidos. Água ingerida. O chefe da Lua apareceu!");
        }
    }

    private void updateAmericans(float delta) {
        for (int i = americans.size - 1; i >= 0; i--) {
            AmericanEnemy enemy = americans.get(i);
            enemy.update(delta, player.getCenterX(), player.getCenterY(), americanBullets);
            if (enemy.isDead()) americans.removeIndex(i);
        }
        if (trumpBoss == null && mission != LuaMission.GO_TO_MARS && americans.size < MAX_ACTIVE_LUA_AMERICANS) {
            americanSpawnTimer -= delta;
            if (americanSpawnTimer <= 0f) {
                spawnAmericanReinforcement();
                americanSpawnTimer = AMERICAN_RESPAWN_INTERVAL;
            }
        }
        if (mission == LuaMission.DEFEAT_TRUMP && trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE && americans.size == 0) {
            startBossCooldown(TrumpAttackPhase.MISSILE_WARNING, "Os 5 americanos foram derrotados. Próximo ataque em 5 segundos.");
        }
    }

    private void spawnAmericanReinforcement() {
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = MathUtils.random(160f, WORLD_WIDTH - 220f);
            float y = MathUtils.random(160f, WORLD_HEIGHT - 220f);
            float dx = x - player.getCenterX();
            float dy = y - player.getCenterY();
            if (dx * dx + dy * dy >= 450f * 450f) {
                americans.add(new AmericanEnemy(x, y));
                return;
            }
        }
        americans.add(new AmericanEnemy(WORLD_WIDTH - 300f, WORLD_HEIGHT - 300f));
    }

    private void updatePlayerShooting() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseWorld);
        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) return;
        dx /= length; dy /= length;
        lasers.add(new Laser(player.getCenterX(), player.getCenterY(), dx, dy));
    }

    private void updatePlayerLasers(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);
            boolean hit = false;
            if (americans.size > 0) hit = hitAmericanWithLaser(laser);
            if (!hit && mission == LuaMission.DEFEAT_TRUMP) {
                if (trumpAttackPhase == TrumpAttackPhase.RIFLE_BARRIER) hit = hitRifleWithLaser(laser);
                if (!hit && trumpBoss != null && !trumpBoss.isDead() && laser.getHitbox().overlaps(trumpBoss.getHitbox())) {
                    trumpBoss.takeDamage(PLAYER_SHOT_DAMAGE);
                    hit = true;
                    if (trumpBoss.isDead()) startBossDeathSequence();
                }
            }
            if (laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT) || hit) lasers.removeIndex(i);
        }
    }

    private boolean hitAmericanWithLaser(Laser laser) {
        for (int i = americans.size - 1; i >= 0; i--) {
            AmericanEnemy enemy = americans.get(i);
            if (!enemy.isDead() && laser.getHitbox().overlaps(enemy.getHitbox())) {
                enemy.takeDamage(PLAYER_SHOT_DAMAGE);
                return true;
            }
        }
        return false;
    }

    private boolean hitRifleWithLaser(Laser laser) {
        for (int i = rifleWeapons.size - 1; i >= 0; i--) {
            RifleWeapon rifle = rifleWeapons.get(i);
            if (!rifle.isDestroyed() && laser.getHitbox().overlaps(rifle.getHitbox())) {
                rifle.takeDamage(PLAYER_SHOT_DAMAGE);
                return true;
            }
        }
        return false;
    }

    private void spawnTrumpBoss() {
        if (trumpBoss != null) return;
        mission = LuaMission.DEFEAT_TRUMP;
        trumpBoss = new TrumpBoss(1240f, 850f);
        americans.clear();
        americanBullets.clear();
        rifleBullets.clear();
        rifleWeapons.clear();
        trumpMissiles.clear();
        missileWarnings.clear();
        missileExplosions.clear();
        bossExplosionTimer = 0f;
        bossDeathSequenceStarted = false;
        portalSpawned = false;
        portalUnlocked = false;
        portalEntryArmed = false;
        redKeyVisible = false;
        redKeyCollected = false;
        redKeyHitbox.set(0f, 0f, RED_KEY_SIZE, RED_KEY_SIZE);
        bossAttackCycle = 0;
        startBossCooldown(TrumpAttackPhase.MISSILE_WARNING, "TRUMP APARECEU! Primeiro ataque em 5 segundos.");
    }

    private void startBossCooldown(TrumpAttackPhase nextAttack, String message) {
        trumpAttackPhase = TrumpAttackPhase.COOLDOWN;
        nextTrumpAttack = nextAttack;
        bossCooldownTimer = BOSS_ATTACK_COOLDOWN;
        bossPhaseTimer = 0f;
        missileWarnings.clear();
        trumpMissiles.clear();
        rifleBullets.clear();
        if (message != null && !message.isEmpty()) showMessage(message);
    }

    private void updateBoss(float delta) {
        if (trumpBoss == null) return;
        if (trumpBoss.isDead()) { finishBossDeath(delta); return; }
        trumpBoss.update(delta, player.getCenterX(), player.getCenterY(), WORLD_WIDTH, WORLD_HEIGHT);
        bossPhaseTimer += delta;
        rifleOrbitAngle += delta * RIFLE_ORBIT_SPEED;
        switch (trumpAttackPhase) {
            case COOLDOWN:
                bossCooldownTimer -= delta;
                if (bossCooldownTimer <= 0f) beginNextBossAttack();
                break;
            case MISSILE_WARNING: updateMissileWarnings(delta); break;
            case MISSILE_TRAVEL:
                if (trumpMissiles.size == 0) startBossCooldown(TrumpAttackPhase.RIFLE_BARRIER, "Mísseis concluídos. Barreira de AK-47 em 5 segundos.");
                break;
            case RIFLE_BARRIER: updateRifleBarrier(delta); break;
            case AMERICAN_WAVE: break;
            default: break;
        }
        updateRifleOrbitPositions();
    }

    private void beginNextBossAttack() {
        if (nextTrumpAttack == TrumpAttackPhase.MISSILE_WARNING) beginMissileWarning();
        else if (nextTrumpAttack == TrumpAttackPhase.RIFLE_BARRIER) beginRifleBarrier();
        else if (nextTrumpAttack == TrumpAttackPhase.AMERICAN_WAVE) beginAmericanWave();
    }

    private void beginMissileWarning() {
        trumpAttackPhase = TrumpAttackPhase.MISSILE_WARNING;
        bossPhaseTimer = 0f;
        bossAttackCycle++;
        missileWarnings.clear();
        trumpMissiles.clear();
        rifleWeapons.clear();
        rifleBullets.clear();
        float shift = (bossAttackCycle % 3) * 100f;
        addMissileWarning(850f + shift, 650f);
        addMissileWarning(1500f - shift, 1180f);
        addMissileWarning(2300f, 720f + shift);
        showMessage("ATAQUE DE MÍSSEIS: áreas vermelhas = impacto em 2 segundos!");
    }

    private void addMissileWarning(float centerX, float centerY) {
        float size = TrumpMissile.IMPACT_SIZE;
        float x = MathUtils.clamp(centerX - size / 2f, 0f, WORLD_WIDTH - size);
        float y = MathUtils.clamp(centerY - size / 2f, 0f, WORLD_HEIGHT - size);
        missileWarnings.add(new MissileWarning(x, y, size, size, MISSILE_WARNING_TIME));
    }

    private void updateMissileWarnings(float delta) {
        boolean ready = true;
        for (MissileWarning warning : missileWarnings) {
            warning.update(delta);
            if (!warning.isReadyToLaunch()) ready = false;
        }
        if (!ready) return;
        trumpMissiles.clear();
        for (MissileWarning warning : missileWarnings) {
            Rectangle area = warning.getArea();
            trumpMissiles.add(new TrumpMissile(trumpBoss.getCenterX(), trumpBoss.getCenterY(), area.x + area.width / 2f, area.y + area.height / 2f));
        }
        missileWarnings.clear();
        trumpAttackPhase = TrumpAttackPhase.MISSILE_TRAVEL;
        bossPhaseTimer = 0f;
    }

    private void updateMissiles(float delta) {
        if (trumpMissiles.size == 0) return;
        for (int i = trumpMissiles.size - 1; i >= 0; i--) {
            TrumpMissile missile = trumpMissiles.get(i);
            missile.update(delta);
            if (missile.hasArrived()) {
                Rectangle impact = missile.getImpactArea();
                missileExplosions.add(new ExplosionEffect(impact.x + impact.width / 2f, impact.y + impact.height / 2f));
                if (player.getHitbox().overlaps(impact)) stats.damage(ENEMY_BULLET_DAMAGE * 2f, DeathCause.TRUMP_MISSILE);
                trumpMissiles.removeIndex(i);
            }
        }
    }

    private void updateMissileExplosions(float delta) {
        for (int i = missileExplosions.size - 1; i >= 0; i--) {
            ExplosionEffect explosion = missileExplosions.get(i);
            explosion.update(delta);
            if (explosion.isFinished()) missileExplosions.removeIndex(i);
        }
    }

    private void beginRifleBarrier() {
        trumpAttackPhase = TrumpAttackPhase.RIFLE_BARRIER;
        bossPhaseTimer = 0f;
        rifleOrbitAngle = 0f;
        rifleWeapons.clear();
        rifleBullets.clear();
        for (int i = 0; i < RifleWeapon.DEFAULT_NAMES.length; i++) {
            rifleWeapons.add(new RifleWeapon(RifleWeapon.DEFAULT_NAMES[i], trumpBoss.getCenterX(), trumpBoss.getCenterY()));
        }
        showMessage("BARREIRA DE AK-47: elas orbitam o Trump e apontam para fora. Destrua todas.");
        updateRifleOrbitPositions();
    }

    private void updateRifleBarrier(float delta) {
        int alive = 0;
        for (RifleWeapon rifle : rifleWeapons) {
            if (rifle.isDestroyed()) continue;
            alive++;
            rifle.update(delta, rifleBullets);
        }
        if (alive == 0) {
            rifleBullets.clear();
            startBossCooldown(TrumpAttackPhase.AMERICAN_WAVE, "Todas as AK-47 foram destruídas. Cinco americanos chegam em 5 segundos.");
        }
    }

    private void beginAmericanWave() {
        trumpAttackPhase = TrumpAttackPhase.AMERICAN_WAVE;
        bossPhaseTimer = 0f;
        americans.clear();
        americanBullets.clear();
        americans.add(new AmericanEnemy(520f, 1550f));
        americans.add(new AmericanEnemy(900f, 1650f));
        americans.add(new AmericanEnemy(2100f, 1550f));
        americans.add(new AmericanEnemy(2450f, 520f));
        americans.add(new AmericanEnemy(1850f, 420f));
        showMessage("ONDA DE REFORÇOS: 5 americanos apareceram!");
    }

    private void updateRifleOrbitPositions() {
        if (trumpBoss == null || rifleWeapons.size == 0) return;
        float centerX = trumpBoss.getCenterX();
        float centerY = trumpBoss.getCenterY();
        int total = rifleWeapons.size;
        for (int i = 0; i < total; i++) {
            RifleWeapon rifle = rifleWeapons.get(i);
            if (rifle.isDestroyed()) continue;
            float angle = rifleOrbitAngle + MathUtils.PI2 * i / total;
            float x = centerX + MathUtils.cos(angle) * RIFLE_ORBIT_RADIUS - rifle.getWidth() / 2f;
            float y = centerY + MathUtils.sin(angle) * RIFLE_ORBIT_RADIUS - rifle.getHeight() / 2f;
            x = MathUtils.clamp(x, 0f, WORLD_WIDTH - rifle.getWidth());
            y = MathUtils.clamp(y, 0f, WORLD_HEIGHT - rifle.getHeight());
            rifle.setPosition(x, y);
            rifle.setOutwardDirection(MathUtils.cos(angle), MathUtils.sin(angle));
            rifle.setRotationDegrees(angle * MathUtils.radiansToDegrees + RIFLE_OUTWARD_ROTATION_OFFSET);
        }
    }

    private void updateAmericanBullets(float delta) {
        for (int i = americanBullets.size - 1; i >= 0; i--) {
            EnemyBullet bullet = americanBullets.get(i);
            bullet.update(delta);
            if (bullet.getHitbox().overlaps(player.getHitbox())) {
                stats.damage(ENEMY_BULLET_DAMAGE, DeathCause.AMERICAN_BULLET);
                americanBullets.removeIndex(i);
                continue;
            }
            if (bullet.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) americanBullets.removeIndex(i);
        }
    }

    private void updateRifleBullets(float delta) {
        for (int i = rifleBullets.size - 1; i >= 0; i--) {
            EnemyBullet bullet = rifleBullets.get(i);
            bullet.update(delta);
            if (bullet.getHitbox().overlaps(player.getHitbox())) {
                stats.damage(ENEMY_BULLET_DAMAGE, DeathCause.RIFLE_BULLET);
                rifleBullets.removeIndex(i);
                continue;
            }
            if (bullet.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) rifleBullets.removeIndex(i);
        }
    }

    private void startBossDeathSequence() {
        if (bossDeathSequenceStarted) return;
        bossDeathSequenceStarted = true;
        bossExplosionTimer = BOSS_EXPLOSION_DURATION;
        mission = LuaMission.GO_TO_MARS;
        americans.clear();
        americanBullets.clear();
        rifleBullets.clear();
        rifleWeapons.clear();
        missileWarnings.clear();
        trumpMissiles.clear();
        missileExplosions.clear();
        bossStorm.start(2580f, 1530f);
        showMessage("TRUMP DERROTADO! UMA TEMPESTADE ROXA FECHOU O PORTAL! Sobreviva por 12s.");
    }

    private void finishBossDeath(float delta) {
        if (!bossDeathSequenceStarted) startBossDeathSequence();
        bossExplosionTimer -= delta;

        // O portal permanece fechado enquanto a tempestade estiver ativa.
        if (bossExplosionTimer <= 0f && bossStorm.isFinished() && !portalSpawned) {
            spawnMarsPortal();
        }
    }

    private void spawnMarsPortal() {
        portalSpawned = true;
        portalUnlocked = false;
        portalEntryArmed = false;
        redKeyCollected = false;
        redKeyVisible = true;
        marsPortal = new MarsPortal(2580f, 1530f);
        redKeyHitbox.set(trumpBoss.getCenterX() - RED_KEY_SIZE / 2f, trumpBoss.getCenterY() - RED_KEY_SIZE / 2f, RED_KEY_SIZE, RED_KEY_SIZE);
        showMessage("A chave vermelha apareceu no local do Trump. Toque nela para coletá-la e siga a flecha até o portal!");
    }

    private void handleMarsPortalInteraction() {
        if (!portalSpawned || marsPortal == null || mission != LuaMission.GO_TO_MARS) return;
        boolean playerAtPortal = player.getHitbox().overlaps(marsPortal.getHitbox());
        if (redKeyVisible && !redKeyCollected && player.getHitbox().overlaps(redKeyHitbox)) {
            redKeyCollected = true;
            redKeyVisible = false;
            showMessage("CHAVE VERMELHA coletada! Agora chegue ao portal e pressione E.");
        }
        if (!portalUnlocked) {
            if (!redKeyCollected) return;
            if (playerAtPortal && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                portalUnlocked = true;
                portalEntryArmed = false;
                showMessage("PORTAL DESBLOQUEADO! Saia e entre novamente no portal para viajar a Marte.");
            }
            return;
        }
        if (!playerAtPortal) portalEntryArmed = true;
    }

    private void openGameOver() {
        if (screenChanged) return;
        screenChanged = true;
        DeathCause cause = stats.getDeathCause();
        dispose();
        game.setScreen(new GameOverScreen(game, cause));
    }

    private void updateCamera() {
        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;
        float cameraX = MathUtils.clamp(player.getCenterX(), halfViewportWidth, WORLD_WIDTH - halfViewportWidth);
        float cameraY = MathUtils.clamp(player.getCenterY(), halfViewportHeight, WORLD_HEIGHT - halfViewportHeight);
        camera.position.set(cameraX, cameraY, 0f);
        camera.update();
    }

    private void drawLuaFloor() {
        Texture tile = assets.getLuaTileTexture();
        for (float x = 0f; x < WORLD_WIDTH; x += TILE_SIZE) {
            for (float y = 0f; y < WORLD_HEIGHT; y += TILE_SIZE) {
                float width = Math.min(TILE_SIZE, WORLD_WIDTH - x);
                float height = Math.min(TILE_SIZE, WORLD_HEIGHT - y);
                batch.draw(tile, x, y, width, height);
            }
        }
    }

    private void drawLuaItems() {
        for (LuaItem item : luaItems) {
            Texture texture;
            switch (item.getType()) {
                case FOOD: texture = assets.getFoodTexture(); break;
                case O2_TANK: texture = assets.getO2TankTexture(); break;
                case ICE: texture = assets.getIceTexture(); break;
                default: continue;
            }
            batch.draw(texture, item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
    }

    private void drawBar(float x, float y, float width, float height, float value, float maxValue, Color color) {
        shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.08f, 0.92f));
        shapeRenderer.rect(x, y, width, height);
        float percent = MathUtils.clamp(value / maxValue, 0f, 1f);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawTrumpHealthBar() {
        if (trumpBoss == null || trumpBoss.isDead()) return;

        float hudWidth = hudViewport.getWorldWidth();
        float barWidth = Math.min(900f, hudWidth - 120f);
        float barHeight = 24f;
        float barX = (hudWidth - barWidth) / 2f;
        float barY = 40f;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBar(barX, barY, barWidth, barHeight, trumpBoss.getHealth(), TrumpBoss.MAX_HEALTH, Color.ORANGE);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        hudFont.getData().setScale(1.15f);
        hudFont.setColor(Color.WHITE);
        String name = "TRUMP";
        GlyphLayout nameLayout = new GlyphLayout(hudFont, name);
        hudFont.draw(batch, name, hudWidth / 2f - nameLayout.width / 2f, barY + 55f);
        hudFont.getData().setScale(0.9f);
        String hpText = String.format("%.0f / %.0f HP", trumpBoss.getHealth(), TrumpBoss.MAX_HEALTH);
        GlyphLayout hpLayout = new GlyphLayout(hudFont, hpText);
        hudFont.draw(batch, hpText, hudWidth / 2f - hpLayout.width / 2f, barY - 8f);
        batch.end();
    }

    private void drawPortalArrow() {
        if (!portalSpawned || marsPortal == null || portalUnlocked) return;
        if (player.getHitbox().overlaps(marsPortal.getHitbox())) return;

        Vector3 targetScreen = new Vector3(marsPortal.getHitbox().x + marsPortal.getHitbox().width / 2f, marsPortal.getHitbox().y + marsPortal.getHitbox().height / 2f, 0f);
        Vector3 playerScreen = new Vector3(player.getCenterX(), player.getCenterY(), 0f);
        camera.project(targetScreen);
        camera.project(playerScreen);
        float dx = targetScreen.x - playerScreen.x;
        float dy = targetScreen.y - playerScreen.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) return;
        dx /= length; dy /= length;

        float centerX = hudViewport.getWorldWidth() / 2f;
        float centerY = hudViewport.getWorldHeight() - 135f;
        float tipDistance = 48f;
        float sideDistance = 18f;
        float tipX = centerX + dx * tipDistance;
        float tipY = centerY + dy * tipDistance;
        float baseX = centerX - dx * 12f;
        float baseY = centerY - dy * 12f;
        float perpX = -dy;
        float perpY = dx;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rectLine(centerX - dx * 15f, centerY - dy * 15f, baseX, baseY, 8f);
        shapeRenderer.triangle(tipX, tipY, baseX + perpX * sideDistance, baseY + perpY * sideDistance, baseX - perpX * sideDistance, baseY - perpY * sideDistance);
        shapeRenderer.end();
    }

    private void drawWarningsAndProjectiles() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(1f, 0f, 0f, 0.34f));
        for (MissileWarning warning : missileWarnings) {
            shapeRenderer.rect(warning.getX(), warning.getY(), warning.getWidth(), warning.getHeight());
        }

        shapeRenderer.setColor(Color.RED);
        for (TrumpMissile missile : trumpMissiles) {
            shapeRenderer.rect(missile.getX(), missile.getY(), missile.getWidth(), missile.getHeight());
        }

        for (ExplosionEffect explosion : missileExplosions) {
            float progress = explosion.getProgress();
            float radius = explosion.getRadius();
            shapeRenderer.setColor(new Color(1f, 0.15f, 0.02f, 0.18f * (1f - progress)));
            shapeRenderer.circle(explosion.getCenterX(), explosion.getCenterY(), radius);
            shapeRenderer.setColor(new Color(1f, 0.65f, 0.05f, 0.70f * (1f - progress)));
            shapeRenderer.circle(explosion.getCenterX(), explosion.getCenterY(), radius * 0.58f);
            shapeRenderer.setColor(new Color(1f, 0.92f, 0.30f, 0.88f * (1f - progress)));
            shapeRenderer.circle(explosion.getCenterX(), explosion.getCenterY(), radius * 0.25f);
        }

        shapeRenderer.setColor(Color.BLUE);
        for (EnemyBullet bullet : americanBullets) shapeRenderer.rect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
        for (EnemyBullet bullet : rifleBullets) shapeRenderer.rect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());

        if (bossDeathSequenceStarted && trumpBoss != null && !portalSpawned) {
            float progress = 1f - MathUtils.clamp(bossExplosionTimer / BOSS_EXPLOSION_DURATION, 0f, 1f);
            float radius = 60f + progress * 240f;
            shapeRenderer.setColor(new Color(1f, 0.55f, 0.05f, 0.70f));
            shapeRenderer.circle(trumpBoss.getCenterX(), trumpBoss.getCenterY(), radius);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        drawLuaFloor();
        batch.draw(assets.getLunarBaseTexture(), LUNAR_BASE_X, LUNAR_BASE_Y, LUNAR_BASE_WIDTH, LUNAR_BASE_HEIGHT);
        drawLuaItems();

        Texture americanTexture = assets.getAmericanTexture();
        for (AmericanEnemy enemy : americans) batch.draw(americanTexture, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());

        if (trumpBoss != null && !trumpBoss.isDead()) {
            Texture trumpTexture = assets.getTrumpTexture();
            batch.draw(trumpTexture, trumpBoss.getX(), trumpBoss.getY(), trumpBoss.getWidth(), trumpBoss.getHeight());
        }

        Texture rifleTexture = assets.getRifleTexture();
        TextureRegion rifleRegion = new TextureRegion(rifleTexture);
        for (RifleWeapon rifle : rifleWeapons) {
            if (!rifle.isDestroyed()) {
                batch.draw(rifleRegion, rifle.getX(), rifle.getY(), rifle.getWidth() / 2f, rifle.getHeight() / 2f, rifle.getWidth(), rifle.getHeight(), 1f, 1f, rifle.getRotationDegrees());
            }
        }

        if (marsPortal != null && portalSpawned) batch.draw(assets.getPortalTexture(), marsPortal.getX(), marsPortal.getY(), marsPortal.getWidth(), marsPortal.getHeight());

        if (redKeyVisible && !redKeyCollected) batch.draw(assets.getRedKeyTexture(), redKeyHitbox.x, redKeyHitbox.y, redKeyHitbox.width, redKeyHitbox.height);

        for (Laser laser : lasers) batch.draw(assets.getLaserTexture(), laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
        batch.draw(assets.getPlayerTexture(), player.getX(), player.getY(), player.getWidth(), player.getHeight());
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

        drawPortalArrow();
        drawTrumpHealthBar();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        hudFont.getData().setScale(1.05f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, String.format("HP: %.0f / 100", stats.getHealth()), x + 10f, firstY + 20f);
        hudFont.draw(batch, String.format("FOME: %.0f / 100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        hudFont.draw(batch, String.format("O2: %.0f / 100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);
        hudFont.getData().setScale(1f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "MISSÃO LUA", 28f, hudViewport.getWorldHeight() - 205f);
        hudFont.getData().setScale(0.92f);
        hudFont.setColor(Color.LIGHT_GRAY);

        String objective;
        switch (mission) {
            case COLLECT_ICE: objective = "Colete 5 gelos: " + Math.min(stats.getIceCollected(), REQUIRED_ICE) + "/5"; break;
            case MELT_ICE: objective = "Base lunar: pressione E para derreter os 5 gelos e beber água"; break;
            case DEFEAT_AMERICAN: objective = "Derrote o inimigo lunar"; break;
            case DEFEAT_TRUMP: objective = "Derrote Trump: 2000 HP"; break;
            case GO_TO_MARS:
                if (!redKeyCollected) objective = "Colete a chave vermelha que caiu no local do Trump";
                else if (!portalUnlocked) objective = (marsPortal != null && player.getHitbox().overlaps(marsPortal.getHitbox())) ? "Portal encontrado: pressione E para usar a chave vermelha" : "Siga a flecha vermelha até o portal para Marte";
                else if (!portalEntryArmed) objective = "Portal aberto: saia e entre novamente para viajar a Marte";
                else objective = "Portal aberto: entre nele para continuar";
                break;
            default: objective = ""; break;
        }

        hudFont.draw(batch, objective, 28f, hudViewport.getWorldHeight() - 235f);
        hudFont.draw(batch, "Gelo: " + stats.getIceCollected() + "/" + REQUIRED_ICE, hudViewport.getWorldWidth() - 190f, hudViewport.getWorldHeight() - 34f);

        if (mission == LuaMission.DEFEAT_TRUMP && trumpBoss != null && !trumpBoss.isDead()) {
            String bossText;
            if (trumpAttackPhase == TrumpAttackPhase.COOLDOWN) bossText = String.format("PRÓXIMO ATAQUE: %.1fs", Math.max(0f, bossCooldownTimer));
            else if (trumpAttackPhase == TrumpAttackPhase.MISSILE_WARNING) bossText = "MÍSSEIS: ÁREAS VERMELHAS";
            else if (trumpAttackPhase == TrumpAttackPhase.RIFLE_BARRIER) bossText = "BARREIRA: DESTRUA AS AK-47";
            else if (trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE) bossText = "REFORÇOS: " + americans.size + "/5";
            else bossText = "MÍSSEIS EM VOO";
            hudFont.setColor(Color.WHITE);
            hudFont.draw(batch, bossText, hudViewport.getWorldWidth() - 350f, hudViewport.getWorldHeight() - 108f);
        }

        if (redKeyVisible && !redKeyCollected) {
            hudFont.setColor(Color.RED);
            hudFont.draw(batch, "CHAVE VERMELHA", hudViewport.getWorldWidth() - 230f, 94f);
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
            hudFont.getData().setScale(1.0f);
            hudFont.setColor(Color.WHITE);
            GlyphLayout messageLayout = new GlyphLayout(hudFont, missionMessage);
            hudFont.draw(batch, missionMessage, hudViewport.getWorldWidth() / 2f - messageLayout.width / 2f, 120f);
        }

        hudFont.getData().setScale(0.86f);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(batch, "WASD / SETAS = mover | Mouse = mirar | Clique = atirar | E = interagir | ESC = pausar", 28f, 18f);
        batch.end();
        messageTimer = Math.max(0f, messageTimer - Gdx.graphics.getDeltaTime());
    }

    private void showMessage(String message) {
        missionMessage = message;
        messageTimer = MESSAGE_DURATION;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                && !pauseMenu.isOpen()) {
            saveGame();
            screenChanged = true;
            game.setScreen(new FastTravelScreen(game, SaveManager.load()));
            return;
        }

        PauseMenu.Action pauseAction = pauseMenu.handleInput();

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
            screenChanged = true;
            dispose();
            game.setScreen(new PhaseSelectScreen(game));
            return;
        }

        if (pauseAction == PauseMenu.Action.MENU) {
            saveGame();
            screenChanged = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (!pauseMenu.isOpen()) {
            if (!update(delta)) return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        drawWorld();
        drawWarningsAndProjectiles();
        drawStormEffect();
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
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
        pauseMenu.dispose();
    }
}
