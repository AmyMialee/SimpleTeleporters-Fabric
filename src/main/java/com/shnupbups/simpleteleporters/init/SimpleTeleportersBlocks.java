package com.shnupbups.simpleteleporters.init;

import com.shnupbups.simpleteleporters.SimpleTeleporters;
import com.shnupbups.simpleteleporters.block.TeleporterBlock;
import com.shnupbups.simpleteleporters.block.TeleporterBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public interface SimpleTeleportersBlocks {
	Block TELEPORTER = register("teleporter", new TeleporterBlock(AbstractBlock.Settings.create().nonOpaque().hardness(1).resistance(1).luminance((s) -> 15)));

	BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = register("teleporter", BlockEntityType.Builder.create(TeleporterBlockEntity::new, SimpleTeleportersBlocks.TELEPORTER));

	static Block register(String name, Block block) {
		SimpleTeleportersItems.register(name, new BlockItem(block, new Item.Settings()), ItemGroups.FUNCTIONAL);
		return Registry.register(Registries.BLOCK, SimpleTeleporters.id(name), block);
	}

	static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.@NotNull Builder<T> builder) {
        var type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, name);
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, name, builder.build(type));
	}
}