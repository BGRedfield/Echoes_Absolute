package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Dois slots independentes de save para Echoes Absolute.
 *
 * Slot 1 também consegue ler o antigo save "echoes_absolute_save",
 * mantendo o progresso criado antes da implementação dos dois slots.
 */
public final class SaveManager {

    private static final String SLOT_PREFS_PREFIX = "echoes_absolute_save_slot";
    private static final String LEGACY_PREFS_NAME = "echoes_absolute_save";
    private static final int SAVE_VERSION = 3;

    private static int activeSlot = 1;

    private SaveManager() {
    }

    public enum Phase {
        LUA,
        MARTE,
        TITA,
        CALISTO,
        AHARIN
    }

    public static class SaveData {
        public int version = SAVE_VERSION;
        public Phase phase = Phase.LUA;

        public float playerX = 400f;
        public float playerY = 400f;
        public float health = 100f;
        public float hunger = 100f;
        public float oxygen = 100f;

        // Lua
        public int luaMission = 0;
        public int iceCollected = 0;
        public boolean luaPortalSpawned;
        public boolean luaPortalUnlocked;
        public boolean luaPortalEntryArmed;
        public boolean redKeyVisible;
        public boolean redKeyCollected;
        public boolean trumpBossExists;
        public float trumpBossHealth = 2000f;
        public float luaStormRemaining;

        // Marte
        public int marsMission = 0;
        public int rawOreCount;
        public int refinedOreCount;
        public boolean weaponUpgraded;
        public boolean marsPortalSpawned;
        public boolean marsPortalUnlocked;
        public boolean marsPortalEntryArmed;
        public boolean greenKeyVisible;
        public boolean greenKeyCollected;
        public boolean supremeAlienExists;
        public float supremeAlienHealth = 5000f;
        public float marsStormRemaining;

        // Titã
        public boolean insideTitanCastle;
        public int currentTitanCastle = -1;
        public boolean finalCastleUnlocked;
        public final boolean[] crystalOwned = new boolean[3];
        public final boolean[] crystalPlaced = new boolean[3];
        public final boolean[] titanBossDefeated = new boolean[4];
        public final float[] titanBossHealth = new float[] {10000f, 800f, 900f, 2500f};

        // Titã -> Calisto
        public boolean yellowKeyVisible;
        public boolean yellowKeyCollected;
        public boolean calistoPortalUnlocked;

        // Calisto
        public final boolean[] calistoAngelBlessings = new boolean[5];
        public float calistoBossHealth = 18000f;
        public boolean calistoBossDefeated;
        public int calistoBossPhase = 1;
        public boolean calistoFinalKeySpawned;
        public boolean calistoFinalKeyCollected;

        // Aharin
        public int aharinDialogueIndex;
        public boolean aharinDialogueFinished;
        // -1 = nenhuma escolha ainda; 0 = ajudar, 1 = dominar, 2 = ficar.
        public int aharinChoice = -1;

        // Titan NPC dialogue
        public int titanDialogueRound;
        public boolean titanDialogueFinished;
    }

    public static int getActiveSlot() {
        return activeSlot;
    }

    public static void setActiveSlot(int slot) {
        activeSlot = normalizeSlot(slot);
    }

    public static boolean hasSave() {
        return hasSave(activeSlot);
    }

    public static boolean hasSave(int slot) {
        int safeSlot = normalizeSlot(slot);
        Preferences p = preferences(safeSlot);

        if (p.getBoolean("exists", false)) {
            return true;
        }

        // Compatibilidade com o save antigo, considerado slot 1.
        return safeSlot == 1
                && Gdx.app.getPreferences(LEGACY_PREFS_NAME).getBoolean("exists", false);
    }

    public static void save(SaveData data) {
        save(data, activeSlot);
    }

