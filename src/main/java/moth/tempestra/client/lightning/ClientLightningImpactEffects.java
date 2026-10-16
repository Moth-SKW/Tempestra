package moth.tempestra.client.lightning;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

final class ClientLightningImpactEffects {
    private ClientLightningImpactEffects() {
    }

    static void start(ClientWorld world, LightningBoltVisualData data) {
        if (data.impactType() == LightningImpactType.COPPER) {
            emitElectricity(world, data, 11, 0);
            return;
        }
        emitGroundFragments(world, data);
        emitFire(world, data, 2, true, 0);
    }

    static void tick(ClientWorld world, LightningBoltVisualData data) {
        int age = data.ageTicks();
        if (data.impactType() == LightningImpactType.COPPER) {
            if (age <= 10 && age % 2 == 0) {
                emitElectricity(world, data, 3, age);
            }
            return;
        }

        if (age > 0 && age <= TempestraLightningVisuals.IMPACT_AFTERGLOW_TICKS && age % 5 == 0) {
            emitFire(world, data, 1, age % 10 == 0, age);
        }
    }

    private static void emitFire(ClientWorld world, LightningBoltVisualData data, int flameCount, boolean smoke, int phase) {
        Random random = random(data, phase, 0x4E4F524D414C464CL);
        ParticleEffect flame = data.impactType() == LightningImpactType.SOUL_FIRE
                ? ParticleTypes.SOUL_FIRE_FLAME
                : ParticleTypes.FLAME;
        Vec3d contact = data.contact().add(0.0D, 0.08D, 0.0D);
        for (int index = 0; index < flameCount; index++) {
            double angle = random.nextDouble(Math.PI * 2.0D);
            double spread = random.nextDouble(0.04D, 0.22D);
            addParticle(
                    world,
                    flame,
                    contact.x + Math.cos(angle) * spread,
                    contact.y + random.nextDouble(0.0D, 0.14D),
                    contact.z + Math.sin(angle) * spread,
                    Math.cos(angle) * random.nextDouble(0.004D, 0.016D),
                    random.nextDouble(0.018D, 0.055D),
                    Math.sin(angle) * random.nextDouble(0.004D, 0.016D)
            );
        }
        if (smoke) {
            addParticle(
                    world,
                    ParticleTypes.LARGE_SMOKE,
                    contact.x + random.nextDouble(-0.12D, 0.12D),
                    contact.y + random.nextDouble(0.04D, 0.16D),
                    contact.z + random.nextDouble(-0.12D, 0.12D),
                    random.nextDouble(-0.006D, 0.006D),
                    random.nextDouble(0.018D, 0.042D),
                    random.nextDouble(-0.006D, 0.006D)
            );
        }
    }

    private static void emitElectricity(ClientWorld world, LightningBoltVisualData data, int count, int phase) {
        Random random = random(data, phase, 0x434F50504552454CL);
        Vec3d contact = data.contact().add(0.0D, 0.12D, 0.0D);
        for (int index = 0; index < count; index++) {
            double angle = random.nextDouble(Math.PI * 2.0D);
            double speed = random.nextDouble(0.045D, 0.15D);
            addParticle(
                    world,
                    ParticleTypes.ELECTRIC_SPARK,
                    contact.x + random.nextDouble(-0.18D, 0.18D),
                    contact.y + random.nextDouble(0.0D, 0.28D),
                    contact.z + random.nextDouble(-0.18D, 0.18D),
                    Math.cos(angle) * speed,
                    random.nextDouble(0.025D, 0.16D),
                    Math.sin(angle) * speed
            );
        }
    }

    private static void emitGroundFragments(ClientWorld world, LightningBoltVisualData data) {
        BlockPos impactPos = BlockPos.ofFloored(data.contact().x, data.contact().y - 1.0E-6D, data.contact().z);
        if (!world.isChunkLoaded(impactPos)) {
            return;
        }
        BlockState state = world.getBlockState(impactPos);
        if (state.isAir() || state.isOf(Blocks.SOUL_FIRE)) {
            state = world.getBlockState(impactPos.down());
        }
        if (state.isAir()) {
            return;
        }

        Random random = random(data, 0, 0x4D414345494D5041L);
        ParticleEffect fragment = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        Vec3d contact = data.contact().add(0.0D, 0.22D, 0.0D);
        int count = data.fullBolt() ? 18 : 12;
        for (int index = 0; index < count; index++) {
            double angle = random.nextDouble(Math.PI * 2.0D);
            double speed = random.nextDouble(0.42D, 0.78D);
            addImportantParticle(
                    world,
                    fragment,
                    true,
                    contact.x + random.nextDouble(-0.22D, 0.22D),
                    contact.y + random.nextDouble(0.0D, 0.12D),
                    contact.z + random.nextDouble(-0.22D, 0.22D),
                    Math.cos(angle) * speed,
                    random.nextDouble(0.34D, 0.62D),
                    Math.sin(angle) * speed
            );
        }
    }

    private static Random random(LightningBoltVisualData data, int phase, long salt) {
        return new Random(data.seed() ^ salt ^ ((long) phase * 0x9E3779B97F4A7C15L));
    }

    private static void addParticle(
            ClientWorld world,
            ParticleEffect effect,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
    ) {
        //? >=1.21.11 {
        world.addParticleClient(effect, x, y, z, velocityX, velocityY, velocityZ);
        //?} else {
        /*world.addParticle(effect, x, y, z, velocityX, velocityY, velocityZ);
        *///?}
    }

    private static void addImportantParticle(
            ClientWorld world,
            ParticleEffect effect,
            boolean alwaysSpawn,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ
    ) {
        //? >=1.21.11 {
        world.addImportantParticleClient(effect, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
        //?} else {
        /*world.addImportantParticle(effect, alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
        *///?}
    }
}
