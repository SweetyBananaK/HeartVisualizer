package github.sweety_banana.heartvisualizer.client.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "HeartVisualizer")
public class HeartVisualizerConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public HeartCircle heartCircle = new HeartCircle();

    public static HeartVisualizerConfig INSTANCE;

    public static void init() {
        INSTANCE = AutoConfig.getConfigHolder(HeartVisualizerConfig.class).getConfig();
    }

    public boolean isThrough = false;

    public static class HeartCircle {
        public long rotateDuration = 5000;
        public long appearDuration = 300;
        public long disappearDuration = 300;
        public long flashingDuration = 1000;
    }
}
