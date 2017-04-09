package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import tterrag.wailaplugins.api.Plugin;

@Plugin(name = "IFluidHandler")
public class PluginIFluidHandler extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileEntity.class);
        registerNBT(TileEntity.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        FluidTankInfo[] infos = readFluidInfosFromNBT(accessor.getNBTData());
        addTankTooltip(currenttip, infos);
    }

    @SuppressWarnings("unchecked")
    public static void addTankTooltip(List<String> currenttip, FluidTankInfo... tanks)
    {
        for (FluidTankInfo tank : tanks)
        {
            if (tank != null && tank.fluid != null)
            {
                ((ITaggedList<String, String>) currenttip).add(tank.fluid.amount + " / " + tank.capacity + " mB " + tank.fluid.getLocalizedName(),
                        "IFluidHandler");
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
        {
            writeFluidInfoToNBT(te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null), tag);
        }
    }

    public static void writeFluidInfoToNBT(IFluidHandler te, NBTTagCompound tag)
    {
        IFluidTankProperties[] infos = ((IFluidHandler) te).getTankProperties();
        if (infos != null && infos.length > 0)
        {
            NBTTagList infoList = new NBTTagList();
            for (IFluidTankProperties info : infos)
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

    public static void writeFluidInfoToNBT(IFluidTankProperties info, NBTTagCompound tag)
    {
        if (info.getContents() != null)
        {
            NBTTagCompound fluidTag = new NBTTagCompound();
            info.getContents().writeToNBT(fluidTag);
            tag.setTag("fluid", fluidTag);
        }
        tag.setInteger("capacity", info.getCapacity());
    }

    public static FluidTankInfo readFluidInfoFromNBT(NBTTagCompound tag)
    {
        FluidStack fluid = tag.hasKey("fluid") ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid")) : null;
        int capacity = tag.getInteger("capacity");
        return new FluidTankInfo(fluid, capacity);
    }
}
