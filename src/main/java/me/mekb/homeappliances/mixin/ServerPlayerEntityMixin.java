package me.mekb.homeappliances.mixin;

import com.mojang.authlib.GameProfile;
import me.mekb.homeappliances.entity.ChairEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void disconnect(CallbackInfo ci) {
        if (getVehicle() instanceof ChairEntity) {
            // so we can set the seated blockstate back to false
            stopRiding();
        }
    }
}
