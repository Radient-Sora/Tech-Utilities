package abused_master.techutilities.items;

import abused_master.abusedlib.items.ItemBase;
import abused_master.techutilities.TechUtilities;
import net.minecraft.client.item.TooltipContext;
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

public class ItemQuarryRecorder extends ItemBase {

    public ItemQuarryRecorder() {
        super("quarry_recorder", new Settings().itemGroup(TechUtilities.modItemGroup).stackSize(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usageContext) {
        BlockPos pos = usageContext.getBlockPos();
        World world = usageContext.getWorld();
        PlayerEntity player = usageContext.getPlayer();
        ItemStack stack = usageContext.getItemStack();

        CompoundTag tag = stack.getTag();
        if (!player.isSneaking()) {
            if (tag == null) {
                tag = new CompoundTag();
            }

            if (!tag.containsKey("coordinates1")) {
                tag.put("coordinates1", TagHelper.serializeBlockPos(pos));
                if (!world.isClient) {
                    player.addChatMessage(new StringTextComponent("Set coordinates for the first corner"), true);
                }

            } else if (!tag.containsKey("coordinates2")) {
                tag.put("coordinates2", TagHelper.serializeBlockPos(pos));
                if (!world.isClient) {
                    player.addChatMessage(new StringTextComponent("Set coordinates for the second corner"), true);
                }
            }

            stack.setTag(tag);
            return ActionResult.SUCCESS;
        }


        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if(player.isSneaking()) {
            clearTag(player.getStackInHand(hand));
            if(!world.isClient) {
                player.addChatMessage(new StringTextComponent("Cleared recorder settings"), true);
            }
        }

        return super.use(world, player, hand);
    }

    public void clearTag(ItemStack stack) {
        stack.setTag(null);
    }

    @Override
    public void buildTooltip(ItemStack itemStack, World world, List<TextComponent> list, TooltipContext tooltipOptions) {
        CompoundTag tag = itemStack.getTag();
        if(tag != null) {
            if(tag.containsKey("coordinates1")) {
                BlockPos pos = TagHelper.deserializeBlockPos(tag.getCompound("coordinates1"));
                list.add(new StringTextComponent("First Corner, x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
            }

            if(tag.containsKey("coordinates2")) {
                BlockPos pos = TagHelper.deserializeBlockPos(tag.getCompound("coordinates2"));
                list.add(new StringTextComponent("Second Corner, x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ()));
            }
        }else {
            list.add(new StringTextComponent("Recorder not yet linked to any blocks."));
        }
    }
}
