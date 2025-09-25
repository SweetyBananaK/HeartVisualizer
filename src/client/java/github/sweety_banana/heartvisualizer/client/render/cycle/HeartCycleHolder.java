package github.sweety_banana.heartvisualizer.client.render.cycle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface HeartCycleHolder {
    public HeartCycleRender heartVisualizer$getHeartCycleRender();
}
