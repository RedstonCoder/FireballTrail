package com.example.fireballpredict;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class FireballEventHandler {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ModConfig.modEnabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        List<Integer> currentFireballs = new ArrayList<Integer>();

        for (Object entityObj : mc.theWorld.loadedEntityList) {
            if (entityObj instanceof EntityFireball) {
                EntityFireball fireball = (EntityFireball) entityObj;
                int entityId = fireball.getEntityId();
                currentFireballs.add(entityId);

                List<Vec3> trajectory = FireballTrajectoryPredictor.predictTrajectory(fireball);
                Vec3 landingPosition = FireballTrajectoryPredictor.predictLandingPosition(fireball);
                FireballTrajectoryRenderer.addFireball(entityId, trajectory, landingPosition);
            }
        }

        FireballTrajectoryRenderer.cleanupRemoved(currentFireballs);
    }
}
