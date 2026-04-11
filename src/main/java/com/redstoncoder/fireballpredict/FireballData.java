package com.redstoncoder.fireballpredict;

import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FireballData {
    private static final Object dataLock = new Object();
    private static final Map<Integer, List<Vec3>> trajectories = new HashMap<Integer, List<Vec3>>();
    private static final Map<Integer, Vec3> landingPositions = new HashMap<Integer, Vec3>();
    private static final Map<Integer, Float> impactTimes = new HashMap<Integer, Float>();
    private static final Map<Integer, Vec3> collisionNormals = new HashMap<Integer, Vec3>();
    private static final Map<Integer, String> debugInfo = new HashMap<Integer, String>();

    public static void addFireball(int entityId, List<Vec3> trajectory, Vec3 landingPosition) {
        addFireball(entityId, trajectory, landingPosition, -1f, null);
    }

    public static void addFireball(int entityId, List<Vec3> trajectory, Vec3 landingPosition, float impactTimeSeconds) {
        addFireball(entityId, trajectory, landingPosition, impactTimeSeconds, null);
    }

    public static void addFireball(int entityId, List<Vec3> trajectory, Vec3 landingPosition, float impactTimeSeconds, Vec3 collisionNormal) {
        addFireball(entityId, trajectory, landingPosition, impactTimeSeconds, collisionNormal, null);
    }

    public static void addFireball(int entityId, List<Vec3> trajectory, Vec3 landingPosition, float impactTimeSeconds, Vec3 collisionNormal, String debugText) {
        synchronized (dataLock) {
            trajectories.put(entityId, trajectory);
            landingPositions.put(entityId, landingPosition);
            if (impactTimeSeconds > 0) {
                impactTimes.put(entityId, impactTimeSeconds);
            }
            if (collisionNormal != null) {
                collisionNormals.put(entityId, collisionNormal);
            }
            if (debugText != null) {
                debugInfo.put(entityId, debugText);
            }
        }
    }

    public static void removeFireball(int entityId) {
        synchronized (dataLock) {
            trajectories.remove(entityId);
            landingPositions.remove(entityId);
            impactTimes.remove(entityId);
            collisionNormals.remove(entityId);
            debugInfo.remove(entityId);
        }
    }

    public static void cleanupRemoved(List<Integer> currentIds) {
        synchronized (dataLock) {
            cleanupMapEntries(trajectories, currentIds);
            cleanupMapEntries(landingPositions, currentIds);
            cleanupMapEntries(impactTimes, currentIds);
            cleanupMapEntries(collisionNormals, currentIds);
            cleanupMapEntries(debugInfo, currentIds);
        }
    }

    private static <V> void cleanupMapEntries(Map<Integer, V> map, List<Integer> currentIds) {
        Iterator<Map.Entry<Integer, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            if (!currentIds.contains(iter.next().getKey())) {
                iter.remove();
            }
        }
    }

    public static Map<Integer, List<Vec3>> getTrajectorySnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, List<Vec3>>(trajectories);
        }
    }

    public static Map<Integer, Vec3> getLandingSnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, Vec3>(landingPositions);
        }
    }

    public static Map<Integer, Float> getImpactTimeSnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, Float>(impactTimes);
        }
    }

    public static Map<Integer, Vec3> getCollisionNormalSnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, Vec3>(collisionNormals);
        }
    }

    public static Map<Integer, String> getDebugInfoSnapshot() {
        synchronized (dataLock) {
            return new HashMap<Integer, String>(debugInfo);
        }
    }
}
