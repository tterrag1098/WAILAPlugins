package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import com.enderio.core.common.util.BlockCoord;

// Plugin for those which should be enabled 100% of the time
public class Plugin_Forge extends PluginBase
{
    public static final String DSU_STACK = "dsuStack";
    public static final String DSU_AMNT = "dsuAmnt";

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(IDeepStorageUnit.class);
        registerNBT(IDeepStorageUnit.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (te instanceof IDeepStorageUnit)
        {
            ItemStack stored = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(DSU_STACK));
            if (stored != null)
            {
                int amount = tag.getInteger(DSU_AMNT);
                int overflow = 0;
                if (amount < 0)
                {
                    overflow = Integer.MAX_VALUE + amount + 1;
                }
                int maxStack = stored.getMaxStackSize();
                int stacks = (amount < 0 ? Integer.MAX_VALUE : amount) / maxStack;
                String stacksStr = "" + stacks;
                if (stacks >= 10000000)
                {
                    stacksStr = (stacks / 1000000) + "M";
                }
                else if (stacks >= 1000000)
                {
                    int num = stacks / 100000;
                    float dec = ((float) num) / 10;
                    stacksStr = dec + "M";
                }
                else if (stacks >= 10000)
                {
                    stacksStr = (stacks / 100 / 10f) + "K";
                }
                int leftover = (amount - (stacks * maxStack)) + overflow;
                String str = stacksStr + "*" + maxStack + " + " + leftover + " " + stored.getDisplayName();
                currenttip.add(str);
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        if (te instanceof IDeepStorageUnit)
        {
            NBTTagCompound stackTag = new NBTTagCompound();
            ItemStack stack = ((IDeepStorageUnit) te).getStoredItemType();
            if (stack != null)
            {
                stack.writeToNBT(stackTag);
                tag.setTag(DSU_STACK, stackTag);
                tag.setInteger(DSU_AMNT, stack.stackSize);
            }
        }
    }
}
