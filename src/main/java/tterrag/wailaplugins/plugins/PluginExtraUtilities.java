package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import com.rwtema.extrautils.tileentity.TileEntityDrum;
import com.rwtema.extrautils.tileentity.transfernodes.TileEntityTransferNode;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

@Plugin(deps = "ExtraUtilities")
public class PluginExtraUtilities extends WailaPluginBase {

    @Override
    public void load() {
        registerBody(TileEntityDrum.class);
        registerNBT(TileEntityDrum.class);
        addConfig("showPipeData");
        if (getConfig("showPipeData")) {
            registerBody(TileEntityTransferNode.class);
            registerNBT(TileEntityTransferNode.class);
        }
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(specialData1)){
                currenttip.add(ClientMessageHandler.getSearchLocationMessage()+tag.getInteger(specialData1)+", "+tag.getInteger(specialData2)+", "+tag.getInteger(specialData3));
            }
            if (tile instanceof TileEntityDrum) {
                if (tag.hasKey("amnt")) {
                    currenttip.add(tag.getInteger("amnt") + " / " + tag.getInteger("max") + " mB");
                }
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof TileEntityTransferNode) {
                tag.setInteger(specialData1, ((TileEntityTransferNode) tile).pipe_x);
                tag.setInteger(specialData2, ((TileEntityTransferNode) tile).pipe_y);
                tag.setInteger(specialData3, ((TileEntityTransferNode) tile).pipe_z);
            }
            if (tile instanceof TileEntityDrum){
                FluidTankInfo info = ((TileEntityDrum) tile).getTankInfo(ForgeDirection.UNKNOWN)[0];
                if (info.fluid != null) {
                    tag.setInteger("amnt", info.fluid.amount);
                    tag.setInteger("max", info.capacity);
                }
            }
        }
    }

}
