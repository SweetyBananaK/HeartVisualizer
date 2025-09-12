package github.sweety_banana.healthbar.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import github.sweety_banana.healthbar.client.HealthbarClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M> {
    @Unique
    private LivingEntity mainLivingEntityThing;

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
            at = @At("RETURN")
    )
    public void renderHealth(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Entity curent_entity = this.dispatcher.targetedEntity;

        if (livingEntityRenderState instanceof PlayerEntityRenderState && HealthbarClient.toggled && curent_entity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity) {
            matrixStack.push();
            double d = livingEntityRenderState.squaredDistanceToCamera;


            matrixStack.translate(0, livingEntityRenderState.height + 0.5f, 0);
            if(this.hasLabel((T) abstractClientPlayerEntity, d)){
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                if (d < 100.0 && abstractClientPlayerEntity.getScoreboard().getObjectiveForSlot(2) != null) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                }
            }

            matrixStack.multiply(this.dispatcher.getRotation());
//            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(mc.gameRenderer.getCamera().getPitch()));

            float pixelSize = 0.025F;
            matrixStack.scale(pixelSize, pixelSize, pixelSize);

            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder vertexConsumer = tessellator.getBuffer();

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//          RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
//            RenderSystem.enableDepthTest();

            Matrix4f model = matrixStack.peek().getPositionMatrix();

            int healthRed = MathHelper.ceil(abstractClientPlayerEntity.getHealth());
            int maxHealth = MathHelper.ceil(abstractClientPlayerEntity.getMaxHealth());
            int healthYellow = MathHelper.ceil(abstractClientPlayerEntity.getAbsorptionAmount());

            int heartsRed = MathHelper.ceil(healthRed / 2.0f);
            boolean lastRedHalf = (healthRed & 1) == 1;
            int heartsNormal = MathHelper.ceil(maxHealth / 2.0f);
            int heartsYellow = MathHelper.ceil(healthYellow / 2.0f);
            boolean lastYellowHalf = (healthYellow & 1) == 1;
            int heartsTotal = heartsNormal + heartsYellow;

            int pixelsTotal = heartsTotal * 8 + 1;
            float maxX = pixelsTotal / 2.0f;

            for (int heart = 0; heart < heartsTotal; heart++){
                float x = maxX - heart * 8;
                drawHeart(model, vertexConsumer, x, 0);
                // Offset in the gui icons texture in hearts
                // 0 - empty, 2 - red, 8 - yellow, +1 for half
                int type;
                if (heart < heartsRed) {
                    type = 2 * 2;
                    if (heart == heartsRed - 1 && lastRedHalf) type += 1;
                } else if (heart < heartsNormal) {
                    type = 0;
                } else {
                    type = 8 * 2;
                    if(heart == heartsTotal - 1 && lastYellowHalf) type += 1;
                }
                if (type != 0) {
                    drawHeart(model, vertexConsumer, x, type);
                }
            }

            tessellator.draw();

            matrixStack.pop();
        }
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, int type){
        float minU = 16F / 256F + type * 9F / 256F;
        float maxU = minU + 9F / 256F;
        float minV = 0;
        float maxV = minV + 9F / 256F;

        float heartSize = 9F;

        drawVertex(model, vertexConsumer, x, 0F - heartSize, 0F, minU, maxV);
        drawVertex(model, vertexConsumer, x - heartSize, 0F - heartSize, 0F, maxU, maxV);
        drawVertex(model, vertexConsumer, x - heartSize, 0F, 0F, maxU, minV);
        drawVertex(model, vertexConsumer, x, 0F, 0F, minU, minV);
    }

    @Unique
    private static void drawVertex(Matrix4f model, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(model, x, y, z).texture(u, v).next();
    }
}
