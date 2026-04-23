package com.redstoncoder.fireballpredict;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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

        if (ModConfig.showHeldFireballPrediction) {
            ItemStack heldItem = mc.thePlayer.inventory.getCurrentItem();
            if (heldItem != null) {
                String itemName = heldItem.getItem().getRegistryName();
                if ("fire_charge".equals(itemName) || "minecraft:fire_charge".equals(itemName)) {
                    Vec3 eyePos = new Vec3(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ
                    );
                    Vec3 lookVec = mc.thePlayer.getLookVec();
                    Vec3 startPos = eyePos.addVector(lookVec.xCoord * 0.5, lookVec.yCoord * 0.5, lookVec.zCoord * 0.5);

                    TrajectoryResult result = FireballTrajectoryPredictor.predictHeldFireballTrajectory(mc.theWorld, startPos, lookVec, mc.thePlayer);
                    Vec3 landingPos = FireballTrajectoryPredictor.predictLandingPosition(result.points);
                    Float impactTime = result.impactTimeSeconds > 0 ? result.impactTimeSeconds : null;
                    FireballData.setHeldFireballData(result.points, landingPos, impactTime, result.collisionNormal);
                } else {
                    FireballData.clearHeldFireballData();
                }
            } else {
                FireballData.clearHeldFireballData();
            }
        } else {
            FireballData.clearHeldFireballData();
        }
    }
}
