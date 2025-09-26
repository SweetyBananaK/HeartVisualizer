package github.sweety_banana.heartvisualizer.client.render.count;

import github.sweety_banana.heartvisualizer.client.HeartVisualizerState;
import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfig;
import github.sweety_banana.heartvisualizer.client.enums.HeartTypeEnum;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import static github.sweety_banana.heartvisualizer.client.util.Util.drawHeart;

@Environment(EnvType.CLIENT)
public class HeartCountRender {
    private static float heartSize = HeartVisualizerConfig.INSTANCE.heartSize;

    public HeartCountRender() {

    }

    public void renderCount(LivingEntity mainLivingEntityThing, MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider, int health, MinecraftClient client){
        Matrix4f model;
        RenderLayer renderLayer;
        Identifier heartTextureId;
        VertexConsumer vertexConsumer;
        String final_type;
        String HeartNum;
        int textWidth;

        final_type = HeartTypeEnum.RED_FULL.getStatusIcon(mainLivingEntityThing);

        heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
        renderLayer = HeartVisualizerConfig.INSTANCE.isThrough ? RenderLayer.getTextSeeThrough(heartTextureId) : RenderLayer.getText(heartTextureId);

        HeartNum = String.valueOf(health);
        textWidth = client.textRenderer.getWidth(HeartNum);

        matrixStack.push();
        matrixStack.translate(0, 2.2f, 0);
        matrixStack.scale(2.0f,2.0f,2.0f);
        model = matrixStack.peek().getPositionMatrix();
        vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
        drawHeart(model, vertexConsumer, heartSize/2f, heartSize, 1f);
        matrixStack.pop();

        matrixStack.push();
        matrixStack.translate(0, 0, 0.1f);
        matrixStack.scale(-0.6F*heartSize/9f, -0.6F*heartSize/9f, heartSize/9f);
        Matrix4f textMatrix = matrixStack.peek().getPositionMatrix();

        client.textRenderer.draw(
                String.valueOf(health),
                -textWidth/2f, heartSize/2f,
                0xFFFFFFFF,
                false,
                textMatrix,
                vertexConsumerProvider,
                HeartVisualizerConfig.INSTANCE.isThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
                0, 15728880
        );
        matrixStack.pop();
    }

    public static void updateConfig(){
        heartSize = HeartVisualizerConfig.INSTANCE.heartSize;
    }
}
