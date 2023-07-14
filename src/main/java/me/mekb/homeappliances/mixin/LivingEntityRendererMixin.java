package me.mekb.homeappliances.mixin;

import me.mekb.homeappliances.entity.ChairEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    private LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Unique
    private static final float snapAngle = 85.0f;

    @ModifyVariable(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "STORE"),
            ordinal = 2 // "h", body yaw
    )
    public float injectRenderBodyAngle(float value, T livingEntity, float f, float g) {
        if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof ChairEntity chairEntity) {
            // slightly modified from vanilla code
            float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
            float l = MathHelper.wrapDegrees(j - chairEntity.getYaw());
            if (l < -snapAngle)
                l = -snapAngle;
            if (l >= snapAngle)
                l = snapAngle;
            return j - l;
        }
        return value;
    }
}
