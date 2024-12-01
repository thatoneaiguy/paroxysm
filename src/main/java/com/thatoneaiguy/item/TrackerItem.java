package com.thatoneaiguy.item;

import com.thatoneaiguy.util.TrackedPlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;

public class TrackerItem extends Item {
    public TrackerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if ( entity instanceof ServerPlayerEntity targetPlayer ) {
            if ( !user.getWorld().isClient ) {
                TrackedPlayerManager.setTrackedPlayer(user.getUuid(), targetPlayer);

                user.sendMessage(Text.literal("Tracking " + targetPlayer.getName().getString()), true);
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
