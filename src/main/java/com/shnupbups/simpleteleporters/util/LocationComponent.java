package com.shnupbups.simpleteleporters.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.shnupbups.simpleteleporters.init.SimpleTeleportersItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record LocationComponent(Optional<GlobalPos> target) {
    public static final Codec<LocationComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(GlobalPos.CODEC.optionalFieldOf("target").forGetter(LocationComponent::target)).apply(instance, LocationComponent::new));
    public static final PacketCodec<ByteBuf, LocationComponent> PACKET_CODEC = PacketCodec.tuple(GlobalPos.PACKET_CODEC.collect(PacketCodecs::optional), LocationComponent::target, LocationComponent::new);

    public boolean hasPosition() {
        return this.target().isPresent();
    }

    public @Nullable BlockPos getPosition() {
        return this.target().isPresent() ? this.target().get().pos() : null;
    }

    public @Nullable Vec3d getPosition3D() {
        var position = this.getPosition();
        if (position != null) return position.toCenterPos();
        return null;
    }

    public @NotNull String getDimensionName() {
        if (this.target().isPresent()) return this.target().get().dimension().getValue().toString();
        return World.OVERWORLD.getValue().toString();
    }

    public RegistryKey<World> getDimensionKey() {
        if (this.target().isPresent()) return this.target().get().dimension();
        return World.OVERWORLD;
    }

    public static boolean hasPosition(@NotNull ItemStack stack) {
        var component = stack.get(SimpleTeleportersItems.LOCATION);
        return component != null && component.hasPosition();
    }

    public static @Nullable BlockPos getPosition(@NotNull ItemStack stack) {
        var component = stack.get(SimpleTeleportersItems.LOCATION);
        return component != null ? component.getPosition() : null;
    }

    public static @Nullable Vec3d getPosition3D(@NotNull ItemStack stack) {
        var component = stack.get(SimpleTeleportersItems.LOCATION);
        return component != null ? component.getPosition3D() : null;
    }

    public static @NotNull String getDimensionName(@NotNull ItemStack stack) {
        var component = stack.get(SimpleTeleportersItems.LOCATION);
        return component != null ? component.getDimensionName() : World.OVERWORLD.getValue().toString();
    }

    public static @NotNull RegistryKey<World> getDimensionKey(@NotNull ItemStack stack) {
        var component = stack.get(SimpleTeleportersItems.LOCATION);
        return component != null ? component.getDimensionKey() : World.OVERWORLD;
    }
}