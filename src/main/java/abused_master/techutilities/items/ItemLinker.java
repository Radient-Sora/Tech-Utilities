package abused_master.techutilities.items;

import abused_master.techutilities.TechUtilities;
import abused_master.techutilities.utils.linker.ILinkerHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TagHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemLinker extends ItemBase {

    public ItemLinker() {
        super("linker", new Settings().itemGroup(TechUtilities.modItemGroup).stackSize(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usageContext) {
        World world = usageContext.getWorld();
        PlayerEntity player = usageContext.getPlayer();
        ItemStack stack = usageContext.getItemStack();

        CompoundTag tag = stack.getTag();
        if (player.isSneaking()) {
            if (tag == null) {
                tag = new CompoundTag();
            }

            BlockEntity blockEntity = world.getBlockEntity(usageContext.getBlockPos());
            if (blockEntity != null && blockEntity instanceof ILinkerHandler) {
                ((ILinkerHandler) blockEntity).link(player, tag);
            } else {
                player.addChatMessage(new StringTextComponent("The selected block is invalid for linking!"), true);
            }

            stack.setTag(tag);
        }


        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if(player.isSneaking()) {
            clearTag(player.getStackInHand(hand));
            if(!world.isClient) {
                player.addChatMessage(new StringTextComponent("Cleared linker settings"), true);
            }
        }

        return super.use(world, player, hand);
    }

    public void clearTag(ItemStack stack) {
        stack.setTag(null);
    }

    @Override
    public void buildTooltip(ItemStack itemStack, World world, List<TextComponent> list, TooltipOptions tooltipOptions) {
        CompoundTag tag = itemStack.getTag();
        if(tag != null) {
            if(tag.containsKey("collectorPos")) {
                BlockPos pos = TagHelper.deserializeBlockPos(tag.getCompound("collectorPos"));
                list.add(new StringTextComponent("Collector Pos, x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
            }else if(tag.containsKey("blockPos")) {
                BlockPos pos = TagHelper.deserializeBlockPos(tag.getCompound("blockPos"));
                list.add(new StringTextComponent("BlockEntity Pos, x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
            }else if(tag.containsKey("itemPos")) {
                BlockPos pos = TagHelper.deserializeBlockPos(tag.getCompound("itemPos"));
                list.add(new StringTextComponent("Transfer Crystal, x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
            }
        }else {
            list.add(new StringTextComponent("No block positions have been saved."));
        }
    }
}
