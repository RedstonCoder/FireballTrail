package com.redstoncoder.fireballpredict;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
public class ConfigKeyHandler {
    public static final KeyBinding OPEN_CONFIG = new KeyBinding("key.fireballpredict.config", Keyboard.KEY_P, "key.categories.fireballpredict");

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_CONFIG);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (OPEN_CONFIG.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && !mc.isGamePaused()) {
                mc.displayGuiScreen(new GuiFireballConfig());
            }
        }
    }
}
