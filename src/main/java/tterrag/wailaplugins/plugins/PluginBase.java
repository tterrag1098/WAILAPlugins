package tterrag.wailaplugins.plugins;

import java.util.List;

import com.enderio.core.common.Lang;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ConfigHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.IPlugin;
import tterrag.wailaplugins.api.Plugin;

public abstract class PluginBase implements IPlugin
{
    private enum RegType
    {
        // @formatter:off
        HEAD        { void register(PluginBase inst, Class<?> c) { inst.reg.registerHeadProvider(inst, c); }}, 
        BODY        { void register(PluginBase inst, Class<?> c) { inst.reg.registerBodyProvider(inst, c); }}, 
        TAIL        { void register(PluginBase inst, Class<?> c) { inst.reg.registerTailProvider(inst, c); }}, 
        NBT         { void register(PluginBase inst, Class<?> c) { inst.reg.registerNBTProvider(inst, c);  }}, 
        STACK       { void register(PluginBase inst, Class<?> c) { inst.reg.registerStackProvider(inst, c);}}, 
        ENTITY_BODY { void register(PluginBase inst, Class<?> c) { inst.reg.registerBodyProvider((IWailaEntityProvider) inst, c); }},
        ENTITY_NBT  { void register(PluginBase inst, Class<?> c) { inst.reg.registerNBTProvider((IWailaEntityProvider) inst, c); }};
        // @formatter:on
        
        abstract void register(PluginBase inst, Class<?> c);
    }
    
    protected static final Lang lang = WailaPlugins.lang;
    protected static final Lang wailaLang = new Lang("hud.msg");
    private static final Lang configLang = new Lang("wp");
    
    private IWailaRegistrar reg;
    
    @Override
    public void load(IWailaRegistrar registrar)
    {
        this.reg = registrar;
    }
    
    @Override
    public void postLoad()
    {
        ;
    }
    
    protected void registerHead(Class<?>... classes)
    {
        registerAll(RegType.HEAD, classes);
    }
    
    protected void registerBody(Class<?>... classes)
    {
        registerAll(RegType.BODY, classes);
    }
    
    protected void registerTail(Class<?>... classes)
    {
        registerAll(RegType.TAIL, classes);
    }

    protected void registerStack(Class<?>... classes)
    {
        registerAll(RegType.STACK, classes);
    }
    
    protected void registerNBT(Class<?>... classes)
    {
        registerAll(RegType.NBT, classes);
    }
    
    protected void registerEntityBody(IWailaEntityProvider inst, Class<?>... classes)
    {
        registerAll(RegType.ENTITY_BODY, classes);
    }
    
    protected void registerEntityNBT(IWailaEntityProvider inst, Class<?>... classes)
    {
        registerAll(RegType.ENTITY_NBT, classes);
    }
    
    protected void registerAll(RegType type, Class<?>... classes)
    {
        for (Class<?> clazz : classes)
        {
            type.register(this, clazz);
        }
    }
    
    protected void addConfig(String key)
    {
        addConfig(key, true);
    }

    protected void addConfig(String key, boolean def)
    {
        ConfigHandler.instance().addConfig("WP: " + PluginRegistrar.getPluginName(this.getClass()), getKey(key), configLang.localize(String.format("config.%s.%s", PluginRegistrar.getPluginName(getClass()), key)), def);
    }
    
    protected boolean getConfig(String key)
    {
        return ConfigHandler.instance().getConfig("modules", getKey(key), true);
    }
    
    private String getKey(String key)
    {
        return PluginRegistrar.getPluginName(this.getClass()) + ":" + key;
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
    
    @Override
    public final NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (enabled())
        {
            getNBTData(te, tag, world, pos);
            tag.setInteger("x", pos.getX());
            tag.setInteger("y", pos.getY());
            tag.setInteger("z", pos.getZ());
        }
        return tag;
    }
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {}

    protected boolean enabled()
    {
        return ConfigHandler.instance().getConfig("modules", PluginRegistrar.getPluginName(this.getClass()), true);
    }

    @Override
    public String toString()
    {
        Plugin annot = getClass().getAnnotation(Plugin.class);
        if (annot == null)
        {
            return super.toString();
        }
        String ret = annot.name();
        return ret.isEmpty() ? PluginRegistrar.getModContainerFromID(annot.deps()[0]).getName() : ret;
    }
}
