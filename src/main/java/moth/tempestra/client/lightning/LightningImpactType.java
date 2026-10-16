package moth.tempestra.client.lightning;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BlockTags;
//? <1.21.11 {
/*import net.minecraft.registry.Registries;
*///?}
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public enum LightningImpactType {
    NORMAL,
    SOUL_FIRE,
    COPPER;

    public static LightningImpactType at(ClientWorld world, Vec3d contact) {
        BlockPos impactPos = BlockPos.ofFloored(contact.x, contact.y - 1.0E-6D, contact.z);
        if (!world.isChunkLoaded(impactPos)) {
            return NORMAL;
        }

        BlockState state = world.getBlockState(impactPos);
        if (isCopper(state)) {
            return COPPER;
        }
        if (state.isOf(Blocks.SOUL_FIRE)
                || state.isIn(BlockTags.SOUL_FIRE_BASE_BLOCKS)
                || world.getBlockState(impactPos.up()).isOf(Blocks.SOUL_FIRE)) {
            return SOUL_FIRE;
        }
        return NORMAL;
    }

    private static boolean isCopper(BlockState state) {
        //? >=1.21.11 {
        return state.isIn(BlockTags.COPPER);
        //?} else {
        /*return Registries.BLOCK.getId(state.getBlock()).getPath().contains("copper");
        *///?}
    }
}
