package com.redstoncoder.fireballpredict;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FireballTrajectoryPredictor {
    private static final int MAX_PREDICTION_TICKS = 600;

    public static TrajectoryResult predictTrajectory(EntityFireball fireball) {
        List<Vec3> trajectory = new ArrayList<Vec3>();
        World world = fireball.worldObj;

        double posX = fireball.posX;
        double posY = fireball.posY;
        double posZ = fireball.posZ;

        double motionX = fireball.motionX;
        double motionY = fireball.motionY;
        double motionZ = fireball.motionZ;

        trajectory.add(new Vec3(posX, posY, posZ));

        float impactTime = -1f;
        Vec3 collisionNormal = null;

        for (int i = 0; i < MAX_PREDICTION_TICKS; i++) {
            double newPosX = posX + motionX;
            double newPosY = posY + motionY;
            double newPosZ = posZ + motionZ;

            Vec3 start = new Vec3(posX, posY, posZ);
            Vec3 end = new Vec3(newPosX, newPosY, newPosZ);

            MovingObjectPosition blockCollision = world.rayTraceBlocks(start, end);
            MovingObjectPosition entityCollision = rayTraceEntities(world, start, end, fireball);

            MovingObjectPosition collision = getClosestCollision(start, blockCollision, entityCollision);
            if (collision != null) {
                trajectory.add(collision.hitVec);
                impactTime = (i + 1) / 20f;
                collisionNormal = getCollisionNormal(collision);
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

        return new TrajectoryResult(trajectory, impactTime, impactTime >= 0, collisionNormal);
    }

    public static List<Vec3> predictTrajectoryLegacy(EntityFireball fireball) {
        return predictTrajectory(fireball).points;
    }

    public static Vec3 predictLandingPosition(List<Vec3> trajectory) {
        if (trajectory.isEmpty()) {
            return new Vec3(0, 0, 0);
        }
        return trajectory.get(trajectory.size() - 1);
    }

    private static Vec3 getCollisionNormal(MovingObjectPosition mop) {
        if (mop == null) return null;
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mop.sideHit != null) {
            EnumFacing facing = mop.sideHit;
            switch (facing) {
                case DOWN:  return new Vec3(0, -1, 0);
                case UP:    return new Vec3(0, 1, 0);
                case NORTH: return new Vec3(0, 0, -1);
                case SOUTH: return new Vec3(0, 0, 1);
                case WEST:  return new Vec3(-1, 0, 0);
                case EAST:  return new Vec3(1, 0, 0);
            }
        }
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.entityHit != null) {
            Vec3 hitPos = mop.hitVec;
            Entity entity = mop.entityHit;
            double cx = entity.posX;
            double cy = entity.posY + entity.height / 2.0;
            double cz = entity.posZ;
            Vec3 center = new Vec3(cx, cy, cz);
            Vec3 normal = hitPos.subtract(center).normalize();
            if (normal.lengthVector() < 0.01) return new Vec3(0, 1, 0);
            return normal;
        }
        return new Vec3(0, 1, 0);
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
        double entityDist = entityCollision.hitVec.distanceTo(start);

        return blockDist <= entityDist ? blockCollision : entityCollision;
    }
}
