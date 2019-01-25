package abused_master.techutilities.client.render;

import abused_master.techutilities.tiles.crystal.BlockEntityEnergyCrystal;
import abused_master.techutilities.utils.render.RenderHelper;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.math.BlockPos;

public class CrystalRenderer extends BlockEntityRenderer<BlockEntityEnergyCrystal> {

    @Override
    public void render(BlockEntityEnergyCrystal tile, double x, double y, double z, float float_1, int int_1) {
        super.render(tile, x, y, z, float_1, int_1);
        if(tile.tilePositions.size() > 0) {
            for (BlockPos pos : tile.tilePositions) {
                RenderHelper.renderLaser(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 120, 0.35F, 0.07, new float[] {253 / 255f, 104 / 255f, 104 / 255f});
            }
        }
    }

    @Override
    public boolean method_3563(BlockEntityEnergyCrystal blockEntity_1) {
        return true;
    }
}
