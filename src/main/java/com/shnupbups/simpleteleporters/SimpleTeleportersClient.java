package com.shnupbups.simpleteleporters;

import com.shnupbups.simpleteleporters.init.SimpleTeleportersBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class SimpleTeleportersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), SimpleTeleportersBlocks.TELEPORTER);
	}
}