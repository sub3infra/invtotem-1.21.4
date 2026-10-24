package com.invtotem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final int MAX_DELAY_MS = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger("invtotem");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance = new ModConfig();

    public boolean enabled = true;
    public int delayMs = 100;
    public boolean searchHotbar = true;
    public boolean replaceOffhandItem = true;
    public boolean closeAfterEquip = false;
    public boolean showMessage = true;

    public static ModConfig get() {
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("invtotem.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                ModConfig loaded = GSON.fromJson(r, ModConfig.class);
                if (loaded != null) instance = loaded;
            } catch (Exception e) {
                LOGGER.error("Could not read config, using defaults", e);
            }
        }
        instance.delayMs = Math.max(0, Math.min(MAX_DELAY_MS, instance.delayMs));
        save();
    }

    public static void save() {
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(instance));
        } catch (IOException e) {
            LOGGER.error("Could not save config", e);
        }
    }

    public static void reset() {
        instance = new ModConfig();
    }
}
