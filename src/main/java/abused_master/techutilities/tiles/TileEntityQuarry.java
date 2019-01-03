package abused_master.techutilities.tiles;

import abused_master.techutilities.api.phase.EnergyStorage;
import abused_master.techutilities.api.utils.hud.IHudSupport;
import abused_master.techutilities.registry.ModTiles;
import net.fabricmc.fabric.block.entity.ClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class TileEntityQuarry extends BlockEntity implements Tickable, ClientSerializable, IHudSupport {

    //TODO CHANGE AMOUNT TO 0
    public EnergyStorage storage = new EnergyStorage(100000, 100000);
    private boolean running = false;
    public BlockPos miningPos = null, firstCorner = null, secondCorner = null;
    public int energyUsagePerBlock = 500, miningSpeed = 0;
    public BlockState miningBlock = null;
    public boolean completedArea = false, miningError = false, hasQuarryRecorder = false;

    public boolean silkTouch = false;
    public int fortuneLevel = 0, speedMultiplier = 1;

    public TileEntityQuarry() {
        super(ModTiles.QUARRY);
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        this.storage.readFromNBT(nbt);
        this.running = nbt.getBoolean("running");
        this.silkTouch = nbt.getBoolean("silkTouch");
        this.fortuneLevel = nbt.getInt("fortuneLevel");
        this.speedMultiplier = nbt.getInt("speedMultiplier");
        this.hasQuarryRecorder = nbt.getBoolean("hasQuarryRecorder");
        if (nbt.containsKey("firstCorner")) {
            this.firstCorner = new BlockPos(nbt.getIntArray("firstCorner")[0], nbt.getIntArray("firstCorner")[1], nbt.getIntArray("firstCorner")[2]);
        }

        if (nbt.containsKey("secondCorner")) {
            this.secondCorner = new BlockPos(nbt.getIntArray("secondCorner")[0], nbt.getIntArray("secondCorner")[1], nbt.getIntArray("secondCorner")[2]);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);
        this.storage.writeEnergyToNBT(nbt);
        nbt.putBoolean("running", this.running);
        nbt.putBoolean("silkTouch", this.silkTouch);
        nbt.putInt("fortuneLevel", this.fortuneLevel);
        nbt.putInt("speedMultiplier", this.speedMultiplier);
        nbt.putBoolean("hasQuarryRecorder", this.hasQuarryRecorder);
        if(firstCorner != null) {
            nbt.putIntArray("firstCorner", new int[] {firstCorner.getX(), firstCorner.getY(), firstCorner.getZ()});
        }

        if(secondCorner != null) {
            nbt.putIntArray("secondCorner", new int[] {secondCorner.getX(), secondCorner.getY(), secondCorner.getZ()});
        }

        return nbt;
    }

    @Override
    public void tick() {
        if (running && canRun() && storage.getEnergyStored() >= energyUsagePerBlock && !completedArea) {
            if (!miningError) {
                miningSpeed++;
                if (miningSpeed >= (20 / speedMultiplier)) {
                    Inventory inventory = getNearbyInventory();
                    this.mineBlocks(inventory);
                }
            } else {
                if (world.getBlockState(miningPos) != null && insertItemIfPossible(getNearbyInventory(), new ItemStack(world.getBlockState(miningPos).getBlock()), true)) {
                    this.setMiningError(false);
                }
            }
        } else if (running && !canRun()) {
            this.setRunning(false);
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    public void mineBlocks(Inventory inventory) {
        Iterable<BlockPos> blocksInQuarry = BlockPos.iterateBoxPositions(secondCorner, firstCorner);

        for (BlockPos currentMiningPos : listBlocksInQuarry(blocksInQuarry)) {
            if (world.isAir(currentMiningPos) || world.getBlockState(currentMiningPos).getBlock() == Blocks.BEDROCK || world.getBlockState(currentMiningPos).getBlock() instanceof FluidBlock || world.getBlockEntity(currentMiningPos) != null) {
                continue;
            }

            if (miningSpeed >= (20 / speedMultiplier)) {
                this.miningPos = currentMiningPos;
                miningSpeed = 0;
                BlockState state = world.getBlockState(currentMiningPos);
                miningBlock = state;
                //List<ItemStack> drops = Block.getDroppedStacks(state, world instanceof ServerWorld ? (ServerWorld) world : null, currentMiningPos, world.getBlockEntity(currentMiningPos));
                List<ItemStack> drops = Arrays.asList(new ItemStack[]{new ItemStack(state.getBlock())});
                world.setBlockState(currentMiningPos, Blocks.AIR.getDefaultState());

                if (silkTouch) {
                    if (!insertItemIfPossible(inventory, new ItemStack(state.getBlock()), false)) {
                        setMiningError(true);
                    }
                } else {
                    for (ItemStack itemStack : drops) {
                        Random random = new Random();
                        ItemStack stackWithFortune = new ItemStack(itemStack.getItem(), fortuneLevel == 0 ? 1 : random.nextInt(fortuneLevel * 2));

                        if (!insertItemIfPossible(inventory, stackWithFortune, false)) {
                            setMiningError(true);
                        }
                    }
                }
            }
        }
    }

    public List<BlockPos> listBlocksInQuarry(Iterable<BlockPos> iterable) {
        List<BlockPos> list = new ArrayList<>();

        for (BlockPos pos : iterable) {
            list.add(pos);
        }

        Collections.sort(list, Collections.reverseOrder());

        return list;
    }

    public BlockPos[] listFourCorners() {
        if(!blockPositionsActive()) {
            return null;
        }

        BlockPos corner1 = this.firstCorner.offset(Direction.UP, 1);
        BlockPos corner2;
        BlockPos corner3;
        BlockPos corner4 = new BlockPos(secondCorner.getX(), corner1.getY(), secondCorner.getZ());

        double xCenter = (firstCorner.getX() + secondCorner.getX()) / 2;
        double zCenter = (firstCorner.getZ() + secondCorner.getZ()) / 2;

        double xDistance = (firstCorner.getX() - secondCorner.getX()) / 2;
        double zDistance = (firstCorner.getZ() - secondCorner.getZ()) / 2;

        double x1 = (xCenter - zDistance);
        double z1 = (zCenter + xDistance);
        corner2 = new BlockPos(x1, corner1.getY(), z1);

        double x2 = (xCenter + zDistance);
        double z2 = (zCenter - xDistance);
        corner3 = new BlockPos(x2, corner1.getY(), z2);

        return new BlockPos[] {corner1, corner2, corner3, corner4};
    }

    public void setCorners(BlockPos firstCorner, BlockPos secondCorner) {
        this.firstCorner = firstCorner;
        this.secondCorner = secondCorner;
    }

    public boolean blockPositionsActive() {
        if(firstCorner == null || secondCorner == null) {
            return false;
        }

        return true;
    }

    public boolean canRun() {
        if (!blockPositionsActive()) {
            return false;
        }

        return true;
    }

    public boolean insertItemIfPossible(Inventory inventory, ItemStack stack, boolean simulate) {
        if(inventory == null) {
            return false;
        }

        for (int i = 0; i < inventory.getInvSize(); i++) {
            if(!inventory.getInvStack(i).isEmpty()) {
                if(canItemStacksStack(inventory.getInvStack(i), stack) && inventory.getInvStack(i).getAmount() < 64) {
                    if(!simulate)
                        inventory.setInvStack(i, new ItemStack(stack.getItem(), stack.getAmount() + inventory.getInvStack(i).getAmount()));

                    return true;
                }
            }else {
                if(!simulate)
                    inventory.setInvStack(i, stack);

                return true;
            }
        }

        return false;
    }

    public boolean canItemStacksStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || !a.isEqualIgnoreTags(b) || a.hasTag() != b.hasTag())
            return false;

        return (!a.hasTag() || a.getTag().equals(b.getTag()));
    }

    public Inventory getNearbyInventory() {
        for (Direction direction : Direction.values()) {
            BlockPos offsetPosition = new BlockPos(pos).offset(direction);
            BlockEntity entity = world.getBlockEntity(offsetPosition);
            if (entity != null && entity instanceof Inventory) {
                Inventory inventory = (Inventory) entity;
                return inventory;
            }
        }

        return null;
    }

    public void setRunning(boolean running) {
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
        this.running = running;
    }

    public void setMiningError(boolean miningError) {
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
        this.miningError = miningError;
    }

    public void setHasQuarryRecorder(boolean hasQuarryRecorder) {
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
        this.hasQuarryRecorder = hasQuarryRecorder;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.fromTag(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.toTag(tag);
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
    public BlockPos getBlockPos() {
        return getPos();
    }

    @Override
    public List<String> getClientLog() {
        List<String> toDisplay = new ArrayList<>();
        if(miningPos == null) {
            toDisplay.add("No mining coords set");
        }else {
            toDisplay.add("Mining at: x: " + miningPos.getX() + " y: " + miningPos.getY() + " z: " + miningPos.getZ());
        }

        toDisplay.add("Energy: " + storage.getEnergyStored() + " / " + storage.getEnergyCapacity() + " PE");
        return toDisplay;
    }
}
