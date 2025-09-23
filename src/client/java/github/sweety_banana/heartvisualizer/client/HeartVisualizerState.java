package github.sweety_banana.heartvisualizer.client;

import java.util.ArrayList;
import java.util.List;

public class HeartVisualizerState {
    public boolean lastHurt = false;
    public float lastHitTime = 0;
    public float animationStartTime = 0;
    public float rotateEndTime = 0;
    public boolean active = false;
    public boolean flashing = false;
    public float scale = 0;
    public float offset = 0;
    public float age = 0;
    public int currentHealth = 0;
    public List<HeartInstance> hearts;
    public HeartVisualizerState(int currentHealth) {this.currentHealth = currentHealth;}
    public HeartVisualizerState() {}

    public void setHearts(int heartsCount) {
        if (this.hearts == null) {
            this.hearts = new ArrayList<>(heartsCount);
        }

        int current = this.hearts.size();

        if (current < heartsCount) {
            // 补足缺少的 HeartInstance
            for (int i = current; i < heartsCount; i++) {
                this.hearts.add(new HeartInstance());
            }
        } else if (current > heartsCount) {
            // 删除多余的
            this.hearts.subList(heartsCount, current).clear();
        }
    }

    public int getValidHearts() {
        if (hearts == null) return 0;
        int count = 0;
        for (HeartInstance heart : hearts) {
            if (heart.active || heart.breaking || heart.healing) {
                count++;
            }
        }
        return count;
    }

    public static class HeartInstance {
        public boolean active = true;         // 是否还在显示
        public float changeStartTime;    // 破碎开始时间
        public float changeProgress;    // 0~1 缩小进度
        public boolean breaking;       // 是否正在破碎动画中
        public boolean healing;

        // 平滑过渡位置
        public double currentAngle = 0; // 当前渲染角度
        public double targetAngle;  // 目标角度
    }
}
