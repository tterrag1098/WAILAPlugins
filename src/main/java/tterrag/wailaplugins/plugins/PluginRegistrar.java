package tterrag.wailaplugins.plugins;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.config.WPConfigHandler;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public enum PluginRegistrar
{
    INSTANCE;

    public List<IPlugin> allPlugins = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public void preInit(FMLPreInitializationEvent event)
    {
        Set<ASMData> datas = event.getAsmData().getAll(Plugin.class.getName());
        ConfigHandler cfg = ConfigHandler.instance();

        WailaPlugins.logger.info("Beginning plugin registration: {} candidates found.", datas.size());

        for (ASMData data : datas)
        {
            Map<String, Object> annotationInfo = data.getAnnotationInfo();
            String name = (String) annotationInfo.get("name");
            if (Strings.isNullOrEmpty(name))
            {
                name = null;
            }
            
            List<String> deps = (List<String>) annotationInfo.get("deps");

            if (allModsLoaded(deps))
            {
                if (name == null)
                {
                    if (deps.isEmpty())
                    {
                        throw new IllegalArgumentException("Name and deps cannot both be null! Culprit: " + data.getClassName());
                    }
                    name = getModContainerFromID(deps.get(0)).getName();
                }
                
                boolean failed = false;

                if (WPConfigHandler.INSTANCE.isPluginEnabled(name))
                {
                    WailaPlugins.logger.info("Attempting to create plugin {}.", name);
                    try
                    {
                        Class<?> clazz = Class.forName(data.getClassName());
                        if (IPlugin.class.isAssignableFrom(clazz))
                        {
                            try
                            {
                                IPlugin inst = (IPlugin) clazz.newInstance();
                                cfg.addConfig(WailaPlugins.NAME, name, name);
                                allPlugins.add(inst);
                            }
                            catch (IllegalAccessException e)
                            {
                                WailaPlugins.logger.error("Construtor for class " + clazz.getName() + " could not be accessed.");
                                failed = true;
                            }
                            catch (InstantiationException e)
                            {
                                WailaPlugins.logger.error("Class " + clazz.getName() + " does not have a default constructor");
                                failed = true;
                            }
                        }
                        else
                        {
                            WailaPlugins.logger.error("Class " + clazz.getName() + " does not implement IPlugin and could not be loaded.");
                        }
                    }
                    catch (Throwable e) // Yes throwable is ugly but Exception won't catch NoClassDefFoundError
                    {
                        WailaPlugins.logger.fatal("Plugin {} threw an error on classload. Skipping...", name);
                        e.printStackTrace();
                        failed = true;
                    }

                    if (failed)
                    {
                        WailaPlugins.logger.fatal("Failed to create plugin {}.", name);
                    }
                    else
                    {
                        WailaPlugins.logger.info("Successfully created plugin {}.", name);
                    }
                }
                else
                {
                    WailaPlugins.logger.info("Skipping over plugin {} as it was disabled.", name);
                }
            }
            else
            {
                if (name == null)
                {
                    name = deps.get(0);
                }
                String[] unfound = getUnfoundDeps(deps);
                WailaPlugins.logger.info("Skipping over plugin {} as its dependencies {} were not found.", name, unfound);
            }
        }

        WailaPlugins.logger.info("Sorting plugins. Before: {}", allPlugins);
        Collections.sort(allPlugins, new Comparator<IPlugin>()
        {
            @Override
            public int compare(IPlugin o1, IPlugin o2)
            {
                Plugin order1 = o1.getClass().getAnnotation(Plugin.class);
                Plugin order2 = o2.getClass().getAnnotation(Plugin.class);

                int i1 = order1 == null ? 0 : order1.order();
                int i2 = order2 == null ? 0 : order2.order();

                return Double.compare(i1, i2);
            }
        });
        WailaPlugins.logger.info("Sorting plugins. After:  {}", allPlugins);

        for (IPlugin p : allPlugins)
        {
            String name = getPluginName(p.getClass());
            try
            {
                p.load(ModuleRegistrar.instance());
                WailaPlugins.logger.info("Successfully loaded plugin {}.", name);
            }
            catch (Throwable e)
            {
                WailaPlugins.logger.fatal("Plugin {} threw an error on init. Skipping...", name);
                e.printStackTrace();
            }
        }

        WailaPlugins.logger.info("Completed plugin registration. {} plugins registered.", allPlugins.size());
    }

    private boolean allModsLoaded(Iterable<String> mods)
    {
        if (mods == null)
        {
            return true;
        }
        for (String s : mods)
        {
            if (!Loader.isModLoaded(s))
            {
                return false;
            }
        }
        return true;
    }

    private String[] getUnfoundDeps(Iterable<String> mods)
    {
        if (mods == null)
        {
            return new String[0];
        }
        List<String> ret = Lists.newArrayList();
        for (String s : mods)
        {
            if (!Loader.isModLoaded(s))
            {
                ret.add(s);
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    public void postInit()
    {
        for (IPlugin p : allPlugins)
        {
            p.postLoad();
        }
    }

    public static String getPluginName(Class<?> c)
    {
        Plugin p = c.getAnnotation(Plugin.class);
        if (p == null)
        {
            return null;
        }
        String name = p.name();
        if (name.isEmpty() && p.deps().length > 0)
        {
            name = p.deps()[0];
        }
        return name;
    }

    public static ModContainer getModContainerFromID(String modid)
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
