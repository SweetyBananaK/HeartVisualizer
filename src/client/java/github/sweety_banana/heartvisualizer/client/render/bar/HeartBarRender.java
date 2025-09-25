package github.sweety_banana.heartvisualizer.client.render.bar;

import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfig;
import github.sweety_banana.heartvisualizer.client.enums.HeartTypeEnum;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import static github.sweety_banana.heartvisualizer.client.util.Util.drawHeart;

@Environment(EnvType.CLIENT)
public class HeartBarRender {
    private static float heartSize = HeartVisualizerConfig.INSTANCE.heartSize;

    public HeartBarRender() {

    }

    public void renderBar(LivingEntity mainLivingEntityThing, MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider,
                          int heartsRed, int heartsNormal, int heartsTotal, boolean lastRedHalf, boolean lastYellowHalf, float maxX){
        Matrix4f model;
        RenderLayer renderLayer;
        Identifier heartTextureId;
        VertexConsumer vertexConsumer;
        String final_type;
        HeartTypeEnum type;
        float x;

        int rows = Math.min(MathHelper.ceil(heartsTotal / 10F), 4) - 1;

        for(int row = 0; row <= rows; row++){
            int hearts = (row == rows && heartsTotal % 10 != 0) ? heartsTotal % 10 : 10;
            matrixStack.push();
            matrixStack.translate(0f,heartSize / (rows+0.1f) * row,-0.1f * row);
            model = matrixStack.peek().getPositionMatrix();

            for (int heart = 0; heart < hearts; heart++) {
                x = maxX - heart * 8;

                final_type = HeartTypeEnum.EMPTY.getStatusIcon(mainLivingEntityThing);

                heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
                renderLayer = RenderLayer.getText(heartTextureId);
                vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                drawHeart(model, vertexConsumer, x, heartSize, 1f);

                // Create heart texture identifier
                if (heart < heartsRed - row * 10) {
                    type = HeartTypeEnum.RED_FULL;
                    if (heart == heartsRed - 1 && lastRedHalf) type = HeartTypeEnum.RED_HALF;
                } else if (heart < heartsNormal) {
                    type = HeartTypeEnum.EMPTY;
                } else {
                    type = HeartTypeEnum.YELLOW_FULL;
                    if(heart == heartsTotal - 1 && lastYellowHalf) type = HeartTypeEnum.YELLOW_HALF;
                }
                if (type != HeartTypeEnum.EMPTY){
                    final_type = type.getStatusIcon(mainLivingEntityThing);
                    heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");

                    // Get vertex consumer for this specific texture with appropriate render layer
                    renderLayer = RenderLayer.getText(heartTextureId);

                    vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    drawHeart(model, vertexConsumer, x, heartSize, 1f);
                }
            }
            matrixStack.pop();
        }
    }

    public static void updateConfig(){
        heartSize = HeartVisualizerConfig.INSTANCE.heartSize;
    }
}
