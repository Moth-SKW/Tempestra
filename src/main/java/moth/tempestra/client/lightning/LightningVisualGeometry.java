package moth.tempestra.client.lightning;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

final class LightningVisualGeometry {
    private static final double LIGHTNING_ROD_STRIKE_EPSILON = 1.0E-6D;

    private LightningVisualGeometry() {
    }

    static boolean canCreateVisualSplits(LightningEntity entity, WorldView world) {
        //? >=1.21.11 {
        return entity.getChanneler() == null && !strikesLightningRod(world, entity.getEntityPos());
        //?} else {
        /*return entity.getChanneler() == null && !strikesLightningRod(world, entity.getPos());
        *///?}
    }

    static long seed(LightningEntity entity, Vec3d contact, long worldTime) {
        long seed = entity.getUuid().getMostSignificantBits() ^ Long.rotateLeft(entity.getUuid().getLeastSignificantBits(), 17);
        seed ^= ((long) entity.getId()) * 0x9E3779B97F4A7C15L;
        seed ^= Double.doubleToLongBits(contact.x * 31.0D);
        seed ^= Long.rotateLeft(Double.doubleToLongBits(contact.y * 17.0D), 21);
        seed ^= Long.rotateLeft(Double.doubleToLongBits(contact.z * 13.0D), 42);
        seed ^= worldTime * 0xD1B54A32D192ED03L;
        return seed;
    }

    private static boolean strikesLightningRod(WorldView world, Vec3d pos) {
        if (world == null) {
            return false;
        }

        BlockPos affectedPos = BlockPos.ofFloored(pos.x, pos.y - LIGHTNING_ROD_STRIKE_EPSILON, pos.z);
        return world.isChunkLoaded(affectedPos) && world.getBlockState(affectedPos).isOf(Blocks.LIGHTNING_ROD);
    }
}
