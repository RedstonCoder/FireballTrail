package com.redstoncoder.fireballpredict;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class ModConfig {
    private static Configuration config;

    public static volatile boolean modEnabled = true;
    public static volatile boolean showLandingMarker = true;
    public static volatile int trajectoryColorR = 255;
    public static volatile int trajectoryColorG = 0;
    public static volatile int trajectoryColorB = 0;
    public static volatile int trajectoryColorA = 204;
    public static volatile float trajectoryLineWidth = 2.5F;
    public static volatile float landingMarkerLineWidth = 1.5F;

    public static void init(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        modEnabled = config.getBoolean("modEnabled", "general", true, "Enable or disable the Fireball trajectory prediction module");
        showLandingMarker = config.getBoolean("showLandingMarker", "display", true, "Show the landing marker (crosshair circle) at predicted landing position");
        trajectoryColorR = config.getInt("trajectoryColorR", "color", 255, 0, 255, "Red component of trajectory line color (0-255)");
        trajectoryColorG = config.getInt("trajectoryColorG", "color", 0, 0, 255, "Green component of trajectory line color (0-255)");
        trajectoryColorB = config.getInt("trajectoryColorB", "color", 0, 0, 255, "Blue component of trajectory line color (0-255)");
        trajectoryColorA = config.getInt("trajectoryColorA", "color", 204, 0, 255, "Alpha/opacity of trajectory line color (0-255)");
        trajectoryLineWidth = config.getFloat("trajectoryLineWidth", "display", 2.5F, 1.0F, 20.0F, "Thickness of trajectory prediction lines (1.0-20.0)");
        landingMarkerLineWidth = config.getFloat("landingMarkerLineWidth", "display", 1.5F, 1.0F, 20.0F, "Thickness of landing marker lines (1.0-20.0)");
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        config.get("general", "modEnabled", true).set(modEnabled);
        config.get("display", "showLandingMarker", true).set(showLandingMarker);
        config.get("color", "trajectoryColorR", 255).set(trajectoryColorR);
        config.get("color", "trajectoryColorG", 0).set(trajectoryColorG);
        config.get("color", "trajectoryColorB", 0).set(trajectoryColorB);
        config.get("color", "trajectoryColorA", 204).set(trajectoryColorA);
        config.get("display", "trajectoryLineWidth", 2.5F).set(trajectoryLineWidth);
        config.get("display", "landingMarkerLineWidth", 1.5F).set(landingMarkerLineWidth);
        if (config.hasChanged()) {
            config.save();
        }
    }
}
