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

public class LuaScreen extends ScreenAdapter {

    private enum TrumpAttackPhase {
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
    private static final float PLAYER_SHOT_DAMAGE = 10f;
    private static final float ENEMY_BULLET_DAMAGE = 10f;
    private static final float MESSAGE_DURATION = 4f;
    private static final float BOSS_EXPLOSION_DURATION = 1.25f;

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
    private final Array<Laser> lasers;
    private final Array<LuaItem> luaItems;
    private final Array<AmericanEnemy> americans;
    private final Array<EnemyBullet> americanBullets;
    private final Array<EnemyBullet> rifleBullets;
    private final Array<TrumpMissile> trumpMissiles;
    private final Array<MissileWarning> missileWarnings;
    private final Array<RifleWeapon> rifleWeapons;

    private LuaMission mission = LuaMission.COLLECT_ICE;
    private TrumpBoss trumpBoss;
    private TrumpAttackPhase trumpAttackPhase;
    private MarsPortal marsPortal;

    private float bossPhaseTimer;
    private float messageTimer;
    private float bossExplosionTimer;
    private boolean portalSpawned;
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
        lasers = new Array<>();
        luaItems = new Array<>();
        americans = new Array<>();
        americanBullets = new Array<>();
        rifleBullets = new Array<>();
        trumpMissiles = new Array<>();
        missileWarnings = new Array<>();
        rifleWeapons = new Array<>();

        createLuaResources();
        showMessage("MISSÃO LUA: encontre e colete 1 gelo.");
        updateCamera();
    }

    private void createLuaResources() {
        luaItems.add(new LuaItem(LuaItem.Type.FOOD, 780f, 620f, 52f, 52f));
        luaItems.add(new LuaItem(LuaItem.Type.FOOD, 1540f, 420f, 52f, 52f));
        luaItems.add(new LuaItem(LuaItem.Type.O2_TANK, 1050f, 920f, 42f, 118f));
        luaItems.add(new LuaItem(LuaItem.Type.O2_TANK, 2050f, 1320f, 42f, 118f));
        luaItems.add(new LuaItem(LuaItem.Type.ICE, 1350f, 1300f, 70f, 70f));
        luaItems.add(new LuaItem(LuaItem.Type.ICE, 2380f, 760f, 70f, 70f));
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
        stats.update(delta);

        if (stats.isDead()) {
            openGameOver();
            return false;
        }

        handlePlayerShooting();
        updatePlayerLasers(delta);
        updateEnemySystems(delta);
        updateMissiles(delta);
        updateRifleBullets(delta);
        updateAmericanBullets(delta);
        updateBoss(delta);

        if (stats.isDead()) {
            openGameOver();
            return false;
        }

        if (mission == LuaMission.GO_TO_MARS
                && portalSpawned
                && marsPortal != null
                && player.getHitbox().overlaps(marsPortal.getHitbox())) {
            screenChanged = true;
            game.setScreen(new MarteScreen(game));
            return false;
        }

        updateCamera();
        return true;
    }

