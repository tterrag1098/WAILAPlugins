package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ConfigHandler;
import net.minecraft.item.ItemStack;
import tterrag.core.common.Lang;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;

public abstract class PluginBase implements IPlugin
{
    protected static final Lang lang = WailaPlugins.lang;
    protected static final Lang wailaLang = new Lang("hud.msg");
    private static final Lang configLang = new Lang("wp");
    
    private IWailaRegistrar reg;
    
    @Override
    public void load(IWailaRegistrar registrar)
    {
        this.reg = registrar;
    }
    
    protected void registerHead(Class<?>... classes)
    {
        for(Class<?> clazz : classes)
        {
            reg.registerHeadProvider(this, clazz);
        }
    }
    
    protected void registerBody(Class<?>... classes)
    {
        for(Class<?> clazz : classes)
        {
            reg.registerBodyProvider(this, clazz);
        }
    }
    
    protected void registerTail(Class<?>... classes)
    {
        for(Class<?> clazz : classes)
        {
            reg.registerTailProvider(this, clazz);
        }
    }
    
    protected void registerStack(Class<?>... classes)
    {
        for(Class<?> clazz : classes)
        {
            reg.registerStackProvider(this, clazz);
        }
    }
    
    protected void syncNBT(Class<?>... classes)
    {
        for (Class<?> clazz : classes)
        {
            reg.registerSyncedNBTKey("*", clazz);
        }
    }
    
    protected void addConfig(String key)
    {
        addConfig(key, true);
    }

    protected void addConfig(String key, boolean def)
    {
        ConfigHandler.instance().addConfig(Plugins.getModName(this.getClass()), getKey(key), configLang.localize(String.format("config.%s.%s", Plugins.getModid(getClass()), key)), def);
    }
    
    protected boolean getConfig(String key)
    {
        return ConfigHandler.instance().getConfig("modules", getKey(key), true);
    }
    
    private String getKey(String key)
    {
        return Plugins.getModid(this.getClass()) + ":" + key;
    }
    
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
