package com.invtotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class InvTotemMod implements ClientModInitializer {
    public static final String MOD_ID = "invtotem";

    private static KeyBinding toggleKey;
    private static KeyBinding settingsKey;
    private final TotemEquipper equipper = new TotemEquipper();

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        String category = "key.categories.invtotem";
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.invtotem.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.invtotem.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                ModConfig cfg = ModConfig.get();
                cfg.enabled = !cfg.enabled;
                ModConfig.save();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Inventory Totem: ").formatted(Formatting.LIGHT_PURPLE)
                            .append(Text.literal(cfg.enabled ? "ON" : "OFF")
                                    .formatted(cfg.enabled ? Formatting.GREEN : Formatting.RED)), true);
                }
            }
            while (settingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
            equipper.tick(client);
        });
    }
}
