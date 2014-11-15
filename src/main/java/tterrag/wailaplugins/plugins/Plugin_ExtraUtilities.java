package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTank;

import com.rwtema.extrautils.tileentity.TileEntityDrum;

public class Plugin_ExtraUtilities extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        
        registerBody(TileEntityDrum.class);
        syncNBT(TileEntityDrum.class);
    }
    
    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();
        
        if (tile instanceof TileEntityDrum)
        {
            FluidTank tank = new FluidTank(Integer.MAX_VALUE);
            tank.readFromNBT(tag.getCompoundTag("tank"));
            int max = TileEntityDrum.getCapacityFromMetadata(accessor.getMetadata());
            
            if (tank.getFluid() != null)
            {
                currenttip.add(tank.getFluidAmount() + " / " + max + " mB");
            }
        }
    }
}
