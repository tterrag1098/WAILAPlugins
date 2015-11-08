package tterrag.wailaplugins.plugins;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.integration.modules.waila.PartWailaDataProvider;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.tile.qnb.TileQuantumBridge;
import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Elec332 on 10-10-2015.
 */
@Plugin(name = "AE2", deps = "appliedenergistics2")
public class PluginAE2 extends WailaPluginBase {

    @Override
    public void load() {
        addConfig("qb");
        addConfig("p2p");
        if (getConfig("qb")) {
            registerBody(TileQuantumBridge.class);
            registerNBT(TileQuantumBridge.class);
        }
        if (getConfig("p2p")) {
            try {
                PartWailaDataProvider aePartHandler = new PartWailaDataProvider();
                Field ae2Providers = PartWailaDataProvider.class.getDeclaredField("providers");
                @SuppressWarnings("unchecked")
                List<IPartWailaDataProvider> list = (List<IPartWailaDataProvider>) makeFinalFieldModifiable(ae2Providers).get(aePartHandler);
                list.clear();
                list.add(new P2PHandler());
                registrar.registerHeadProvider(aePartHandler, IPartHost.class);
                registrar.registerBodyProvider(aePartHandler, IPartHost.class);
                registrar.registerTailProvider(aePartHandler, IPartHost.class);
                registrar.registerNBTProvider(aePartHandler, IPartHost.class);
            } catch (Exception e) {
                WailaPlugins.logger.error("Error registering AE2 part handler.");
            }
        }
    }

    @Override
    public void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(specialData1)){
                currenttip.add(ClientMessageHandler.getFrequencyMessage()+tag.getLong(specialData1));
            }
        }
    }

    @Override
    public void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof TileQuantumBridge){
                tag.setLong(specialData1, ((TileQuantumBridge) tile).getQEFrequency());
            }
        }
    }

    public static class P2PHandler extends BasePartWailaDataProvider{

        @Override
        public List<String> getWailaBody(IPart part, List<String> currentToolTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            NBTTagCompound tag = accessor.getNBTData();
            if (tag != null) {
                if (tag.hasKey(specialData1)) {
                    long l = tag.getLong(specialData1);
                    if (l != 0L) {
                        currentToolTip.add(ClientMessageHandler.getFrequencyMessage() + tag.getLong(specialData1));
                    } else {
                        currentToolTip.add(ClientMessageHandler.getNoConnectionMessage());
                    }
                    currentToolTip.add(ClientMessageHandler.getNiceInputModeMessage(tag.getBoolean(specialData2)));
                }
            }
            return currentToolTip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity tile, NBTTagCompound tag, World world, int x, int y, int z) {
            if (tile != null && tag != null){
                if (part instanceof PartP2PTunnel){
                    tag.setLong(specialData1, ((PartP2PTunnel) part).freq);
                    tag.setBoolean(specialData2, ((PartP2PTunnel) part).output);
                }
            }
            return tag;
        }

    }

    /*
     * Copied from ElecCore
     */
    public static Field makeFinalFieldModifiable(Field field) throws NoSuchFieldException, IllegalAccessException{
        field.setAccessible(true);
        int i = field.getModifiers();
        Field modifier = field.getClass().getDeclaredField("modifiers");
        i &= -17;
        modifier.setAccessible(true);
        modifier.setInt(field, i);
        return field;
    }

}
