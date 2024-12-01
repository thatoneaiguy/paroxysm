package com.thatoneaiguy.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public class TrackerCompassItem extends CompassItem {
    public TrackerCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player) || world.isClient) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains("TrackedPlayer")) {
            UUID trackedUUID = nbt.getUuid("TrackedPlayer");
            PlayerEntity trackedPlayer = world.getPlayerByUuid(trackedUUID);

            if (trackedPlayer != null) {
                BlockPos trackedPos = trackedPlayer.getBlockPos();
                setLodestonePos(stack, world.getRegistryKey(), trackedPos);
            } else {
                if (nbt.contains("LastKnownX") && nbt.contains("LastKnownDimension")) {
                    int x = nbt.getInt("LastKnownX");
                    int y = nbt.getInt("LastKnownY");
                    int z = nbt.getInt("LastKnownZ");
                    String dimension = nbt.getString("LastKnownDimension");

                    if (world.getRegistryKey().getValue().toString().equals(dimension)) {
                        setLodestonePos(stack, world.getRegistryKey(), new BlockPos(x, y, z));
                    } else {
                        stack.removeSubNbt("LodestonePos");
                    }
                }
            }
        }
    }

    private void setLodestonePos(ItemStack stack, RegistryKey<World> dimension, BlockPos position) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("LodestoneX", position.getX());
        nbt.putInt("LodestoneY", position.getY());
        nbt.putInt("LodestoneZ", position.getZ());
        nbt.putString("LodestoneDimension", dimension.getValue().toString());
    }
}
