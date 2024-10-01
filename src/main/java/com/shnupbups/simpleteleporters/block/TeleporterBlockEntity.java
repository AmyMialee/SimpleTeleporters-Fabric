package com.shnupbups.simpleteleporters.block;

import com.shnupbups.simpleteleporters.init.SimpleTeleportersBlocks;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersItems;
import com.shnupbups.simpleteleporters.util.LocationComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class TeleporterBlockEntity extends BlockEntity implements Inventory {
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private int cooldown = 0;

	public TeleporterBlockEntity(BlockPos pos, BlockState state) {
		super(SimpleTeleportersBlocks.TELEPORTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {
		for (var itemStack : this.inventory) if (!itemStack.isEmpty()) return false;
		return true;
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
        var itemStack = Inventories.splitStack(this.inventory, slot, amount);
		if (!itemStack.isEmpty()) this.markDirty();
		return itemStack;
	}

	@Override
	public ItemStack removeStack(int slot) {
		var itemStack = Inventories.removeStack(this.inventory, slot);
		if (!itemStack.isEmpty()) this.markDirty();
		return itemStack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		stack.capCount(this.getMaxCount(stack));
		this.markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return Inventory.canPlayerUse(this, player);
	}

	@Override
	public void clear() {
		this.inventory.clear();
	}

	public boolean isCoolingDown() {
		return cooldown > 0;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public LocationComponent getLocation() {
        return this.getStack(0).get(SimpleTeleportersItems.LOCATION);
	}

	public static void tick(@NotNull TeleporterBlockEntity teleporter) {
		if (teleporter.isCoolingDown()) teleporter.cooldown--;
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.inventory.clear();
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.cooldown = nbt.getInt("cooldown");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		nbt.putInt("cooldown", cooldown);
	}
}