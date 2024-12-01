package com.thatoneaiguy.item;

import com.thatoneaiguy.Paroxysm;
import net.minecraft.block.Block;
import net.minecraft.block.IceBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BombardItem extends Item {

    private static final String ITEMS_KEY = "IceItems"; // Key for storing ice items
    private static final String SELECTED_KEY = "SelectedItem"; // Key for selected item
    private static final String ITEM_TYPE_KEY = "ItemType"; // Key for ice type
    private static final String ITEM_COUNT_KEY = "Count"; // Key for item count
    private static final int MAX_CAPACITY = 64; // Max capacity for the Bombard
    private static final String SELECTED_INDEX_KEY = "SelectedIndex";


    public BombardItem(Settings settings) {
        super(settings);
    }

    /**
     * Adds ice to the Bombard item.
     */
    public static boolean addItem(PlayerEntity player, ItemStack bombardStack, ItemStack iceStack) {
        if (!isIce(iceStack)) return false;

        NbtCompound nbt = bombardStack.getOrCreateNbt();
        NbtList itemsList = nbt.getList(ITEMS_KEY, 10);

        int currentCapacity = getCurrentCount(bombardStack);
        if (currentCapacity >= MAX_CAPACITY) return false;

        boolean itemAdded = false;
        int countToAdd = Math.min(iceStack.getCount(), MAX_CAPACITY - currentCapacity);
        String iceTypeName = iceStack.getName().getString(); // Get the display name

        if (countToAdd > 0) {
            for (int i = 0; i < itemsList.size(); i++) {
                NbtCompound itemData = itemsList.getCompound(i);
                if (itemData.getString(ITEM_TYPE_KEY).equals(iceStack.getItem().getTranslationKey())) {
                    int existingCount = itemData.getInt(ITEM_COUNT_KEY);
                    itemData.putInt(ITEM_COUNT_KEY, existingCount + countToAdd);
                    itemsList.set(i, itemData);
                    iceStack.decrement(countToAdd);
                    itemAdded = true;
                    break;
                }
            }

            if (!itemAdded) {
                NbtCompound newItemData = new NbtCompound();
                newItemData.putString(ITEM_TYPE_KEY, iceStack.getItem().getTranslationKey());
                newItemData.putInt(ITEM_COUNT_KEY, countToAdd);
                itemsList.add(newItemData);
                iceStack.decrement(countToAdd);
            }

            player.sendMessage(Text.of("Added " + countToAdd + "x " + iceTypeName + " to the Bombard."), true);
        }

        nbt.put(ITEMS_KEY, itemsList);
        return true;
    }

    /**
     * Gets the current total count of items in the Bombard.
     */
    public static int getCurrentCount(ItemStack bombardStack) {
        NbtCompound nbt = bombardStack.getOrCreateNbt();
        NbtList itemsList = nbt.getList(ITEMS_KEY, 10);
        int total = 0;

        for (int i = 0; i < itemsList.size(); i++) {
            total += itemsList.getCompound(i).getInt(ITEM_COUNT_KEY);
        }

        return total;
    }

    /**
     * Cycles through stored ice types and updates the selected item.
     */
    private static void cycleSelectedItem(PlayerEntity player, ItemStack bombardStack) {
        NbtCompound nbt = bombardStack.getOrCreateNbt();
        NbtList itemsList = nbt.getList(ITEMS_KEY, 10);

        if (itemsList.isEmpty()) {
            player.sendMessage(Text.of("The Bombard is empty."), true);
            return;
        }

        // Get or initialize the selected index
        int selectedIndex = nbt.contains(SELECTED_INDEX_KEY) ? nbt.getInt(SELECTED_INDEX_KEY) : -1;
        selectedIndex = (selectedIndex + 1) % itemsList.size(); // Cycle to the next item
        nbt.putInt(SELECTED_INDEX_KEY, selectedIndex);          // Update the NBT

        // Get the selected item's display name
        NbtCompound selectedItemData = itemsList.getCompound(selectedIndex);
        String itemType = selectedItemData.getString(ITEM_TYPE_KEY);
        int count = selectedItemData.getInt(ITEM_COUNT_KEY);

        // Use the registry to get the item
        Item selectedItem = Registry.ITEM.get(new Identifier(itemType));
        ItemStack itemStack = new ItemStack(selectedItem);
        String displayName = itemStack.getName().getString();

        player.sendMessage(Text.of("Selected: " + count + "x " + displayName), true);
    }

    /**
     * Determines if the stack is one of the valid ice items.
     */
    private static boolean isIce(ItemStack stack) {
        return stack.getItem() == Items.ICE || stack.getItem() == Items.PACKED_ICE || stack.getItem() == Items.BLUE_ICE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack bombardStack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            // Cycle selected item
            if (!world.isClient) {
                cycleSelectedItem(player, bombardStack);
            }
        } else {
            // Display currently selected item
            NbtCompound nbt = bombardStack.getOrCreateNbt();
            NbtList itemsList = nbt.getList(ITEMS_KEY, 10);
            if (!nbt.contains(SELECTED_KEY) || itemsList.isEmpty()) {
                player.sendMessage(Text.of("No item selected."), true);
            } else {
                int selectedIndex = nbt.getInt(SELECTED_KEY);
                NbtCompound selectedItemData = itemsList.getCompound(selectedIndex);
                String selectedItemType = selectedItemData.getString(ITEM_TYPE_KEY);
                int count = selectedItemData.getInt(ITEM_COUNT_KEY);
                player.sendMessage(Text.of("Currently selected: " + count + "x " + selectedItemType), true);
            }
        }

        return TypedActionResult.success(bombardStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList itemsList = nbt.getList(ITEMS_KEY, 10);
        int total = getCurrentCount(stack);

        if (itemsList.isEmpty()) {
            tooltip.add(Text.of("The Bombard is empty."));
        } else {
            for (int i = 0; i < itemsList.size(); i++) {
                NbtCompound itemData = itemsList.getCompound(i);
                String itemType = itemData.getString(ITEM_TYPE_KEY);
                int count = itemData.getInt(ITEM_COUNT_KEY);

                // Use the registry to get the item
                Item item = Registry.ITEM.get(new Identifier(itemType));
                ItemStack itemStack = new ItemStack(item);
                String displayName = itemStack.getName().getString();

                tooltip.add(Text.of(count + "x " + displayName));
            }
            tooltip.add(Text.of("Total: " + total));
        }

        // Show selected item if one exists
        if (nbt.contains(SELECTED_INDEX_KEY)) {
            int selectedIndex = nbt.getInt(SELECTED_INDEX_KEY);
            if (selectedIndex >= 0 && selectedIndex < itemsList.size()) {
                NbtCompound selectedItemData = itemsList.getCompound(selectedIndex);
                String selectedType = selectedItemData.getString(ITEM_TYPE_KEY);
                int selectedCount = selectedItemData.getInt(ITEM_COUNT_KEY);

                // Get the selected item's display name
                Item selectedItem = Registry.ITEM.get(new Identifier(selectedType));
                ItemStack selectedItemStack = new ItemStack(selectedItem);
                String selectedDisplayName = selectedItemStack.getName().getString();

                tooltip.add(Text.of("Selected: " + selectedCount + "x " + selectedDisplayName));
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getWorld().isClient()) {
            return ActionResult.PASS;
        }

        // Get the Bombard stack and the offhand stack
        ItemStack bombardStack = context.getStack();
        ItemStack offHandStack = player.getOffHandStack();

        // Check if the player is holding ice in their offhand
        if (isIce(offHandStack)) {
            // Try to add the ice to the Bombard
            boolean added = addItem(player, bombardStack, offHandStack);

            // Provide feedback
            if (added) {
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.of("The Bombard is full or can't accept more ice."), true);
                return ActionResult.FAIL;
            }
        } else {
            player.sendMessage(Text.of("You need to hold ice in your offhand to load the Bombard."), true);
            return ActionResult.FAIL;
        }
    }
}