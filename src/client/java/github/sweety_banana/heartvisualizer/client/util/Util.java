package github.sweety_banana.heartvisualizer.client.util;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public class Util {
    public static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, float heartSize, float opacity){
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
