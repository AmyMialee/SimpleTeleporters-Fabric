package com.shnupbups.simpleteleporters.init;

import com.shnupbups.simpleteleporters.SimpleTeleporters;
import com.shnupbups.simpleteleporters.item.TeleportCrystalItem;
import com.shnupbups.simpleteleporters.util.LocationComponent;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface SimpleTeleportersItems {
	ComponentType<LocationComponent> LOCATION = register("location", builder -> builder.codec(LocationComponent.CODEC).packetCodec(LocationComponent.PACKET_CODEC).cache());

	Item ENDER_SHARD = register("ender_shard", new TeleportCrystalItem(new Item.Settings().component(LOCATION, new LocationComponent(Optional.empty())).maxCount(16)), ItemGroups.TOOLS);

	@SafeVarargs
    static Item register(String name, Item item, RegistryKey<ItemGroup> @NotNull ... groups) {
		for (var group : groups) ItemGroupEvents.modifyEntriesEvent(group).register((entries) -> entries.add(item));
		return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), SimpleTeleporters.id(name)), item);
	}

	static <T> ComponentType<T> register(String name, @NotNull UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, name, builderOperator.apply(ComponentType.builder()).build());
	}
}