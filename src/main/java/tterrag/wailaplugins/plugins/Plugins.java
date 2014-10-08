package tterrag.wailaplugins.plugins;

import java.io.IOException;
import java.util.Set;

import mcp.mobius.waila.api.impl.ModuleRegistrar;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.Loader;

public class Plugins
{
    private static Plugins instance = new Plugins();

    private Plugins()
    {}

    public static Plugins instance()
    {
        return instance;
    }

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

        for (ClassInfo info : classes)
        {
            if (!info.getName().equals(this.getClass().getName()))
            {
                boolean failed = false;

                String modid = info.getSimpleName().replace("Plugin", "");
                if (Loader.isModLoaded(modid))
                {
                    WailaPlugins.logger.info("Attempting to load plugin for " + modid + ".");
                    Class<?> clazz = info.load();
                    if (IPlugin.class.isAssignableFrom(clazz))
                    {
                        try
                        {
                            IPlugin inst = (IPlugin) clazz.newInstance();
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
}
