package tterrag.wailaplugins.plugins;

import com.cout970.magneticraft.api.electricity.IElectricConductor;
import com.cout970.magneticraft.api.electricity.IElectricMultiPart;
import com.cout970.magneticraft.api.electricity.IElectricTile;
import com.cout970.magneticraft.api.electricity.prefab.ElectricConductor;
import com.cout970.magneticraft.api.heat.IHeatConductor;
import com.cout970.magneticraft.api.heat.IHeatMultipart;
import com.cout970.magneticraft.api.heat.IHeatTile;
import com.cout970.magneticraft.api.pressure.IPressureConductor;
import com.cout970.magneticraft.api.pressure.IPressureMultipart;
import com.cout970.magneticraft.api.pressure.IPressurePipe;
import com.cout970.magneticraft.api.util.VecInt;
import com.cout970.magneticraft.util.tile.TileConductorLow;
import com.cout970.magneticraft.util.tile.TileConductorMedium;
import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.util.List;

/**
 * Created by Elec332 on 12-10-2015.
 */
@Plugin(deps = "Magneticraft")
public class PluginMagnetiCraft extends WailaPluginBase {

    @Override
    public void load() {
        addConfig("showHeat");
        registerBody(IPressurePipe.class,IHeatTile.class, IElectricTile.class);
        registerNBT(IPressurePipe.class, IHeatTile.class, IElectricTile.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            if (tag.hasKey(pressure)){
                currenttip.add(ClientMessageHandler.getPressureMessage()+ClientMessageHandler.format(tag.getDouble(pressure))+"/"+ClientMessageHandler.format(tag.getDouble(maxPressure)));
                currenttip.add(ClientMessageHandler.getFluidMessage().replace(": ", "")+ClientMessageHandler.getTemperatureMessage()+ClientMessageHandler.format(tag.getDouble(temperature)));
                currenttip.add(ClientMessageHandler.getFluidMessage()+tag.getString(fluid));
            }
            if (tag.hasKey(heat)){
                currenttip.add(ClientMessageHandler.getHeatMessage()+ClientMessageHandler.format(tag.getDouble(heat)));
            }
            if (tag.hasKey(specialData1)){
                if (tag.hasKey(energy)) {
                    currenttip.add(ClientMessageHandler.getEnergyMessage() + ClientMessageHandler.format(tag.getDouble(energy)) + "/" + ClientMessageHandler.format(tag.getDouble(maxEnergy)));
                }
                currenttip.add(ClientMessageHandler.getVoltageMessage()+ClientMessageHandler.format(tag.getDouble(specialData1)));
                currenttip.add(ClientMessageHandler.getResistanceMessage()+ClientMessageHandler.format(tag.getDouble(specialData2)));
                currenttip.add(ClientMessageHandler.getEnergyTierMessage()+tag.getInteger(tier));
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof IPressureMultipart || tile instanceof IPressurePipe){
                IPressureConductor pressureConductor;
                if (tile instanceof IPressurePipe){
                    pressureConductor = ((IPressurePipe) tile).getPressureConductor()[0];
                } else {
                    System.out.println("ERROR: Received request from multipart!");
                    pressureConductor = ((IPressureMultipart) tile).getPressureConductor();
                }
                if (pressureConductor != null){
                    tag.setDouble(pressure, pressureConductor.getPressure());
                    tag.setDouble(maxPressure, pressureConductor.getMaxPressure());
                    tag.setDouble(temperature, pressureConductor.getTemperature());
                    tag.setString(fluid, pressureConductor.getFluid().getName());
                }
            }
            if ((tile instanceof IHeatMultipart || tile instanceof IHeatTile) && getConfig("showHeat")){
                IHeatConductor heatConductor;
                if (tile instanceof IHeatTile){
                    heatConductor = ((IHeatTile) tile).getHeatCond(new VecInt(tile))[0];
                } else {
                    System.out.println("ERROR: Received request from multipart!");
                    heatConductor = ((IHeatMultipart) tile).getHeatConductor();
                }
                if (heatConductor != null){
                    tag.setDouble(heat, heatConductor.getTemperature());
                }
            }
            if (tile instanceof IElectricTile || tile instanceof IElectricMultiPart){
                IElectricConductor electricConductor = null;
                int tier = -1;
                if (tile instanceof IElectricTile){
                    VecInt loc = VecInt.NULL_VECTOR;//new VecInt(tile);
                    if (tile instanceof TileConductorLow){
                        electricConductor = ((TileConductorLow) tile).getConds(loc, 0)[0];
                        tier = electricConductor.getTier();
                    }
                    if (tile instanceof TileConductorMedium){
                        electricConductor = ((TileConductorMedium) tile).getConds(loc, 2)[0];
                        tier = electricConductor.getTier();
                    }
                } else {
                    System.out.println("ERROR: Received request from multipart!");
                }
                if (electricConductor != null && tier != -1){
                    if (!(electricConductor instanceof ElectricConductor) && electricConductor.getMaxStorage() != 0){
                        tag.setDouble(energy, electricConductor.getStorage());
                        tag.setDouble(maxEnergy, electricConductor.getMaxStorage());
                    }
                    tag.setDouble(specialData1, electricConductor.getVoltage());
                    tag.setDouble(specialData2, electricConductor.getResistance());
                    tag.setInteger(PluginMagnetiCraft.tier, tier);
                }
            }
        }
    }

}
