package tterrag.wailaplugins.plugins;

import static tterrag.wailaplugins.WailaPlugins.*;

import java.util.List;
import java.util.Map;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import tterrag.wailaplugins.api.IPlugin;

import com.sun.xml.internal.ws.util.StringUtils;

import flaxbeard.steamcraft.api.CrucibleLiquid;
import flaxbeard.steamcraft.block.BlockSteamGauge;
import flaxbeard.steamcraft.block.BlockSteamTank;
import flaxbeard.steamcraft.block.BlockSteamcraftCrucible;
import flaxbeard.steamcraft.block.BlockValvePipe;
import flaxbeard.steamcraft.tile.TileEntityCrucible;
import flaxbeard.steamcraft.tile.TileEntitySteamGauge;
import flaxbeard.steamcraft.tile.TileEntitySteamTank;
import flaxbeard.steamcraft.tile.TileEntityValvePipe;

public class PluginSteamcraft implements IPlugin
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(this, BlockSteamcraftCrucible.class);
        registrar.registerBodyProvider(this, BlockSteamGauge.class);
        registrar.registerBodyProvider(this, BlockValvePipe.class);
        registrar.registerBodyProvider(this, BlockSteamTank.class);
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        Block block = accessor.getBlock();
        if (block instanceof BlockSteamcraftCrucible)
        {
            Map<CrucibleLiquid, Integer> map = ((TileEntityCrucible) accessor.getTileEntity()).number;
            for (CrucibleLiquid liquid : map.keySet())
            {
                currenttip.add(StringUtils.capitalize(liquid.name) + ": " + map.get(liquid) + "mB");
            }
        }
        else if (block instanceof BlockSteamGauge)
        {
            TileEntitySteamGauge te = (TileEntitySteamGauge) accessor.getTileEntity();
            int percent = Math.round(te.getPressure() * 100);
            String pressure = (percent > 100 ? EnumChatFormatting.RED : "") + "" + percent +  "% "; 
            currenttip.add(pressure + lang.localize("pressure"));
        }
        else if (block instanceof BlockValvePipe)
        {
            currenttip.add(((TileEntityValvePipe)accessor.getTileEntity()).open ? lang.localize("open") : lang.localize("closed")); 
        }
        else if (block instanceof BlockSteamTank)
        {
            TileEntitySteamTank te = (TileEntitySteamTank) accessor.getTileEntity();
            currenttip.add((te.getPressure() > 1 ? EnumChatFormatting.RED : "") + ((int) (te.getCapacity() * te.getPressure()) + " mB"));
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }
}
