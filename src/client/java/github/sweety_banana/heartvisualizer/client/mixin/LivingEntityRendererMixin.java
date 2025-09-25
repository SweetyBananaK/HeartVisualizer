package github.sweety_banana.heartvisualizer.client.mixin;

import github.sweety_banana.heartvisualizer.client.render.bar.HeartBarRender;
import github.sweety_banana.heartvisualizer.client.render.count.HeartCountRender;
import github.sweety_banana.heartvisualizer.client.render.cycle.HeartCycleRender;
import github.sweety_banana.heartvisualizer.client.render.cycle.HeartCycleHolder;
import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M> {
    @Unique
    private LivingEntity mainLivingEntityThing;

    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    public LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci){
        this.mainLivingEntityThing = livingEntity;
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    public void renderHeart(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
//        if (!HealthbarClient.toggled || mainLivingEntityThing == client.player) {
//            return;
//        }
//        if (mainLivingEntityThing != client.player) {
//            return;
//        }
        if (!HeartVisualizerConfig.INSTANCE.isActive){
            return;
        }
        double d = livingEntityRenderState.squaredDistanceToCamera;

        if (d > 250 && client.options.getPerspective().isFirstPerson()){
            return;
        } else if (d > 300){
            return;
        }
        float f_health = this.mainLivingEntityThing.getHealth();
        float f_healthYellow = this.mainLivingEntityThing.getAbsorptionAmount();

        int health = MathHelper.ceil(f_health);
        int maxHealth = MathHelper.ceil(this.mainLivingEntityThing.getMaxHealth());
        int healthYellow = MathHelper.ceil(f_healthYellow);

        int heartsRed = MathHelper.ceil(health / 2.0f);
        boolean lastRedHalf = (health & 1) == 1;
        int heartsNormal = MathHelper.ceil(maxHealth / 2.0f);
        int heartsYellow = MathHelper.ceil(healthYellow / 2.0f);
        boolean lastYellowHalf = (healthYellow & 1) == 1;
        int heartsTotal = heartsNormal + heartsYellow;

        int pixelsTotal = heartsTotal >= 10 ? 81 : heartsTotal * 8 + 1;
        float maxX = pixelsTotal / 2.0f;

        matrixStack.push();
        try {
            if (HeartVisualizerConfig.INSTANCE.displayType == HeartVisualizerConfig.HeartDisplayType.HEART_CIRCLE && this.mainLivingEntityThing instanceof HeartCycleHolder) {
                HeartCycleRender heartCycleRender = ((HeartCycleHolder) this.mainLivingEntityThing).heartVisualizer$getHeartCycleRender();
                if ((f_health + f_healthYellow) != heartCycleRender.getState().currentHealth) {
                    System.out.println("Cycle active..." + f_health + f_healthYellow);
                    heartCycleRender.activeState(livingEntityRenderState.age, true, f_health + f_healthYellow);
                }
                heartCycleRender.updateState(livingEntityRenderState, f_health + f_healthYellow);
                heartCycleRender.renderCycle(this.mainLivingEntityThing, matrixStack, livingEntityRenderState, this.dispatcher, vertexConsumerProvider, lastRedHalf, lastYellowHalf, heartsRed, heartsYellow);
                heartCycleRender.getState().lastHurt = livingEntityRenderState.hurt;
            } else {
                matrixStack.translate(0, livingEntityRenderState.height + 0.5f, 0);
                float pixelSize = 0.025F;
                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-pixelSize, pixelSize, pixelSize);

                if (heartsTotal > 40 || HeartVisualizerConfig.INSTANCE.displayType == HeartVisualizerConfig.HeartDisplayType.COUNT) {
                    HeartCountRender heartCountRender = new HeartCountRender();
                    heartCountRender.renderCount(this.mainLivingEntityThing, matrixStack, vertexConsumerProvider, health, this.client);
                } else if (HeartVisualizerConfig.INSTANCE.displayType == HeartVisualizerConfig.HeartDisplayType.HEART_BAR) {
                    HeartBarRender heartBarRender = new HeartBarRender();
                    heartBarRender.renderBar(this.mainLivingEntityThing, matrixStack, vertexConsumerProvider, heartsRed, heartsNormal, heartsTotal, lastRedHalf, lastYellowHalf, maxX);
                }
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            matrixStack.pop();
        }
        matrixStack.pop();
    }
}