    private void handlePlayerShooting() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        lasers.add(new Laser(
                player.getCenterX(),
                player.getCenterY(),
                player.getDirectionX(),
                player.getDirectionY()
        ));
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
                    showMessage("Comida coletada: fome restaurada.");
                    break;
                case O2_TANK:
                    stats.addOxygen(20f);
                    showMessage("Tanque de O2 coletado: oxigênio restaurado.");
                    break;
                case ICE:
                    stats.collectIce();
                    if (mission == LuaMission.COLLECT_ICE) {
                        mission = LuaMission.MELT_ICE;
                        showMessage("Gelo coletado. Leve-o à base lunar e pressione E para derreter e beber a água.");
                    } else {
                        showMessage("Gelo extra coletado.");
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && stats.consumeIce()) {
            stats.drinkWater();
            mission = LuaMission.DEFEAT_AMERICAN;

            americans.clear();
            americanBullets.clear();
            americans.add(new AmericanEnemy(2380f, 1540f));

            showMessage("Água derretida e bebida! Um inimigo estava escondido na Lua. Derrote-o com 2 tiros.");
        }
    }

    private void updatePlayerLasers(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);

            boolean hit = false;

            if (mission == LuaMission.DEFEAT_AMERICAN) {
                hit = hitAmericanWithLaser(laser);
            } else if (mission == LuaMission.DEFEAT_TRUMP) {
                hit = hitRifleWithLaser(laser);
                if (!hit && trumpBoss != null && !trumpBoss.isDead()
                        && laser.getHitbox().overlaps(trumpBoss.getHitbox())) {
                    trumpBoss.takeDamage(PLAYER_SHOT_DAMAGE);
                    hit = true;
                }

                if (!hit && trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE) {
                    hit = hitAmericanWithLaser(laser);
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
        if (trumpAttackPhase != TrumpAttackPhase.RIFLE_BARRIER) {
            return false;
        }

        for (int i = rifleWeapons.size - 1; i >= 0; i--) {
            RifleWeapon rifle = rifleWeapons.get(i);
            if (!rifle.isDestroyed() && laser.getHitbox().overlaps(rifle.getHitbox())) {
                rifle.takeDamage(PLAYER_SHOT_DAMAGE);
                return true;
            }
        }

        return false;
    }

    private void updateEnemySystems(float delta) {
        if (mission != LuaMission.DEFEAT_AMERICAN
                && mission != LuaMission.DEFEAT_TRUMP) {
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

        if (mission == LuaMission.DEFEAT_AMERICAN && americans.size == 0) {
            spawnTrumpBoss();
        }

        if (mission == LuaMission.DEFEAT_TRUMP
                && trumpAttackPhase == TrumpAttackPhase.AMERICAN_WAVE
                && americans.size == 0) {
            beginMissileWarning();
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

    private void spawnTrumpBoss() {
        if (trumpBoss != null) {
            return;
        }

        mission = LuaMission.DEFEAT_TRUMP;
        trumpBoss = new TrumpBoss(1240f, 850f);
        americans.clear();
        americanBullets.clear();
        rifleBullets.clear();
        rifleWeapons.clear();
        trumpMissiles.clear();
        missileWarnings.clear();
        bossExplosionTimer = 0f;
        portalSpawned = false;
        bossAttackCycle = 0;

        showMessage("CHEFE: TRUMP // 500 HP. O dano do seu tiro é 10. Prepare-se.");
        beginMissileWarning();
    }

    private void updateBoss(float delta) {
        if (mission != LuaMission.DEFEAT_TRUMP || trumpBoss == null) {
            if (bossExplosionTimer > 0f) {
                bossExplosionTimer -= delta;
                if (bossExplosionTimer <= 0f && !portalSpawned) {
                    spawnMarsPortal();
                }
            }
            return;
        }

        if (trumpBoss.isDead()) {
            finishBossFight(delta);
            return;
        }

        bossPhaseTimer += delta;

        switch (trumpAttackPhase) {
            case MISSILE_WARNING:
                updateMissileWarnings();
                break;
            case MISSILE_TRAVEL:
                if (trumpMissiles.size == 0) {
                    spawnRifleBarrier();
                }
                break;
            case RIFLE_BARRIER:
                updateRifleBarrier(delta);
                break;
            case AMERICAN_WAVE:
                // Os cinco inimigos são atualizados em updateEnemySystems().
                break;
            default:
                break;
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

        float shift = (bossAttackCycle % 3) * 90f;

        addWarning(850f + shift, 650f);
        addWarning(1480f - shift, 1180f);
        addWarning(2300f, 730f + shift);

        showMessage("TRUMP: MÍSSEIS INICIADOS — saia das áreas vermelhas! Impacto em 2 segundos.");
    }

    private void addWarning(float centerX, float centerY) {
        float size = TrumpMissile.IMPACT_SIZE;
        float x = MathUtils.clamp(centerX - size / 2f, 0f, WORLD_WIDTH - size);
        float y = MathUtils.clamp(centerY - size / 2f, 0f, WORLD_HEIGHT - size);
        missileWarnings.add(new MissileWarning(x, y, size, size, 2f));
    }

    private void updateMissileWarnings() {
        boolean allReady = true;

        for (MissileWarning warning : missileWarnings) {
            warning.update(Math.min(Gdx.graphics.getDeltaTime(), 0.05f));
            if (!warning.isReadyToLaunch()) {
                allReady = false;
            }
        }

        if (!allReady) {
            return;
        }

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
        showMessage("MÍSSEIS LANÇADOS! Evite os cubos até o impacto.");
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
                    stats.damage(35f, DeathCause.TRUMP_MISSILE);
                }
                trumpMissiles.removeIndex(i);
            }
        }
    }

    private void spawnRifleBarrier() {
        trumpAttackPhase = TrumpAttackPhase.RIFLE_BARRIER;
        bossPhaseTimer = 0f;
        rifleWeapons.clear();
        rifleBullets.clear();

        String[] names = RifleWeapon.DEFAULT_NAMES;
        float startX = trumpBoss.getX() + 10f;
        float y = trumpBoss.getY() + trumpBoss.getHeight() + 35f;

        for (int i = 0; i < names.length; i++) {
            rifleWeapons.add(new RifleWeapon(
                    names[i],
                    startX + i * 84f,
                    y
            ));
        }

        showMessage("BARREIRA DE RIFLES: cada arma dispara a cada 1 segundo. Destrua todas!");
    }

    private void updateRifleBarrier(float delta) {
        for (int i = rifleWeapons.size - 1; i >= 0; i--) {
            RifleWeapon rifle = rifleWeapons.get(i);
            rifle.update(
                    delta,
                    player.getCenterX(),
                    player.getCenterY(),
                    rifleBullets
            );

            if (rifle.isDestroyed()) {
                rifleWeapons.removeIndex(i);
            }
        }

        if (rifleWeapons.size == 0) {
            spawnAmericanWave();
        }
    }

    private void spawnAmericanWave() {
        trumpAttackPhase = TrumpAttackPhase.AMERICAN_WAVE;
        bossPhaseTimer = 0f;
        americans.clear();
        americanBullets.clear();

        americans.add(new AmericanEnemy(820f, 1600f));
        americans.add(new AmericanEnemy(1180f, 1460f));
        americans.add(new AmericanEnemy(1510f, 1560f));
        americans.add(new AmericanEnemy(1840f, 1460f));
        americans.add(new AmericanEnemy(2200f, 1600f));

        showMessage("REFORÇOS: 5 inimigos apareceram. Elimine todos para a próxima sequência.");
    }

    private void finishBossFight(float delta) {
        if (bossExplosionTimer <= 0f && !portalSpawned) {
            bossExplosionTimer = BOSS_EXPLOSION_DURATION;
            trumpAttackPhase = null;
            mission = LuaMission.GO_TO_MARS;
            missileWarnings.clear();
            trumpMissiles.clear();
            rifleWeapons.clear();
            rifleBullets.clear();
            americans.clear();
            americanBullets.clear();
            lasers.clear();
            showMessage("TRUMP DERROTADO! A explosão abriu um portal para Marte.");
        }

        bossExplosionTimer -= delta;
        if (bossExplosionTimer <= 0f && !portalSpawned) {
            spawnMarsPortal();
        }
    }

    private void spawnMarsPortal() {
        portalSpawned = true;
        marsPortal = new MarsPortal(2500f, 1430f);
        showMessage("PORTAL PARA MARTE ABERTO! Entre no cubo azul para continuar.");
    }

    private void openGameOver() {
        if (screenChanged) {
            return;
        }

        screenChanged = true;
        DeathCause cause = stats.getDeathCause();
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

            batch.draw(texture, item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
    }

    private void drawWorldCubes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawMissileWarnings();
        drawAmericans();
        drawAmericanBullets();
        drawRifleBarrier();
        drawRifleBullets();
        drawTrumpMissiles();
        drawBoss();
        drawPortal();
        drawPlayerLasers();

        shapeRenderer.end();

        if (bossExplosionTimer > 0f && trumpBoss != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            float progress = 1f - bossExplosionTimer / BOSS_EXPLOSION_DURATION;
            float size = 160f + progress * 260f;
            float x = trumpBoss.getCenterX() - size / 2f;
            float y = trumpBoss.getCenterY() - size / 2f;

            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.rect(x, y, size, size);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(x + 35f, y + 35f, size - 70f, size - 70f);
            shapeRenderer.end();
        }
    }

    private void drawMissileWarnings() {
        for (MissileWarning warning : missileWarnings) {
            Rectangle area = warning.getArea();
            shapeRenderer.setColor(new Color(0.65f, 0.02f, 0.02f, 1f));
            shapeRenderer.rect(area.x, area.y, area.width, area.height);
            shapeRenderer.setColor(new Color(1f, 0.08f, 0.08f, 1f));
            shapeRenderer.rect(area.x + 12f, area.y + 12f, area.width - 24f, area.height - 24f);
        }
    }

    private void drawAmericans() {
        shapeRenderer.setColor(Color.RED);
        for (AmericanEnemy enemy : americans) {
            shapeRenderer.rect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
        }
    }

    private void drawAmericanBullets() {
        shapeRenderer.setColor(Color.BLUE);
        for (EnemyBullet bullet : americanBullets) {
            shapeRenderer.rect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
        }
    }

    private void drawRifleBarrier() {
        for (RifleWeapon rifle : rifleWeapons) {
            shapeRenderer.setColor(new Color(0.10f, 0.10f, 0.12f, 1f));
            shapeRenderer.rect(rifle.getX(), rifle.getY(), rifle.getWidth(), rifle.getHeight());
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.rect(rifle.getX() + rifle.getWidth() - 9f, rifle.getY() + 8f, 8f, 12f);
        }
    }

    private void drawRifleBullets() {
        shapeRenderer.setColor(Color.BLUE);
        for (EnemyBullet bullet : rifleBullets) {
            shapeRenderer.rect(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight());
        }
    }

    private void drawTrumpMissiles() {
        shapeRenderer.setColor(new Color(0.55f, 0.55f, 0.60f, 1f));
        for (TrumpMissile missile : trumpMissiles) {
            shapeRenderer.rect(missile.getX(), missile.getY(), missile.getWidth(), missile.getHeight());
        }
    }

    private void drawBoss() {
        if (trumpBoss == null || trumpBoss.isDead()) {
            return;
        }

        shapeRenderer.setColor(new Color(1f, 0.48f, 0.02f, 1f));
        shapeRenderer.rect(
                trumpBoss.getX(),
                trumpBoss.getY(),
                trumpBoss.getWidth(),
                trumpBoss.getHeight()
        );

        float barWidth = trumpBoss.getWidth();
        float percent = MathUtils.clamp(
                trumpBoss.getHealth() / TrumpBoss.MAX_HEALTH,
                0f,
                1f
        );
        shapeRenderer.setColor(new Color(0.12f, 0.02f, 0.02f, 1f));
        shapeRenderer.rect(trumpBoss.getX(), trumpBoss.getY() + trumpBoss.getHeight() + 18f, barWidth, 22f);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(trumpBoss.getX(), trumpBoss.getY() + trumpBoss.getHeight() + 18f, barWidth * percent, 22f);
    }

    private void drawPortal() {
        if (!portalSpawned || marsPortal == null) {
            return;
        }

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(marsPortal.getX(), marsPortal.getY(), marsPortal.getWidth(), marsPortal.getHeight());
        shapeRenderer.setColor(new Color(0.04f, 0.12f, 0.28f, 1f));
        shapeRenderer.rect(
                marsPortal.getX() + 18f,
                marsPortal.getY() + 18f,
                marsPortal.getWidth() - 36f,
                marsPortal.getHeight() - 36f
        );
    }

    private void drawPlayerLasers() {
        shapeRenderer.setColor(Color.YELLOW);
        for (Laser laser : lasers) {
            shapeRenderer.rect(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
        }
    }

    private void drawBar(float x, float y, float width, float height, float value, float maxValue, Color color) {
        shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.08f, 1f));
        shapeRenderer.rect(x, y, width, height);

        float percent = MathUtils.clamp(value / maxValue, 0f, 1f);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawHud() {
        hudViewport.apply(false);
        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float x = 28f;
        float width = 330f;
        float height = 26f;
        float firstY = hudViewport.getWorldHeight() - 46f;
        float gap = 43f;

        drawBar(x, firstY, width, height, stats.getHealth(), PlayerStats.MAX_HEALTH, Color.RED);
        drawBar(x, firstY - gap, width, height, stats.getHunger(), PlayerStats.MAX_HUNGER, Color.ORANGE);
        drawBar(x, firstY - gap * 2f, width, height, stats.getOxygen(), PlayerStats.MAX_OXYGEN, Color.CYAN);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.05f);
        hudFont.draw(batch, String.format("HP: %.0f / 100", stats.getHealth()), x + 9f, firstY + 19f);
        hudFont.draw(batch, String.format("FOME: %.0f / 100", stats.getHunger()), x + 9f, firstY - gap + 19f);
        hudFont.draw(batch, String.format("O2: %.0f / 100", stats.getOxygen()), x + 9f, firstY - gap * 2f + 19f);

        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.05f);
        String missionText = getMissionText();
        hudFont.draw(batch, missionText, 28f, hudViewport.getWorldHeight() - 175f);

        hudFont.getData().setScale(0.9f);
        hudFont.setColor(Color.LIGHT_GRAY);
        String controls = "WASD/SETAS mover | MOUSE esquerdo atirar";
        hudFont.draw(batch, controls, 28f, 28f);

        if (mission == LuaMission.MELT_ICE) {
            hudFont.setColor(Color.CYAN);
            hudFont.draw(batch, "BASE LUNAR: fique dentro dela e pressione E", 28f, hudViewport.getWorldHeight() - 205f);
        }

        if (mission == LuaMission.DEFEAT_TRUMP && trumpBoss != null) {
            hudFont.setColor(Color.ORANGE);
            hudFont.getData().setScale(1.15f);
            String bossText = String.format("TRUMP — %.0f / 500 HP", trumpBoss.getHealth());
            hudFont.draw(batch, bossText, hudViewport.getWorldWidth() - 340f, hudViewport.getWorldHeight() - 38f);

            hudFont.getData().setScale(0.88f);
            hudFont.setColor(Color.WHITE);
            String phaseText = getBossPhaseText();
            hudFont.draw(batch, phaseText, hudViewport.getWorldWidth() - 340f, hudViewport.getWorldHeight() - 68f);

            if (trumpAttackPhase == TrumpAttackPhase.RIFLE_BARRIER) {
                String weapons = "ARMAS: M4 | M16 | AR-15 | HK416 | SCAR-L | FAL";
                hudFont.draw(batch, weapons, 28f, 78f);
            }
        }

        if (messageTimer > 0f) {
            hudFont.setColor(Color.YELLOW);
            hudFont.getData().setScale(1.0f);
            GlyphLayout layout = new GlyphLayout(hudFont, missionMessage);
            hudFont.draw(
                    batch,
                    missionMessage,
                    hudViewport.getWorldWidth() / 2f - layout.width / 2f,
                    92f
            );
        }

        batch.end();

        if (messageTimer > 0f) {
            messageTimer -= Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        }
    }

    private String getMissionText() {
        switch (mission) {
            case COLLECT_ICE:
                return "MISSÃO: colete 1 gelo.";
            case MELT_ICE:
                return "MISSÃO: derreta o gelo na base e beba a água.";
            case DEFEAT_AMERICAN:
                return "MISSÃO: derrote o inimigo escondido. 2 tiros para matar.";
            case DEFEAT_TRUMP:
                return "MISSÃO: derrote TRUMP (500 HP). Sequência: mísseis → rifles → 5 inimigos.";
            case GO_TO_MARS:
                return "MISSÃO CONCLUÍDA: entre no portal azul para Marte.";
            default:
                return "MISSÃO: continue.";
        }
    }

    private String getBossPhaseText() {
        if (trumpAttackPhase == null) {
            return "EXPLOSÃO";
        }

        switch (trumpAttackPhase) {
            case MISSILE_WARNING:
                return "FASE: alerta vermelho de mísseis (2s)";
            case MISSILE_TRAVEL:
                return "FASE: mísseis em rota";
            case RIFLE_BARRIER:
                return "FASE: destrua todas as armas";
            case AMERICAN_WAVE:
                return "FASE: 5 inimigos — elimine todos";
            default:
                return "FASE: ataque";
        }
    }

    private void showMessage(String message) {
        missionMessage = message;
        messageTimer = MESSAGE_DURATION;
    }

    @Override
    public void render(float delta) {
        if (!update(delta) || screenChanged) {
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        drawLuaFloor();

        Texture lunarBase = assets.getLunarBaseTexture();
        batch.draw(lunarBase, LUNAR_BASE_X, LUNAR_BASE_Y, LUNAR_BASE_WIDTH, LUNAR_BASE_HEIGHT);
        drawLuaItems();
        batch.end();

        drawWorldCubes();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(playerTexture, player.getX(), player.getY(), player.getWidth(), player.getHeight());
        batch.end();

        drawHud();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        updateCamera();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
    }
}
