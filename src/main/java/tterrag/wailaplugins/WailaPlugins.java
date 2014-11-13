package tterrag.wailaplugins;

import static tterrag.wailaplugins.WailaPlugins.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tterrag.core.IModTT;
import tterrag.core.common.Lang;
import tterrag.wailaplugins.config.WPConfigHandler;
import tterrag.wailaplugins.plugins.Plugins;
import tterrag.wailaplugins.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, guiFactory = "tterrag.wailaplugins.config.WPConfigFactory")
public class WailaPlugins implements IModTT
{
    public static final String MODID   = "wailaplugins";
    public static final String NAME    = "WAILA Plugins";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "after:ttCore;after:Waila";

    @SidedProxy(serverSide = "tterrag.wailaplugins.proxy.CommonProxy", clientSide = "tterrag.wailaplugins.proxy.ClientProxy")
    public static CommonProxy proxy;

    public static final Logger logger = LogManager.getLogger(NAME);
    
    public static final Lang lang = new Lang("wp.hud.msg");
    
    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        WPConfigHandler.INSTANCE.initialize(event.getSuggestedConfigurationFile());
        
        Plugins.INSTANCE.preInit();
    }
    
    @Override
    public String modid()
    {
        return MODID;
    }
    
    @Override
    public String name()
    {
        return NAME;
    }
    
    @Override
    public String version()
    {
        return VERSION;
    }
}
