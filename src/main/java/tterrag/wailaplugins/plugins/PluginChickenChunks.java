package tterrag.wailaplugins.plugins;

import codechicken.chunkloader.TileChunkLoader;
import codechicken.chunkloader.TileChunkLoaderBase;
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
 * Created by Elec332 on 10-10-2015.
 */
@Plugin(deps = "ChickenChunks")
public class PluginChickenChunks extends WailaPluginBase {

    @Override
    public void load() {
        addConfig("showOnlyIfAccess");
        registerBody(TileChunkLoaderBase.class, TileChunkLoader.class);
        registerNBT(TileChunkLoaderBase.class);//, TileChunkLoader.class);
    }

    @Override
    public void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(access)) {
                boolean access = tag.getBoolean(PluginChickenChunks.access);
                if (!access) {
                    currenttip.add(ClientMessageHandler.getNoAccessMessage());
                    return;
                }
                currenttip.add(ClientMessageHandler.getOwnerMessage()+tag.getString(name));
                currenttip.add(ClientMessageHandler.getActiveMessage()+tag.getBoolean(active));
                if (tag.hasKey(range)){
                    currenttip.add(ClientMessageHandler.getRangeMessage()+tag.getInteger(range));
                    currenttip.add(ClientMessageHandler.getLoadedChunksMessage()+tag.getInteger(specialData1));
                }
            }
        }
    }

    @Override
    public void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tag != null && tile != null){
            if (tile instanceof TileChunkLoaderBase){
                boolean access = true;//!(((TileChunkLoaderBase) tile).getOwner() != null && !((TileChunkLoaderBase) tile).getOwner().equals(player.getCommandSenderName()) && (!ChunkLoaderManager.opInteract() || !ServerUtils.isPlayerOP(player.getCommandSenderName()))) || !getConfig("showOnlyIfAccess");
                tag.setBoolean(PluginChickenChunks.access, access);
                if (access){
                    tag.setString(name, ((TileChunkLoaderBase) tile).getOwner());
                    tag.setBoolean(active, ((TileChunkLoaderBase) tile).active);
                    if (tile instanceof TileChunkLoader){
                        tag.setInteger(range, ((TileChunkLoader) tile).radius);
                        tag.setInteger(specialData1, ((TileChunkLoader) tile).countLoadedChunks());
                    }
                }
            }
        }
    }

}
