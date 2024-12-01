package com.thatoneaiguy.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class CrateCracker extends Item {
    private static final String KEY_TAG = "HasKey";
    private static final String CONTAINER_X = "ContainerX";
    private static final String CONTAINER_Y = "ContainerY";
    private static final String CONTAINER_Z = "ContainerZ";

    public static double mode = 0.1;

    public static double getMode(ItemStack stack) {
        return mode;
    }

    public CrateCracker(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        ItemStack stack = context.getStack();

        // Check if the block is a container by confirming it has a LockableContainerBlockEntity
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient() && blockEntity instanceof LockableContainerBlockEntity && context.getPlayer().isSneaking()) {
            if (blockState.getBlock().getBlastResistance() >= 1200) return ActionResult.FAIL;
            // Add a key and save container coordinates to the item's NBT
            NbtCompound tag = stack.getOrCreateNbt();
            tag.putBoolean(KEY_TAG, true);
            tag.putInt(CONTAINER_X, pos.getX());
            tag.putInt(CONTAINER_Y, pos.getY());
            tag.putInt(CONTAINER_Z, pos.getZ());
            stack.setNbt(tag);
            mode = 0.2;

            // Play a sound to indicate the container has been keyed
            world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 1.0f, 0.8f);
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && !user.isSneaking()) {
            ItemStack stack = user.getStackInHand(hand);
            NbtCompound tag = stack.getNbt();

            if (tag != null && tag.getBoolean(KEY_TAG)) {
                // Retrieve stored coordinates from the item's NBT
                int x = tag.getInt(CONTAINER_X);
                int y = tag.getInt(CONTAINER_Y);
                int z = tag.getInt(CONTAINER_Z);
                BlockPos storedPos = new BlockPos(x, y, z);
                BlockEntity blockEntity = world.getBlockEntity(storedPos);
                Block block = world.getBlockState(storedPos).getBlock();
                BlockState state = world.getBlockState(storedPos);

                // Check if there's a container block at the stored coordinates
                if (blockEntity instanceof LockableContainerBlockEntity containerEntity) {

                    for (int i = 0; i < containerEntity.size(); i++) {
                        ItemStack itemStack = containerEntity.getStack(i);
                        if (!itemStack.isEmpty()) {
                            ItemEntity itemEntity = new ItemEntity(
                                    world,
                                    user.getX(), user.getY(), user.getZ(),
                                    itemStack.copy()
                            );
                            world.spawnEntity(itemEntity); // Spawn the item entity
                        }
                    }

                    // If the container is a double chest, handle both sides
                    if (blockEntity instanceof ChestBlockEntity) {
                        ChestBlockEntity chestEntity = (ChestBlockEntity) blockEntity;

                        // Get both parts of the double chest if it exists
                        Inventory chestInventory = ChestBlock.getInventory((ChestBlock) block, chestEntity.getCachedState(), world, storedPos, false);
                        if (chestInventory != null) {
                            // Clear items from both sides of the double chest
                            for (int i = 0; i < chestInventory.size(); i++) {
                                chestInventory.setStack(i, ItemStack.EMPTY);
                            }
                        }
                    } else {
                        // If it’s a single container, clear the items in it

                        for (int i = 0; i < containerEntity.size(); i++) {
                            ItemStack itemStack = containerEntity.getStack(i);
                            if (!itemStack.isEmpty()) {
                                ItemEntity itemEntity = new ItemEntity(
                                        world,
                                        user.getX(), user.getY(), user.getZ(),
                                        itemStack.copy()
                                );
                                world.spawnEntity(itemEntity); // Spawn the item entity
                            }
                        }

                        containerEntity.clear();
                    }

                    // Remove the container and create an explosion
                    world.removeBlock(storedPos, false);
                    world.createExplosion(null, x, y, z, 4.0f, Explosion.DestructionType.DESTROY);

                    // Clear the key and coordinates from the item, and consume one CrateCracker item on successful use
                    tag.remove(KEY_TAG);
                    tag.remove(CONTAINER_X);
                    tag.remove(CONTAINER_Y);
                    tag.remove(CONTAINER_Z);
                    stack.setNbt(tag);
                    stack.decrement(1);
                    mode = 0.1;

                    // Play a sound to indicate the container has been bombed
                    world.playSound(null, storedPos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, 1.0f);

                    return TypedActionResult.success(stack);
                }
                // If no container is found at the location, clear the NBT coordinates
                tag.remove(KEY_TAG);
                tag.remove(CONTAINER_X);
                tag.remove(CONTAINER_Y);
                tag.remove(CONTAINER_Z);
                mode = 0.1;
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.getBoolean(KEY_TAG)) {
            int x = tag.getInt(CONTAINER_X);
            int y = tag.getInt(CONTAINER_Y);
            int z = tag.getInt(CONTAINER_Z);
            tooltip.add(Text.literal("Keyed to container at (" + x + ", " + y + ", " + z + ")").formatted(Formatting.DARK_GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        Text nameText = super.getName(stack);

        if (tag != null && tag.getBoolean(KEY_TAG)) {
            return nameText.copy().setStyle(nameText.getStyle().withColor(new Color(135, 255, 191, 255).getRGB()));
        }
        return nameText.copy().setStyle(nameText.getStyle().withColor(new Color(143, 61, 66, 255).getRGB()));
    }
}
