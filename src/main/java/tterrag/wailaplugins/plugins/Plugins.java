package tterrag.wailaplugins.plugins;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;
import tterrag.wailaplugins.config.WPConfigHandler;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public enum Plugins
{
    INSTANCE;
    
    public List<IPlugin> allPlugins = Lists.newArrayList();

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

        for (ClassInfo info : classes)
        {
            if (!info.getName().equals(this.getClass().getName()) && !info.getName().equals(PluginBase.class.getName()))
            {
                boolean failed = false;

                String modid = getModid(info);
                if (WPConfigHandler.INSTANCE.isPluginEnabled(modid) && Loader.isModLoaded(modid))
                {
                    WailaPlugins.logger.info("Attempting to load plugin for " + modid + ".");
                    try
                    {
                        Class<?> clazz = info.load();
                        if (IPlugin.class.isAssignableFrom(clazz))
                        {
                            try
                            {
                                IPlugin inst = (IPlugin) clazz.newInstance();
                                cfg.addConfig(WailaPlugins.NAME, modid, getModContainerFromID(modid).getName());
                                inst.load(ModuleRegistrar.instance());
                                allPlugins.add(inst);
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
                    }
                    catch (Throwable e) // Yes throwable is ugly but Exception won't catch NoClassDefFoundError
                    {
                        WailaPlugins.logger.fatal("Plugin {} threw an error on load. Skipping...", modid);
                        e.printStackTrace();
                        failed = true;
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
                    String err = !Loader.isModLoaded(modid)
                            ? "Skipping over plugin {} as its dependency was not found."
                            : "Skipping over plugin {} as it was disabled.";
                    
                    WailaPlugins.logger.info(err, modid);
                }
            }
        }
    }
    
    public void postInit()
    {
        for (IPlugin p : allPlugins)
        {
            p.postLoad();
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
