package abused_master.techutilities.tiles.machine;

import abused_master.techutilities.blocks.machines.BlockMobGrinder;
import abused_master.techutilities.registry.ModBlockEntities;
import abused_master.techutilities.tiles.BlockEntityEnergy;
import abused_master.techutilities.utils.energy.EnergyStorage;
import abused_master.techutilities.utils.energy.IEnergyReceiver;
import abused_master.techutilities.utils.render.hud.IHudSupport;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;

public class BlockEntityMobGrinder extends BlockEntityEnergy implements IEnergyReceiver, IHudSupport {

    public EnergyStorage storage = new EnergyStorage(100000);
    public BoundingBox mobKillBox = null;
    public int killTimer = 0;
    public int costPerHeart = 50;

    public BlockEntityMobGrinder() {
        super(ModBlockEntities.MOB_GRINDER);
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        this.storage.readFromNBT(nbt);
        killTimer = nbt.getInt("killTimer");
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);
        this.storage.writeEnergyToNBT(nbt);
        nbt.putInt("killTimer", this.killTimer);
        return nbt;
    }

    @Override
    public void tick() {
        if(mobKillBox == null) {
            updateOrientation();
        }

        if(storage.getEnergyStored() >= costPerHeart) {
            killTimer++;
            if (killTimer >= 20) {
                killMobs();
            }
        }
    }

    public void killMobs() {
        LivingEntity target = getTarget();

        if(target == null) {
            return;
        }

        int totalCost = (int) (target.getHealth() * costPerHeart);
        if(storage.getEnergyStored() >= totalCost) {
            target.damage(DamageSource.MAGIC, target.getHealth());
            storage.extractEnergy(totalCost);
        }else {
            target.damage(DamageSource.GENERIC, 1);
            storage.extractEnergy(costPerHeart);
        }
        killTimer = 0;
    }

    public LivingEntity getTarget() {
        List<LivingEntity> entitiesInRange = world.getVisibleEntities(LivingEntity.class, mobKillBox);

        for (LivingEntity entity : entitiesInRange) {
            if(entity != null && !(entity instanceof PlayerEntity) && !entity.isInvulnerable()) {
                return entity;
            }
        }

        return null;
    }

    public void updateOrientation() {
        Direction direction = world.getBlockState(pos).get(BlockMobGrinder.FACING);
        BlockPos pos1 = pos.add(-3, -3, -3).add(direction.getOffsetX() * 4, 0, direction.getOffsetZ() * 4);
        BlockPos pos2 = pos.add(4, 4, 4).add(direction.getOffsetX() * 4, 0, direction.getOffsetZ() * 4);
        mobKillBox = new BoundingBox(pos1, pos2);
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return storage;
    }

    @Override
    public boolean receiveEnergy(int amount) {
        return handleEnergyReceive(storage, amount);
    }

    @Override
    public Direction getBlockOrientation() {
        return null;
    }

    @Override
    public boolean isBlockAboveAir() {
        return getWorld().isAir(pos.up());
    }

    @Override
    public List<String> getClientLog() {
        return Collections.singletonList("Energy: " + storage.getEnergyStored() + " / " + storage.getEnergyCapacity() + " PE");
    }

    @Override
    public BlockPos getBlockPos() {
        return getPos();
    }
}
