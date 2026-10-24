package com.invtotem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int HEADER_H = 32;
    private static final int ROW_H = 26;
    private static final int PAD = 14;
    private static final int WIDGET_W = 120;

    private static final int ACCENT = 0xFF8B5CF6;
    private static final int PANEL_BG = 0xF0141420;
    private static final int HEADER_BG = 0xFF1E1E30;
    private static final int STRIPE = 0x14FFFFFF;
    private static final int LABEL = 0xFFE5E7EB;
    private static final int MUTED = 0xFF9CA3AF;

    private static final String[] LABELS = {
            "Enabled",
            "Equip delay",
            "Search hotbar",
            "Replace offhand item",
            "Close inventory after",
            "Show action-bar message"
    };

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Inventory Totem"));
        this.parent = parent;
    }

    private int panelH() {
        return HEADER_H + 2 + 8 + LABELS.length * ROW_H + 8 + 20 + 10;
    }

    private int top() {
        return Math.max(8, (height - panelH()) / 2);
    }

    private int left() {
        return (width - PANEL_W) / 2;
    }

    private int rowY(int i) {
        return top() + HEADER_H + 2 + 8 + i * ROW_H;
    }

    @Override
    protected void init() {
        ModConfig cfg = ModConfig.get();
        int wx = left() + PANEL_W - PAD - WIDGET_W;

        addDrawableChild(toggle(wx, rowY(0), () -> cfg.enabled, v -> cfg.enabled = v,
                "Master switch. When off, nothing happens when you open your inventory."));

        addDrawableChild(new DelaySlider(wx, rowY(1)));

        addDrawableChild(toggle(wx, rowY(2), () -> cfg.searchHotbar, v -> cfg.searchHotbar = v,
                "Also take totems from your hotbar. Totems in the main inventory are used first."));

        addDrawableChild(toggle(wx, rowY(3), () -> cfg.replaceOffhandItem, v -> cfg.replaceOffhandItem = v,
                "If your offhand holds something else (e.g. a shield), swap it out for a totem."));

        addDrawableChild(toggle(wx, rowY(4), () -> cfg.closeAfterEquip, v -> cfg.closeAfterEquip = v,
                "Automatically close the inventory once the totem is equipped."));

        addDrawableChild(toggle(wx, rowY(5), () -> cfg.showMessage, v -> cfg.showMessage = v,
                "Show a short message above your hotbar when a totem is equipped."));

        int by = rowY(LABELS.length) + 8;
        int half = (PANEL_W - PAD * 2 - 8) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset to defaults").formatted(Formatting.RED), b -> {
            ModConfig.reset();
            clearAndInit();
        }).dimensions(left() + PAD, by, half, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done").formatted(Formatting.GREEN), b -> close())
                .dimensions(left() + PAD + half + 8, by, half, 20).build());
    }

    private ButtonWidget toggle(int x, int y, BooleanSupplier get, Consumer<Boolean> set, String tip) {
        return ButtonWidget.builder(stateText(get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(stateText(get.getAsBoolean()));
        }).dimensions(x, y, WIDGET_W, 20).tooltip(Tooltip.of(Text.literal(tip))).build();
    }

    private static Text stateText(boolean on) {
        return Text.literal(on ? "ON" : "OFF").formatted(on ? Formatting.GREEN : Formatting.RED, Formatting.BOLD);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);

        int px = left();
        int top = top();
        int ph = panelH();

        // Border + body
        ctx.fill(px - 1, top - 1, px + PANEL_W + 1, top + ph + 1, ACCENT);
        ctx.fill(px, top, px + PANEL_W, top + ph, PANEL_BG);

        // Header
        ctx.fill(px, top, px + PANEL_W, top + HEADER_H, HEADER_BG);
        ctx.fill(px, top + HEADER_H, px + PANEL_W, top + HEADER_H + 2, ACCENT);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Inventory Totem").formatted(Formatting.BOLD),
                px + PAD, top + 8, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Auto-equip when your inventory opens"),
                px + PAD, top + 20, MUTED);

        // Status pill on the right
        boolean on = ModConfig.get().enabled;
        Text status = Text.literal(on ? "● ACTIVE" : "● DISABLED").formatted(on ? Formatting.GREEN : Formatting.RED);
        ctx.drawTextWithShadow(textRenderer, status,
                px + PANEL_W - PAD - textRenderer.getWidth(status), top + 12, 0xFFFFFFFF);

        // Row stripes + labels
        for (int i = 0; i < LABELS.length; i++) {
            int y = rowY(i);
            if (i % 2 == 0) ctx.fill(px + 4, y - 3, px + PANEL_W - 4, y + 23, STRIPE);
            ctx.drawTextWithShadow(textRenderer, Text.literal(LABELS[i]), px + PAD, y + 6, LABEL);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void removed() {
        ModConfig.save();
    }

    /** Slider from 0 to 1000 ms in 10 ms steps. */
    private static class DelaySlider extends SliderWidget {
        DelaySlider(int x, int y) {
            super(x, y, WIDGET_W, 20, Text.empty(), ModConfig.get().delayMs / (double) ModConfig.MAX_DELAY_MS);
            updateMessage();
            setTooltip(Tooltip.of(Text.literal("How long to wait after opening the inventory before swapping. 0 = instant.")));
        }

        private int ms() {
            return (int) (Math.round(value * ModConfig.MAX_DELAY_MS / 10.0) * 10);
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(ms() + " ms").formatted(Formatting.LIGHT_PURPLE));
        }

        @Override
        protected void applyValue() {
            ModConfig.get().delayMs = ms();
        }
    }
}
