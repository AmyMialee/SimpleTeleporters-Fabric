package com.shnupbups.simpleteleporters.block;

import com.mojang.serialization.MapCodec;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersBlocks;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersItems;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersSoundEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleporterBlock extends BlockWithEntity {
	public static final MapCodec<TeleporterBlock> CODEC = createCodec(TeleporterBlock::new);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final BooleanProperty ENABLED = Properties.ENABLED;
	protected static final VoxelShape SHAPE = VoxelShapes.cuboid(0D, 0.0D, 0D, 1D, 0.38D, 1D);

	public TeleporterBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(ENABLED, false).with(WATERLOGGED, false));
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) return ItemActionResult.SUCCESS;
		if (world.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
			var had = !teleporter.getStack(0).isEmpty();
			var changed = false;
			if (!teleporter.getStack(0).isEmpty()) {
				ItemScatterer.spawn(world, pos, teleporter);
				teleporter.clear();
				changed = true;
			}
			if (stack.isOf(SimpleTeleportersItems.ENDER_SHARD)) {
				var component = stack.get(SimpleTeleportersItems.LOCATION);
				if (component != null && component.hasPosition()) {
					changed = true;
					teleporter.setStack(0, stack.split(1));
				} else {
					player.sendMessage(Text.translatable("text.simpleteleporters.error.unlinked_shard").formatted(Formatting.RED), true);
				}
			}
			if (changed) {
				world.setBlockState(pos, state.with(ENABLED, !teleporter.getStack(0).isEmpty()));
				world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
				var has = !teleporter.getStack(0).isEmpty();
				if (had && !has) player.playSound(SimpleTeleportersSoundEvents.TELEPORTER_CRYSTAL_REMOVED, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
				if (has) player.playSound(SimpleTeleportersSoundEvents.TELEPORTER_CRYSTAL_INSERTED, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
				return ItemActionResult.SUCCESS;
			}
		}
		return ItemActionResult.CONSUME;
	}

	@Override
	public void onEntityCollision(BlockState state, @NotNull World world, BlockPos blockPos, Entity entity) {
		if (!(world.getBlockEntity(blockPos) instanceof TeleporterBlockEntity teleporter) || teleporter.isCoolingDown() || !(entity instanceof ServerPlayerEntity player) || !entity.isSneaking()) return;
		teleporter.setCooldown(20);
		if (!teleporter.getStack(0).isEmpty()) {
			player.sendMessage(Text.translatable("text.simpleteleporters.error.no_crystal").formatted(Formatting.RED), true);
			return;
		}
		var location = teleporter.getLocation();
		if (location == null) return;
		if (!entity.getWorld().getRegistryKey().equals(location.getDimensionKey())) {
			player.sendMessage(Text.translatable("text.simpleteleporters.error.wrong_dimension").formatted(Formatting.RED), true);
			return;
		}
		var pos = location.getPosition();
		if (pos == null) {
			player.sendMessage(Text.translatable("text.simpleteleporters.error.unlinked_teleporter").formatted(Formatting.RED), true);
		} else if (world.getBlockState(pos).shouldSuffocate(world, pos)) {
			player.sendMessage(Text.translatable("text.simpleteleporters.error.invalid_position").formatted(Formatting.RED), true);
		} else {
			var playerPos = location.getPosition3D();
			if (playerPos == null) return;
			player.networkHandler.requestTeleport(playerPos.getX(), playerPos.getY(), playerPos.getZ(), entity.getYaw(), entity.getPitch());
			player.setVelocity(0, 0, 0);
			player.velocityModified = true;
			player.velocityDirty = true;
			world.playSound(null, player.getBlockPos(), SimpleTeleportersSoundEvents.TELEPORTER_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.playSound(null, pos, SimpleTeleportersSoundEvents.TELEPORTER_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScatterer.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, SimpleTeleportersBlocks.TELEPORTER_BLOCK_ENTITY, (world1, pos, state1, teleporter) -> TeleporterBlockEntity.tick(teleporter));
	}

	@Override
	public BlockState getPlacementState(@NotNull ItemPlacementContext ctx) {
		var fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid().equals(Fluids.WATER));
	}

	@Override
	protected BlockState getStateForNeighborUpdate(@NotNull BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected FluidState getFluidState(@NotNull BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public void randomDisplayTick(@NotNull BlockState state, World world, BlockPos pos, Random random) {
		if (!state.get(ENABLED)) return;
		for (var i = 0; i < 15; i++) {
			world.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.2F + (random.nextFloat() / 2), pos.getY() + 0.4F, pos.getZ() + 0.2F + (random.nextFloat() / 2), 0, random.nextFloat(), 0);
		}
	}

	@Override
	protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
		builder.add(WATERLOGGED);
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}
}