package github.sweety_banana.heartvisualizer.client.render.cycle;

import github.sweety_banana.heartvisualizer.client.HeartVisualizerState;
import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfig;
import github.sweety_banana.heartvisualizer.client.enums.HeartTypeEnum;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
public class HeartCycleRender {
    private final HeartVisualizerState State;
    private static float heartSize = HeartVisualizerConfig.INSTANCE.heartSize;
    private static float rotateDuration = HeartVisualizerConfig.INSTANCE.heartCircle.rotateDuration / 50f;
    private static float appearDuration = HeartVisualizerConfig.INSTANCE.heartCircle.appearDuration / 50f;
    private static float disappearDuration = HeartVisualizerConfig.INSTANCE.heartCircle.disappearDuration / 50f;
    private static float flashingDuration = HeartVisualizerConfig.INSTANCE.heartCircle.flashingDuration / 50f;

    public HeartCycleRender(HeartVisualizerState State){
        this.State = State;
    }

    public HeartVisualizerState getState(){
        return this.State;
    }

    public void onChange(int oldHearts, int newHearts, float time) {
        for (int i = newHearts; i < oldHearts; i++) {
            HeartVisualizerState.HeartInstance heart = this.State.hearts.get(i);
            heart.breaking = true;
            heart.changeStartTime = time;
        }
        for (int i = oldHearts; i < newHearts; i++){
            HeartVisualizerState.HeartInstance heart = this.State.hearts.get(i);
            heart.healing = true;
            heart.changeStartTime = time;
        }
        for (int i = 0; i < newHearts; i++){
            HeartVisualizerState.HeartInstance heart = this.State.hearts.get(i);
            heart.targetAngle = 2 * Math.PI / newHearts * i;
        }
    }

    public void activeState(float time, boolean active, float f_health) {
        int health = MathHelper.ceil(f_health);
        int currentHeart = MathHelper.ceil(MathHelper.ceil(this.State.currentHealth) / 2.0f);
        int newHeart = MathHelper.ceil(health / 2.0f);

        this.State.setHearts(Math.max(currentHeart, newHeart));
        onChange(currentHeart, newHeart, time);

        this.State.lastHitTime = time;
        if (!this.State.active) {
            // 第一次触发
            this.State.animationStartTime = time;
            this.State.rotateEndTime = time + appearDuration + rotateDuration;
            this.State.flashing = true;
            this.State.active = active;
            for (int heart = 0; heart < this.State.hearts.size(); heart++) {
                this.State.hearts.get(heart).currentAngle = 0;
                //this.State.hearts.get(heart).currentAngle = 2 * Math.PI / this.State.hearts.size() * heart;
            }
        } else {
            // 已经处于动画中
            float nowDelta = time - this.State.animationStartTime;

            if (nowDelta >= appearDuration && nowDelta < appearDuration + rotateDuration) {
                // 只在旋转阶段刷新
                this.State.rotateEndTime = time + rotateDuration;
                System.out.println("Current Time: " + time/20f + "   End Time: " + this.State.rotateEndTime/20f);
            }
        }
    }

    public void updateState(LivingEntityRenderState livingEntityRenderState, float f_health){
        if(f_health != -1) this.State.currentHealth = f_health;
        if (!this.State.active) return;


        float time = livingEntityRenderState.age;
        float delta = time - this.State.animationStartTime;

        for (HeartVisualizerState.HeartInstance heart : this.State.hearts) {
            if (heart.breaking) {
                float t = (time - heart.changeStartTime) / 15f;
                if (t >= 1.0f) {
                    heart.active = false; // 彻底消失
                    heart.breaking = false;
                } else {
                    heart.changeProgress = t;
                }
            }
            if (heart.healing) {
                float t = (time - heart.changeStartTime) / 15f;
                if (t >= 1.0f) {
                    heart.active = true;
                    heart.healing = false;
                } else {
                    heart.changeProgress = t;
                }
            }
            heart.currentAngle += (heart.targetAngle - heart.currentAngle) * 0.05;
        }

        float targetOffset = livingEntityRenderState.width + 0.3f; // 最大外移距离
        // 出现
        if (delta < appearDuration) {
            float t = delta / (float)appearDuration;
            this.State.scale = t;
            this.State.offset = t * targetOffset;
        }
        // 旋转
        else if (time < this.State.rotateEndTime) {
            this.State.scale = 1.0f;
            this.State.offset = targetOffset;
        }
        // 消失
        else {
            float disappearStart = this.State.rotateEndTime;
            float disappearDelta = time - disappearStart;
            if (disappearDelta < disappearDuration) {
                float t = disappearDelta / disappearDuration;
                this.State.scale = 1.0f - t;
                this.State.offset = targetOffset * (1.0f - t);
            } else {
                this.State.active = false;
            }
        }
        if (this.State.flashing && delta > flashingDuration){
            this.State.flashing = false;
        }
    }

