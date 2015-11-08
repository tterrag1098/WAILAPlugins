package tterrag.wailaplugins.plugins;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.robotics.EntityRobot;
import buildcraft.silicon.TileLaserTableBase;
import com.enderio.core.common.util.BlockCoord;
import com.google.common.collect.Lists;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

/**
 * Created by Elec332 on 10-10-2015.
 */
@Plugin(name = "BuildCraft", deps = "BuildCraft|Core")
public class PluginBuildCraft extends WailaPluginBase implements IWailaEntityProvider{

    @Override
    public void load() {
        addConfig("robot");
        addConfig("energy");
        addConfig("heat");
        addConfig("avgLaserEnergy");
        registerBody(TileBuildCraft.class, TileEngineBase.class, TileLaserTableBase.class);
        registerNBT(TileBuildCraft.class, TileEngineBase.class, TileLaserTableBase.class);
        if (getConfig("robot")) {
            registerEntityBody(this, EntityRobotBase.class);
            registerEntityNBT(this, EntityRobotBase.class);
        }
    }

    @Override
    public void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(energy)){
                int energy = tag.getInteger(PluginBuildCraft.energy);
                if (tag.hasKey(maxEnergy)){
                    currenttip.add(ClientMessageHandler.getEnergyMessage()+energy+"/"+tag.getInteger(maxEnergy)+" RF");
                }
                currenttip.add(ClientMessageHandler.getEnergyMessage()+energy+" RF");
            }
            if (tag.hasKey(avgEnergy)){
                currenttip.add(ClientMessageHandler.getAverageMessage()+ClientMessageHandler.getHeatMessage()+ClientMessageHandler.format(tag.getFloat(avgEnergy)));
            }
            if (tag.hasKey(heat)){
                currenttip.add(ClientMessageHandler.getHeatMessage()+tag.getInteger(heat));
            }
        }
    }

    @Override
    public void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof TileBuildCraft && getConfig("energy")){
                tag.setInteger(energy, ((TileBuildCraft) tile).getBattery().getEnergyStored());
                tag.setInteger(maxEnergy, ((TileBuildCraft) tile).getBattery().getMaxEnergyStored());
            }
            if (tile instanceof TileEngineBase && getConfig("heat")){
                tag.removeTag(maxEnergy);
                tag.setInteger(energy, ((TileEngineBase) tile).getEnergyStored());
                tag.setFloat(heat, (float)((TileEngineBase) tile).getCurrentHeatValue());
            }
            if (tile instanceof TileLaserTableBase && getConfig("avgLaserEnergy")){
                tag.removeTag(maxEnergy);
                tag.setInteger(energy, ((TileLaserTableBase) tile).getEnergy());
                tag.setInteger(avgEnergy, ((TileLaserTableBase) tile).getRecentEnergyAverage());
            }
        }
    }

    @Override
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config){
        currenttip.clear();
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(energy)){
                currenttip.add(ClientMessageHandler.getEnergyMessage()+tag.getInteger(energy)+"/"+tag.getInteger(maxEnergy)+" RF");
            }
            if (tag.hasKey(name)){
                String id = tag.getString(name);
                List<String> dummy = Lists.newArrayList();
                RedstoneBoardRegistry.instance.getRedstoneBoard(id).addInformation(null, Minecraft.getMinecraft().thePlayer, dummy, true);
                if (!dummy.isEmpty()) {
                    String desc = dummy.get(0);
                    currenttip.add(ClientMessageHandler.WailaSpecialChars.ALIGNCENTER + EnumChatFormatting.getTextWithoutFormattingCodes(desc));
                }
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world){
        if (ent != null && tag != null){
            if (ent instanceof EntityRobotBase){
                tag.setInteger(energy, ((EntityRobotBase) ent).getEnergy());
                tag.setInteger(maxEnergy, ((EntityRobotBase) ent).getBattery().getMaxEnergyStored());
            }
            if (ent instanceof EntityRobot){
                RedstoneBoardRobotNBT robotNBT = ((EntityRobot) ent).board.getNBTHandler();
                if (robotNBT != RedstoneBoardRegistry.instance.getEmptyRobotBoard()){
                    tag.setString(name, robotNBT.getID());
                }
            }
        }
        return tag;
    }

}
