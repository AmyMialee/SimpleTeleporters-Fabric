package com.shnupbups.simpleteleporters;

import com.google.common.reflect.Reflection;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersBlocks;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersItems;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersSoundEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SimpleTeleporters implements ModInitializer {
	public static final String MOD_ID = "simpleteleporters";

	@Override
	public void onInitialize() {
		Reflection.initialize(SimpleTeleportersBlocks.class);
		Reflection.initialize(SimpleTeleportersItems.class);
		Reflection.initialize(SimpleTeleportersSoundEvents.class);
	}

	public static @NotNull Identifier id(@NotNull String path) {
		return Identifier.of(MOD_ID, path);
	}
}