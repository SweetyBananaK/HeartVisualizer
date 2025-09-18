package github.sweety_banana.healthbar.client;

import github.sweety_banana.healthbar.client.config.HealthbarConfig;
import github.sweety_banana.healthbar.client.enums.HeartTypeEnum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class HeartCycleRender {
    private final HeartCycleState State;
    private final static long rotateDuration = HealthbarConfig.HeartRotateDuration;
    private final static long appearDuration = HealthbarConfig.appearDuration;
    private final static long disappearDuration = HealthbarConfig.disappearDuration;

    public void onDamage(int oldHearts, int newHearts) {
        for (int i = newHearts; i < oldHearts; i++) {
            HeartCycleState.HeartInstance heart = this.State.hearts.get(i);
            heart.breaking = true;
            heart.breakStartTime = System.currentTimeMillis();
        }
    }

    public void activeState(boolean active) {
        long time = System.currentTimeMillis();

        if (!this.State.active) {
            // 第一次触发
            this.State.animationStartTime = time;
            this.State.rotateEndTime = time + appearDuration + rotateDuration;
            this.State.active = active;
        } else {
            // 已经处于动画中
            long nowDelta = time - this.State.animationStartTime;

            if (nowDelta >= appearDuration && nowDelta < appearDuration + rotateDuration) {
                // 只在旋转阶段刷新
                this.State.rotateEndTime = time + rotateDuration;
            }
        }

        this.State.lastHitTime = time;
    }

    public HeartCycleRender(HeartCycleState State){
        this.State = State;
    }

    public void updateState(int heartCount){
        if (!this.State.active) return;
        this.State.setHearts(this.State.heartCount);
        onDamage(this.State.heartCount, heartCount);
        this.State.heartCount = heartCount;

        long now = System.currentTimeMillis();
        long delta = now - this.State.animationStartTime;

        for (HeartCycleState.HeartInstance heart : this.State.hearts) {
            if (heart.breaking) {
                //float t = (now - heart.breakStartTime) / (float) HealthbarConfig.heartBreakDuration;
                float t = (now - heart.breakStartTime) / 100f;
                if (t >= 1.0f) {
                    heart.active = false; // 彻底消失
                    heart.breaking = false;
                } else {
                    heart.breakProgress = t;
                }
            }
        }

        float targetOffset = 1.0f; // 你希望的最大外移距离（比如 1.0 block）
        // 1. 出现阶段
        if (delta < appearDuration) {
            float t = delta / (float)appearDuration;
            this.State.scale = t;
            this.State.offset = t * targetOffset;
        }
        // 2. 旋转阶段
        else if (now < this.State.rotateEndTime) {
            this.State.scale = 1.0f;
            this.State.offset = targetOffset;
        }
        // 3. 消失阶段
        else {
            long disappearStart = this.State.rotateEndTime;
            long disappearDelta = now - disappearStart;
            if (disappearDelta < disappearDuration) {
                float t = disappearDelta / (float)disappearDuration;
                this.State.scale = 1.0f - t;
                this.State.offset = targetOffset * (1.0f - t);
            } else {
                this.State.active = false;
            }
        }
    }

    public void renderCycle(LivingEntity mainLivingEntityThing, MatrixStack matrixStack,
                            LivingEntityRenderState livingEntityRenderState, EntityRenderDispatcher dispatcher,
                            VertexConsumerProvider vertexConsumerProvider){
        if(!this.State.active) return;
        RenderLayer renderLayer;
        Identifier heartTextureId;
        HeartTypeEnum type;
        String final_type;
        float opacity;
        boolean lastRedHalf = (this.State.heartCount & 1) == 1;

        for (int heart = 0; heart < this.State.heartCount; heart++) {
            type = HeartTypeEnum.RED_FULL;
            if (heart == this.State.heartCount - 1 && lastRedHalf) type = HeartTypeEnum.RED_HALF;

            final_type = type.getStatusIcon(mainLivingEntityThing);

            heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
            renderLayer = HealthbarConfig.isThrough ? RenderLayer.getTextSeeThrough(heartTextureId) : RenderLayer.getText(heartTextureId);

            double angle = 2 * Math.PI / this.State.heartCount * heart;
            double time = (double) (System.currentTimeMillis() % rotateDuration) / rotateDuration;
            angle += 2 * Math.PI * time;

            // 把 state.offset 融合进来
            float x = (float) (Math.cos(angle) * this.State.offset);
            float z = (float) (Math.sin(angle) * this.State.offset);

            matrixStack.push();
            matrixStack.translate(x, livingEntityRenderState.height*0.7f, z); // y=实体高度+偏移
            matrixStack.multiply(dispatcher.getRotation()); // 始终朝向摄像机
            float baseScale = 0.05f;
            float scale = baseScale * this.State.scale;
            opacity = 1F;

            if (this.State.hearts.get(heart).breaking) {
                scale *= (1.0f - this.State.hearts.get(heart).breakProgress);
                opacity = (1.0f - this.State.hearts.get(heart).breakProgress);
            }
            matrixStack.scale(-scale, scale, scale);

            // 绘制心贴图
            Matrix4f model = matrixStack.peek().getPositionMatrix();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            drawHeart(model, vertexConsumer, 9f/2f, 9f, opacity);

            matrixStack.pop();
        }
    }
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, float heartSize, float opacity){
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
