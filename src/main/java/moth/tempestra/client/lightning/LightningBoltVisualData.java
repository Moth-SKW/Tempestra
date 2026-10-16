package moth.tempestra.client.lightning;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class LightningBoltVisualData {
    private final int entityId;
    private final long seed;
    private final Vec3d origin;
    private final Vec3d contact;
    private final List<LightningPath> paths;
    private final List<LightningImpactRay> impactRays;
    private final List<LightningSpark> sparks;
    private final List<LightningGroundImpactParticle> groundImpactParticles;
    private final LightningImpactType impactType;
    private final boolean fullBolt;
    private final float intensity;
    private final int lifetimeTicks;
    private int ageTicks;
    private long lastSeenWorldTime;

    public LightningBoltVisualData(
            int entityId,
            long seed,
            Vec3d origin,
            Vec3d contact,
            List<LightningPath> paths,
            List<LightningImpactRay> impactRays,
            List<LightningSpark> sparks,
            List<LightningGroundImpactParticle> groundImpactParticles,
            LightningImpactType impactType,
            boolean fullBolt,
            float intensity,
            int lifetimeTicks,
            long worldTime
    ) {
        this.entityId = entityId;
        this.seed = seed;
        this.origin = origin;
        this.contact = contact;
        this.paths = List.copyOf(paths);
        this.impactRays = List.copyOf(impactRays);
        this.sparks = new ArrayList<>(sparks);
        this.groundImpactParticles = new ArrayList<>(groundImpactParticles);
        this.impactType = impactType;
        this.fullBolt = fullBolt;
        this.intensity = intensity;
        this.lifetimeTicks = lifetimeTicks;
        this.lastSeenWorldTime = worldTime;
    }

    public void tick() {
        ageTicks++;
        Iterator<LightningSpark> iterator = sparks.iterator();
        while (iterator.hasNext()) {
            LightningSpark spark = iterator.next();
            spark.tick();
            if (spark.isExpired()) {
                iterator.remove();
            }
        }
        Iterator<LightningGroundImpactParticle> groundParticleIterator = groundImpactParticles.iterator();
        while (groundParticleIterator.hasNext()) {
            LightningGroundImpactParticle particle = groundParticleIterator.next();
            particle.tick();
            if (particle.isExpired()) {
                groundParticleIterator.remove();
            }
        }
    }

    public boolean isExpired() {
        return ageTicks > Math.max(lifetimeTicks, TempestraLightningVisuals.IMPACT_AFTERGLOW_TICKS)
                && sparks.isEmpty()
                && groundImpactParticles.isEmpty();
    }

    public void markSeen(long worldTime) {
        lastSeenWorldTime = worldTime;
    }

    public int entityId() {
        return entityId;
    }

    public long seed() {
        return seed;
    }

    public Vec3d origin() {
        return origin;
    }

    public Vec3d contact() {
        return contact;
    }

    public List<LightningPath> paths() {
        return paths;
    }

    public List<LightningImpactRay> impactRays() {
        return impactRays;
    }

    public List<LightningSpark> sparks() {
        return sparks;
    }

    public List<LightningGroundImpactParticle> groundImpactParticles() {
        return groundImpactParticles;
    }

    public LightningImpactType impactType() {
        return impactType;
    }

    public boolean fullBolt() {
        return fullBolt;
    }

    public float intensity() {
        return intensity;
    }

    public int ageTicks() {
        return ageTicks;
    }

    public float renderAge(float tickDelta) {
        return ageTicks + tickDelta;
    }

    public float lifetimeProgress(float tickDelta) {
        return Math.min(1.0F, renderAge(tickDelta) / lifetimeTicks);
    }

    public long lastSeenWorldTime() {
        return lastSeenWorldTime;
    }
}
