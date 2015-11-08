package tterrag.wailaplugins.plugins;

import com.brandon3055.draconicevolution.common.tileentities.energynet.TileRemoteEnergyBase;
import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

/**
 * Created by Elec332 on 13-10-2015.
 */
@Plugin(deps = "DraconicEvolution")
public class PluginDraconicEvolution extends WailaPluginBase {

    @Override
    public void load() {
        addConfig("showLinkedDevices");
        if (getConfig("showLinkedDevices")) {
            registerBody(TileRemoteEnergyBase.class);
            registerNBT(TileRemoteEnergyBase.class);
        }
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(specialData1)){
                currenttip.add(ClientMessageHandler.getConnectedMachinesMessage()+tag.getInteger(specialData1));
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof TileRemoteEnergyBase){
                tag.setInteger(specialData1, ((TileRemoteEnergyBase) tile).linkedDevices.size());
            }
        }
    }
}
