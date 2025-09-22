package github.sweety_banana.healthbar.client.mixin;

import github.sweety_banana.healthbar.client.HeartCycleRender;
import github.sweety_banana.healthbar.client.HeartCycleHolder;
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

//    @Unique
//    private final HeartCycleRender heartCycleRender = new HeartCycleRender(new HeartCycleState());

    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    protected abstract boolean hasLabel(T livingEntity, double d);

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
        double d = livingEntityRenderState.squaredDistanceToCamera;

        if (d > 250 && client.options.getPerspective().isFirstPerson()){
            return;
        } else if (d > 300){
            return;
        }

        int health = MathHelper.ceil(this.mainLivingEntityThing.getHealth());
        int maxHealth = MathHelper.ceil(this.mainLivingEntityThing.getMaxHealth());
        int healthYellow = MathHelper.ceil(this.mainLivingEntityThing.getAbsorptionAmount());

        int heartsRed = MathHelper.ceil(health / 2.0f);
        boolean lastRedHalf = (health & 1) == 1;
        int heartsNormal = MathHelper.ceil(maxHealth / 2.0f);
        int heartsYellow = MathHelper.ceil(healthYellow / 2.0f);
        boolean lastYellowHalf = (healthYellow & 1) == 1;
        int heartsTotal = heartsNormal + heartsYellow;
        float heartSize = 9F;

        int pixelsTotal = heartsTotal >= 10 ? 81 : heartsTotal * 8 + 1;
        float maxX = pixelsTotal / 2.0f;

        if(this.mainLivingEntityThing instanceof HeartCycleHolder){
            HeartCycleRender heartCycleRender = ((HeartCycleHolder)this.mainLivingEntityThing).healthBar$getHeartCycleRender();
            if (health != heartCycleRender.getState().currentHealth) {
                heartCycleRender.activeState(livingEntityRenderState.age,true, health);
            }
            heartCycleRender.updateState(livingEntityRenderState.age, health);
            heartCycleRender.renderCycle(this.mainLivingEntityThing, matrixStack, livingEntityRenderState, this.dispatcher, vertexConsumerProvider, lastRedHalf);
            heartCycleRender.getState().lastHurt = livingEntityRenderState.hurt;
        }




//        matrixStack.push();
//        try{
//            matrixStack.translate(0, livingEntityRenderState.height + 0.5f, 0);
//            float pixelSize = 0.025F;
//            matrixStack.multiply(this.dispatcher.getRotation());
//            matrixStack.scale(-pixelSize, pixelSize, pixelSize);
//
//            Matrix4f model;
//            RenderLayer renderLayer;
//            HeartTypeEnum type;
//            Identifier heartTextureId;
//            VertexConsumer vertexConsumer;
//            String final_type;
//            String HeartNum;
//            int textWidth;
//            float x;
//
//            if (heartsTotal > 40){
//                final_type = HeartTypeEnum.RED_FULL.getStatusIcon(mainLivingEntityThing);
//
//                heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
//                renderLayer = HealthbarConfig.isThrough ? RenderLayer.getTextSeeThrough(heartTextureId) : RenderLayer.getText(heartTextureId);
//
//                HeartNum = String.valueOf(heartsRed);
//                textWidth = this.client.textRenderer.getWidth(HeartNum);
//
//                matrixStack.push();
//                matrixStack.translate(0, 2.2f, 0);
//                matrixStack.scale(2.0f,2.0f,2.0f);
//                model = matrixStack.peek().getPositionMatrix();
//                vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
//                drawHeart(model, vertexConsumer, heartSize/2f, heartSize);
//                matrixStack.pop();
//
//                matrixStack.push();
//                matrixStack.translate(0, 0, 0.1f); // 把文字稍微往前挪，避免被心覆盖
//                matrixStack.scale(-0.6F, -0.6F, 1F); // 把坐标系缩放到能正常显示文字
//                Matrix4f textMatrix = matrixStack.peek().getPositionMatrix();
//                //VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
//
//                this.client.textRenderer.draw(
//                        String.valueOf(heartsRed),
//                        -textWidth/2f, 8,
//                        0xFFFFFFFF,
//                        false,
//                        textMatrix,
//                        vertexConsumerProvider,
//                        HealthbarConfig.isThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
//                        0, 15728880
//                        );
//                //immediate.draw();
//                matrixStack.pop();
//            } else {
//                int rows = Math.min(MathHelper.ceil(heartsTotal / 10F), 4) - 1;
//
//                for(int row = 0; row <= rows; row++){
//                    int hearts = (row == rows && heartsTotal % 10 != 0) ? heartsTotal % 10 : 10;
//                    matrixStack.push();
//                    matrixStack.translate(0f,heartSize / (rows+0.1f) * row,-0.1f * row);
//                    model = matrixStack.peek().getPositionMatrix();
//
//                    for (int heart = 0; heart < hearts; heart++) {
//                        x = maxX - heart * 8;
//
//                        final_type = HeartTypeEnum.EMPTY.getStatusIcon(mainLivingEntityThing);
//
//                        heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
//                        renderLayer = RenderLayer.getText(heartTextureId);
//                        vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
//                        drawHeart(model, vertexConsumer, x, heartSize);
//
//                        // Create heart texture identifier
//                        if (heart < heartsRed - row * 10) {
//                            type = HeartTypeEnum.RED_FULL;
//                            if (heart == heartsRed - 1 && lastRedHalf) type = HeartTypeEnum.RED_HALF;
//                        } else if (heart < heartsNormal) {
//                            type = HeartTypeEnum.EMPTY;
//                        } else {
//                            type = HeartTypeEnum.YELLOW_FULL;
//                            if(heart == heartsTotal - 1 && lastYellowHalf) type = HeartTypeEnum.YELLOW_HALF;
//                        }
//                        if (type != HeartTypeEnum.EMPTY){
//                            final_type = type.getStatusIcon(mainLivingEntityThing);
//                            heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
//
//                            // Get vertex consumer for this specific texture with appropriate render layer
//                            renderLayer = RenderLayer.getText(heartTextureId);
//
//                            vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
//                            drawHeart(model, vertexConsumer, x, heartSize);
//                        }
//                    }
//                    matrixStack.pop();
//                }
//            }
//        } catch (Exception e){
//            System.out.println(e.getMessage());
//            matrixStack.pop();
//        }
//        matrixStack.pop();
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, float heartSize){
        float opacity = 1F;
        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;


        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
    }

}
