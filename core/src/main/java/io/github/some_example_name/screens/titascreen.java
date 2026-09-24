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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import io.github.some_example_name.entities.ExplosionEffect;
import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.MissileWarning;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.entities.RifleWeapon;
import io.github.some_example_name.entities.TitaBoss;
import io.github.some_example_name.entities.TitaBossProjectile;
import io.github.some_example_name.entities.TrumpMissile;
import io.github.some_example_name.managers.AssetManager;
import io.github.some_example_name.managers.SaveManager;

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

    // NPC de Titã que abre o diálogo estilo Undertale.
    private static final float TITA_NPC_X = 1650f;
    private static final float TITA_NPC_Y = 945f;
    private static final float TITA_NPC_WIDTH = 82f;
    private static final float TITA_NPC_HEIGHT = 118f;

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
    private static final float PORTAL_ROTATION_SPEED = 120f;

    private static final float YELLOW_KEY_X = 2480f;
    private static final float YELLOW_KEY_Y = 620f;
    private static final float YELLOW_KEY_SIZE = 72f;

    private static final float CALISTO_PORTAL_X = 2580f;
    private static final float CALISTO_PORTAL_Y = 650f;
    private static final float CALISTO_PORTAL_SIZE = 150f;

    // Arena ampliada para as boss fights.
    private static final float ARENA_MIN_X = 450f;
    private static final float ARENA_MIN_Y = 250f;
    private static final float ARENA_MAX_X = 2550f;
    private static final float ARENA_MAX_Y = 1750f;

    // Barack Obama usa a mesma sequência de ataques do Trump,
    // porém em uma versão muito mais pesada.
    private enum ObamaAttackPhase {
        COOLDOWN,
        MISSILE_WARNING,
        MISSILE_TRAVEL,
        RIFLE_BARRIER,
        AMERICAN_WAVE
    }

    private static final int OBAMA_MISSILE_COUNT = 14;
    private static final int OBAMA_RIFLE_COUNT = 12;
    private static final int OBAMA_AMERICAN_COUNT = 10;
    private static final float OBAMA_ATTACK_COOLDOWN = 3.5f;
    private static final float OBAMA_MISSILE_WARNING_TIME = 2f;
    private static final float OBAMA_MISSILE_DAMAGE = 35f;
    private static final float OBAMA_RIFLE_DAMAGE = 18f;
    private static final float OBAMA_AMERICAN_DAMAGE = 15f;
    // As armas ficam bem próximas, praticamente contornando o tamanho do boss.
    private static final float OBAMA_RIFLE_ORBIT_RADIUS = 145f;
    private static final float OBAMA_RIFLE_ORBIT_SPEED = 0.95f;

    // Titã sempre entra com a arma aprimorada do Marte.
    private static final float EVOLVED_FIRE_INTERVAL = 0.09f;
    private static final float EVOLVED_BURST_SPREAD = 0.045f;
    private static final float LASER_RENDER_LENGTH = 42f;
    private static final float TITAN_DIALOGUE_CHAR_DELAY = 0.025f;

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
    private final QuestLog questLog;

    private final Array<Laser> lasers = new Array<>();
    private final Array<TitaBossProjectile> bossProjectiles = new Array<>();

    private final Array<TrumpMissile> obamaMissiles = new Array<>();
    private final Array<MissileWarning> obamaMissileWarnings = new Array<>();
    private final Array<RifleWeapon> obamaRifles = new Array<>();
    private final Array<EnemyBullet> obamaRifleBullets = new Array<>();
    private final Array<AmericanEnemy> obamaAmericans = new Array<>();
    private final Array<EnemyBullet> obamaAmericanBullets = new Array<>();
    private final Array<ExplosionEffect> obamaMissileExplosions = new Array<>();

    private final Rectangle[] castleDoors = {
            new Rectangle(TOP_CASTLE_X + CASTLE_WIDTH / 2f - 45f, TOP_CASTLE_Y - 10f, 90f, 55f),
            new Rectangle(BOTTOM_CASTLE_X + CASTLE_WIDTH / 2f - 45f, BOTTOM_CASTLE_Y - 10f, 90f, 55f),
            new Rectangle(LEFT_CASTLE_X + CASTLE_WIDTH - 10f, LEFT_CASTLE_Y + CASTLE_HEIGHT / 2f - 45f, 55f, 90f),
            new Rectangle(FINAL_CASTLE_X + 350f - 70f, FINAL_CASTLE_Y - 10f, 140f, 70f)
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
    private final Rectangle yellowKey = new Rectangle(
            YELLOW_KEY_X, YELLOW_KEY_Y, YELLOW_KEY_SIZE, YELLOW_KEY_SIZE
    );
    private final Rectangle calistoPortal = new Rectangle(
            CALISTO_PORTAL_X, CALISTO_PORTAL_Y, CALISTO_PORTAL_SIZE, CALISTO_PORTAL_SIZE
    );

    private final Array<LuaItem> exteriorResources = new Array<>();

    private final Rectangle titanNpcHitbox = new Rectangle(
            TITA_NPC_X,
            TITA_NPC_Y,
            TITA_NPC_WIDTH,
            TITA_NPC_HEIGHT
    );

    private float portalRotationDegrees;

    private boolean titanDialogueOpen;
    private boolean titanDialogueWaiting;
    private boolean titanDialogueShowingResponse;
    private boolean titanDialogueFinished;
    private int titanDialogueRound;
    private int titanDialogueChoice = -1;
    private String titanDialogueResponse = "";
    private float titanDialogueTypeTimer;
    private int titanDialogueCharIndex;

    private int currentCastle = -1;
    private boolean insideCastle;
    private boolean changingScreen;
    private boolean disposed;
    private boolean finalCastleUnlocked;
    private boolean yellowKeyVisible;
    private boolean yellowKeyCollected;
    private boolean calistoPortalUnlocked;

    private float fireTimer;
    private float baseRecoveryTimer;
    private float messageTimer;
    private float collectibleFloatTime;
    private float obamaAttackTimer;
    private float obamaRifleOrbitAngle;
    private int obamaAttackCycle;
    private ObamaAttackPhase obamaAttackPhase = ObamaAttackPhase.COOLDOWN;
    private ObamaAttackPhase nextObamaAttack = ObamaAttackPhase.MISSILE_WARNING;
    private String message = "";

    public titascreen(Game game) {
        this(game, null);
    }

    public titascreen(Game game, SaveManager.SaveData saveData) {
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
        questLog = new QuestLog();

        for (int i = 0; i < crystalDrops.length; i++) {
            crystalDrops[i] = new Rectangle();
        }

        createExteriorResources();

        if (saveData != null) {
            applySave(saveData);
            showMessage("SAVE CARREGADO: retomando Titã.");
        } else {
            showMessage("TITÃ: derrote os 3 chefes, pegue os cristais e abra o castelo final.");
        }

        updateCamera();
    }

    private void applySave(SaveManager.SaveData data) {
        player.getHitbox().set(data.playerX, data.playerY, player.getWidth(), player.getHeight());
        stats.setHealth(data.health);
        stats.setHunger(data.hunger);
        stats.setOxygen(data.oxygen);

        insideCastle = data.insideTitanCastle;
        currentCastle = data.currentTitanCastle;
        finalCastleUnlocked = data.finalCastleUnlocked;
        yellowKeyVisible = data.yellowKeyVisible;
        yellowKeyCollected = data.yellowKeyCollected;
        calistoPortalUnlocked = data.calistoPortalUnlocked;

        for (int i = 0; i < 3; i++) {
            crystalOwned[i] = data.crystalOwned[i];
            crystalPlaced[i] = data.crystalPlaced[i];
        }

        for (int i = 0; i < 4; i++) {
            bossDefeated[i] = data.titanBossDefeated[i];
        }

        titanDialogueRound = MathUtils.clamp(data.titanDialogueRound, 0, 3);
        titanDialogueFinished = data.titanDialogueFinished;

        for (int i = 0; i < 4; i++) {
            if (bossDefeated[i]) {
                bosses[i] = createTitanBoss(i);
                bosses[i].setHealth(0f);
                continue;
            }

            if (data.titanBossHealth[i] >= getTitanBossMaxHealth(i) && currentCastle != i) {
                continue;
            }

            bosses[i] = createTitanBoss(i);
            bosses[i].setHealth(data.titanBossHealth[i]);
        }

        if (insideCastle && currentCastle >= 0 && currentCastle < bosses.length) {
            if (bosses[currentCastle] == null) {
                bosses[currentCastle] = createTitanBoss(currentCastle);
                bosses[currentCastle].setHealth(data.titanBossHealth[currentCastle]);
            }
        }
    }

    private TitaBoss createTitanBoss(int castleIndex) {
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

        return new TitaBoss(
                type,
                (ARENA_MIN_X + ARENA_MAX_X) / 2f - 75f,
                ARENA_MIN_Y + 470f
        );
    }

    private float getTitanBossMaxHealth(int index) {
        switch (index) {
            case 0:
                return 10000f;
            case 1:
                return 800f;
            case 2:
                return 900f;
            case 3:
            default:
                return 2500f;
        }
    }

    private void saveGame() {
        saveGame(SaveManager.getActiveSlot());
    }

    private void saveGame(int slot) {
        SaveManager.SaveData data = new SaveManager.SaveData();
        data.phase = SaveManager.Phase.TITA;
        data.playerX = player.getX();
        data.playerY = player.getY();
        data.health = stats.getHealth();
        data.hunger = stats.getHunger();
        data.oxygen = stats.getOxygen();

        data.insideTitanCastle = insideCastle;
        data.currentTitanCastle = currentCastle;
        data.finalCastleUnlocked = finalCastleUnlocked;
        data.yellowKeyVisible = yellowKeyVisible;
        data.yellowKeyCollected = yellowKeyCollected;
        data.calistoPortalUnlocked = calistoPortalUnlocked;

        for (int i = 0; i < 3; i++) {
            data.crystalOwned[i] = crystalOwned[i];
            data.crystalPlaced[i] = crystalPlaced[i];
        }

        for (int i = 0; i < 4; i++) {
            data.titanBossDefeated[i] = bossDefeated[i];
            data.titanBossHealth[i] = bosses[i] == null
                    ? getTitanBossMaxHealth(i)
                    : bosses[i].getHealth();
        }

        data.titanDialogueRound = titanDialogueRound;
        data.titanDialogueFinished = titanDialogueFinished;

        SaveManager.save(data);
        showMessage("JOGO SALVO! O save permanece mesmo fechando o jogo.");
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

        if (calistoPortalUnlocked) {
            portalRotationDegrees -= PORTAL_ROTATION_SPEED * delta;
        }

        if (!titanDialogueOpen) {
            player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        }

        if (titanDialogueOpen) {
            updateTitanDialogueInput(delta);
            stats.updateInvulnerability(delta);
            updateCamera();
            return true;
        }

        if (insideCastle) {
            // Fome/O2 ficam congelados dentro dos bosses, mas a invulnerabilidade
            // continua contando normalmente.
            stats.updateInvulnerability(delta);
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

                if (currentCastle == 0) {
                    updateObamaAttack(delta, boss);
                } else if (boss.canShoot()) {
                    spawnBossProjectiles(boss);
                    boss.resetAttackTimer();
                }
            }

            updateBossProjectiles(delta);
            updateLasers(delta);

            if (changingScreen) {
                return false;
            }

            handleCastleCrystalPickup();
            handleYellowKeyPickup();

            if (Gdx.input.isKeyJustPressed(Input.Keys.E)
                    && playerNearArenaDoor()
                    && (boss == null || boss.isDead())) {
                leaveCastle();
            }
        } else {
            handleExteriorInteractions();
            updateLasers(delta);
            collectExteriorResources();
            recoverAtBase(delta);

            Rectangle lunarBase = new Rectangle(
                    BASE_X,
                    BASE_Y,
                    BASE_WIDTH,
                    BASE_HEIGHT
            );
            if (!player.getHitbox().overlaps(lunarBase)) {
                // Fora da base, os recursos continuam diminuindo normalmente.
                stats.update(delta);
            }
        }

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

    private void createExteriorResources() {
        // Food and O2 exist only on the surface, outside the boss arenas.
        exteriorResources.add(
                new LuaItem(LuaItem.Type.FOOD, 720f, 1080f, 58f, 58f)
        );
        exteriorResources.add(
                new LuaItem(LuaItem.Type.O2_TANK, 880f, 1120f, 46f, 110f)
        );
        exteriorResources.add(
                new LuaItem(LuaItem.Type.FOOD, 1620f, 1420f, 58f, 58f)
        );
        exteriorResources.add(
                new LuaItem(LuaItem.Type.O2_TANK, 1760f, 1380f, 46f, 110f)
        );
        exteriorResources.add(
                new LuaItem(LuaItem.Type.FOOD, 2240f, 520f, 58f, 58f)
        );
        exteriorResources.add(
                new LuaItem(LuaItem.Type.O2_TANK, 2460f, 560f, 46f, 110f)
        );
    }

    private void collectExteriorResources() {
        for (int i = exteriorResources.size - 1; i >= 0; i--) {
            LuaItem item = exteriorResources.get(i);

            if (!player.getHitbox().overlaps(item.getHitbox())) {
                continue;
            }

            if (item.getType() == LuaItem.Type.FOOD) {
                stats.eatFood();
                showMessage("COMIDA COLETADA: fome recuperada.");
            } else if (item.getType() == LuaItem.Type.O2_TANK) {
                stats.addOxygen(20f);
                showMessage("O2 COLETADO: oxigênio recuperado.");
            }

            exteriorResources.removeIndex(i);
        }
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
            stats.addOxygen(10f);
            stats.addHunger(10f);
        }
    }

    private void handleExteriorInteractions() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (player.getHitbox().overlaps(titanNpcHitbox) && !titanDialogueFinished) {
                openTitanDialogue();
                return;
            }

            if (calistoPortalUnlocked && player.getHitbox().overlaps(calistoPortal)) {
                enterCalisto();
                return;
            }

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

    private void openTitanDialogue() {
        titanDialogueOpen = true;
        titanDialogueWaiting = true;
        titanDialogueShowingResponse = false;
        titanDialogueChoice = -1;
        titanDialogueResponse = "";
        startTitanDialogueText();
    }

    private void closeTitanDialogue() {
        titanDialogueOpen = false;
        titanDialogueWaiting = false;
        titanDialogueShowingResponse = false;
        titanDialogueChoice = -1;
        titanDialogueResponse = "";

        if (titanDialogueRound >= 3) {
            titanDialogueFinished = true;
        }
    }

    private void updateTitanDialogueInput(float delta) {
        updateTitanDialogueTypewriter(delta);

        if (titanDialogueWaiting) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

                if (!isTitanDialogueTextComplete()) {
                    // Primeiro ENTER termina a animação; o próximo avança.
                    showCompleteTitanDialogueText();
                    return;
                }

                if (titanDialogueShowingResponse) {
                    titanDialogueRound++;

                    if (titanDialogueRound >= 3) {
                        closeTitanDialogue();
                    } else {
                        titanDialogueShowingResponse = false;
                        titanDialogueChoice = -1;
                        titanDialogueResponse = "";
                        startTitanDialogueText();
                    }
                } else {
                    // A fala do NPC terminou. Agora aparecem as opções.
                    titanDialogueWaiting = false;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                closeTitanDialogue();
            }
            return;
        }

        int choice = getHoveredTitanDialogueChoice();

        // Teclado continua funcionando como alternativa ao mouse.
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) choice = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) choice = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) choice = 2;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && choice >= 0) {
            titanDialogueChoice = choice;
            titanDialogueResponse = getTitanDialogueResponse(titanDialogueRound, choice);
            titanDialogueShowingResponse = true;
            titanDialogueWaiting = true;
            startTitanDialogueText();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            closeTitanDialogue();
        }
    }

    private String getTitanDialogueCurrentText() {
        return titanDialogueShowingResponse
                ? titanDialogueResponse
                : getTitanDialogueSpeech(titanDialogueRound);
    }

    private void startTitanDialogueText() {
        titanDialogueTypeTimer = 0f;
        titanDialogueCharIndex = 0;
    }

    private void updateTitanDialogueTypewriter(float delta) {
        String fullText = getTitanDialogueCurrentText();
        if (titanDialogueCharIndex >= fullText.length()) {
            return;
        }

        titanDialogueTypeTimer += delta;
        while (titanDialogueTypeTimer >= TITAN_DIALOGUE_CHAR_DELAY) {
            titanDialogueTypeTimer -= TITAN_DIALOGUE_CHAR_DELAY;
            titanDialogueCharIndex++;

            if (titanDialogueCharIndex >= fullText.length()) {
                titanDialogueCharIndex = fullText.length();
                titanDialogueTypeTimer = 0f;
                break;
            }
        }
    }

    private boolean isTitanDialogueTextComplete() {
        return titanDialogueCharIndex >= getTitanDialogueCurrentText().length();
    }

    private void showCompleteTitanDialogueText() {
        titanDialogueCharIndex = getTitanDialogueCurrentText().length();
        titanDialogueTypeTimer = 0f;
    }

    private int getHoveredTitanDialogueChoice() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        hudViewport.unproject(mouse);

        float width = hudViewport.getWorldWidth();
        float panelX = 55f;
        float panelY = 42f;
        float panelWidth = width - 110f;
        float portraitSize = 155f;
        float portraitX = panelX + panelWidth - portraitSize - 28f;
        float textX = panelX + 28f;
        float buttonX = textX;
        float buttonWidth = Math.min(565f, portraitX - textX - 24f);
        float buttonHeight = 48f;
        float buttonGap = 10f;

        for (int i = 0; i < 3; i++) {
            float buttonY = panelY + 24f + (2 - i) * (buttonHeight + buttonGap);
            if (mouse.x >= buttonX && mouse.x <= buttonX + buttonWidth
                    && mouse.y >= buttonY && mouse.y <= buttonY + buttonHeight) {
                return i;
            }
        }

        return -1;
    }

    private String getTitanDialogueSpeech(int round) {
        String[] speeches = {
                "Bem-vindo a Titã. Para avançar, derrote os três primeiros chefes, colete os três cristais e coloque cada cristal no altar da mesma cor. Isso abrirá o Bastião, onde está o chefe final. Depois de derrotá-lo, pegue a chave amarela e entre no portal para Calisto.",
                "Os três castelos iniciais guardam os cristais. Derrote cada chefe e leve os três cristais aos altares no centro da superfície. Depois, a grande porta do Bastião será liberada.",
                "Quando o Bastião for aberto, derrote o chefe final. A chave amarela aparecerá depois da vitória; pegue-a e use o portal para continuar sua jornada."
        };

        return speeches[MathUtils.clamp(round, 0, speeches.length - 1)];
    }

    private String getTitanDialogueQuestion(int round, int choice) {
        String[][] questions = {
                {
                        "Quem construiu estas fortalezas?",
                        "Por que Titã tem essa cor vermelha?",
                        "O que são os cristais?"
                },
                {
                        "Obama é realmente o líder daqui?",
                        "Como eu abro o Bastião?",
                        "Existe perigo fora dos castelos?"
                },
                {
                        "Você vai me ajudar nessa missão?",
                        "O que existe depois de Titã?",
                        "Qual é o maior segredo deste lugar?"
                }
        };

        return questions[MathUtils.clamp(round, 0, 2)][MathUtils.clamp(choice, 0, 2)];
    }

    private String getTitanDialogueResponse(int round, int choice) {
        String[][] responses = {
                {
                        "O povo antigo construiu tudo isso antes de a poeira cobrir Titã.",
                        "A poeira vermelha não é só poeira. Ela esconde sinais de energia.",
                        "Os cristais guardam energia. Três deles juntos conseguem abrir o Bastião."
                },
                {
                        "Ele se chama de líder, mas até líderes precisam responder às ruínas daqui.",
                        "Você não abre com força. Ative os três altares e a porta reconhecerá você.",
                        "Existe. O silêncio daqui costuma esconder coisas que preferem continuar escondidas."
                },
                {
                        "Já estou ajudando. Só não posso lutar no seu lugar.",
                        "Depois de Titã existe um caminho que poucos conseguem atravessar.",
                        "O maior segredo é que os cristais não estão só abrindo portas. Eles estão acordando algo."
                }
        };

        return responses[MathUtils.clamp(round, 0, 2)][MathUtils.clamp(choice, 0, 2)];
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

    private void drawTitanNpc() {
        if (titanDialogueOpen || titanDialogueFinished) {
            return;
        }

        Texture bodyTexture = assets.getTitaNpcBodyTexture();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // No mapa aparece SOMENTE o corpo. A cabeça fica exclusiva do retrato do diálogo.
        float bodyWidth = titanNpcHitbox.width;
        float bodyHeight = titanNpcHitbox.height;

        drawNpcTexturePreservingAspect(
                bodyTexture,
                titanNpcHitbox.x + titanNpcHitbox.width / 2f,
                titanNpcHitbox.y + titanNpcHitbox.height / 2f,
                bodyWidth,
                bodyHeight
        );

        batch.end();
    }

    private void drawNpcTexturePreservingAspect(
            Texture texture,
            float centerX,
            float centerY,
            float maxWidth,
            float maxHeight
    ) {
        float textureWidth = Math.max(1f, texture.getWidth());
        float textureHeight = Math.max(1f, texture.getHeight());
        float scale = Math.min(maxWidth / textureWidth, maxHeight / textureHeight);

        float width = textureWidth * scale;
        float height = textureHeight * scale;

        batch.draw(
                texture,
                centerX - width / 2f,
                centerY - height / 2f,
                width,
                height
        );
    }

    private void drawTitanDialogue() {
        if (!titanDialogueOpen) {
            return;
        }

        float width = hudViewport.getWorldWidth();
        float height = hudViewport.getWorldHeight();

        // Caixa compacta no estilo de caixas de diálogo de RPG retrô:
        // ocupa apenas a faixa inferior da tela.
        float panelX = 55f;
        float panelY = 42f;
        float panelWidth = width - 110f;
        float panelHeight = 225f;

        float portraitSize = 155f;
        float portraitX = panelX + panelWidth - portraitSize - 28f;
        float portraitY = panelY + 36f;

        float textX = panelX + 28f;
        float contentWidth = portraitX - textX - 24f;

        float buttonX = textX;
        float buttonWidth = Math.min(565f, contentWidth);
        float buttonHeight = 48f;
        float buttonGap = 10f;

        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);

        // Borda branca/preta, lembrando as caixas de diálogo retrô.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(panelX - 5f, panelY - 5f, panelWidth + 10f, panelHeight + 10f);

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);

        // Retrato menor, sem dominar a tela.
        shapeRenderer.setColor(new Color(0.035f, 0.035f, 0.035f, 1f));
        shapeRenderer.rect(portraitX, portraitY, portraitSize, portraitSize);

        // O retrato do NPC é desenhado com o corpo e a cabeça reais depois,
        // sobre este fundo escuro.

        if (!titanDialogueWaiting) {
            int hovered = getHoveredTitanDialogueChoice();

            for (int i = 0; i < 3; i++) {
                float buttonY = panelY + 24f + (2 - i) * (buttonHeight + buttonGap);

                shapeRenderer.setColor(hovered == i
                        ? new Color(0.35f, 0.28f, 0f, 1f)
                        : Color.BLACK);
                shapeRenderer.rect(
                        buttonX,
                        buttonY,
                        buttonWidth,
                        buttonHeight
                );

                shapeRenderer.setColor(hovered == i ? Color.YELLOW : Color.WHITE);
                shapeRenderer.rect(buttonX, buttonY, buttonWidth, 3f);
                shapeRenderer.rect(buttonX, buttonY + buttonHeight - 3f, buttonWidth, 3f);
                shapeRenderer.rect(buttonX, buttonY, 3f, buttonHeight);
                shapeRenderer.rect(buttonX + buttonWidth - 3f, buttonY, 3f, buttonHeight);
            }
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        // No diálogo aparece SOMENTE a cabeça do NPC.
        drawNpcTexturePreservingAspect(
                assets.getTitaNpcHeadTexture(),
                portraitX + portraitSize / 2f,
                portraitY + portraitSize / 2f,
                110f,
                110f
        );

        font.setColor(Color.YELLOW);
        font.getData().setScale(1.35f);
        font.draw(batch, "NPC", textX, panelY + panelHeight - 26f);

        font.setColor(Color.WHITE);

        if (titanDialogueWaiting) {
            String fullSpeech = getTitanDialogueCurrentText();
            int visibleCharacters = Math.min(titanDialogueCharIndex, fullSpeech.length());
            String speech = fullSpeech.substring(0, visibleCharacters);

            font.getData().setScale(1.32f);
            font.draw(
                    batch,
                    speech,
                    textX,
                    panelY + panelHeight - 55f,
                    contentWidth,
                    0,
                    true
            );

            font.getData().setScale(0.95f);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(
                    batch,
                    isTitanDialogueTextComplete()
                            ? "ENTER = continuar"
                            : "ENTER = mostrar tudo",
                    textX,
                    panelY + 20f
            );
            font.draw(batch, "ESC = fechar", portraitX - 120f, panelY + 20f);
        } else {
            font.getData().setScale(1.05f);
            font.setColor(Color.WHITE);
            font.draw(
                    batch,
                    "Escolha uma pergunta:",
                    textX,
                    panelY + panelHeight - 42f
            );

            font.getData().setScale(0.95f);

            int hovered = getHoveredTitanDialogueChoice();
            for (int i = 0; i < 3; i++) {
                float buttonY = panelY + 24f + (2 - i) * (buttonHeight + buttonGap);
                font.setColor(hovered == i ? Color.YELLOW : Color.WHITE);

                font.draw(
                        batch,
                        (i + 1) + ". " + getTitanDialogueQuestion(titanDialogueRound, i),
                        buttonX + 12f,
                        buttonY + 31f,
                        buttonWidth - 24f,
                        0,
                        true
                );
            }

            font.getData().setScale(0.72f);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "MOUSE = escolher | CLIQUE = confirmar", portraitX - 15f, panelY + 18f);
        }

        font.getData().setScale(0.85f);
        font.setColor(Color.GRAY);
        font.draw(
                batch,
                "RODADA " + (titanDialogueRound + 1) + "/3",
                portraitX,
                panelY + portraitSize + 2f
        );

        batch.end();
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

        if (castleIndex == 0) {
            clearObamaAttackObjects();
        }

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
            if (castleIndex == 0) {
                startObamaCooldown(ObamaAttackPhase.MISSILE_WARNING,
                        "OBAMA APARECEU! Mísseis, AK-47 e 10 americanos estão vindo.");
            } else {
                showMessage("Você entrou no castelo: " + title + ". Derrote o chefe!");
            }
        }
    }

    private void leaveCastle() {
        insideCastle = false;
        int index = currentCastle;
        currentCastle = -1;
        bossProjectiles.clear();
        lasers.clear();

        if (index == 0) {
            clearObamaAttackObjects();
        }

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

    private void handleYellowKeyPickup() {
        if (currentCastle != 3 || !yellowKeyVisible || yellowKeyCollected) {
            return;
        }

        if (player.getHitbox().overlaps(yellowKey)) {
            yellowKeyCollected = true;
            yellowKeyVisible = false;
            calistoPortalUnlocked = true;
            saveGame();
            showMessage("CHAVE AMARELA COLETADA! Um portal para CALISTO surgiu na superfície.");
        }
    }

    private void enterCalisto() {
        SaveManager.SaveData data = SaveManager.load();
        if (data == null) {
            data = new SaveManager.SaveData();
        }

        data.phase = SaveManager.Phase.CALISTO;
        data.playerX = 210f;
        data.playerY = 620f;
        data.health = stats.getHealth();
        data.hunger = stats.getHunger();
        data.oxygen = stats.getOxygen();

        for (int i = 0; i < 5; i++) {
            data.calistoAngelBlessings[i] = false;
        }

        data.calistoBossHealth = 18000f;
        data.calistoBossDefeated = false;
        data.calistoBossPhase = 1;
        data.calistoFinalKeySpawned = false;
        data.calistoFinalKeyCollected = false;

        SaveManager.save(data);

        changingScreen = true;
        dispose();
        game.setScreen(new CalistoScreen(game, data));
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

                // Titã sempre usa a arma aprimorada do Marte:
                // dois tiros levemente espalhados a cada disparo.
                addLaserWithAngle(dx, dy, EVOLVED_BURST_SPREAD);
                addLaserWithAngle(dx, dy, -EVOLVED_BURST_SPREAD);
                fireTimer = EVOLVED_FIRE_INTERVAL;
            }
        }

        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);
            boolean hit = false;

            if (insideCastle && currentCastle == 0) {
                if (hitObamaRifleOrAmerican(laser)) {
                    hit = true;
                }
            }

            if (!hit && insideCastle && currentCastle >= 0 && currentCastle < bosses.length) {
                TitaBoss boss = bosses[currentCastle];
                if (boss != null && !boss.isDead()
                        && laser.getHitbox().overlaps(boss.getHitbox())) {
                    boss.takeDamage(10f);
                    hit = true;

                    if (boss.isDead()) {
                        bossDefeated[currentCastle] = true;

                        if (currentCastle <= 2) {
                            if (currentCastle == 0) {
                                clearObamaAttackObjects();
                            }

                            crystalDrops[currentCastle].set(
                                    boss.getCenterX() - 32f,
                                    boss.getCenterY() - 32f,
                                    64f,
                                    64f
                            );
                            showMessage(boss.getName() + " derrotado! O cristal " + crystalName(currentCastle) + " caiu.");
                        } else {
                            bossProjectiles.clear();
                            yellowKeyVisible = true;
                            yellowKeyCollected = false;
                            yellowKey.set(
                                    boss.getCenterX() - YELLOW_KEY_SIZE / 2f,
                                    boss.getCenterY() - YELLOW_KEY_SIZE / 2f,
                                    YELLOW_KEY_SIZE,
                                    YELLOW_KEY_SIZE
                            );
                            showMessage("CR7 DERROTADO! A CHAVE AMARELA CAIU. Colete-a para abrir o caminho para CALISTO.");
                        }
                    }
                }
            }

            if (hit || laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                lasers.removeIndex(i);
            }
        }
    }

    private void addLaserWithAngle(float dx, float dy, float angle) {
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float rotatedX = dx * cos - dy * sin;
        float rotatedY = dx * sin + dy * cos;
        lasers.add(new Laser(
                player.getCenterX(),
                player.getCenterY(),
                rotatedX,
                rotatedY
        ));
    }

    private boolean isObamaBoss(TitaBoss boss) {
        return currentCastle == 0
                && boss != null
                && !boss.isDead();
    }

    private void clearObamaAttackObjects() {
        obamaMissiles.clear();
        obamaMissileWarnings.clear();
        obamaRifles.clear();
        obamaRifleBullets.clear();
        obamaAmericans.clear();
        obamaAmericanBullets.clear();
        obamaMissileExplosions.clear();
        obamaAttackPhase = ObamaAttackPhase.COOLDOWN;
        obamaAttackTimer = 0f;
        obamaRifleOrbitAngle = 0f;
        obamaAttackCycle = 0;
        nextObamaAttack = ObamaAttackPhase.MISSILE_WARNING;
    }

    private void startObamaCooldown(ObamaAttackPhase nextAttack, String text) {
        obamaAttackPhase = ObamaAttackPhase.COOLDOWN;
        nextObamaAttack = nextAttack;
        obamaAttackTimer = OBAMA_ATTACK_COOLDOWN;
        showMessage(text);
    }

    private void beginObamaMissileWarning(TitaBoss boss) {
        obamaAttackPhase = ObamaAttackPhase.MISSILE_WARNING;
        obamaAttackCycle++;
        obamaMissileWarnings.clear();
        obamaMissiles.clear();
        obamaRifles.clear();
        obamaRifleBullets.clear();

        float centerX = boss.getCenterX();
        float centerY = boss.getCenterY();

        for (int i = 0; i < OBAMA_MISSILE_COUNT; i++) {
            addRandomObamaMissileWarning(centerX, centerY);
        }

        showMessage("OBAMA: CHUVA DE 14 MÍSSEIS! Áreas vermelhas = impacto.");
    }

    private void addRandomObamaMissileWarning(float centerX, float centerY) {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float dx = MathUtils.cos(angle);
        float dy = MathUtils.sin(angle);

        // Distância máxima naquela direção até a borda interna da arena.
        float maxDistanceX;
        float maxDistanceY;

        if (dx > 0.001f) {
            maxDistanceX = (ARENA_MAX_X - 100f - centerX) / dx;
        } else if (dx < -0.001f) {
            maxDistanceX = (ARENA_MIN_X + 100f - centerX) / dx;
        } else {
            maxDistanceX = Float.POSITIVE_INFINITY;
        }

        if (dy > 0.001f) {
            maxDistanceY = (ARENA_MAX_Y - 100f - centerY) / dy;
        } else if (dy < -0.001f) {
            maxDistanceY = (ARENA_MIN_Y + 100f - centerY) / dy;
        } else {
            maxDistanceY = Float.POSITIVE_INFINITY;
        }

        float maxDistance = Math.min(
                Math.abs(maxDistanceX),
                Math.abs(maxDistanceY)
        );

        float minDistance = 280f;
        float distance = MathUtils.random(
                minDistance,
                Math.max(minDistance, maxDistance)
        );

        float targetX = centerX + dx * distance;
        float targetY = centerY + dy * distance;

        float size = TrumpMissile.IMPACT_SIZE;

        targetX = MathUtils.clamp(
                targetX,
                ARENA_MIN_X + size / 2f,
                ARENA_MAX_X - size / 2f
        );

        targetY = MathUtils.clamp(
                targetY,
                ARENA_MIN_Y + size / 2f,
                ARENA_MAX_Y - size / 2f
        );

        obamaMissileWarnings.add(
                new MissileWarning(
                        targetX,
                        targetY,
                        size / 2f,
                        OBAMA_MISSILE_WARNING_TIME
                )
        );
    }

    private void updateObamaMissileWarnings(TitaBoss boss, float delta) {
        boolean ready = true;

        for (MissileWarning warning : obamaMissileWarnings) {
            warning.update(delta);
            if (!warning.isReadyToLaunch()) {
                ready = false;
            }
        }

        if (!ready) {
            return;
        }

        obamaMissiles.clear();

        for (MissileWarning warning : obamaMissileWarnings) {
            Rectangle area = warning.getArea();
            obamaMissiles.add(new TrumpMissile(
                    boss.getCenterX(),
                    boss.getCenterY(),
                    area.x + area.width / 2f,
                    area.y + area.height / 2f
            ));
        }

        obamaMissileWarnings.clear();
        obamaAttackPhase = ObamaAttackPhase.MISSILE_TRAVEL;
    }

    private void updateObamaMissiles(float delta) {
        for (int i = obamaMissiles.size - 1; i >= 0; i--) {
            TrumpMissile missile = obamaMissiles.get(i);
            missile.update(delta);

            if (!missile.hasArrived()) {
                continue;
            }

            Rectangle impact = missile.getImpactArea();
            obamaMissileExplosions.add(
                    new ExplosionEffect(
                            impact.x + impact.width / 2f,
                            impact.y + impact.height / 2f
                    )
            );

            if (player.getHitbox().overlaps(impact)) {
                stats.damage(OBAMA_MISSILE_DAMAGE, DeathCause.UNKNOWN);
            }

            obamaMissiles.removeIndex(i);
        }
    }

    private void beginObamaRifleBarrier(TitaBoss boss) {
        obamaAttackPhase = ObamaAttackPhase.RIFLE_BARRIER;
        obamaRifleOrbitAngle = 0f;
        obamaRifles.clear();
        obamaRifleBullets.clear();

        for (int i = 0; i < OBAMA_RIFLE_COUNT; i++) {
            obamaRifles.add(new RifleWeapon(
                    "OBAMA-RIFLE-" + (i + 1),
                    boss.getCenterX(),
                    boss.getCenterY()
            ));
        }

        updateObamaRifleOrbitPositions(boss);
        showMessage("OBAMA: 12 ARMAS ORBITANDO! Destrua todas.");
    }

    private void updateObamaRifleBarrier(TitaBoss boss, float delta) {
        int alive = 0;

        for (RifleWeapon rifle : obamaRifles) {
            if (rifle.isDestroyed()) {
                continue;
            }

            alive++;
            rifle.update(delta, obamaRifleBullets);
        }

        if (alive == 0) {
            obamaRifleBullets.clear();
            startObamaCooldown(
                    ObamaAttackPhase.AMERICAN_WAVE,
                    "As 12 armas foram destruídas. Obama chama 10 americanos."
            );
            return;
        }

        obamaRifleOrbitAngle += delta * OBAMA_RIFLE_ORBIT_SPEED;
        updateObamaRifleOrbitPositions(boss);
    }

    private void updateObamaRifleOrbitPositions(TitaBoss boss) {
        if (obamaRifles.size == 0) {
            return;
        }

        float centerX = boss.getCenterX();
        float centerY = boss.getCenterY();
        int total = obamaRifles.size;

        for (int i = 0; i < total; i++) {
            RifleWeapon rifle = obamaRifles.get(i);
            if (rifle.isDestroyed()) {
                continue;
            }

            float angle = obamaRifleOrbitAngle + MathUtils.PI2 * i / total;
            float x = centerX
                    + MathUtils.cos(angle) * OBAMA_RIFLE_ORBIT_RADIUS
                    - rifle.getWidth() / 2f;
            float y = centerY
                    + MathUtils.sin(angle) * OBAMA_RIFLE_ORBIT_RADIUS
                    - rifle.getHeight() / 2f;

            x = MathUtils.clamp(x, ARENA_MIN_X, ARENA_MAX_X - rifle.getWidth());
            y = MathUtils.clamp(y, ARENA_MIN_Y, ARENA_MAX_Y - rifle.getHeight());

            rifle.setPosition(x, y);
            rifle.setOutwardDirection(
                    MathUtils.cos(angle),
                    MathUtils.sin(angle)
            );
            rifle.setRotationDegrees(angle * MathUtils.radiansToDegrees);
        }
    }

    private void beginObamaAmericanWave(TitaBoss boss) {
        obamaAttackPhase = ObamaAttackPhase.AMERICAN_WAVE;
        obamaAmericans.clear();
        obamaAmericanBullets.clear();

        float centerX = boss.getCenterX();
        float centerY = boss.getCenterY();

        for (int i = 0; i < OBAMA_AMERICAN_COUNT; i++) {
            // Todos nascem dentro do Obama, mas cada um recebe
            // uma direção de espalhamento diferente.
            float angle = MathUtils.PI2 * i / OBAMA_AMERICAN_COUNT
                    + MathUtils.random(-0.18f, 0.18f);
            float lateralSign = (i % 2 == 0) ? 1f : -1f;

            obamaAmericans.add(new AmericanEnemy(
                    centerX - AmericanEnemy.WIDTH / 2f,
                    centerY - AmericanEnemy.HEIGHT / 2f,
                    angle,
                    lateralSign
            ));
        }

        showMessage("OBAMA: 10 AMERICANOS DE REFORÇO!");
    }

    private void updateObamaAmericans(float delta) {
        for (int i = obamaAmericans.size - 1; i >= 0; i--) {
            AmericanEnemy enemy = obamaAmericans.get(i);
            enemy.update(
                    delta,
                    player.getCenterX(),
                    player.getCenterY(),
                    obamaAmericanBullets
            );

            if (enemy.isDead()) {
                obamaAmericans.removeIndex(i);
            }
        }

        if (obamaAmericans.size == 0) {
            startObamaCooldown(
                    ObamaAttackPhase.MISSILE_WARNING,
                    "Os 10 americanos foram derrotados. Próxima chuva de mísseis."
            );
        }
    }

    private void updateObamaBullets(float delta) {
        for (int i = obamaRifleBullets.size - 1; i >= 0; i--) {
            EnemyBullet bullet = obamaRifleBullets.get(i);
            bullet.update(delta);

            if (bullet.getHitbox().overlaps(player.getHitbox())) {
                stats.damage(OBAMA_RIFLE_DAMAGE, DeathCause.UNKNOWN);
                obamaRifleBullets.removeIndex(i);
                continue;
            }

            if (bullet.getX() < ARENA_MIN_X - 100f
                    || bullet.getX() > ARENA_MAX_X + 100f
                    || bullet.getY() < ARENA_MIN_Y - 100f
                    || bullet.getY() > ARENA_MAX_Y + 100f) {
                obamaRifleBullets.removeIndex(i);
            }
        }

        for (int i = obamaAmericanBullets.size - 1; i >= 0; i--) {
            EnemyBullet bullet = obamaAmericanBullets.get(i);
            bullet.update(delta);

            if (bullet.getHitbox().overlaps(player.getHitbox())) {
                stats.damage(OBAMA_AMERICAN_DAMAGE, DeathCause.AMERICAN_BULLET);
                obamaAmericanBullets.removeIndex(i);
                continue;
            }

            if (bullet.getX() < ARENA_MIN_X - 100f
                    || bullet.getX() > ARENA_MAX_X + 100f
                    || bullet.getY() < ARENA_MIN_Y - 100f
                    || bullet.getY() > ARENA_MAX_Y + 100f) {
                obamaAmericanBullets.removeIndex(i);
            }
        }

        for (int i = obamaMissileExplosions.size - 1; i >= 0; i--) {
            ExplosionEffect explosion = obamaMissileExplosions.get(i);
            explosion.update(delta);

            if (explosion.isFinished()) {
                obamaMissileExplosions.removeIndex(i);
            }
        }
    }

    private void updateObamaAttack(float delta, TitaBoss boss) {
        if (!isObamaBoss(boss)) {
            return;
        }

        updateObamaMissiles(delta);
        updateObamaBullets(delta);

        switch (obamaAttackPhase) {
            case COOLDOWN:
                obamaAttackTimer -= delta;
                if (obamaAttackTimer <= 0f) {
                    if (nextObamaAttack == ObamaAttackPhase.MISSILE_WARNING) {
                        beginObamaMissileWarning(boss);
                    } else if (nextObamaAttack == ObamaAttackPhase.RIFLE_BARRIER) {
                        beginObamaRifleBarrier(boss);
                    } else {
                        beginObamaAmericanWave(boss);
                    }
                }
                break;

            case MISSILE_WARNING:
                updateObamaMissileWarnings(boss, delta);
                break;

            case MISSILE_TRAVEL:
                if (obamaMissiles.size == 0) {
                    startObamaCooldown(
                            ObamaAttackPhase.RIFLE_BARRIER,
                            "Mísseis concluídos. 12 armas vão cercar Obama."
                    );
                }
                break;

            case RIFLE_BARRIER:
                updateObamaRifleBarrier(boss, delta);
                break;

            case AMERICAN_WAVE:
                updateObamaAmericans(delta);
                break;

            default:
                break;
        }
    }

    private boolean hitObamaRifleOrAmerican(Laser laser) {
        if (obamaAttackPhase == ObamaAttackPhase.RIFLE_BARRIER) {
            for (RifleWeapon rifle : obamaRifles) {
                if (!rifle.isDestroyed()
                        && laser.getHitbox().overlaps(rifle.getHitbox())) {
                    rifle.takeDamage(10f);
                    return true;
                }
            }
        }

        if (obamaAttackPhase == ObamaAttackPhase.AMERICAN_WAVE) {
            for (int i = obamaAmericans.size - 1; i >= 0; i--) {
                AmericanEnemy enemy = obamaAmericans.get(i);
                if (!enemy.isDead()
                        && laser.getHitbox().overlaps(enemy.getHitbox())) {
                    enemy.takeDamage(10f);
                    return true;
                }
            }
        }

        return false;
    }

    private void drawObamaSupportUnits() {
        Texture americanTexture = assets.getAmericanTexture();
        for (AmericanEnemy enemy : obamaAmericans) {
            drawTextureFacingPlayer(
                    americanTexture,
                    enemy.getX(),
                    enemy.getY(),
                    enemy.getWidth(),
                    enemy.getHeight(),
                    enemy.getCenterX(),
                    enemy.getCenterY()
            );
        }

        Texture rifleTexture = assets.getRifleTexture();
        TextureRegion rifleRegion = new TextureRegion(rifleTexture);

        for (RifleWeapon rifle : obamaRifles) {
            if (!rifle.isDestroyed()) {
                batch.draw(
                        rifleRegion,
                        rifle.getX(),
                        rifle.getY(),
                        rifle.getWidth() / 2f,
                        rifle.getHeight() / 2f,
                        rifle.getWidth(),
                        rifle.getHeight(),
                        1f,
                        1f,
                        rifle.getRotationDegrees()
                );
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
            if (currentCastle == 0) {
                drawObamaSupportUnits();
            }
        } else {
            drawExteriorFloor();
            drawExteriorBuildings();
            drawExteriorResources();
        }

        drawLasers();
        drawAmericanBullets();

        drawPlayerFacingMouse();

        batch.end();

        if (!insideCastle) {
            drawTitanNpc();
        }

        if (insideCastle) {
            drawArenaEffects();
            drawYellowKey();
        } else {
            drawCalistoPortal();

        }
    }

    

    

    private void drawArenaExitGuide() {
        if (!insideCastle || currentCastle < 0 || bosses[currentCastle] == null
                || !bosses[currentCastle].isDead()) {
            return;
        }

        Rectangle exitDoor = new Rectangle(
                ARENA_MIN_X + 480f,
                ARENA_MIN_Y + 5f,
                320f,
                100f
        );

        float targetX = exitDoor.x + exitDoor.width / 2f;
        float targetY = exitDoor.y + exitDoor.height + 28f;

        float startX = player.getCenterX();
        float startY = player.getCenterY();

        float dx = targetX - startX;
        float dy = targetY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1f) {
            length = 1f;
        }

        dx /= length;
        dy /= length;

        shapeRenderer.setProjectionMatrix(camera.combined);

        // A porta aparece somente depois da vitória.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.16f, 0.08f, 0.04f, 1f));
        shapeRenderer.rect(exitDoor.x, exitDoor.y, exitDoor.width, exitDoor.height);

        shapeRenderer.setColor(new Color(0.65f, 0.46f, 0.16f, 1f));
        shapeRenderer.rect(exitDoor.x + 8f, exitDoor.y + 8f,
                exitDoor.width - 16f, exitDoor.height - 16f);

        // Seta amarela apontando para a porta.
        shapeRenderer.setColor(Color.YELLOW);
        float arrowLength = Math.min(260f, Math.max(90f, length - 80f));
        float arrowStartX = startX + dx * 55f;
        float arrowStartY = startY + dy * 55f;
        float arrowEndX = arrowStartX + dx * arrowLength;
        float arrowEndY = arrowStartY + dy * arrowLength;

        shapeRenderer.rectLine(arrowStartX, arrowStartY, arrowEndX, arrowEndY, 12f);

        float headSize = 32f;
        float perpX = -dy;
        float perpY = dx;

        shapeRenderer.triangle(
                arrowEndX + dx * headSize,
                arrowEndY + dy * headSize,
                arrowEndX + perpX * headSize,
                arrowEndY + perpY * headSize,
                arrowEndX - perpX * headSize,
                arrowEndY - perpY * headSize
        );
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.YELLOW);
        font.getData().setScale(0.92f);
        GlyphLayout layout = new GlyphLayout(font, "SAÍDA");
        font.draw(
                batch,
                "SAÍDA",
                exitDoor.x + exitDoor.width / 2f - layout.width / 2f,
                exitDoor.y + exitDoor.height + 30f
        );
        batch.end();
    }

    private void drawYellowKey() {
        if (!insideCastle || currentCastle != 3 || !yellowKeyVisible || yellowKeyCollected) {
            return;
        }

        float keyFloatOffset = MathUtils.sin(
                collectibleFloatTime * 2.2f + 1.7f
        ) * 8f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(1f, 0.85f, 0.05f, 1f));
        shapeRenderer.circle(
                yellowKey.x + yellowKey.width / 2f,
                yellowKey.y + yellowKey.height / 2f + keyFloatOffset,
                27f
        );
        shapeRenderer.rect(
                yellowKey.x + 22f,
                yellowKey.y + 8f + keyFloatOffset,
                16f,
                45f
        );
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(
                yellowKey.x + 34f,
                yellowKey.y + 20f + keyFloatOffset,
                22f,
                8f
        );
        shapeRenderer.end();
    }

    private void drawCalistoPortal() {
        if (!calistoPortalUnlocked) {
            return;
        }

        float x = calistoPortal.x;
        float y = calistoPortal.y;
        float w = calistoPortal.width;
        float h = calistoPortal.height;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        TextureRegion portalRegion = new TextureRegion(assets.getPortalCalistoTexture());
        batch.draw(
                portalRegion,
                x,
                y,
                w / 2f,
                h / 2f,
                w,
                h,
                1f,
                1f,
                portalRotationDegrees
        );
        batch.end();
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
        drawObamaAttackEffects();
        drawArenaExitGuide();
    }

    private void drawObamaAttackEffects() {
        if (currentCastle != 0) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(1f, 0f, 0f, 0.32f));
        for (MissileWarning warning : obamaMissileWarnings) {
            shapeRenderer.rect(
                    warning.getX(),
                    warning.getY(),
                    warning.getWidth(),
                    warning.getHeight()
            );
        }

        shapeRenderer.setColor(Color.RED);
        for (TrumpMissile missile : obamaMissiles) {
            shapeRenderer.rect(
                    missile.getX(),
                    missile.getY(),
                    missile.getWidth(),
                    missile.getHeight()
            );
        }

        for (ExplosionEffect explosion : obamaMissileExplosions) {
            float progress = explosion.getProgress();
            float radius = explosion.getRadius();

            shapeRenderer.setColor(new Color(
                    1f, 0.10f, 0.02f,
                    0.18f * (1f - progress)
            ));
            shapeRenderer.circle(
                    explosion.getCenterX(),
                    explosion.getCenterY(),
                    radius
            );

            shapeRenderer.setColor(new Color(
                    1f, 0.70f, 0.05f,
                    0.70f * (1f - progress)
            ));
            shapeRenderer.circle(
                    explosion.getCenterX(),
                    explosion.getCenterY(),
                    radius * 0.58f
            );

            shapeRenderer.setColor(new Color(
                    1f, 0.95f, 0.30f,
                    0.88f * (1f - progress)
            ));
            shapeRenderer.circle(
                    explosion.getCenterX(),
                    explosion.getCenterY(),
                    radius * 0.25f
            );
        }

        shapeRenderer.setColor(Color.BLUE);
        for (EnemyBullet bullet : obamaRifleBullets) {
            shapeRenderer.rect(
                    bullet.getX(),
                    bullet.getY(),
                    bullet.getWidth(),
                    bullet.getHeight()
            );
        }

        // Os tiros dos americanos são desenhados como americanobullet.png em drawWorld().


        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
        // A entrada é invisível: a interação usa apenas castleDoors[].
        batch.draw(assets.getTitaCastleTexture(), x, y, CASTLE_WIDTH, CASTLE_HEIGHT);
    }

    private void drawFinalCastle() {
        // Bastião final maior; a porta também é invisível e funciona só por hitbox.
        float width = 700f;
        float height = 520f;
        batch.draw(assets.getTitaFinalCastleTexture(), FINAL_CASTLE_X, FINAL_CASTLE_Y, width, height);
    }

        private void drawExteriorResources() {
        int index = 0;
        for (LuaItem item : exteriorResources) {
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
        TextureRegion region = new TextureRegion(texture);

        float aspect = texture.getWidth()
                / (float) Math.max(1, texture.getHeight());

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

            if (texture.getHeight() > texture.getWidth()) {
                angle -= 90f;
            }

            batch.draw(
                    region,
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

    private void drawAmericanBullets() {
        TextureRegion bulletRegion = new TextureRegion(assets.getAmericanBulletTexture());

        for (EnemyBullet bullet : obamaAmericanBullets) {
            float width = Math.max(20f, bullet.getWidth() * 1.8f);
            float height = Math.max(20f, bullet.getHeight() * 1.8f);
            float angle = MathUtils.atan2(
                    bullet.getDirectionY(),
                    bullet.getDirectionX()
            ) * MathUtils.radiansToDegrees;

            batch.draw(
                    bulletRegion,
                    bullet.getX() + bullet.getWidth() / 2f - width / 2f,
                    bullet.getY() + bullet.getHeight() / 2f - height / 2f,
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
        font.draw(batch, String.format("SACIAÇÃO %.0f/100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        font.draw(batch, String.format("O2 %.0f/100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);

        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);
        font.draw(batch, "TITÃ // VERMELHO-ESCURO", 28f, hudViewport.getWorldHeight() - 205f);

        font.getData().setScale(0.90f);
        font.setColor(Color.GREEN);
        font.draw(batch, "ARMA: APRIMORADA | 2 tiros | cadência 2x",
                28f, hudViewport.getWorldHeight() - 225f);

        font.getData().setScale(0.90f);
        font.setColor(Color.LIGHT_GRAY);

        if (insideCastle && currentCastle >= 0) {
            TitaBoss boss = bosses[currentCastle];
            font.draw(batch, "CASTELO: " + boss.getName(), 28f, hudViewport.getWorldHeight() - 238f);
            if (boss.isDead()) {
                font.draw(batch, "Boss derrotado. Siga a SETA AMARELA até a porta e pressione E.", 28f,
                        hudViewport.getWorldHeight() - 270f);
            } else {
                font.draw(batch, "Derrote o chefe | Clique/segure = atirar", 28f,
                        hudViewport.getWorldHeight() - 270f);
                font.setColor(Color.CYAN);
                if (currentCastle == 0) {
                    font.draw(batch,
                            "OBAMA: 14 mísseis | 12 armas orbitais | 10 americanos | ataques mais fortes",
                            28f, hudViewport.getWorldHeight() - 302f);
                } else {
                    font.draw(batch, "CASTELO: sem comida/O2 | O2 e fome não diminuem durante o boss.",
                            28f, hudViewport.getWorldHeight() - 302f);
                }
            }
        } else {
            font.draw(batch, "EXTERIOR: comida e O2 ficam no mapa | Base: recupera +5 O2 e +5 fome por segundo", 28f,
                    hudViewport.getWorldHeight() - 238f);

            font.draw(batch,
                    "Cristais: "
                            + (crystalPlaced[0] ? "VERMELHO " : crystalOwned[0] ? "[VERMELHO] " : "- ")
                            + (crystalPlaced[1] ? "AZUL " : crystalOwned[1] ? "[AZUL] " : "- ")
                            + (crystalPlaced[2] ? "AMARELO" : crystalOwned[2] ? "[AMARELO]" : "-"),
                    28f,
                    hudViewport.getWorldHeight() - 270f);

            if (calistoPortalUnlocked) {
                font.setColor(Color.CYAN);
                font.draw(batch, "CHAVE AMARELA COLETADA! E no portal azul = entrar em CALISTO.", 28f,
                        hudViewport.getWorldHeight() - 302f);
            } else if (yellowKeyVisible) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "A CHAVE AMARELA ESTÁ DENTRO DO BASTIÃO. Pegue-a após derrotar CR7.", 28f,
                        hudViewport.getWorldHeight() - 302f);
            } else if (finalCastleUnlocked) {
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

        if (!titanDialogueOpen && !titanDialogueFinished && !insideCastle) {
            font.getData().setScale(0.78f);
            font.setColor(Color.CYAN);
            font.draw(batch, "E = falar com o NPC de Titã", 28f, hudViewport.getWorldHeight() - 335f);
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

        drawTitanDialogue();

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
            if (!update(delta)) {
                return;
            }
        }

        ScreenUtils.clear(0.22f, 0.015f, 0.025f, 1f);
        drawWorld();
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
        if (disposed) {
            return;
        }

        disposed = true;
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        assets.dispose();
        pauseMenu.dispose();
        questLog.dispose();
    }
}
