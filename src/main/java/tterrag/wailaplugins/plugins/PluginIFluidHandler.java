package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
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
import tterrag.wailaplugins.api.Plugin;

import java.util.List;

@Plugin(name = "IFluidHandler")
public class PluginIFluidHandler extends PluginBase {
    @SuppressWarnings("unchecked")
    public static void addTankTooltip(List<String> currenttip, FluidTankInfo... tanks) {
        for (FluidTankInfo tank : tanks) {
            if (tank != null && tank.fluid != null) {
                ((ITaggedList<String, String>) currenttip).add(tank.fluid.amount + " / " + tank.capacity + " mB " + tank.fluid.getLocalizedName(),
                        "IFluidHandler");
            }
        }
    }

    public static void writeFluidInfoToNBT(IFluidHandler te, NBTTagCompound tag) {
        FluidTankInfo[] infos = ((IFluidHandler) te).getTankInfo(ForgeDirection.UNKNOWN);
        if (infos != null && infos.length > 0) {
            NBTTagList infoList = new NBTTagList();
            for (FluidTankInfo info : infos) {
                NBTTagCompound infoTag = new NBTTagCompound();
                writeFluidInfoToNBT(info, infoTag);
                infoList.appendTag(infoTag);
            }
            tag.setTag("fluidInfo", infoList);
        }
    }

    public static FluidTankInfo[] readFluidInfosFromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("fluidInfo", NBT.TAG_COMPOUND);
        FluidTankInfo[] ret = new FluidTankInfo[list.tagCount()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = readFluidInfoFromNBT(list.getCompoundTagAt(i));
        }
        return ret;
    }

    public static void writeFluidInfoToNBT(FluidTankInfo info, NBTTagCompound tag) {
        if (info.fluid != null) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            info.fluid.writeToNBT(fluidTag);
            tag.setTag("fluid", fluidTag);
        }
        tag.setInteger("capacity", info.capacity);
    }

    public static FluidTankInfo readFluidInfoFromNBT(NBTTagCompound tag) {
        FluidStack fluid = tag.hasKey("fluid") ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid")) : null;
        int capacity = tag.getInteger("capacity");
        return new FluidTankInfo(fluid, capacity);
    }

    @Override
    public void load(IWailaRegistrar registrar) {
        super.load(registrar);

        registerBody(IFluidHandler.class);
        registerNBT(IFluidHandler.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        FluidTankInfo[] infos = readFluidInfosFromNBT(accessor.getNBTData());
        addTankTooltip(currenttip, infos);
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        writeFluidInfoToNBT((IFluidHandler) te, tag);
    }
}
