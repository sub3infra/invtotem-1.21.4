package com.invtotem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Runs every client tick. Only acts while the player inventory screen is open. */
public class TotemEquipper {
    // Slot layout of PlayerScreenHandler: 9-35 = main inventory, 36-44 = hotbar, 45 = offhand
    private static final int MAIN_START = 9;
    private static final int HOTBAR_START = 36;
    private static final int HOTBAR_END = 44;
    private static final int OFFHAND_BUTTON = 40; // "swap with offhand" button id (same as pressing F)

    private long openedAt = -1;
    private boolean done = false;

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null
                || !(client.currentScreen instanceof InventoryScreen screen)) {
            openedAt = -1;
            done = false;
            return;
        }

        if (openedAt < 0) {
            openedAt = System.currentTimeMillis();
            done = false;
        }

        ModConfig cfg = ModConfig.get();
        if (!cfg.enabled || done) return;
        if (System.currentTimeMillis() - openedAt < cfg.delayMs) return;

        PlayerScreenHandler handler = screen.getScreenHandler();
        // Don't interfere while the player is holding an item on the cursor
        if (!handler.getCursorStack().isEmpty()) return;

        ItemStack offhand = player.getOffHandStack();
        if (offhand.isOf(Items.TOTEM_OF_UNDYING)) {
            done = true;
            return;
        }
        if (!offhand.isEmpty() && !cfg.replaceOffhandItem) {
            done = true;
            return;
        }

        int slot = findTotemSlot(handler, cfg);
        done = true;
        if (slot == -1) return;

        client.interactionManager.clickSlot(handler.syncId, slot, OFFHAND_BUTTON, SlotActionType.SWAP, player);

        if (cfg.showMessage) {
            player.sendMessage(Text.literal("[Inventory Totem] ").formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal("Totem equipped").formatted(Formatting.GREEN)), true);
        }
        if (cfg.closeAfterEquip) {
            player.closeHandledScreen();
        }
    }

    private int findTotemSlot(PlayerScreenHandler handler, ModConfig cfg) {
        int max = Math.min(HOTBAR_END, handler.slots.size() - 1);
        for (int i = MAIN_START; i < HOTBAR_START && i <= max; i++) {
            if (handler.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        if (cfg.searchHotbar) {
            for (int i = HOTBAR_START; i <= max; i++) {
                if (handler.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        return -1;
    }
}
