package github.sweety_banana.heartvisualizer.client.mixin;

import github.sweety_banana.heartvisualizer.client.render.cycle.HeartCycleHolder;
import github.sweety_banana.heartvisualizer.client.render.cycle.HeartCycleRender;
import github.sweety_banana.heartvisualizer.client.HeartVisualizerState;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, ServerWaypoint, HeartCycleHolder {
    @Shadow
    public abstract float getHealth();

    @Unique
    private final HeartCycleRender heartCycleRender = new HeartCycleRender(new HeartVisualizerState());

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType entityType, World world, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        this.heartCycleRender.getState().currentHealth = self.getHealth() + self.getAbsorptionAmount();
    }

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public HeartCycleRender heartVisualizer$getHeartCycleRender() {
        return this.heartCycleRender;
    }
}
