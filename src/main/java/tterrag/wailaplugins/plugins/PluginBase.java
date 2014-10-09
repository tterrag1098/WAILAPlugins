package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.impl.ConfigHandler;
import net.minecraft.item.ItemStack;
import tterrag.wailaplugins.api.IPlugin;

public abstract class PluginBase implements IPlugin
{
    @Override
    public final ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (enabled())
        {
            return getWailaStack(accessor);
        }
        return null;
    }
    protected ItemStack getWailaStack(IWailaDataAccessor accessor) { return null; }

    @Override
    public final List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (enabled())
        {
            getHead(itemStack, currenttip, accessor);
        }
        
        return currenttip;
    }
    protected void getHead(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {}

    @Override
    public final List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (enabled())
        {
            getBody(itemStack, currenttip, accessor);
        }
        return currenttip;
    }
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {}

    @Override
    public final List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (enabled())
        {
            getTail(itemStack, currenttip, accessor);
        }
        return currenttip;
    }
    protected void getTail(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {}

    protected boolean enabled()
    {
        return ConfigHandler.instance().getConfig("modules", Plugins.getModid(this.getClass()), true);
    }
}
