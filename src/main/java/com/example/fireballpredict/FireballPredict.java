package com.example.fireballpredict;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
    modid = FireballPredict.MODID,
    version = FireballPredict.VERSION,
    name = FireballPredict.NAME
)
public class FireballPredict {
    public static final String MODID = "fireballpredict";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "FireballPredict";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.init(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new FireballEventHandler());

        if (event.getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new FireballTrajectoryRenderer());
            ConfigKeyHandler.register();
            MinecraftForge.EVENT_BUS.register(new ConfigKeyHandler());
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
