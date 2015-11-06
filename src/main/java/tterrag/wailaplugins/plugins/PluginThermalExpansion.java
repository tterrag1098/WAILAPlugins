package tterrag.wailaplugins.plugins;

import cofh.api.item.IAugmentItem;
import cofh.thermalexpansion.block.TileAugmentable;
import cofh.thermalexpansion.block.machine.TileMachineBase;
import com.enderio.core.common.Lang;
import com.enderio.core.common.util.BlockCoord;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.Plugin;

import java.util.List;
import java.util.Set;

@Plugin(name = "TE Augments", deps = "ThermalExpansion")
public class PluginThermalExpansion extends PluginBase {
    private Lang teLang = new Lang("info.thermalexpansion");

    public void load(IWailaRegistrar registrar) {
        super.load(registrar);
        registerBody(TileMachineBase.class);
        registerNBT(TileMachineBase.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        TileEntity te = accessor.getTileEntity();

        if (te instanceof TileAugmentable) {
            if (WailaPlugins.proxy.isShiftKeyDown()) {
                currenttip.add(EnumChatFormatting.AQUA.toString() + "> " + lang.localize("augments.shown"));
                NBTTagList augments = accessor.getNBTData().getTagList("augments", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < augments.tagCount(); i++) {
                    ItemStack augmentStack = ItemStack.loadItemStackFromNBT(augments.getCompoundTagAt(i));
                    if (augmentStack != null) {
                        IAugmentItem augment = (IAugmentItem) augmentStack.getItem();
                        Set<String> descs = augment.getAugmentTypes(augmentStack);
                        for (String s : descs) {
                            currenttip.add(EnumChatFormatting.WHITE + "-" + SpecialChars.TAB + EnumChatFormatting.WHITE
                                    + teLang.localize("augment." + s));
                        }
                    }
                }
            } else {
                currenttip.add(EnumChatFormatting.AQUA.toString() + "<" + EnumChatFormatting.ITALIC + lang.localize("augments.hidden") + EnumChatFormatting.AQUA + ">");
            }
        }
    }

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        if (te instanceof TileAugmentable) {
            NBTTagList stacks = new NBTTagList();
            for (ItemStack stack : ((TileAugmentable) te).getAugmentSlots()) {
                if (stack != null) {
                    NBTTagCompound stackTag = new NBTTagCompound();
                    stack.writeToNBT(stackTag);
                    stacks.appendTag(stackTag);
                }
            }
            tag.setTag("augments", stacks);
        }
        te.writeToNBT(tag);
    }
}
