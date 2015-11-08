package tterrag.wailaplugins.plugins;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

/**
 * Created by Elec332 on 6-10-2015.
 */
@Plugin(name = "Vanilla")
public class PluginVanilla extends WailaPluginBase implements IWailaEntityProvider{

    /*
    public ForgeWailaHandler(){
        ignored = Lists.newArrayList();
    }*/

    @Override
    public void load() {
        /*if (Config.WAILA.Forge.showTankInfo) {
            List<Class> toRemove = Lists.newArrayList();
            for (Class clazz : ModuleRegistrar.instance().bodyBlockProviders.keySet()) {
                if (IFluidHandler.class.isAssignableFrom(clazz)) {
                    toRemove.add(clazz);
                } /*else if (Block.class.isAssignableFrom(clazz)){
                for (Block block : GameData.getBlockRegistry().typeSafeIterable()){
                    if (ITileEntityProvider.class.isAssignableFrom(clazz)){
                        try {
                            TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null, 0);
                            if (IFluidHandler.class.isAssignableFrom(tile.getClass()))
                                toRemove.add(clazz);
                        } catch (Exception e){
                            //Ignore
                        }
                    } else if (block.hasTileEntity(0)){
                        try {
                            TileEntity tile = block.createTileEntity(null, 0);
                            if (IFluidHandler.class.isAssignableFrom(tile.getClass()))
                                toRemove.add(clazz);
                        } catch (Exception e){
                            //Ignore
                        }
                    }
                }
            }*//*
            }
            for (Class clazz : toRemove) {
                ModuleRegistrar.instance().bodyBlockProviders.remove(clazz);
            }
            registerHandler(Type.BODY, IFluidHandler.class);
            ignored.add("tconstruct.smeltery.logic.LavaTankLogic");
        }*/
        addConfig("showVillagerProfession");
        if (getConfig("showVillagerProfession")) {
            registerEntityBody(this, EntityVillager.class);
        }
    }
/*
    public List<String> ignored;

    @Override
    public void getWailaBody(List<String> currenttip, ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (isIgnored(accessor))
            return;
        List<FluidTankInfo> data = filter(((IFluidHandler) accessor.getTileEntity()).getTankInfo(accessor.getSide()));
        if (data.isEmpty()) {
            currenttip.add(ClientMessageHandler.getEmptyMessage());
        } else if (data.size() == 1){
            FluidTankInfo info = data.get(0);
            if (info.fluid == null || info.fluid.amount == 0) {
                currenttip.add(ClientMessageHandler.getEmptyMessage());
            } else {
                currenttip.add(ClientMessageHandler.getLiquidMessage() + info.fluid.getLocalizedName());
                currenttip.add(ClientMessageHandler.getAmountMessage() + info.fluid.amount + "/" + info.capacity);
            }
        } else {
            for (FluidTankInfo info : data){
                currenttip.add((info.fluid == null ? 0 : info.fluid.amount) + "/" + info.capacity + "mb" + (info.fluid == null ? "" : " " + info.fluid.getLocalizedName()));
            }
        }
    }

    private boolean isIgnored(IWailaDataAccessor accessor){
        return ignored.contains(accessor.getTileEntity().getClass().getCanonicalName());
    }

    private List<FluidTankInfo> filter(FluidTankInfo... data){
        List<FluidTankInfo> ret = Lists.newArrayList();
        if (data != null){
            for (FluidTankInfo fluidTankInfo : data){
                if (fluidTankInfo != null)
                    ret.add(fluidTankInfo);
            }
        }
        return ret;
    }
*/
    @Override
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config){
        if (entity instanceof EntityVillager){
            currenttip.add(ClientMessageHandler.getProfessionMessage()+((EntityVillager)entity).getProfession());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world){
        return tag;
    }

}