    public void renderCycle(LivingEntity mainLivingEntityThing, MatrixStack matrixStack,
                            LivingEntityRenderState livingEntityRenderState, EntityRenderDispatcher dispatcher,
                            VertexConsumerProvider vertexConsumerProvider, boolean lastRedHalf, boolean lastYellowHalf, int heartsYellowStart, int heartsYellow){
        if(!this.State.active) return;
        RenderLayer renderLayer;
        Identifier heartTextureId;
        HeartTypeEnum type;
        String final_type;
        float opacity;
        int heartTotal = this.State.getValidHearts();

        for (int heart = 0; heart < heartTotal; heart++) {
            HeartVisualizerState.HeartInstance currentHeart = this.State.hearts.get(heart);

            type = HeartTypeEnum.RED_FULL;
            if (heart == heartsYellowStart - 1 && (lastRedHalf || currentHeart.breaking)) type = HeartTypeEnum.RED_HALF;
            if (heart >= heartsYellowStart && !currentHeart.breaking) type = HeartTypeEnum.YELLOW_FULL;
            if (heartsYellow != 0 && heart == heartTotal - 1 && (lastYellowHalf || currentHeart.breaking)) type = HeartTypeEnum.YELLOW_HALF;

            final_type = type.getStatusIcon(mainLivingEntityThing);

            heartTextureId = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + final_type + ".png");
            renderLayer = HeartVisualizerConfig.INSTANCE.isThrough ? RenderLayer.getTextSeeThrough(heartTextureId) : RenderLayer.getText(heartTextureId);

            double angle = currentHeart.currentAngle;
            double time = (livingEntityRenderState.age % 30) / 30;
            angle += 2 * Math.PI * time;

            float x = (float) (Math.cos(angle) * this.State.offset);
            float z = (float) (Math.sin(angle) * this.State.offset);

            matrixStack.push();
            matrixStack.translate(x, livingEntityRenderState.height*0.7f, z); // y=实体高度+偏移
            matrixStack.multiply(dispatcher.getRotation()); // 始终朝向摄像机
            float baseScale = 0.05f;
            float scale = baseScale * this.State.scale;
            opacity = 1F;
            if (this.State.flashing && heartTotal < 10){
                float frequency = 4.0f; // 每秒 4 次完整闪烁
                double t = livingEntityRenderState.age * (Math.PI * 2 * frequency / 20.0);

                double phaseOffset = (heart % 2 == 0) ? 0.0 : Math.PI;
                float wave = (float)((Math.sin(t + phaseOffset) + 1.0) / 2.0);
                float minOpacity = 0.5f;
                opacity *= minOpacity + (1.0f - minOpacity) * wave;
            }

            if (currentHeart.breaking) {
                float eased = (float)Math.sin(currentHeart.changeProgress * Math.PI * 0.5);
                scale *= (1.0f - eased);
                opacity *= (1.0f - eased);
            } else if(currentHeart.healing) {
                float eased = (float)Math.sin(currentHeart.changeProgress * Math.PI * 0.5);
                scale *= eased;
                opacity *= eased;
            }
            matrixStack.scale(-scale, scale, scale);

            // 绘制心贴图
            Matrix4f model = matrixStack.peek().getPositionMatrix();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            drawHeart(model, vertexConsumer, heartSize/2f, heartSize, opacity);

            matrixStack.pop();
        }
    }

    public static void updateConfig(){
        heartSize = HeartVisualizerConfig.INSTANCE.heartSize;
        rotateDuration = HeartVisualizerConfig.INSTANCE.heartCircle.rotateDuration / 50f;
        appearDuration = HeartVisualizerConfig.INSTANCE.heartCircle.appearDuration / 50f;
        disappearDuration = HeartVisualizerConfig.INSTANCE.heartCircle.disappearDuration / 50f;
        flashingDuration = HeartVisualizerConfig.INSTANCE.heartCircle.flashingDuration / 50f;
    }
}
