package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import com.rwtema.extrautils.tileentity.TileEntityDrum;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import tterrag.wailaplugins.api.Plugin;

import java.util.List;

@Plugin(deps = "ExtraUtilities")
public class PluginExtraUtilities extends WailaPluginBase {

    @Override
    public void load() {
        registerBody(TileEntityDrum.class);
        registerNBT(TileEntityDrum.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (tile instanceof TileEntityDrum) {
            if (tag.hasKey("amnt")) {
                currenttip.add(tag.getInteger("amnt") + " / " + tag.getInteger("max") + " mB");
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        FluidTankInfo info = ((TileEntityDrum) te).getTankInfo(ForgeDirection.UNKNOWN)[0];
        if (info.fluid != null) {
            tag.setInteger("amnt", info.fluid.amount);
            tag.setInteger("max", info.capacity);
        }
    }
}
