package tterrag.wailaplugins.plugins;

import java.io.IOException;
import java.util.Set;

import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.api.impl.ConfigModule;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;
import tterrag.wailaplugins.config.WPConfigHandler;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class Plugins
{
    public static final Plugins INSTANCE = new Plugins();

    private Plugins()
    {}

    public void preInit()
    {
        ClassPath classpath;

        try
        {
            classpath = ClassPath.from(WailaPlugins.class.getClassLoader());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        Set<ClassInfo> classes = classpath.getTopLevelClassesRecursive("tterrag.wailaplugins.plugins");
        
        ConfigHandler cfg = ConfigHandler.instance();
        cfg.addModule(WailaPlugins.MODID, new ConfigModule(WailaPlugins.MODID));

        for (ClassInfo info : classes)
        {
            if (!info.getName().equals(this.getClass().getName()) && !info.getName().equals(PluginBase.class.getName()))
            {
                boolean failed = false;

                String modid = getModid(info);
                if (WPConfigHandler.INSTANCE.isPluginEnabled(modid) && Loader.isModLoaded(modid))
                {
                    WailaPlugins.logger.info("Attempting to load plugin for " + modid + ".");
                    Class<?> clazz = info.load();
                    if (IPlugin.class.isAssignableFrom(clazz))
                    {
                        try
                        {
                            IPlugin inst = (IPlugin) clazz.newInstance();
                            cfg.addConfig(WailaPlugins.NAME, modid, getModContainerFromID(modid).getName());
                            inst.load(ModuleRegistrar.instance());
                        }
                        catch (IllegalAccessException e)
                        {
                            WailaPlugins.logger.error("Construtor for class " + info.getName() + " could not be accessed.");
                            failed = true;
                        }
                        catch (InstantiationException e)
                        {
                            WailaPlugins.logger.error("Class " + info.getName() + " does not have a default constructor");
                            failed = true;
                        }
                    }
                    else
                    {
                        WailaPlugins.logger.error("Class " + info.getName() + " does not implement IPlugin and could not be loaded.");
                    }

                    if (failed)
                    {
                        WailaPlugins.logger.fatal("Failed to load plugin for " + modid + ".");
                    }
                    else
                    {
                        WailaPlugins.logger.info("Successfully loaded plugin for " + modid + ".");
                    }
                }
                else
                {
                    WailaPlugins.logger.info("Skipping over plugin " + info.getName() + " as its dependency was not found.");
                }
            }
        }
    }
    
    public static String getModid(Class<?> c)
    {
        return getModid(c.getSimpleName());
    }
    
    public static String getModid(ClassInfo c)
    {
        return getModid(c.getSimpleName());
    }
    
    public static String getModName(Class<?> c)
    {
        return getModContainerFromID(getModid(c)).getName();
    }
    
    public static String getModName(ClassInfo c)
    {
        return getModContainerFromID(getModid(c)).getName();
    }
    
    private static String getModid(String className)
    {
        return className.replace("Plugin_", "");
    }
    
    private static ModContainer getModContainerFromID(String modid)
    {
        for (ModContainer c : Loader.instance().getModList())
        {
            if (c.getModId().equals(modid))
            {
                return c;
            }
        }
        return null;
    }
}
