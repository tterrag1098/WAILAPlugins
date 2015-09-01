package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.utils.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
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

        registerBody(IDeepStorageUnit.class, IFluidHandler.class);
        registerNBT(IDeepStorageUnit.class, IFluidHandler.class);
    }

    @Override
    public void postLoad()
    {
        ConfigHandler.instance().setConfig(Constants.CATEGORY_MODULES, "thermalexpansion.fluidamount", false);
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
        if (te instanceof IFluidHandler)
        {
            FluidTankInfo[] infos = readFluidInfosFromNBT(tag);
            addTankTooltip(currenttip, infos);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addTankTooltip(List<String> currenttip, FluidTankInfo... tanks)
    {
        for (FluidTankInfo tank : tanks)
        {
            if (tank != null && tank.fluid != null)
            {
                ((ITaggedList<String, String>)currenttip).add(tank.fluid.amount + " / " + tank.capacity + " mB " + tank.fluid.getLocalizedName(), "IFluidHandler");
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
        if (te instanceof IFluidHandler)
        {
            writeFluidInfoToNBT((IFluidHandler) te, tag);
        }
    }
    
    public static void writeFluidInfoToNBT(IFluidHandler te, NBTTagCompound tag)
    {
        FluidTankInfo[] infos = ((IFluidHandler)te).getTankInfo(ForgeDirection.UNKNOWN);
        if (infos != null && infos.length > 0)
        {
            NBTTagList infoList = new NBTTagList();
            for (FluidTankInfo info : infos)
            {
                NBTTagCompound infoTag = new NBTTagCompound();
                writeFluidInfoToNBT(info, infoTag);
                infoList.appendTag(infoTag);
            }
            tag.setTag("fluidInfo", infoList);
        }
    }
    
    public static FluidTankInfo[] readFluidInfosFromNBT(NBTTagCompound tag)
    {
        NBTTagList list = tag.getTagList("fluidInfo", NBT.TAG_COMPOUND);
        FluidTankInfo[] ret = new FluidTankInfo[list.tagCount()];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = readFluidInfoFromNBT(list.getCompoundTagAt(i));
        }
        return ret;
    }

    public static void writeFluidInfoToNBT(FluidTankInfo info, NBTTagCompound tag)
    {
        if (info.fluid != null)
        {
            NBTTagCompound fluidTag = new NBTTagCompound();
            info.fluid.writeToNBT(fluidTag);
            tag.setTag("fluid", fluidTag);
        }
        tag.setInteger("capacity", info.capacity);
    }
    
    public static FluidTankInfo readFluidInfoFromNBT(NBTTagCompound tag)
    {
        FluidStack fluid = tag.hasKey("fluid") ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid")) : null;
        int capacity = tag.getInteger("capacity");
        return new FluidTankInfo(fluid, capacity);
    }
}
