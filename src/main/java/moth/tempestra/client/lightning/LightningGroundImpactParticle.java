package moth.tempestra.client.lightning;

import net.minecraft.util.math.Vec3d;

public final class LightningGroundImpactParticle {
    private Vec3d position;
    private Vec3d previousPosition;
    private Vec3d velocity;
    private final double groundY;
    private final int lifetimeTicks;
    private final float radius;
    private final int color;
    private int ageTicks;

    public LightningGroundImpactParticle(
            Vec3d position,
            Vec3d velocity,
            double groundY,
            int lifetimeTicks,
            float radius,
            int color
    ) {
        this.position = position;
        this.previousPosition = position;
        this.velocity = velocity;
        this.groundY = groundY;
        this.lifetimeTicks = lifetimeTicks;
        this.radius = radius;
        this.color = color;
    }

    public void tick() {
        previousPosition = position;
        Vec3d next = position.add(velocity);
        if (next.y <= groundY && velocity.y < 0.0D) {
            next = new Vec3d(next.x, groundY, next.z);
            velocity = new Vec3d(velocity.x * 0.56D, -velocity.y * 0.32D + 0.012D, velocity.z * 0.56D);
        } else {
            velocity = new Vec3d(velocity.x * 0.84D, velocity.y - 0.024D, velocity.z * 0.84D);
        }
        position = next;
        ageTicks++;
    }

    public boolean isExpired() {
        return ageTicks >= lifetimeTicks;
    }

    public Vec3d interpolatedPosition(float tickDelta) {
        return previousPosition.lerp(position, tickDelta);
    }

    public Vec3d velocity() {
        return velocity;
    }

    public float ageProgress(float tickDelta) {
        return Math.min(1.0F, (ageTicks + tickDelta) / lifetimeTicks);
    }

    public float radius() {
        return radius;
    }

    public int color() {
        return color;
    }
}
