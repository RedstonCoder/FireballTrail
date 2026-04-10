package com.example.fireballpredict;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFireballConfig extends GuiScreen {
    private static final int BUTTON_MOD_TOGGLE = 100;
    private static final int BUTTON_MARKER_TOGGLE = 101;
    private static final int BUTTON_COLOR_R_MINUS = 200;
    private static final int BUTTON_COLOR_R_PLUS = 201;
    private static final int BUTTON_COLOR_G_MINUS = 202;
    private static final int BUTTON_COLOR_G_PLUS = 203;
    private static final int BUTTON_COLOR_B_MINUS = 204;
    private static final int BUTTON_COLOR_B_PLUS = 205;
    private static final int BUTTON_COLOR_A_MINUS = 206;
    private static final int BUTTON_COLOR_A_PLUS = 207;
    private static final int BUTTON_WIDTH_MINUS = 208;
    private static final int BUTTON_WIDTH_PLUS = 209;
    private static final int BUTTON_MARKER_WIDTH_MINUS = 210;
    private static final int BUTTON_MARKER_WIDTH_PLUS = 211;
    private static final int BUTTON_DONE = 300;

    private boolean modEnabled;
    private boolean showLandingMarker;
    private int colorR, colorG, colorB, colorA;
    private float lineWidth;
    private float markerLineWidth;

    public GuiFireballConfig() {
        this.modEnabled = ModConfig.modEnabled;
        this.showLandingMarker = ModConfig.showLandingMarker;
        this.colorR = ModConfig.trajectoryColorR;
        this.colorG = ModConfig.trajectoryColorG;
        this.colorB = ModConfig.trajectoryColorB;
        this.colorA = ModConfig.trajectoryColorA;
        this.lineWidth = ModConfig.trajectoryLineWidth;
        this.markerLineWidth = ModConfig.landingMarkerLineWidth;
    }

    public void initGui() {
        super.initGui();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;

        this.buttonList.add(new GuiButton(BUTTON_MOD_TOGGLE, centerX - 80, startY + 0, 160, 22, getToggleLabel("Mod Enabled", modEnabled)));
        this.buttonList.add(new GuiButton(BUTTON_MARKER_TOGGLE, centerX - 80, startY + 30, 160, 22, getToggleLabel("Show Landing Marker", showLandingMarker)));

        int colorStartY = startY + 78;
        int rowHeight = 28;

        this.buttonList.add(new GuiButton(BUTTON_COLOR_R_MINUS, centerX - 90, colorStartY, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_COLOR_R_PLUS, centerX + 16, colorStartY, 24, 20, "+"));

        this.buttonList.add(new GuiButton(BUTTON_COLOR_G_MINUS, centerX - 90, colorStartY + rowHeight, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_COLOR_G_PLUS, centerX + 16, colorStartY + rowHeight, 24, 20, "+"));

        this.buttonList.add(new GuiButton(BUTTON_COLOR_B_MINUS, centerX - 90, colorStartY + rowHeight * 2, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_COLOR_B_PLUS, centerX + 16, colorStartY + rowHeight * 2, 24, 20, "+"));

        this.buttonList.add(new GuiButton(BUTTON_COLOR_A_MINUS, centerX - 90, colorStartY + rowHeight * 3, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_COLOR_A_PLUS, centerX + 16, colorStartY + rowHeight * 3, 24, 20, "+"));

        int widthStartY = colorStartY + rowHeight * 4 + 8;
        fontRendererObj.drawStringWithShadow("--- Line Style ---", centerX - 55, widthStartY - 14, 10526880);
        this.buttonList.add(new GuiButton(BUTTON_WIDTH_MINUS, centerX - 90, widthStartY, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_WIDTH_PLUS, centerX + 16, widthStartY, 24, 20, "+"));

        this.buttonList.add(new GuiButton(BUTTON_MARKER_WIDTH_MINUS, centerX - 90, widthStartY + rowHeight, 24, 20, "-"));
        this.buttonList.add(new GuiButton(BUTTON_MARKER_WIDTH_PLUS, centerX + 16, widthStartY + rowHeight, 24, 20, "+"));

        int btnY = widthStartY + rowHeight * 2 + 8;
        this.buttonList.add(new GuiButton(BUTTON_DONE, centerX - 40, btnY, 80, 22, "Done"));
    }

    private String getToggleLabel(String name, boolean value) {
        return name + ": " + (value ? "ON" : "OFF");
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BUTTON_MOD_TOGGLE:
                modEnabled = !modEnabled;
                button.displayString = getToggleLabel("Mod Enabled", modEnabled);
                break;
            case BUTTON_MARKER_TOGGLE:
                showLandingMarker = !showLandingMarker;
                button.displayString = getToggleLabel("Show Landing Marker", showLandingMarker);
                break;
            case BUTTON_COLOR_R_MINUS:
                colorR = Math.max(0, colorR - 15);
                break;
            case BUTTON_COLOR_R_PLUS:
                colorR = Math.min(255, colorR + 15);
                break;
            case BUTTON_COLOR_G_MINUS:
                colorG = Math.max(0, colorG - 15);
                break;
            case BUTTON_COLOR_G_PLUS:
                colorG = Math.min(255, colorG + 15);
                break;
            case BUTTON_COLOR_B_MINUS:
                colorB = Math.max(0, colorB - 15);
                break;
            case BUTTON_COLOR_B_PLUS:
                colorB = Math.min(255, colorB + 15);
                break;
            case BUTTON_COLOR_A_MINUS:
                colorA = Math.max(0, colorA - 15);
                break;
            case BUTTON_COLOR_A_PLUS:
                colorA = Math.min(255, colorA + 15);
                break;
            case BUTTON_WIDTH_MINUS:
                lineWidth = Math.max(1.0F, lineWidth - 0.5F);
                break;
            case BUTTON_WIDTH_PLUS:
                lineWidth = Math.min(20.0F, lineWidth + 0.5F);
                break;
            case BUTTON_MARKER_WIDTH_MINUS:
                markerLineWidth = Math.max(1.0F, markerLineWidth - 0.5F);
                break;
            case BUTTON_MARKER_WIDTH_PLUS:
                markerLineWidth = Math.min(20.0F, markerLineWidth + 0.5F);
                break;
            case BUTTON_DONE:
                applyAndSave();
                mc.displayGuiScreen(null);
                break;
        }
    }

    private void applyAndSave() {
        ModConfig.modEnabled = modEnabled;
        ModConfig.showLandingMarker = showLandingMarker;
        ModConfig.trajectoryColorR = colorR;
        ModConfig.trajectoryColorG = colorG;
        ModConfig.trajectoryColorB = colorB;
        ModConfig.trajectoryColorA = colorA;
        ModConfig.trajectoryLineWidth = lineWidth;
        ModConfig.landingMarkerLineWidth = markerLineWidth;
        ModConfig.save();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "Fireball Predict Config", width / 2, 18, 16777215);

        int centerX = width / 2;
        int startY = this.height / 2 - 80;

        fontRendererObj.drawStringWithShadow("--- General Settings ---", centerX - 70, startY - 14, 10526880);

        int colorStartY = startY + 78;
        fontRendererObj.drawStringWithShadow("--- Trajectory Line Color ---", centerX - 82, colorStartY - 14, 10526880);

        int rowHeight = 28;
        int labelX = centerX - 60;

        String rStr = "Red:   " + colorR;
        String gStr = "Green: " + colorG;
        String bStr = "Blue:  " + colorB;
        String aStr = "Alpha: " + colorA;
        fontRendererObj.drawStringWithShadow(rStr, labelX, colorStartY + 6, 0xFF000000 | (colorR << 16));
        fontRendererObj.drawStringWithShadow(gStr, labelX, colorStartY + rowHeight + 6, 0xFF000000 | (colorG << 8));
        fontRendererObj.drawStringWithShadow(bStr, labelX, colorStartY + rowHeight * 2 + 6, 0xFF000000 | colorB);
        fontRendererObj.drawStringWithShadow(aStr, labelX, colorStartY + rowHeight * 3 + 6, 0xFFAAAAAA);

        int widthStartY = colorStartY + rowHeight * 4 + 8;
        fontRendererObj.drawStringWithShadow("--- Line Style ---", centerX - 55, widthStartY - 14, 10526880);
        String wStr = "Width: " + String.format("%.1f", lineWidth);
        fontRendererObj.drawStringWithShadow(wStr, labelX, widthStartY + 6, 0xFFCCCCCC);
        String mwStr = "Marker: " + String.format("%.1f", markerLineWidth);
        fontRendererObj.drawStringWithShadow(mwStr, labelX, widthStartY + rowHeight + 6, 0xFFCCCCCC);

        int previewX = centerX + 62;
        int previewY = colorStartY + 4;
        drawGradientRect(previewX, previewY, previewX + 44, previewY + rowHeight * 3 + 12,
            ((colorA & 0xFF) << 24) | ((colorR & 0xFF) << 16) | ((colorG & 0xFF) << 8) | (colorB & 0xFF),
            ((colorA & 0xFF) << 24) | ((colorR & 0xFF) << 16) | ((colorG & 0xFF) << 8) | (colorB & 0xFF));

        fontRendererObj.drawStringWithShadow("Preview", previewX + 6, previewY - 12, 14737632);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            applyAndSave();
            mc.displayGuiScreen(null);
        }
    }
}
