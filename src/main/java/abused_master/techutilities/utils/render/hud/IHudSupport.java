package abused_master.techutilities.utils.render.hud;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * Credits to McJty for this
 */
public interface IHudSupport {

    Direction getBlockOrientation();
    boolean isBlockAboveAir();
    List<String> getClientLog();
    BlockPos getBlockPos();
}