package com.example.fireballpredict;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FireballTrajectoryPredictor {
    private static final float MOTION_FACTOR = 0.95F;
    private static final int MAX_PREDICTION_TICKS = 600;

    public static List<Vec3> predictTrajectory(EntityFireball fireball) {
        List<Vec3> trajectory = new ArrayList<Vec3>();
        World world = fireball.worldObj;

        double posX = fireball.posX;
        double posY = fireball.posY;
        double posZ = fireball.posZ;

        double motionX = fireball.motionX;
        double motionY = fireball.motionY;
        double motionZ = fireball.motionZ;

        double accelerationX = fireball.accelerationX;
        double accelerationY = fireball.accelerationY;
        double accelerationZ = fireball.accelerationZ;

        trajectory.add(new Vec3(posX, posY, posZ));

        for (int i = 0; i < MAX_PREDICTION_TICKS; i++) {
            motionX += accelerationX;
            motionY += accelerationY;
            motionZ += accelerationZ;

            motionX *= MOTION_FACTOR;
            motionY *= MOTION_FACTOR;
            motionZ *= MOTION_FACTOR;

            double newPosX = posX + motionX;
            double newPosY = posY + motionY;
            double newPosZ = posZ + motionZ;

            Vec3 start = new Vec3(posX, posY, posZ);
            Vec3 end = new Vec3(newPosX, newPosY, newPosZ);

            MovingObjectPosition blockCollision = world.rayTraceBlocks(start, end);
            MovingObjectPosition entityCollision = rayTraceEntities(world, start, end, fireball);

            MovingObjectPosition collision = getClosestCollision(start, blockCollision, entityCollision);
            if (collision != null) {
                trajectory.add(new Vec3(collision.hitVec.xCoord, collision.hitVec.yCoord, collision.hitVec.zCoord));
                break;
            }

            posX = newPosX;
            posY = newPosY;
            posZ = newPosZ;

            trajectory.add(new Vec3(posX, posY, posZ));

            if (posY < 0 || posY > 256 || Math.abs(posX) > 30000000 || Math.abs(posZ) > 30000000) {
                break;
            }
        }

        return trajectory;
    }

    public static Vec3 predictLandingPosition(EntityFireball fireball) {
        List<Vec3> trajectory = predictTrajectory(fireball);
        if (trajectory.isEmpty()) {
            return new Vec3(fireball.posX, fireball.posY, fireball.posZ);
        }
        return trajectory.get(trajectory.size() - 1);
    }

    private static MovingObjectPosition rayTraceEntities(World world, Vec3 start, Vec3 end, EntityFireball fireball) {
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        AxisAlignedBB rayBB = new AxisAlignedBB(
            Math.min(start.xCoord, end.xCoord),
            Math.min(start.yCoord, end.yCoord),
            Math.min(start.zCoord, end.zCoord),
            Math.max(start.xCoord, end.xCoord),
            Math.max(start.yCoord, end.yCoord),
            Math.max(start.zCoord, end.zCoord)
        ).expand(1.0, 1.0, 1.0);

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, rayBB);
        for (Entity entity : entities) {
            if (entity == fireball || !entity.canBeCollidedWith()) {
                continue;
            }

            float entityWidth = entity.width;
            float entityHeight = entity.height;
            AxisAlignedBB entityBB = new AxisAlignedBB(
                entity.posX - entityWidth / 2.0,
                entity.posY,
                entity.posZ - entityWidth / 2.0,
                entity.posX + entityWidth / 2.0,
                entity.posY + entityHeight,
                entity.posZ + entityWidth / 2.0
            );

            MovingObjectPosition hitResult = entityBB.calculateIntercept(start, end);
            if (hitResult != null) {
                double distance = hitResult.hitVec.distanceTo(start);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null) {
            return new MovingObjectPosition(closestEntity);
        }

        return null;
    }

    private static MovingObjectPosition getClosestCollision(Vec3 start, MovingObjectPosition blockCollision, MovingObjectPosition entityCollision) {
        if (blockCollision == null && entityCollision == null) {
            return null;
        }

        if (blockCollision == null) {
            return entityCollision;
        }

        if (entityCollision == null) {
            return blockCollision;
        }

        double blockDist = blockCollision.hitVec.distanceTo(start);

        Entity entity = entityCollision.entityHit;
        double entityDist = Double.MAX_VALUE;
        if (entity != null) {
            AxisAlignedBB entityBB = entity.getEntityBoundingBox();
            Vec3 entityDir = new Vec3(entity.posX - start.xCoord, entity.posY - start.yCoord, entity.posZ - start.zCoord);
            Vec3 farEnd = start.addVector(entityDir.xCoord * 100, entityDir.yCoord * 100, entityDir.zCoord * 100);
            MovingObjectPosition intercept = entityBB.calculateIntercept(start, farEnd);
            if (intercept != null) {
                entityDist = intercept.hitVec.distanceTo(start);
            }
        }

        return blockDist <= entityDist ? blockCollision : entityCollision;
    }
}
