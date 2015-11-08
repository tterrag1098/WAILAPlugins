package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import com.google.common.collect.Lists;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;
import elec332.cmip.mods.MainCompatHandler;
import elec332.cmip.mods.waila.AbstractWailaCompatHandler;
import elec332.cmip.util.Config;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by Elec332 on 6-10-2015.
 */
@Plugin(deps = "Mystcraft")
public class PluginMystCraft extends WailaPluginBase {

    @Override
    public void load() {
        if (Config.WAILA.MystCraft.showDimData) {
            for (Class clazz : findClasses(mystcraftBookStands)) {
                registerBody(clazz);
                registerNBT(clazz);
            }
        }
    }

    private static String[] mystcraftBookStands = new String[]{"com.xcompwiz.mystcraft.tileentity.TileEntityLectern",
            "com.xcompwiz.mystcraft.tileentity.TileEntityBookstand", "com.xcompwiz.mystcraft.tileentity.TileEntityBookReceptacle"};

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        ItemStack inv = ItemStack.loadItemStackFromNBT(accessor.getNBTData().getTagList("Items", 10).getCompoundTagAt(0));
        boolean valid = false;
        int dimension = 0;
        String name = "???";
        if (inv != null && inv.stackTagCompound != null){
            NBTTagCompound tag = inv.stackTagCompound;
            if (tag.hasKey("Dimension")){
                dimension = tag.getInteger("Dimension");
                valid = true;
            } else if (tag.hasKey("AgeUID")){
                dimension = tag.getInteger("AgeUID");
                valid = true;
            }
            if (tag.hasKey("DisplayName")){
                name = tag.getString("DisplayName");
            } else if (tag.hasKey("agename")){
                name = tag.getString("agename");
            }
        }
        if (valid){
            currenttip.add(ClientMessageHandler.getDimensionMessage() + dimension);
            currenttip.add(ClientMessageHandler.getNameMessage() + name);
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        if (te != null){
            te.writeToNBT(tag);
        }
    }

    public List<Class> findClasses(String... s){
        List<Class> ret = Lists.newArrayList();
        for (String s1 : s) {
            try {
                ret.add(Class.forName(s1));
            } catch (Exception e) {
                WailaPlugins.logger.info("Error finding class: " + s1);
            }
        }
        return ret;
    }

}
