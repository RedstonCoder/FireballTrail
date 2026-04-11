package com.redstoncoder.fireballpredict;

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

                TrajectoryResult result = FireballTrajectoryPredictor.predictTrajectory(fireball);
                Vec3 landingPosition = FireballTrajectoryPredictor.predictLandingPosition(result.points);
                
                String debugText = null;
                if (ModConfig.showDebugInfo) {
                    double speed = Math.sqrt(
                        fireball.motionX * fireball.motionX +
                        fireball.motionY * fireball.motionY +
                        fireball.motionZ * fireball.motionZ
                    );
                    debugText = String.format(
                        "ID:%d Pos:[%.1f,%.1f,%.1f] Motion:[%.2f,%.2f,%.2f] Speed:%.2f Accel:[%.3f,%.3f,%.3f]",
                        entityId,
                        fireball.posX, fireball.posY, fireball.posZ,
                        fireball.motionX, fireball.motionY, fireball.motionZ,
                        speed,
                        fireball.accelerationX, fireball.accelerationY, fireball.accelerationZ
                    );
                }
                
                FireballData.addFireball(entityId, result.points, landingPosition, result.impactTimeSeconds, result.collisionNormal, debugText);
            }
        }

        FireballData.cleanupRemoved(currentFireballs);
    }
}
