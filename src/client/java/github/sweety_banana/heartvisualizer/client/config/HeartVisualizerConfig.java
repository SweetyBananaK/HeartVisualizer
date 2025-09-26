package github.sweety_banana.heartvisualizer.client.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
@Config(name = "HeartVisualizer")
public class HeartVisualizerConfig implements ConfigData {
    public HeartCircle heartCircle = new HeartCircle();
    //public HeartCount heartCount = new HeartCount();

    public static HeartVisualizerConfig INSTANCE;

    public static void init() {
        INSTANCE = AutoConfig.getConfigHolder(HeartVisualizerConfig.class).getConfig();
        //INSTANCE.validate();
    }

    public void validate() {
        heartCircle.rotateDuration = MathHelper.clamp(heartCircle.rotateDuration, 1000, 10000);
        heartCircle.appearDuration = MathHelper.clamp(heartCircle.appearDuration, 100, 1000);
        heartCircle.disappearDuration = MathHelper.clamp(heartCircle.disappearDuration, 100, 1000);
        heartCircle.flashingDuration = MathHelper.clamp(heartCircle.flashingDuration, 500, 1000);
        heartSize = MathHelper.clamp(heartSize, 5, 15);

        if (displayType == null) {
            displayType = HeartDisplayType.HEART_CIRCLE;
        }
    }

    public boolean isActive = true;
    public HeartDisplayType displayType = HeartDisplayType.HEART_CIRCLE;
    public boolean isThrough = false;
    public float heartSize = 9F;

    public static class HeartCount {

    }

    public static class HeartCircle {
        public long rotateDuration = 5000;
        public long appearDuration = 300;
        public long disappearDuration = 300;
        public long flashingDuration = 1000;
        public boolean isFlashing = true;
    }

    public enum HeartDisplayType {
        HEART_CIRCLE("option.heartvisualizer.displaytype.heart_circle"),
        HEART_BAR("option.heartvisualizer.displaytype.heart_bar"),
        COUNT("option.heartvisualizer.displaytype.count");

        private final String translationKey;

        HeartDisplayType(String key) {
            this.translationKey = key;
        }

        public Text getDisplayName() {
            return Text.translatable(translationKey);
        }

        public static HeartDisplayType getEnumByTranslationKey(String key){
            for (HeartDisplayType type : values()) {
                if (type.translationKey.equals(key)) {
                    return type;
                }
            }
            return HEART_CIRCLE;
        }
    }
}