    public static void save(SaveData data, int slot) {
        int requestedSlot = normalizeSlot(slot);
        int sourceSlot = activeSlot;

        // Ao criar um segundo slot pela primeira vez, parte do slot atual
        // para que SALVAR 2 seja uma cópia real da campanha naquele momento.
        SaveData sourceProgress = null;
        boolean targetExists = preferences(requestedSlot).getBoolean("exists", false);

        if (requestedSlot != sourceSlot
                && !targetExists
                && hasSave(sourceSlot)) {
            sourceProgress = loadSlot(sourceSlot);
        }

        activeSlot = requestedSlot;
        Preferences p = preferences(activeSlot);

        if (sourceProgress != null) {
            mergePreviousProgress(sourceProgress, data);
        }

        // Preserva automaticamente o progresso dos mundos anteriores
        // já existentes neste mesmo slot.
        if (p.getBoolean("exists", false)) {
            SaveData previous = loadSlot(activeSlot);
            if (previous != null) {
                mergePreviousProgress(previous, data);
            }
        }

        p.putBoolean("exists", true);
        p.putInteger("version", SAVE_VERSION);
        p.putString("phase", data.phase.name());

        p.putFloat("playerX", data.playerX);
        p.putFloat("playerY", data.playerY);
        p.putFloat("health", data.health);
        p.putFloat("hunger", data.hunger);
        p.putFloat("oxygen", data.oxygen);

        p.putInteger("luaMission", data.luaMission);
        p.putInteger("iceCollected", data.iceCollected);
        p.putBoolean("luaPortalSpawned", data.luaPortalSpawned);
        p.putBoolean("luaPortalUnlocked", data.luaPortalUnlocked);
        p.putBoolean("luaPortalEntryArmed", data.luaPortalEntryArmed);
        p.putBoolean("redKeyVisible", data.redKeyVisible);
        p.putBoolean("redKeyCollected", data.redKeyCollected);
        p.putBoolean("trumpBossExists", data.trumpBossExists);
        p.putFloat("trumpBossHealth", data.trumpBossHealth);
        p.putFloat("luaStormRemaining", data.luaStormRemaining);

        p.putInteger("marsMission", data.marsMission);
        p.putInteger("rawOreCount", data.rawOreCount);
        p.putInteger("refinedOreCount", data.refinedOreCount);
        p.putBoolean("weaponUpgraded", data.weaponUpgraded);
        p.putBoolean("marsPortalSpawned", data.marsPortalSpawned);
        p.putBoolean("marsPortalUnlocked", data.marsPortalUnlocked);
        p.putBoolean("marsPortalEntryArmed", data.marsPortalEntryArmed);
        p.putBoolean("greenKeyVisible", data.greenKeyVisible);
        p.putBoolean("greenKeyCollected", data.greenKeyCollected);
        p.putBoolean("supremeAlienExists", data.supremeAlienExists);
        p.putFloat("supremeAlienHealth", data.supremeAlienHealth);
        p.putFloat("marsStormRemaining", data.marsStormRemaining);

        p.putBoolean("insideTitanCastle", data.insideTitanCastle);
        p.putInteger("currentTitanCastle", data.currentTitanCastle);
        p.putBoolean("finalCastleUnlocked", data.finalCastleUnlocked);
        for (int i = 0; i < 3; i++) {
            p.putBoolean("crystalOwned" + i, data.crystalOwned[i]);
            p.putBoolean("crystalPlaced" + i, data.crystalPlaced[i]);
        }
        for (int i = 0; i < 4; i++) {
            p.putBoolean("titanBossDefeated" + i, data.titanBossDefeated[i]);
            p.putFloat("titanBossHealth" + i, data.titanBossHealth[i]);
        }

        p.putBoolean("yellowKeyVisible", data.yellowKeyVisible);
        p.putBoolean("yellowKeyCollected", data.yellowKeyCollected);
        p.putBoolean("calistoPortalUnlocked", data.calistoPortalUnlocked);

        for (int i = 0; i < 5; i++) {
            p.putBoolean("calistoAngelBlessing" + i, data.calistoAngelBlessings[i]);
        }
        p.putFloat("calistoBossHealth", data.calistoBossHealth);
        p.putBoolean("calistoBossDefeated", data.calistoBossDefeated);
        p.putInteger("calistoBossPhase", data.calistoBossPhase);
        p.putBoolean("calistoFinalKeySpawned", data.calistoFinalKeySpawned);
        p.putBoolean("calistoFinalKeyCollected", data.calistoFinalKeyCollected);

        p.putInteger("aharinDialogueIndex", data.aharinDialogueIndex);
        p.putBoolean("aharinDialogueFinished", data.aharinDialogueFinished);
        p.putInteger("aharinChoice", data.aharinChoice);

        p.putInteger("titanDialogueRound", data.titanDialogueRound);
        p.putBoolean("titanDialogueFinished", data.titanDialogueFinished);

        p.flush();
        Gdx.app.log("SaveManager", "Save slot " + activeSlot + " gravado. Fase=" + data.phase);
    }

    public static SaveData load() {
        return loadSlot(activeSlot);
    }

