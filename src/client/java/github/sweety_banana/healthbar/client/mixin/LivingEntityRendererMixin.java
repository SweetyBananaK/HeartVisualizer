package github.sweety_banana.healthbar.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import github.sweety_banana.healthbar.Healthbar;
import github.sweety_banana.healthbar.client.HealthbarClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import github.sweety_banana.healthbar.client.enums.HeartTypeEnum;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M> {
    @Unique
    private LivingEntity mainLivingEntityThing;

    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    protected abstract boolean hasLabel(T livingEntity, double d);

    public LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci){
        mainLivingEntityThing = livingEntity;
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    public void renderHealth(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (HealthbarClient.toggled) {
            double d = livingEntityRenderState.squaredDistanceToCamera;

            if (d > 50 && client.options.getPerspective().isFirstPerson()){
                return;
            } else if (d > 100){
                return;
            }

            int healthRed = MathHelper.ceil(this.mainLivingEntityThing.getHealth());
            int maxHealth = MathHelper.ceil(this.mainLivingEntityThing.getMaxHealth());
            int healthYellow = MathHelper.ceil(this.mainLivingEntityThing.getAbsorptionAmount());

            int heartsRed = MathHelper.ceil(healthRed / 2.0f);
            boolean lastRedHalf = (healthRed & 1) == 1;
            int heartsNormal = MathHelper.ceil(maxHealth / 2.0f);
            int heartsYellow = MathHelper.ceil(healthYellow / 2.0f);
            boolean lastYellowHalf = (healthYellow & 1) == 1;
            int heartsTotal = heartsNormal + heartsYellow;

            int pixelsTotal = heartsTotal * 8 + 1;
            float maxX = pixelsTotal / 2.0f;

            matrixStack.push();
            try{
                matrixStack.translate(0, livingEntityRenderState.height + 0.5f, 0);
                float pixelSize = 0.025F;
                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-pixelSize, pixelSize, pixelSize);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                RenderLayer renderLayer;
                HeartTypeEnum type;

                for (int heart = 0; heart < heartsTotal; heart++) {
                    float x = maxX - heart * 8;

                    String final_type = HeartTypeEnum.EMPTY.getStatusIcon(mainLivingEntityThing);

                    Identifier heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/container.png");
                    renderLayer = RenderLayer.getText(heartTextureId);
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    drawHeart(model, vertexConsumer, x);

                    // Create heart texture identifier
                    if (heart < heartsRed) {
                        type = HeartTypeEnum.RED_FULL;
                        if (heart == heartsRed - 1 && lastRedHalf) type = HeartTypeEnum.RED_HALF;
                    } else if (heart < heartsNormal) {
                        type = HeartTypeEnum.EMPTY;
                    } else {
                        type = HeartTypeEnum.YELLOW_FULL;
                        if(heart == heartsTotal - 1 && lastYellowHalf) type = HeartTypeEnum.YELLOW_HALF;
                    }
                    final_type = type.getStatusIcon(mainLivingEntityThing);
                    heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + type.icon + ".png");

                    // Get vertex consumer for this specific texture with appropriate render layer
                    renderLayer = RenderLayer.getText(heartTextureId);

                    vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    drawHeart(model, vertexConsumer, x);

//                    if (type != HeartTypeEnum.EMPTY){
//
//                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                matrixStack.pop();
            }
            matrixStack.pop();
        }
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x){
        float opacity = 1F;
        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;
        float heartSize = 9F;

        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
    }

//    @Unique
//    private static void drawVertex(Matrix4f model, VertexConsumer vertices, float x, float y, float z, float u, float v) {
//        vertices.vertex(model, x, y, z).texture(u, v);
//    }
}
