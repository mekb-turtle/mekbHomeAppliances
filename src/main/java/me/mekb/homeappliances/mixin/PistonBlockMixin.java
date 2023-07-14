package me.mekb.homeappliances.mixin;

import me.mekb.homeappliances.block.ChairBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    private PistonBlockMixin() {}

    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void injectMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof ChairBlock) {
            // prevent moving chair block, causes buggy behavior otherwise
            cir.setReturnValue(false);
        }
    }
}
