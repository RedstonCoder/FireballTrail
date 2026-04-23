package com.redstoncoder.fireballpredict;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class FireballTrajectoryRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!ModConfig.modEnabled) return;

        Map<Integer, List<Vec3>> trajectorySnapshot = FireballData.getTrajectorySnapshot();
        Map<Integer, Vec3> landingSnapshot = FireballData.getLandingSnapshot();
        Map<Integer, Float> impactTimeSnapshot = FireballData.getImpactTimeSnapshot();
        Map<Integer, Vec3> normalSnapshot = FireballData.getCollisionNormalSnapshot();
        Map<Integer, String> debugInfoSnapshot = FireballData.getDebugInfoSnapshot();

        if (trajectorySnapshot.isEmpty() && !ModConfig.showHeldFireballPrediction) return;

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
            int entityId = entry.getKey();
            List<Vec3> trajectory = entry.getValue();
            if (trajectory.size() < 2) continue;

            GL11.glColor4f(r, g, b, a);
            GL11.glLineWidth(ModConfig.trajectoryLineWidth);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (Vec3 point : trajectory) {
                GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
            }
            GL11.glEnd();

            if (ModConfig.showImpactTime && impactTimeSnapshot.containsKey(entityId)) {
                Vec3 lastPoint = trajectory.get(trajectory.size() - 1);
                renderImpactTime(lastPoint, impactTimeSnapshot.get(entityId));
            }

            if (ModConfig.showDebugInfo && debugInfoSnapshot.containsKey(entityId) && !trajectory.isEmpty()) {
                Vec3 firstPoint = trajectory.get(0);
                renderDebugText(firstPoint, debugInfoSnapshot.get(entityId));
            }
        }

        if (ModConfig.showLandingMarker) {
            for (Map.Entry<Integer, Vec3> entry : landingSnapshot.entrySet()) {
                int entityId = entry.getKey();
                Vec3 landingPos = entry.getValue();
                Vec3 normal = normalSnapshot.get(entityId);
                renderLandingMarker(landingPos, r, g, b, normal);
            }
        }

        if (ModConfig.showHeldFireballPrediction) {
            List<Vec3> heldTrajectory = FireballData.getHeldTrajectorySnapshot();
            if (heldTrajectory != null && heldTrajectory.size() >= 2) {
                GL11.glColor4f(r, g, b, a);
                GL11.glLineWidth(ModConfig.trajectoryLineWidth);

                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (Vec3 point : heldTrajectory) {
                    GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
                }
                GL11.glEnd();

                if (ModConfig.showImpactTime) {
                    Float heldImpactTimeVal = FireballData.getHeldImpactTimeSnapshot();
                    if (heldImpactTimeVal != null) {
                        Vec3 lastPoint = heldTrajectory.get(heldTrajectory.size() - 1);
                        renderImpactTime(lastPoint, heldImpactTimeVal);
                    }
                }

                if (ModConfig.showLandingMarker) {
                    Vec3 heldLanding = FireballData.getHeldLandingSnapshot();
                    if (heldLanding != null) {
                        Vec3 heldNormal = FireballData.getHeldCollisionNormalSnapshot();
                        renderLandingMarker(heldLanding, r, g, b, heldNormal);
                    }
                }
            }
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void renderImpactTime(Vec3 pos, float seconds) {
        Minecraft mc = Minecraft.getMinecraft();
        String text = String.format("%.2fs", seconds);
        FontRenderer fr = mc.fontRendererObj;

        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord + 1.5, pos.zCoord);

        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = ModConfig.impactTimeFontSize;
        GL11.glScalef(-scale, -scale, scale);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        fr.drawStringWithShadow(text, -fr.getStringWidth(text) / 2, 0, 0xFFFF55);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    private static void renderDebugText(Vec3 pos, String text) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;

        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord + 2.0, pos.zCoord);

        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        float scale = 0.015F;
        GL11.glScalef(-scale, -scale, scale);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        fr.drawStringWithShadow(text, -fr.getStringWidth(text) / 2, 0, 0x00FFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    private static void renderLandingMarker(Vec3 pos, float r, float g, float b, Vec3 normal) {
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);

        if (normal != null) {
            applyRotationToNormal(normal);
        }

        GL11.glTranslated(0, 0.02D, 0);

        GL11.glColor4f(r, g, b, 0.6F);
        GL11.glLineWidth(ModConfig.landingMarkerLineWidth);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * 0.5;
            double y = Math.sin(angle) * 0.5;
            GL11.glVertex3d(x, y, 0.01);
        }
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(-0.5, 0.0, 0.02);
        GL11.glVertex3d(0.5, 0.0, 0.02);
        GL11.glVertex3d(0.0, -0.5, 0.02);
        GL11.glVertex3d(0.0, 0.5, 0.02);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    private static void applyRotationToNormal(Vec3 normal) {
        double nx = normal.xCoord;
        double ny = normal.yCoord;
        double nz = normal.zCoord;
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 0.001) return;
        nx /= len;
        ny /= len;
        nz /= len;

        double dot = nz;
        if (dot > 0.9999) {
            return;
        } else if (dot < -0.9999) {
            GL11.glRotatef(180, 1, 0, 0);
            return;
        }

        double ax = 0 * nz - 1 * ny;
        double ay = 1 * nx - 0 * nz;
        double az = 0 * ny - 0 * nx;
        double axisLen = Math.sqrt(ax * ax + ay * ay + az * az);
        if (axisLen < 0.001) {
            ax = 1;
            ay = 0;
            az = 0;
        } else {
            ax /= axisLen;
            ay /= axisLen;
            az /= axisLen;
        }

        double angleRad = Math.acos(Math.max(-1, Math.min(1, dot)));
        float angleDeg = (float) Math.toDegrees(angleRad);

        GL11.glRotatef(angleDeg, (float) ax, (float) ay, (float) az);
    }
}
