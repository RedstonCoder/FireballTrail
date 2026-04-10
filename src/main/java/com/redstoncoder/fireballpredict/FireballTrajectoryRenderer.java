package com.redstoncoder.fireballpredict;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class FireballTrajectoryRenderer {
    private static final Object dataLock = new Object();
    private static final Map<Integer, List<Vec3>> trajectories = new HashMap<Integer, List<Vec3>>();
    private static final Map<Integer, Vec3> landingPositions = new HashMap<Integer, Vec3>();

    public static void addFireball(int entityId, List<Vec3> trajectory, Vec3 landingPosition) {
        synchronized (dataLock) {
            trajectories.put(entityId, trajectory);
            landingPositions.put(entityId, landingPosition);
        }
    }

    public static void removeFireball(int entityId) {
        synchronized (dataLock) {
            trajectories.remove(entityId);
            landingPositions.remove(entityId);
        }
    }

    public static void cleanupRemoved(List<Integer> currentIds) {
        synchronized (dataLock) {
            Iterator<Map.Entry<Integer, List<Vec3>>> iter = trajectories.entrySet().iterator();
            while (iter.hasNext()) {
                if (!currentIds.contains(iter.next().getKey())) {
                    iter.remove();
                }
            }
            Iterator<Map.Entry<Integer, Vec3>> iter2 = landingPositions.entrySet().iterator();
            while (iter2.hasNext()) {
                if (!currentIds.contains(iter2.next().getKey())) {
                    iter2.remove();
                }
            }
        }
    }

    private static Map<Integer, List<Vec3>> getTrajectorySnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, List<Vec3>>(trajectories);
        }
    }

    private static Map<Integer, Vec3> getLandingSnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, Vec3>(landingPositions);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!ModConfig.modEnabled) return;

        Map<Integer, List<Vec3>> trajectorySnapshot = getTrajectorySnapshot();
        Map<Integer, Vec3> landingSnapshot = getLandingSnapshot();
        if (trajectorySnapshot.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        float partialTicks = event.partialTicks;
        double interpX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double interpY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double interpZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        float r = ModConfig.trajectoryColorR / 255.0F;
        float g = ModConfig.trajectoryColorG / 255.0F;
        float b = ModConfig.trajectoryColorB / 255.0F;
        float a = ModConfig.trajectoryColorA / 255.0F;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        GL11.glTranslated(-interpX, -interpY, -interpZ);

        for (Map.Entry<Integer, List<Vec3>> entry : trajectorySnapshot.entrySet()) {
            List<Vec3> trajectory = entry.getValue();
            if (trajectory.size() < 2) continue;

            GL11.glColor4f(r, g, b, a);
            GL11.glLineWidth(ModConfig.trajectoryLineWidth);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Vec3 point : trajectory) {
                GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
            }
            GL11.glEnd();
        }

        if (ModConfig.showLandingMarker) {
            for (Vec3 landingPos : landingSnapshot.values()) {
                renderLandingMarker(landingPos, r, g, b);
            }
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void renderLandingMarker(Vec3 pos, float r, float g, float b) {
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord + 0.02D, pos.zCoord);

        GL11.glColor4f(r, g, b, 0.6F);
        GL11.glLineWidth(ModConfig.landingMarkerLineWidth);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * 0.5;
            double z = Math.sin(angle) * 0.5;
            GL11.glVertex3d(x, 0.01, z);
        }
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(-0.5, 0.02, 0.0);
        GL11.glVertex3d(0.5, 0.02, 0.0);
        GL11.glVertex3d(0.0, 0.02, -0.5);
        GL11.glVertex3d(0.0, 0.02, 0.5);
        GL11.glEnd();

        GL11.glPopMatrix();
    }
}
