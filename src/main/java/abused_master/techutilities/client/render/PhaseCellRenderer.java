package abused_master.techutilities.client.render;

import abused_master.abusedlib.client.render.hud.HudRender;
import abused_master.techutilities.tiles.machine.BlockEntityPhaseCell;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

public class PhaseCellRenderer extends BlockEntityRenderer<BlockEntityPhaseCell> {

    @Override
    public void render(BlockEntityPhaseCell tile, double x, double y, double z, float float_1, int int_1) {
        super.render(tile, x, y, z, float_1, int_1);
        HudRender.renderHud(tile, x, y, z);
    }

    @Override
    public boolean method_3563(BlockEntityPhaseCell blockEntity_1) {
        return true;
    }
}
