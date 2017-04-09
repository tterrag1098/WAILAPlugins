// TODO dead?
//package tterrag.wailaplugins.plugins;
//
//import java.util.List;
//import java.util.Map;
//
//import mcp.mobius.waila.api.IWailaDataAccessor;
//import mcp.mobius.waila.api.IWailaRegistrar;
//import net.minecraft.block.Block;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.EnumChatFormatting;
//
//import org.apache.commons.lang3.StringUtils;
//
//import tterrag.wailaplugins.api.Plugin;
//import flaxbeard.steamcraft.api.CrucibleLiquid;
//import flaxbeard.steamcraft.block.BlockSteamGauge;
//import flaxbeard.steamcraft.block.BlockSteamTank;
//import flaxbeard.steamcraft.block.BlockSteamcraftCrucible;
//import flaxbeard.steamcraft.block.BlockValvePipe;
//import flaxbeard.steamcraft.tile.TileEntityCrucible;
//import flaxbeard.steamcraft.tile.TileEntitySteamGauge;
//import flaxbeard.steamcraft.tile.TileEntitySteamTank;
//import flaxbeard.steamcraft.tile.TileEntityValvePipe;
//
//@Plugin(deps = "Steamcraft")
//public class PluginSteamcraft extends PluginBase
//{
//    @Override
//    public void load(IWailaRegistrar registrar)
//    {
//        super.load(registrar);
//        
//        registerBody(BlockSteamcraftCrucible.class, BlockSteamGauge.class, BlockValvePipe.class, BlockSteamTank.class);
//        
//        addConfig("crucible");
//        addConfig("gauge");
//        addConfig("valve");
//        addConfig("tank");
//    }
//
//    @Override
//    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
//    {        
//        Block block = accessor.getBlock();
//        if (block instanceof BlockSteamcraftCrucible)
//        {
//            Map<CrucibleLiquid, Integer> map = ((TileEntityCrucible) accessor.getTileEntity()).number;
//            for (CrucibleLiquid liquid : map.keySet())
//            {
//                currenttip.add(StringUtils.capitalize(liquid.name) + ": " + map.get(liquid) + "mB");
//            }
//        }
//        else if (block instanceof BlockSteamGauge)
//        {
//            TileEntitySteamGauge te = (TileEntitySteamGauge) accessor.getTileEntity();
//            int percent = Math.round(te.getPressure() * 100);
//            String pressure = (percent > 100 ? EnumChatFormatting.RED : "") + "" + percent +  "% "; 
//            currenttip.add(pressure + lang.localize("pressure"));
//        }
//        else if (block instanceof BlockValvePipe)
//        {
//            currenttip.add(((TileEntityValvePipe)accessor.getTileEntity()).open ? lang.localize("open") : lang.localize("closed")); 
//        }
//        else if (block instanceof BlockSteamTank)
//        {
//            TileEntitySteamTank te = (TileEntitySteamTank) accessor.getTileEntity();
//            currenttip.add((te.getPressure() > 1 ? EnumChatFormatting.RED : "") + ((int) (te.getCapacity() * te.getPressure()) + " mB"));
//        }
//    }
//}
