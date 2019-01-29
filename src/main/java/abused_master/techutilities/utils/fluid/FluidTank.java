package abused_master.techutilities.utils.fluid;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

public class FluidTank implements IFluidTank {

    private int capacity;
    private FluidStack fluidStack;
    private BlockEntity blockEntity;

    public FluidTank(int capacity) {
        this(null, capacity, null);
    }

    public FluidTank(FluidStack stack, int capacity) {
        this(stack, capacity, null);
    }

    public FluidTank(FluidStack stack, int capacity, BlockEntity blockEntity) {
        this.fluidStack = stack;
        this.capacity = capacity;
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean extractFluid(int amount) {
        if(fluidStack == null || fluidStack.getAmount() == 0 || amount > fluidStack.getAmount()) {
            return false;
        }

        fluidStack.subtractAmount(amount);
        updateEntity();
        if (fluidStack.getAmount() <= 0) {
            fluidStack = null;
        }

        return true;
    }

    @Override
    public boolean fillFluid(FluidStack stack) {
        if(stack == null || stack.getAmount() <= 0) {
            return false;
        }

        if(fluidStack == null) {
            fluidStack = stack;
            updateEntity();

            return true;
        }else if (fluidStack.areFluidsEqual(stack) && (stack.getAmount() + fluidStack.getAmount()) <= capacity) {
            fluidStack.addAmount(stack.getAmount());
            updateEntity();

            return true;
        }

        return false;
    }

    @Override
    public int getFluidAmount() {
        return fluidStack == null ? 0 : fluidStack.getAmount();
    }

    @Override
    public int getFluidCapacity() {
        return this.capacity;
    }

    @Override
    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void setFluidStack(FluidStack stack) {
        this.fluidStack = stack;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public FluidTank readFromNBT(CompoundTag nbt) {
        if(!nbt.containsKey("empty")) {
            this.fluidStack = FluidStack.fluidFromTag(nbt);
        }else {
            this.fluidStack = null;
        }

        return this;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        if (fluidStack != null) {
            fluidStack.toTag(nbt);
        } else {
            nbt.putString("empty", "");
        }

        return nbt;
    }

    public void updateEntity() {
        if(blockEntity != null) {
            blockEntity.markDirty();
            blockEntity.getWorld().updateListeners(blockEntity.getPos(), blockEntity.getWorld().getBlockState(blockEntity.getPos()), blockEntity.getWorld().getBlockState(blockEntity.getPos()), 3);
        }
    }
}
