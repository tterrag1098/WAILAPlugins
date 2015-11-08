package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import elec332.cmip.util.Config;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

/**
 * Created by Elec332 on 9-10-2015.
 */
@Plugin(deps = "IC2")
public class PluginIC2 extends WailaPluginBase {

    @Override
    public void load() {
        registerBody(TileEntityElectricMachine.class, IReactor.class, IReactorChamber.class);
        registerNBT(TileEntityElectricMachine.class, IReactor.class, IReactorChamber.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){

            if (tag.hasKey(tier)){
                currenttip.add(ClientMessageHandler.getEnergyMessage()+tag.getDouble(energy)+"/"+tag.getDouble(maxEnergy)+" EU");
                currenttip.add(ClientMessageHandler.getEnergyTierMessage()+tag.getInteger(tier));
            }

            if (tag.hasKey(progress)){
                currenttip.add(ClientMessageHandler.getProgressMessage()+ClientMessageHandler.format(tag.getFloat(progress)*100)+"%");
            }

            if (tag.hasKey(tpLoc)){
                int[] loc = tag.getIntArray(tpLoc);
                currenttip.add(ClientMessageHandler.getTargetMessage()+loc[0]+","+loc[1]+","+loc[2]);
            }

            if (tag.hasKey(heat)){
                currenttip.add(ClientMessageHandler.getHeatMessage()+tag.getInteger(heat));
                if (tag.hasKey(maxHeat)){
                    currenttip.add(ClientMessageHandler.getMaxMessage() + ClientMessageHandler.getHeatMessage() + tag.getInteger(maxHeat));
                }
            }

        }
    }

    @Override
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null){

            //Electric machines
            if (tile instanceof TileEntityElectricMachine){
                tag.setDouble(energy, ((TileEntityElectricMachine) tile).energy);
                tag.setInteger(maxEnergy, ((TileEntityElectricMachine) tile).maxEnergy);
                tag.setInteger(tier, ((TileEntityElectricMachine) tile).getSinkTier());
            }
            if (tile instanceof TileEntityStandardMachine && Config.WAILA.IC2.showProgress){
                tag.setFloat(progress, ((TileEntityStandardMachine) tile).getProgress());
            }
            if (tile instanceof TileEntityTeleporter && Config.WAILA.IC2.tpLocation){
                tag.setIntArray(tpLoc, new int[]{((TileEntityTeleporter) tile).targetX, ((TileEntityTeleporter) tile).targetY, ((TileEntityTeleporter) tile).targetZ});
            }

            //Heat stuff
            if (tile instanceof TileEntityHeatSourceInventory){
                tag.setInteger(heat, ((TileEntityHeatSourceInventory) tile).getHeatBuffer());
            }

            //Reactor stuff
            if (tile instanceof IReactor || tile instanceof IReactorChamber){
                IReactor reactor;
                if (tile instanceof IReactorChamber){
                    reactor = ((IReactorChamber) tile).getReactor();
                } else {
                    reactor = (IReactor) tile;
                }
                if (reactor != null) {
                    tag.setInteger(heat, reactor.getHeat());
                    tag.setInteger(maxHeat, reactor.getMaxHeat());
                }
            }

        }
    }

}
