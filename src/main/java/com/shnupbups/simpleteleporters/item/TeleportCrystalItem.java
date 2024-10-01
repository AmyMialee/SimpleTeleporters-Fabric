package com.shnupbups.simpleteleporters.item;

import com.shnupbups.simpleteleporters.init.SimpleTeleportersBlocks;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersItems;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersSoundEvents;
import com.shnupbups.simpleteleporters.util.LocationComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TeleportCrystalItem extends Item {
	public TeleportCrystalItem(Settings settings) {
		super(settings);
	}

	@Override @Environment(EnvType.CLIENT)
	public void inventoryTick(ItemStack stack, @NotNull World world, Entity entity, int slot, boolean selected) {
		if (!world.isClient || !selected || entity != MinecraftClient.getInstance().player) return;
		var component = stack.get(SimpleTeleportersItems.LOCATION);
		if (component == null || !world.getRegistryKey().equals(component.getDimensionKey())) return;
		var position = component.getPosition();
		if (position == null) return;
		if (world.getBlockState(position.down()).isOf(SimpleTeleportersBlocks.TELEPORTER)) position = position.down();
		if (entity.getBlockPos().getManhattanDistance(position) >= 15) return;
		world.addParticle(ParticleTypes.MYCELIUM,
				world.getRandom().nextTriangular(position.getX() + 0.5, 0.2),
				world.getRandom().nextTriangular(position.getY() + 0.5, 0.2),
				world.getRandom().nextTriangular(position.getZ() + 0.5, 0.2),
				0, 0, 0);
	}

	@Override
	public ActionResult useOnBlock(@NotNull ItemUsageContext ctx) {
		if (!ctx.shouldCancelInteraction()) return ActionResult.PASS;
		var player = ctx.getPlayer();
		if (player == null) return ActionResult.PASS;
		var world = ctx.getWorld();
		var pos = ctx.getBlockPos();
		if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
			pos = world.getBlockState(pos).isOf(SimpleTeleportersBlocks.TELEPORTER) ? pos.up() : pos.offset(ctx.getSide());
		}
		var component = new LocationComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), pos)));
		var stack = ctx.getStack().split(1);
		stack.set(SimpleTeleportersItems.LOCATION, component);
		if (!player.giveItemStack(stack)) player.dropItem(stack, false);
		player.sendMessage(Text.translatable("text.simpleteleporters.crystal_info",
						pos.getX(), pos.getY(), pos.getZ(), component.getDimensionName())
				.formatted(Formatting.GREEN), true);
		player.playSound(SimpleTeleportersSoundEvents.ENDER_SHARD_LINK, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(@NotNull ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		var component = stack.get(SimpleTeleportersItems.LOCATION);
		if (component != null) {
			var pos = component.getPosition();
			if (pos != null) {
				tooltip.add(Text.translatable("text.simpleteleporters.linked", pos.getX(), pos.getY(), pos.getZ(), component.getDimensionName()).formatted(Formatting.GREEN));
				return;
			}
		}
		tooltip.add(Text.translatable("text.simpleteleporters.unlinked").formatted(Formatting.RED));
		tooltip.add(Text.translatable("text.simpleteleporters.how_to_link", Text.keybind("key.sneak"), Text.keybind("key.use")).formatted(Formatting.BLUE));
	}
}