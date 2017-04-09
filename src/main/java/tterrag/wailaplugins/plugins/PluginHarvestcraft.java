// TODO maybe
//package tterrag.wailaplugins.plugins;
//
//import java.util.List;
//
//import tterrag.wailaplugins.api.Plugin;
//import mcp.mobius.waila.api.IWailaDataAccessor;
//import mcp.mobius.waila.api.IWailaRegistrar;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.StatCollector;
//
//import com.pam.harvestcraft.BlockPamFruit;
//
//@Plugin(name = "Pam's Fruit", deps = "harvestcraft")
//public class PluginHarvestcraft extends PluginBase
//{
//    @Override
//    public void load(IWailaRegistrar registrar)
//    {
//        super.load(registrar);
//        
//        registerBody(BlockPamFruit.class);
//    }
//
//    @Override
//    public void getBody(ItemStack itemStack, List<String> toolTip, IWailaDataAccessor accessor)
//    {
//        if (accessor.getBlock() instanceof BlockPamFruit)
//        {
//            float growthValue = (accessor.getMetadata() / 2.0F) * 100.0F;
//            if (growthValue < 100)
//                toolTip.add(String.format("%s : %.0f %%", StatCollector.translateToLocal("hud.msg.growth"), growthValue));
//            else
//                toolTip.add(String.format("%s : %s", StatCollector.translateToLocal("hud.msg.growth"), StatCollector.translateToLocal("hud.msg.mature")));
//        }
//    }
//}