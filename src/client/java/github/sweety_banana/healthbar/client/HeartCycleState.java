package github.sweety_banana.healthbar.client;

import java.util.ArrayList;
import java.util.List;

public class HeartCycleState {
    public boolean lastHurt = false;
    public float lastHitTime = 0;
    public float animationStartTime = 0;
    public float rotateEndTime = 0;
    public boolean active = false;
    public float scale = 0;
    public float offset = 0;
    public float age = 0;
    public int currentHeartCount = 0;
    public List<HeartInstance> hearts;
    public HeartCycleState(int currentHeartCount) {this.currentHeartCount = currentHeartCount;}
    public HeartCycleState() {}

    public void setHearts(int count) {
        if (this.hearts == null) {
            this.hearts = new ArrayList<>(count);
        }

        int current = this.hearts.size();

        if (current < count) {
            // 补足缺少的 HeartInstance
            for (int i = current; i < count; i++) {
                this.hearts.add(new HeartInstance());
            }
        } else if (current > count) {
            // 删除多余的
            this.hearts.subList(count, current).clear();
        }
    }

    public int getValidHearts() {
        if (hearts == null) return 0;
        int count = 0;
        for (HeartInstance heart : hearts) {
            if (heart.active || heart.breaking) {
                count++;
            }
        }
        return count;
    }

    public static class HeartInstance {
        public boolean active = true;         // 是否还在显示
        public float breakStartTime;    // 破碎开始时间
        public float breakProgress;    // 0~1 缩小进度
        public boolean breaking;       // 是否正在破碎动画中
    }
}