    public static SaveData loadSlot(int slot) {
        activeSlot = normalizeSlot(slot);

        Preferences p = preferences(activeSlot);
        if (!p.getBoolean("exists", false) && activeSlot == 1) {
            Preferences legacy = Gdx.app.getPreferences(LEGACY_PREFS_NAME);
            if (legacy.getBoolean("exists", false)) {
                p = legacy;
            }
        }

        if (!p.getBoolean("exists", false)) {
            return null;
        }

        SaveData data = new SaveData();
        data.version = p.getInteger("version", SAVE_VERSION);

        try {
            data.phase = Phase.valueOf(p.getString("phase", Phase.LUA.name()));
        } catch (IllegalArgumentException ignored) {
            data.phase = Phase.LUA;
        }

        data.playerX = p.getFloat("playerX", data.playerX);
        data.playerY = p.getFloat("playerY", data.playerY);
        data.health = p.getFloat("health", data.health);
        data.hunger = p.getFloat("hunger", data.hunger);
        data.oxygen = p.getFloat("oxygen", data.oxygen);

        data.luaMission = p.getInteger("luaMission", data.luaMission);
        data.iceCollected = p.getInteger("iceCollected", data.iceCollected);
        data.luaPortalSpawned = p.getBoolean("luaPortalSpawned", false);
        data.luaPortalUnlocked = p.getBoolean("luaPortalUnlocked", false);
        data.luaPortalEntryArmed = p.getBoolean("luaPortalEntryArmed", false);
        data.redKeyVisible = p.getBoolean("redKeyVisible", false);
        data.redKeyCollected = p.getBoolean("redKeyCollected", false);
        data.trumpBossExists = p.getBoolean("trumpBossExists", false);
        data.trumpBossHealth = p.getFloat("trumpBossHealth", data.trumpBossHealth);
        data.luaStormRemaining = p.getFloat("luaStormRemaining", 0f);

        data.marsMission = p.getInteger("marsMission", data.marsMission);
        data.rawOreCount = p.getInteger("rawOreCount", data.rawOreCount);
        data.refinedOreCount = p.getInteger("refinedOreCount", data.refinedOreCount);
        data.weaponUpgraded = p.getBoolean("weaponUpgraded", false);
        data.marsPortalSpawned = p.getBoolean("marsPortalSpawned", false);
        data.marsPortalUnlocked = p.getBoolean("marsPortalUnlocked", false);
        data.marsPortalEntryArmed = p.getBoolean("marsPortalEntryArmed", false);
        data.greenKeyVisible = p.getBoolean("greenKeyVisible", false);
        data.greenKeyCollected = p.getBoolean("greenKeyCollected", false);
        data.supremeAlienExists = p.getBoolean("supremeAlienExists", false);
        data.supremeAlienHealth = p.getFloat("supremeAlienHealth", data.supremeAlienHealth);
        data.marsStormRemaining = p.getFloat("marsStormRemaining", 0f);

        data.insideTitanCastle = p.getBoolean("insideTitanCastle", false);
        data.currentTitanCastle = p.getInteger("currentTitanCastle", -1);
        data.finalCastleUnlocked = p.getBoolean("finalCastleUnlocked", false);
        for (int i = 0; i < 3; i++) {
            data.crystalOwned[i] = p.getBoolean("crystalOwned" + i, false);
            data.crystalPlaced[i] = p.getBoolean("crystalPlaced" + i, false);
        }
        for (int i = 0; i < 4; i++) {
            data.titanBossDefeated[i] = p.getBoolean("titanBossDefeated" + i, false);
            data.titanBossHealth[i] = p.getFloat("titanBossHealth" + i, data.titanBossHealth[i]);
        }

        data.yellowKeyVisible = p.getBoolean("yellowKeyVisible", false);
        data.yellowKeyCollected = p.getBoolean("yellowKeyCollected", false);
        data.calistoPortalUnlocked = p.getBoolean("calistoPortalUnlocked", false);

        for (int i = 0; i < 5; i++) {
            data.calistoAngelBlessings[i] = p.getBoolean("calistoAngelBlessing" + i, false);
        }
        data.calistoBossHealth = p.getFloat("calistoBossHealth", data.calistoBossHealth);
        data.calistoBossDefeated = p.getBoolean("calistoBossDefeated", false);
        data.calistoBossPhase = p.getInteger("calistoBossPhase", 1);
        data.calistoFinalKeySpawned = p.getBoolean("calistoFinalKeySpawned", data.calistoBossDefeated);
        data.calistoFinalKeyCollected = p.getBoolean("calistoFinalKeyCollected", false);

        data.aharinDialogueIndex = p.getInteger("aharinDialogueIndex", 0);
        data.aharinDialogueFinished = p.getBoolean("aharinDialogueFinished", false);
        data.aharinChoice = p.getInteger("aharinChoice", -1);

        data.titanDialogueRound = p.getInteger("titanDialogueRound", 0);
        data.titanDialogueFinished = p.getBoolean("titanDialogueFinished", false);

        return data;
    }

