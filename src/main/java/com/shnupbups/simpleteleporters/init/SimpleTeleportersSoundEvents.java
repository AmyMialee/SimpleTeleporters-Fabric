package com.shnupbups.simpleteleporters.init;

import com.shnupbups.simpleteleporters.SimpleTeleporters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public interface SimpleTeleportersSoundEvents {
	SoundEvent TELEPORTER_TELEPORT = register("block.teleporter.teleport");
	SoundEvent TELEPORTER_CRYSTAL_INSERTED = register("block.teleporter.crystal_inserted");
	SoundEvent TELEPORTER_CRYSTAL_REMOVED = register("block.teleporter.crystal_removed");
	SoundEvent ENDER_SHARD_LINK = register("item.ender_shard.link");

	static SoundEvent register(String name) {
		var id = SimpleTeleporters.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
}