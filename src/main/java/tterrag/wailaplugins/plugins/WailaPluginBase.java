package tterrag.wailaplugins.plugins;

import com.enderio.core.common.Lang;
import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.impl.ConfigHandler;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;
import tterrag.wailaplugins.api.Plugin;

import java.util.List;

public abstract class WailaPluginBase implements IPlugin, IWailaDataProvider{

    protected static final Lang lang = WailaPlugins.lang;
    protected static final Lang wailaLang = new Lang("hud.msg");
    private static final Lang configLang = new Lang("wp");
    protected static final IWailaRegistrar registrar = ModuleRegistrar.instance();

    @Override
    public void postLoad() {
        ;
    }

    protected void registerHead(Class<?>... classes) {
        registerAll(RegType.HEAD, classes);
    }

    protected void registerBody(Class<?>... classes) {
        registerAll(RegType.BODY, classes);
    }

    protected void registerTail(Class<?>... classes) {
        registerAll(RegType.TAIL, classes);
    }

    protected void registerStack(Class<?>... classes) {
        registerAll(RegType.STACK, classes);
    }

    protected void registerNBT(Class<?>... classes) {
        registerAll(RegType.NBT, classes);
    }

    protected void registerEntityBody(IWailaEntityProvider inst, Class<?>... classes) {
        registerAll(RegType.ENTITY_BODY, classes);
    }

    protected void registerEntityNBT(IWailaEntityProvider inst, Class<?>... classes) {
        registerAll(RegType.ENTITY_NBT, classes);
    }

    protected void registerAll(RegType type, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            type.register(this, clazz);
        }
    }

    protected void addConfig(String key) {
        addConfig(key, true);
    }

    protected void addConfig(String key, boolean def) {
        ConfigHandler.instance().addConfig(PluginRegistrar.getPluginName(this.getClass()), getKey(key), configLang.localize(String.format("config.%s.%s", PluginRegistrar.getPluginName(getClass()), key)), def);
    }

    protected boolean getConfig(String key) {
        return ConfigHandler.instance().getConfig("modules", getKey(key), true);
    }

    private String getKey(String key) {
        return PluginRegistrar.getPluginName(this.getClass()) + ":" + key;
    }

    @Override
    public final ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (enabled()) {
            return getWailaStack(accessor);
        }
        return null;
    }

    protected ItemStack getWailaStack(IWailaDataAccessor accessor) {
        return null;
    }

    @Override
    public final List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (enabled()) {
            getHead(itemStack, currenttip, accessor);
        }

        return currenttip;
    }

    protected void getHead(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
    }

    @Override
    public final List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (enabled()) {
            getBody(itemStack, currenttip, accessor);
        }
        return currenttip;
    }

    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
    }

    @Override
    public final List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (enabled()) {
            getTail(itemStack, currenttip, accessor);
        }
        return currenttip;
    }

    protected void getTail(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
    }

    @Override
    public final NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
        if (enabled()) {
            getNBTData(te, tag, world, new BlockCoord(x, y, z));
            tag.setInteger("x", x);
            tag.setInteger("y", y);
            tag.setInteger("z", z);
        }
        return tag;
    }

    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
    }

    protected boolean enabled() {
        return ConfigHandler.instance().getConfig("modules", PluginRegistrar.getPluginName(this.getClass()), true);
    }

    @Override
    public String toString() {
        Plugin annot = getClass().getAnnotation(Plugin.class);
        if (annot == null) {
            return super.toString();
        }
        String ret = annot.name();
        return ret.isEmpty() ? PluginRegistrar.getModContainerFromID(annot.deps()[0]).getName() : ret;
    }

    protected enum RegType {
        // @formatter:off
        HEAD {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerHeadProvider(inst, c);
            }
        },
        BODY {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerBodyProvider(inst, c);
            }
        },
        TAIL {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerTailProvider(inst, c);
            }
        },
        NBT {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerNBTProvider(inst, c);
            }
        },
        STACK {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerStackProvider(inst, c);
            }
        },
        ENTITY_BODY {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerBodyProvider((IWailaEntityProvider) inst, c);
            }
        },
        ENTITY_NBT {
            void register(WailaPluginBase inst, Class<?> c) {
                registrar.registerNBTProvider((IWailaEntityProvider) inst, c);
            }
        };
        // @formatter:on

        abstract void register(WailaPluginBase inst, Class<?> c);
    }

    protected static final String energy, maxEnergy, tier, progress, tpLoc, heat, maxHeat, laser, avgEnergy, name, access,
            range, active, specialData1, energyOut, specialData2, specialData3, specialData4, specialData5,
            pressure, maxPressure, temperature, maxTemperature, fluid;

    static {
        energy = "energy";
        maxEnergy = "maxEnergy";
        tier = "tier";
        progress = "progress";
        tpLoc = "tpLoc";
        heat = "heat";
        maxHeat = "maxHeat";
        laser = "laser";
        avgEnergy = "avgEnergy";
        name = "name";
        access = "access";
        range = "range";
        active = "active";
        specialData1 = "specialData1";
        energyOut = "energyOut";
        specialData2 = "specialData2";
        specialData3 = "specialData3";
        specialData4 = "specialData4";
        specialData5 = "specialData5";
        pressure = "pressure";
        maxPressure = "maxPressure";
        temperature = "temperature";
        maxTemperature = "maxTemperature";
        fluid = "fluid";
    }

}