    private static void mergePreviousProgress(
            SaveData previous,
            SaveData current
    ) {
        if (current.phase.ordinal() >= Phase.MARTE.ordinal()) {
            current.luaMission = Math.max(current.luaMission, previous.luaMission);
            current.iceCollected = Math.max(current.iceCollected, previous.iceCollected);
            current.luaPortalSpawned |= previous.luaPortalSpawned;
            current.luaPortalUnlocked |= previous.luaPortalUnlocked;
            current.luaPortalEntryArmed |= previous.luaPortalEntryArmed;
            current.redKeyVisible |= previous.redKeyVisible;
            current.redKeyCollected |= previous.redKeyCollected;
            current.trumpBossExists |= previous.trumpBossExists;

            if (previous.trumpBossHealth <= 0f) {
                current.trumpBossHealth = 0f;
            }
        }

        if (current.phase.ordinal() >= Phase.TITA.ordinal()) {
            current.marsMission = Math.max(current.marsMission, previous.marsMission);
            current.rawOreCount = Math.max(current.rawOreCount, previous.rawOreCount);
            current.refinedOreCount = Math.max(current.refinedOreCount, previous.refinedOreCount);
            current.weaponUpgraded |= previous.weaponUpgraded;
            current.marsPortalSpawned |= previous.marsPortalSpawned;
            current.marsPortalUnlocked |= previous.marsPortalUnlocked;
            current.marsPortalEntryArmed |= previous.marsPortalEntryArmed;
            current.greenKeyVisible |= previous.greenKeyVisible;
            current.greenKeyCollected |= previous.greenKeyCollected;
            current.supremeAlienExists |= previous.supremeAlienExists;

            if (previous.supremeAlienHealth <= 0f) {
                current.supremeAlienHealth = 0f;
            }
        }

        if (current.phase.ordinal() >= Phase.CALISTO.ordinal()) {
            current.yellowKeyVisible |= previous.yellowKeyVisible;
            current.yellowKeyCollected |= previous.yellowKeyCollected;
            current.calistoPortalUnlocked |= previous.calistoPortalUnlocked;

            for (int i = 0; i < current.crystalOwned.length; i++) {
                current.crystalOwned[i] |= previous.crystalOwned[i];
                current.crystalPlaced[i] |= previous.crystalPlaced[i];
            }

            for (int i = 0; i < current.titanBossDefeated.length; i++) {
                current.titanBossDefeated[i] |= previous.titanBossDefeated[i];
            }

            for (int i = 0; i < current.calistoAngelBlessings.length; i++) {
                current.calistoAngelBlessings[i] |= previous.calistoAngelBlessings[i];
            }

            current.calistoBossDefeated |= previous.calistoBossDefeated;
            current.calistoFinalKeySpawned |= previous.calistoFinalKeySpawned;
            current.calistoFinalKeyCollected |= previous.calistoFinalKeyCollected;
        }

        if (current.phase == Phase.AHARIN) {
            // Aharin é posterior a todos os mundos anteriores:
            // mantém os estados completos para que o Fast Travel possa retornar.
            current.luaStormRemaining = Math.max(current.luaStormRemaining, previous.luaStormRemaining);
            current.marsStormRemaining = Math.max(current.marsStormRemaining, previous.marsStormRemaining);

            current.trumpBossHealth = previous.trumpBossHealth;
            current.supremeAlienHealth = previous.supremeAlienHealth;

            current.insideTitanCastle = previous.insideTitanCastle;
            current.currentTitanCastle = previous.currentTitanCastle;
            current.finalCastleUnlocked |= previous.finalCastleUnlocked;

            for (int i = 0; i < current.titanBossHealth.length; i++) {
                current.titanBossHealth[i] = Math.min(
                        current.titanBossHealth[i],
                        previous.titanBossHealth[i]
                );
            }

            current.calistoBossHealth = previous.calistoBossHealth;
            current.calistoBossPhase = previous.calistoBossPhase;

            current.aharinDialogueFinished |= previous.aharinDialogueFinished;
            if (previous.aharinDialogueIndex > current.aharinDialogueIndex) {
                current.aharinDialogueIndex = previous.aharinDialogueIndex;
            }
            if (current.aharinChoice < 0 && previous.aharinChoice >= 0) {
                current.aharinChoice = previous.aharinChoice;
            }
        }
    }

    public static void clear() {
        clear(activeSlot);
    }

    public static void clear(int slot) {
        preferences(slot).clear();
        preferences(slot).flush();
    }

    private static int normalizeSlot(int slot) {
        return slot == 2 ? 2 : 1;
    }

    private static Preferences preferences(int slot) {
        return Gdx.app.getPreferences(SLOT_PREFS_PREFIX + normalizeSlot(slot));
    }
}
