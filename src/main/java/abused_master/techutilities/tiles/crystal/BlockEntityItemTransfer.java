package abused_master.techutilities.tiles.crystal;

import abused_master.abusedlib.tiles.BlockEntityBase;
import abused_master.abusedlib.utils.InventoryHelper;
import abused_master.techutilities.registry.ModBlockEntities;
import abused_master.techutilities.utils.linker.ILinkerHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;

public class BlockEntityItemTransfer extends BlockEntityBase implements ILinkerHandler {

    public int counter = 0;
    public BlockPos receiverPosition = null;

    public BlockEntityItemTransfer() {
        super(ModBlockEntities.ITEM_TRANSFER);
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        counter = nbt.getInt("counter");
        if(nbt.containsKey("receiverPosition")) {
            receiverPosition = TagHelper.deserializeBlockPos(nbt.getCompound("receiverPosition"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        super.toTag(nbt);
        nbt.putInt("counter", this.counter);
        if(receiverPosition != null) {
            nbt.put("receiverPosition", TagHelper.serializeBlockPos(receiverPosition));
        }
        return nbt;
    }

    @Override
    public void tick() {
        if(canSend()) {
            counter++;
            if (counter >= 5) {
                sendItems();
            }
        }
    }

    public void sendItems() {
        BlockEntityItemReceiver itemReceiver = (BlockEntityItemReceiver) world.getBlockEntity(receiverPosition);
        Inventory nearbyInventory = InventoryHelper.getNearbyInventory(world, pos);
        if(nearbyInventory != null) {
            for (int i = 0; i < (nearbyInventory.getInvSize() - 1); i++) {
                if(counter >= 5) {
                    if (!nearbyInventory.getInvStack(i).isEmpty() && itemReceiver.canReceive(nearbyInventory.getInvStack(i))) {
                        if (InventoryHelper.insertItemIfPossible(itemReceiver, nearbyInventory.getInvStack(i), false)) {
                            nearbyInventory.takeInvStack(i, 1);
                            this.markDirty();
                        }
                    }
                    counter = 0;
                }
            }
        }
    }

    public boolean canSend() {
        Inventory nearbyInventory = InventoryHelper.getNearbyInventory(world, pos);
        if(receiverPosition != null && world.getBlockEntity(receiverPosition) == null) {
            this.receiverPosition = null;
            markDirty();
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }

        if(receiverPosition == null || nearbyInventory == null || isInventoryEmpty(nearbyInventory)) {
            return false;
        }


        return true;
    }

    public boolean isInventoryEmpty(Inventory inventory) {
        for (int i = 0; i < inventory.getInvSize() - 1; i++) {
            if(!inventory.getInvStack(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setReceiverPosition(BlockPos receiverPosition) {
        this.receiverPosition = receiverPosition;
        this.markDirty();
        world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    @Override
    public void link(PlayerEntity player, CompoundTag tag) {
        if(!world.isClient) {
            tag.put("itemPos", TagHelper.serializeBlockPos(pos));
            player.addChatMessage(new StringTextComponent("Saved Item Transferer position!"), true);
        }
    }
}
