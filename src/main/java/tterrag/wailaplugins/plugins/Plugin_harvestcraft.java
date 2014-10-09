package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ConfigHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.pam.harvestcraft.BlockPamFruit;

public class Plugin_harvestcraft extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(this, BlockPamFruit.class);
        ConfigHandler.instance().addConfig("general", "showcrop", "Show Fruits");
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> toolTip, IWailaDataAccessor accessor)
    {
        if (accessor.getBlock() instanceof BlockPamFruit)
        {
            float growthValue = (accessor.getMetadata() / 2.0F) * 100.0F;
            if (growthValue < 100)
                toolTip.add(String.format("%s : %.0f %%", StatCollector.translateToLocal("hud.msg.growth"), growthValue));
            else
                toolTip.add(String.format("%s : %s", StatCollector.translateToLocal("hud.msg.growth"), StatCollector.translateToLocal("hud.msg.mature")));
        }
    }
}