package tterrag.wailaplugins.plugins;

import java.util.List;

import com.enderio.core.common.Lang;

import cofh.api.core.IAugmentable;
import cofh.api.item.IAugmentItem;
import cofh.thermalexpansion.block.machine.TileMachineBase;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.Plugin;

@Plugin(name = "TE Augments", deps = "thermalexpansion")
public class PluginThermalExpansion extends PluginBase
{
    private Lang teLang = new Lang("item.thermalexpansion");

    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        registerBody(TileMachineBase.class);
        registerNBT(TileMachineBase.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity te = accessor.getTileEntity();

        if (te instanceof IAugmentable)
        {
            if (WailaPlugins.proxy.isShiftKeyDown())
            {
                currenttip.add(TextFormatting.AQUA.toString() + "> " + lang.localize("augments.shown"));
                NBTTagList augments = accessor.getNBTData().getTagList("augments", Constants.NBT.TAG_COMPOUND);
                TObjectIntMap<String> occurances = new TObjectIntHashMap<>();
                for (int i = 0; i < augments.tagCount(); i++)
                {
                    ItemStack augmentStack = ItemStack.loadItemStackFromNBT(augments.getCompoundTagAt(i));
                    if (augmentStack != null)
                    {
                        IAugmentItem augment = (IAugmentItem) augmentStack.getItem();
                        String ident = augment.getAugmentIdentifier(augmentStack);
                        occurances.adjustOrPutValue(ident, 1, 1);
                    }
                }
                for (String s : occurances.keySet()) {
                    String line = TextFormatting.WHITE + "-" + SpecialChars.TAB + TextFormatting.WHITE + teLang.localize("augment." + s + ".name");
                    if (occurances.get(s) > 1) {
                        line += " (x" + occurances.get(s) + ")";
                    }
                    currenttip.add(line);
                }
            }
            else
            {
                currenttip.add(TextFormatting.AQUA.toString() + "<" + TextFormatting.ITALIC + lang.localize("augments.hidden") + TextFormatting.AQUA + ">");
            }
        }
    }

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (te instanceof IAugmentable)
        {
            NBTTagList stacks = new NBTTagList();
            for (ItemStack stack : ((IAugmentable) te).getAugmentSlots())
            {
                if (stack != null)
                {
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
