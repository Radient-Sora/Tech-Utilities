package abused_master.techutilities.tiles;

import abused_master.abusedlib.tiles.TileEntityEnergyBase;
import abused_master.abusedlib.utils.energy.EnergyStorage;
import abused_master.techutilities.registry.ModTiles;

public class TileEntityEnergyCrystal extends TileEntityEnergyBase {

    public EnergyStorage storage = new EnergyStorage(10000);

    public TileEntityEnergyCrystal() {
        super(ModTiles.ENERGY_CRYSTAL);
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return storage;
    }

    @Override
    public boolean isEnergyReceiver() {
        return true;
    }
}
