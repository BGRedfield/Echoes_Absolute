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

import io.github.some_example_name.entities.AmericanEnemy;
import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.EnemyBullet;
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
    private final Array<Laser> lasers = new Array<>();
    private final Array<LuaItem> luaItems = new Array<>();
    private final Array<AmericanEnemy> americans = new Array<>();
    private final Array<EnemyBullet> americanBullets = new Array<>();
    private final Array<EnemyBullet> rifleBullets = new Array<>();
    private final Array<TrumpMissile> trumpMissiles = new Array<>();
    private final Array<MissileWarning> missileWarnings = new Array<>();
    private final Array<RifleWeapon> rifleWeapons = new Array<>();

    private LuaMission mission = LuaMission.COLLECT_ICE;
    private TrumpBoss trumpBoss;
    private TrumpAttackPhase trumpAttackPhase;
    private TrumpAttackPhase nextTrumpAttack;
    private MarsPortal marsPortal;

    private float bossPhaseTimer;
    private float bossCooldownTimer;
    private float messageTimer;
    private float bossExplosionTimer;
    private float rifleOrbitAngle;
    private boolean portalSpawned;
    private boolean bossDeathSequenceStarted;
    private int bossAttackCycle;
    private String missionMessage = "";
    private boolean screenChanged;

    public LuaScreen(Game game) {
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

        createLuaResources();
        createStartingAmericans();

        showMessage("MISSÃO LUA: colete 5 gelos e leve os 5 até a base lunar.");
        updateCamera();
    }

    private void createLuaResources() {
        // Mais comida espalhada pela Lua.
        addItem(LuaItem.Type.FOOD, 780f, 620f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1120f, 430f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1680f, 560f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 2240f, 420f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 720f, 1510f, 56f, 56f);
        addItem(LuaItem.Type.FOOD, 1900f, 1560f, 56f, 56f);

        // Mais oxigênio.
        addItem(LuaItem.Type.O2_TANK, 1050f, 920f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2050f, 1320f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 1450f, 560f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2640f, 1150f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 560f, 1180f, 46f, 125f);
        addItem(LuaItem.Type.O2_TANK, 2400f, 1540f, 46f, 125f);

        // Oito pontos de gelo. A missão exige cinco.
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
        updateAmericans(delta);
        updatePlayerShooting();
        updatePlayerLasers(delta);
        updateMissiles(delta);
        updateRifleBullets(delta);
        updateAmericanBullets(delta);
        updateBoss(delta);
        stats.update(delta);

        if (stats.isDead()) {
            openGameOver();
            return false;
        }

        if (mission == LuaMission.GO_TO_MARS
                && portalSpawned
                && marsPortal != null
                && player.getHitbox().overlaps(marsPortal.getHitbox())) {
            screenChanged = true;
            dispose();
            game.setScreen(new MarteScreen(game));
            return false;
        }

        updateCamera();
        return true;
    }

    private void collectItems() {
        for (int i = luaItems.size - 1; i >= 0; i--) {
            LuaItem item = luaItems.get(i);

            if (!player.getHitbox().overlaps(item.getHitbox())) {
                continue;
            }

            switch (item.getType()) {
                case FOOD:
                    stats.eatFood();
                    showMessage("Comida coletada: fome e vida restauradas.");
                    break;
                case O2_TANK:
                    stats.addOxygen(20f);
                    showMessage("O2 coletado: oxigênio restaurado.");
                    break;
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
                default:
                    break;
            }

            luaItems.removeIndex(i);
        }
    }

    private void handleMissionInteraction() {
        if (mission != LuaMission.MELT_ICE) {
            return;
        }

        Rectangle base = new Rectangle(
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

        if (!player.getHitbox().overlaps(base)) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
                && stats.consumeIce(REQUIRED_ICE)) {
            stats.drinkWater();
            spawnTrumpBoss();
            showMessage("Os 5 gelos foram derretidos. Água ingerida. O chefe da Lua apareceu!");
        }
    }

    private void updateAmericans(float delta) {
        if (americans.size == 0) {
            return;
        }

        for (int i = americans.size - 1; i >= 0; i--) {
            AmericanEnemy enemy = americans.get(i);
            enemy.update(
                    delta,
                    player.getCenterX(),
                    player.getCenterY(),
                    americanBullets
            );

            if (enemy.isDead()) {
                americans.removeIndex(i);
            }
        }

        if (mission == LuaMission.DEFEAT_TRUMP
                && trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE
                && americans.size == 0) {
            startBossCooldown(TrumpAttackPhase.MISSILE_WARNING,
                    "Os 5 americanos foram derrotados. Próximo ataque em 5 segundos.");
        }
    }

    private void updatePlayerShooting() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        // A direção do tiro é calculada pelo mouse no mundo, não pela direção do movimento.
        Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseWorld);

        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length <= 0.001f) {
            return;
        }

        dx /= length;
        dy /= length;

        lasers.add(new Laser(
                player.getCenterX(),
                player.getCenterY(),
                dx,
                dy
        ));
    }

    private void updatePlayerLasers(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);

            boolean hit = false;

            // Americanos podem ser derrotados tanto antes da missão quanto na onda do chefe.
            if (americans.size > 0) {
                hit = hitAmericanWithLaser(laser);
            }

            if (!hit && mission == LuaMission.DEFEAT_TRUMP) {
                if (trumpAttackPhase == TrumpAttackPhase.RIFLE_BARRIER) {
                    hit = hitRifleWithLaser(laser);
                }

                if (!hit
                        && trumpBoss != null
                        && !trumpBoss.isDead()
                        && laser.getHitbox().overlaps(trumpBoss.getHitbox())) {
                    trumpBoss.takeDamage(PLAYER_SHOT_DAMAGE);
                    hit = true;

                    if (trumpBoss.isDead()) {
                        startBossDeathSequence();
                    }
                }
            }

            if (laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT) || hit) {
                lasers.removeIndex(i);
            }
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

            if (!rifle.isDestroyed()
                    && laser.getHitbox().overlaps(rifle.getHitbox())) {
                rifle.takeDamage(PLAYER_SHOT_DAMAGE);
                return true;
            }
        }

        return false;
    }

    private void spawnTrumpBoss() {
        if (trumpBoss != null) {
            return;
        }

        mission = LuaMission.DEFEAT_TRUMP;
        trumpBoss = new TrumpBoss(1240f, 850f);

        // Os inimigos da fase normal saem quando o boss aparece.
        americans.clear();
        americanBullets.clear();
        rifleBullets.clear();
        rifleWeapons.clear();
        trumpMissiles.clear();
        missileWarnings.clear();

        bossExplosionTimer = 0f;
        bossDeathSequenceStarted = false;
        portalSpawned = false;
        bossAttackCycle = 0;

        startBossCooldown(
                TrumpAttackPhase.MISSILE_WARNING,
                "TRUMP APARECEU! Primeiro ataque em 5 segundos."
        );
    }

    private void startBossCooldown(TrumpAttackPhase nextAttack, String message) {
        trumpAttackPhase = TrumpAttackPhase.COOLDOWN;
        nextTrumpAttack = nextAttack;
        bossCooldownTimer = BOSS_ATTACK_COOLDOWN;
        bossPhaseTimer = 0f;
        missileWarnings.clear();
        trumpMissiles.clear();
        rifleBullets.clear();

        if (message != null && !message.isEmpty()) {
            showMessage(message);
        }
    }

    private void updateBoss(float delta) {
        if (trumpBoss == null) {
            return;
        }

        if (trumpBoss.isDead()) {
            finishBossDeath(delta);
            return;
        }

        bossPhaseTimer += delta;
        rifleOrbitAngle += delta * RIFLE_ORBIT_SPEED;

        switch (trumpAttackPhase) {
            case COOLDOWN:
                bossCooldownTimer -= delta;
                if (bossCooldownTimer <= 0f) {
                    beginNextBossAttack();
                }
                break;

            case MISSILE_WARNING:
                updateMissileWarnings(delta);
                break;

            case MISSILE_TRAVEL:
                if (trumpMissiles.size == 0) {
                    startBossCooldown(
                            TrumpAttackPhase.RIFLE_BARRIER,
                            "Mísseis concluídos. Barreira de AK-47 em 5 segundos."
                    );
                }
                break;

            case RIFLE_BARRIER:
                updateRifleBarrier(delta);
                break;

            case AMERICAN_WAVE:
                // A lista dos cinco americanos é atualizada em updateAmericans().
                break;

            default:
                break;
        }

        updateRifleOrbitPositions();
    }

    private void beginNextBossAttack() {
        if (nextTrumpAttack == TrumpAttackPhase.MISSILE_WARNING) {
            beginMissileWarning();
        } else if (nextTrumpAttack == TrumpAttackPhase.RIFLE_BARRIER) {
            beginRifleBarrier();
        } else if (nextTrumpAttack == TrumpAttackPhase.AMERICAN_WAVE) {
            beginAmericanWave();
        }
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
        missileWarnings.add(new MissileWarning(
                x,
                y,
                size,
                size,
                MISSILE_WARNING_TIME
        ));
    }

    private void updateMissileWarnings(float delta) {
        boolean ready = true;

        for (MissileWarning warning : missileWarnings) {
            warning.update(delta);
            if (!warning.isReadyToLaunch()) {
                ready = false;
            }
        }

        if (!ready) {
            return;
        }

        trumpMissiles.clear();

        for (MissileWarning warning : missileWarnings) {
            Rectangle area = warning.getArea();
            trumpMissiles.add(new TrumpMissile(
                    trumpBoss.getCenterX(),
                    trumpBoss.getCenterY(),
                    area.x + area.width / 2f,
                    area.y + area.height / 2f
            ));
        }

        missileWarnings.clear();
        trumpAttackPhase = TrumpAttackPhase.MISSILE_TRAVEL;
        bossPhaseTimer = 0f;
    }

    private void updateMissiles(float delta) {
        if (trumpMissiles.size == 0) {
            return;
        }

        for (int i = trumpMissiles.size - 1; i >= 0; i--) {
            TrumpMissile missile = trumpMissiles.get(i);
            missile.update(delta);

            if (missile.hasArrived()) {
                if (player.getHitbox().overlaps(missile.getImpactArea())) {
                    stats.damage(ENEMY_BULLET_DAMAGE * 2f, DeathCause.TRUMP_MISSILE);
                }
                trumpMissiles.removeIndex(i);
            }
        }
    }

    private void beginRifleBarrier() {
        trumpAttackPhase = TrumpAttackPhase.RIFLE_BARRIER;
        bossPhaseTimer = 0f;
        rifleOrbitAngle = 0f;
        rifleWeapons.clear();
        rifleBullets.clear();

        for (int i = 0; i < RifleWeapon.DEFAULT_NAMES.length; i++) {
            rifleWeapons.add(new RifleWeapon(
                    RifleWeapon.DEFAULT_NAMES[i],
                    trumpBoss.getCenterX(),
                    trumpBoss.getCenterY()
            ));
        }

        showMessage("BARREIRA DE AK-47: elas orbitam o Trump e disparam a cada 1 segundo. Destrua todas.");
        updateRifleOrbitPositions();
    }

    private void updateRifleBarrier(float delta) {
        int alive = 0;

        for (RifleWeapon rifle : rifleWeapons) {
            if (rifle.isDestroyed()) {
                continue;
            }

            alive++;
            rifle.update(
                    delta,
                    player.getCenterX(),
                    player.getCenterY(),
                    rifleBullets
            );
        }

        if (alive == 0) {
            rifleBullets.clear();
            startBossCooldown(
                    TrumpAttackPhase.AMERICAN_WAVE,
                    "Todas as AK-47 foram destruídas. Cinco americanos chegam em 5 segundos."
            );
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
        if (trumpBoss == null || rifleWeapons.size == 0) {
            return;
        }

        float centerX = trumpBoss.getCenterX();
        float centerY = trumpBoss.getCenterY();
        int total = rifleWeapons.size;

        for (int i = 0; i < total; i++) {
            RifleWeapon rifle = rifleWeapons.get(i);
            if (rifle.isDestroyed()) {
                continue;
            }

            float angle = rifleOrbitAngle + MathUtils.PI2 * i / total;
            float x = centerX + MathUtils.cos(angle) * RIFLE_ORBIT_RADIUS - rifle.getWidth() / 2f;
            float y = centerY + MathUtils.sin(angle) * RIFLE_ORBIT_RADIUS - rifle.getHeight() / 2f;

            x = MathUtils.clamp(x, 0f, WORLD_WIDTH - rifle.getWidth());
            y = MathUtils.clamp(y, 0f, WORLD_HEIGHT - rifle.getHeight());
            rifle.setPosition(x, y);
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

            if (bullet.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                americanBullets.removeIndex(i);
            }
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

            if (bullet.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                rifleBullets.removeIndex(i);
            }
        }
    }

    private void startBossDeathSequence() {
        if (bossDeathSequenceStarted) {
            return;
        }

        bossDeathSequenceStarted = true;
        bossExplosionTimer = BOSS_EXPLOSION_DURATION;
        mission = LuaMission.GO_TO_MARS;
        americans.clear();
        americanBullets.clear();
        rifleBullets.clear();
        rifleWeapons.clear();
        missileWarnings.clear();
        trumpMissiles.clear();
        showMessage("TRUMP DERROTADO! O portal para Marte está sendo aberto.");
    }

    private void finishBossDeath(float delta) {
        if (!bossDeathSequenceStarted) {
            startBossDeathSequence();
        }

        bossExplosionTimer -= delta;
        if (bossExplosionTimer <= 0f && !portalSpawned) {
            spawnMarsPortal();
        }
    }

    private void spawnMarsPortal() {
        portalSpawned = true;
        marsPortal = new MarsPortal(2580f, 1530f);
        showMessage("PORTAL PARA MARTE ABERTO! Entre no portal azul.");
    }

    private void openGameOver() {
        if (screenChanged) {
            return;
        }

        screenChanged = true;
        DeathCause cause = stats.getDeathCause();
        dispose();
        game.setScreen(new GameOverScreen(game, cause));
    }

    private void updateCamera() {
        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;

        float cameraX = MathUtils.clamp(
                player.getCenterX(),
                halfViewportWidth,
                WORLD_WIDTH - halfViewportWidth
        );

        float cameraY = MathUtils.clamp(
                player.getCenterY(),
                halfViewportHeight,
                WORLD_HEIGHT - halfViewportHeight
        );

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
                case FOOD:
                    texture = assets.getFoodTexture();
                    break;
                case O2_TANK:
                    texture = assets.getO2TankTexture();
                    break;
                case ICE:
                    texture = assets.getIceTexture();
                    break;
                default:
                    continue;
            }

            batch.draw(
                    texture,
                    item.getX(),
                    item.getY(),
                    item.getWidth(),
                    item.getHeight()
            );
        }
    }

    private void drawBar(
            float x,
            float y,
            float width,
            float height,
            float value,
            float maxValue,
            Color color
    ) {
        shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.08f, 0.92f));
        shapeRenderer.rect(x, y, width, height);

        float percent = MathUtils.clamp(value / maxValue, 0f, 1f);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawWarningsAndProjectiles() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Áreas vermelhas que avisam dois segundos antes dos mísseis.
        shapeRenderer.setColor(new Color(1f, 0f, 0f, 0.34f));
        for (MissileWarning warning : missileWarnings) {
            shapeRenderer.rect(
                    warning.getX(),
                    warning.getY(),
                    warning.getWidth(),
                    warning.getHeight()
            );
        }

        // Mísseis sem sprite são cubos laranja/vermelhos.
        shapeRenderer.setColor(Color.RED);
        for (TrumpMissile missile : trumpMissiles) {
            shapeRenderer.rect(
                    missile.getX(),
                    missile.getY(),
                    missile.getWidth(),
                    missile.getHeight()
            );
        }

        // Tiros inimigos são cubos azuis.
        shapeRenderer.setColor(Color.BLUE);
        for (EnemyBullet bullet : americanBullets) {
            shapeRenderer.rect(
                    bullet.getX(),
                    bullet.getY(),
                    bullet.getWidth(),
                    bullet.getHeight()
            );
        }
        for (EnemyBullet bullet : rifleBullets) {
            shapeRenderer.rect(
                    bullet.getX(),
                    bullet.getY(),
                    bullet.getWidth(),
                    bullet.getHeight()
            );
        }

        // Explosão do boss após os 500 HP chegarem a zero.
        if (bossDeathSequenceStarted && trumpBoss != null && !portalSpawned) {
            float progress = 1f - MathUtils.clamp(
                    bossExplosionTimer / BOSS_EXPLOSION_DURATION,
                    0f,
                    1f
            );
            float radius = 60f + progress * 240f;
            shapeRenderer.setColor(new Color(1f, 0.55f, 0.05f, 0.70f));
            shapeRenderer.circle(trumpBoss.getCenterX(), trumpBoss.getCenterY(), radius);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // TileSet ocupa todo o mundo; o background antigo continua disponível como fallback.
        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        drawLuaFloor();

        Texture lunarBase = assets.getLunarBaseTexture();
        batch.draw(
                lunarBase,
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

        drawLuaItems();

        // Americanos da fase normal/onda do boss.
        Texture americanTexture = assets.getAmericanTexture();
        for (AmericanEnemy enemy : americans) {
            batch.draw(
                    americanTexture,
                    enemy.getX(),
                    enemy.getY(),
                    enemy.getWidth(),
                    enemy.getHeight()
            );
        }

        // Boss.
        if (trumpBoss != null && !trumpBoss.isDead()) {
            Texture trumpTexture = assets.getTrumpTexture();
            batch.draw(
                    trumpTexture,
                    trumpBoss.getX(),
                    trumpBoss.getY(),
                    trumpBoss.getWidth(),
                    trumpBoss.getHeight()
            );
        }

        // AK-47 orbitando o Trump.
        Texture rifleTexture = assets.getRifleTexture();
        for (RifleWeapon rifle : rifleWeapons) {
            if (!rifle.isDestroyed()) {
                batch.draw(
                        rifleTexture,
                        rifle.getX(),
                        rifle.getY(),
                        rifle.getWidth(),
                        rifle.getHeight()
                );
            }
        }

        // Portal para Marte.
        if (marsPortal != null && portalSpawned) {
            Texture portalTexture = assets.getPortalTexture();
            batch.draw(
                    portalTexture,
                    marsPortal.getX(),
                    marsPortal.getY(),
                    marsPortal.getWidth(),
                    marsPortal.getHeight()
            );
        }

        Texture laserTexture = assets.getLaserTexture();
        for (Laser laser : lasers) {
            batch.draw(
                    laserTexture,
                    laser.getX(),
                    laser.getY(),
                    laser.getWidth(),
                    laser.getHeight()
            );
        }

        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(
                playerTexture,
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

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

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        hudFont.getData().setScale(1.05f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, String.format("HP: %.0f / 100", stats.getHealth()), x + 10f, firstY + 20f);
        hudFont.draw(batch, String.format("FOME: %.0f / 100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        hudFont.draw(batch, String.format("O2: %.0f / 100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);

        hudFont.getData().setScale(1.0f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "MISSÃO LUA", 28f, hudViewport.getWorldHeight() - 205f);

        hudFont.getData().setScale(0.92f);
        hudFont.setColor(Color.LIGHT_GRAY);

        String objective;
        switch (mission) {
            case COLLECT_ICE:
                objective = "Colete 5 gelos: " + Math.min(stats.getIceCollected(), REQUIRED_ICE) + "/5";
                break;
            case MELT_ICE:
                objective = "Base lunar: pressione E para derreter os 5 gelos e beber água";
                break;
            case DEFEAT_AMERICAN:
                objective = "Derrote o inimigo lunar";
                break;
            case DEFEAT_TRUMP:
                objective = "Derrote Trump: 500 HP";
                break;
            case GO_TO_MARS:
                objective = portalSpawned ? "Entre no portal para Marte" : "Portal para Marte abrindo...";
                break;
            default:
                objective = "";
                break;
        }

        GlyphLayout objectiveLayout = new GlyphLayout(hudFont, objective);
        hudFont.draw(batch, objective, 28f, hudViewport.getWorldHeight() - 235f);

        String iceText = "Gelo: " + stats.getIceCollected() + "/" + REQUIRED_ICE;
        hudFont.draw(batch, iceText, hudViewport.getWorldWidth() - 190f, hudViewport.getWorldHeight() - 34f);

        if (mission == LuaMission.DEFEAT_TRUMP && trumpBoss != null && !trumpBoss.isDead()) {
            String bossText;
            if (trumpAttackPhase == TrumpAttackPhase.COOLDOWN) {
                bossText = String.format("PRÓXIMO ATAQUE: %.1fs", Math.max(0f, bossCooldownTimer));
            } else if (trumpAttackPhase == TrumpAttackPhase.MISSILE_WARNING) {
                bossText = "MÍSSEIS: ÁREAS VERMELHAS";
            } else if (trumpAttackPhase == TrumpAttackPhase.RIFLE_BARRIER) {
                bossText = "BARREIRA: DESTRUA AS AK-47";
            } else if (trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE) {
                bossText = "REFORÇOS: " + americans.size + "/5";
            } else {
                bossText = "MÍSSEIS EM VOO";
            }

            hudFont.setColor(Color.ORANGE);
            hudFont.draw(
                    batch,
                    String.format("TRUMP: %.0f / %.0f HP", trumpBoss.getHealth(), TrumpBoss.MAX_HEALTH),
                    hudViewport.getWorldWidth() - 260f,
                    hudViewport.getWorldHeight() - 78f
            );

            hudFont.setColor(Color.WHITE);
            hudFont.draw(batch, bossText, hudViewport.getWorldWidth() - 350f, hudViewport.getWorldHeight() - 108f);
        }

        if (messageTimer > 0f) {
            hudFont.getData().setScale(1.0f);
            hudFont.setColor(Color.WHITE);
            GlyphLayout messageLayout = new GlyphLayout(hudFont, missionMessage);
            hudFont.draw(
                    batch,
                    missionMessage,
                    hudViewport.getWorldWidth() / 2f - messageLayout.width / 2f,
                    42f
            );
        }

        hudFont.getData().setScale(0.86f);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(batch, "WASD / SETAS = mover | Mouse = mirar | Clique = atirar | E = interagir", 28f, 18f);

        batch.end();

        messageTimer = Math.max(0f, messageTimer - Gdx.graphics.getDeltaTime());
    }

    private void showMessage(String message) {
        missionMessage = message;
        messageTimer = MESSAGE_DURATION;
    }

    @Override
    public void render(float delta) {
        if (!update(delta)) {
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        drawWorld();
        drawWarningsAndProjectiles();
        drawHud();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        updateCamera();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
    }
}
