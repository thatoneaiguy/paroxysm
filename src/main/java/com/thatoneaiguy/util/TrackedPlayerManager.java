package com.thatoneaiguy.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackedPlayerManager {
    private static final Map<UUID, UUID> trackedPlayers = new HashMap<>();

    public static void setTrackedPlayer(UUID userUUID, PlayerEntity target) {
        clearPreviousTracking();

        trackedPlayers.put(userUUID, target.getUuid());
    }

    public static PlayerEntity getTrackedPlayer(World world, UUID userUuid) {
        UUID targetUUID = trackedPlayers.get(userUuid);
        return targetUUID != null ? world.getPlayerByUuid(targetUUID) : null;
    }

    public static void clearPreviousTracking() {
        trackedPlayers.clear();
    }
}
