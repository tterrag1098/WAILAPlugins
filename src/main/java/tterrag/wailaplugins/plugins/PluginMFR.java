package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;
import tterrag.wailaplugins.api.Plugin;

import com.enderio.core.common.util.BlockCoord;

@Plugin(name = "MFR", deps = "MineFactoryReloaded")
public class PluginMFR extends PluginBase
{
    public static final String WORK_DONE = "workDone";
    public static final String IDLE_TIME = "idleTime";

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        registerBody(TileEntityFactoryPowered.class);
        registerNBT(TileEntityFactoryPowered.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (te instanceof TileEntityFactoryPowered)
        {
            currenttip.add(currenttip.size(), tag.getInteger(WORK_DONE) + " / " + ((TileEntityFactoryPowered) te).getWorkMax() + " Wk");
            currenttip.add(currenttip.size(), tag.getInteger(IDLE_TIME) + " / " + ((TileEntityFactoryPowered) te).getIdleTicksMax() + " Idle");
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        if (te instanceof TileEntityFactoryPowered)
        {
            tag.setInteger(WORK_DONE, ((TileEntityFactoryPowered) te).getWorkDone());
            tag.setInteger(IDLE_TIME, ((TileEntityFactoryPowered) te).getIdleTicks());
        }
    }
}
