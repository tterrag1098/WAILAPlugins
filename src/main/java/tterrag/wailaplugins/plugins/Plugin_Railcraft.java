package tterrag.wailaplugins.plugins;

import java.text.DecimalFormat;
import java.util.List;

import cofh.api.energy.IEnergyHandler;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mods.railcraft.api.electricity.IElectricGrid;
import mods.railcraft.common.blocks.machine.TileMachineBase;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.beta.TileBoiler;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFirebox;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFireboxLiquid;
import mods.railcraft.common.blocks.machine.beta.TileEngine;
import mods.railcraft.common.blocks.machine.beta.TileEngineSteam;
import mods.railcraft.common.blocks.machine.beta.TileEngineSteamHobby;
import mods.railcraft.common.blocks.tracks.TileTrack;
import mods.railcraft.common.blocks.tracks.TrackElectric;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.carts.EntityLocomotiveElectric;
import mods.railcraft.common.carts.EntityLocomotiveSteam;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.BoilerFuelTank;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.fluids.tanks.StandardTank;
import mods.railcraft.common.items.ItemElectricMeter;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tterrag.core.common.util.TTItemUtils;
import tterrag.wailaplugins.config.WPConfigHandler;

public class Plugin_Railcraft extends PluginBase implements IWailaEntityProvider
{
    private static final DecimalFormat fmtCharge = new DecimalFormat("#.##");
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        
        registerBody(TileMachineBase.class, TileTrack.class);
        
        registerEntityBody(this, EntityLocomotive.class);
        
        syncNBT(TileEngineSteam.class, TileBoilerFirebox.class, IElectricGrid.class, TileTrack.class, EntityLocomotive.class);
        
        addConfig("multiblocks");
        addConfig("heat");
        addConfig("tanks");
        addConfig("energy");
        addConfig("engines");
        addConfig("charge");
        addConfig("locomotives");
    }
    
    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();
        ItemStack current = accessor.getPlayer().getCurrentEquippedItem();
        
        boolean hasMeter = !WPConfigHandler.meterInHand || (current != null && TTItemUtils.stacksEqual(current, ItemElectricMeter.getItem()));
        
        if (tile instanceof TileMultiBlock && getConfig("multiblocks"))
        {
            currenttip.add(String.format(lang.localize("formed"), lang.localize(((TileMultiBlock)tile).isStructureValid() ? "yes" : "no")));
        }

        if ((tile instanceof TileEngineSteamHobby || tile instanceof TileBoilerFirebox) && getConfig("heat"))
        {
            addHeatTooltip(currenttip, tag);
        }
        
        if (tile instanceof IEnergyHandler && getConfig("energy") && tag.hasKey("Energy") && !(tile instanceof TileMultiBlock /* :( */))
        {
            int energy = tag.getInteger("Energy");
            currenttip.add(energy + " / " + ((IEnergyHandler) tile).getMaxEnergyStored(ForgeDirection.UP) + " RF");
        }
        
        if (tile instanceof TileEngine && getConfig("engines"))
        {
            int energy  = tag.getInteger("energyRF");
            int gen     = Math.round(tag.getFloat("currentOutput"));
            
            currenttip.add(energy + " / " + ((TileEngine)tile).maxEnergy() + " RF");
            currenttip.add(String.format(lang.localize("generating"), gen));
        }
        
        if (getConfig("tanks"))
        {
            if (tile instanceof TileEngineSteamHobby)
            {
                currenttip.add("");

                TankManager manager = new TankManager(
                        new FilteredTank(((TileEngineSteam) tile).getTankManager().get(0).getCapacity(), Fluids.STEAM.get(), tile),
                        new FilteredTank(((TileEngineSteamHobby) tile).getTankManager().get(1).getCapacity(), Fluids.WATER.get(), tile)
                );

                manager.readTanksFromNBT(tag);
                if (!addTankTooltip(currenttip, manager))
                {
                    currenttip.remove("");
                }
            }
            else if (tile instanceof TileEngineSteam)
            {
                currenttip.add("");

                TankManager manager = new TankManager(
                        new FilteredTank(((TileEngineSteam) tile).getTankManager().get(0).getCapacity(), Fluids.STEAM.get(), tile)
                );

                manager.readTanksFromNBT(tag);
                if (!addTankTooltip(currenttip, manager))
                {
                    currenttip.remove("");
                }
            }

            if (tile instanceof TileBoilerFireboxLiquid)
            {
                currenttip.add("");

                TankManager manager = new TankManager(
                        new FilteredTank(((TileBoiler) tile).getTankManager().get(0).getCapacity(), Fluids.WATER.get(), tile), 
                        new FilteredTank(((TileBoiler) tile).getTankManager().get(1).getCapacity(), Fluids.STEAM.get(), tile), 
                        new BoilerFuelTank(((TileBoiler) tile).getTankManager().get(2).getCapacity(), tile)
                );

                manager.readTanksFromNBT(tag);
                if (!addTankTooltip(currenttip, manager))
                {
                    currenttip.remove("");
                }
            }
            else if (tile instanceof TileBoilerFirebox)
            {
                currenttip.add("");

                TankManager manager = new TankManager(
                        new FilteredTank(((TileBoiler) tile).getTankManager().get(0).getCapacity(), Fluids.WATER.get(), tile),
                        new FilteredTank(((TileBoiler) tile).getTankManager().get(1).getCapacity(), Fluids.STEAM.get(), tile)
                );

                manager.readTanksFromNBT(tag);
                if (!addTankTooltip(currenttip, manager))
                {
                    currenttip.remove("");
                }
            }
        }
        
        if (getConfig("charge") && ((tile instanceof IElectricGrid && tag.hasKey("chargeHandler")) || (tile instanceof TileTrack && ((TileTrack)tile).getTrackInstance() instanceof TrackElectric)))
        {
            addChargeTooltip(currenttip, tag, hasMeter);
        }
    }
    
    private static void addChargeTooltip(List<String> currenttip, NBTTagCompound tag, boolean hasMeter)
    {
        double charge = tag.getCompoundTag("chargeHandler").getDouble("charge");
        String chargeFmt = fmtCharge.format(charge) + "c";
        
        currenttip.add(EnumChatFormatting.RESET + String.format(lang.localize("charge"), hasMeter ? chargeFmt : (EnumChatFormatting.ITALIC + lang.localize("needMeter"))));
    }
    
    private static void addHeatTooltip(List<String> currenttip, NBTTagCompound tag)
    {
        int heat = Math.round(tag.getFloat("heat"));
        int max  = Math.round(tag.getFloat("maxHeat"));
                
        currenttip.add(String.format(lang.localize("engineTemp"), heat, max));
    }
    
    private static boolean addTankTooltip(List<String> currenttip, TankManager manager)
    {
        int idx = 0;
        StandardTank tank = null;
        boolean ret = false;
        while ((tank = manager.get(idx)) != null)
        {
            FluidStack stored = tank.getFluid();

            if (stored != null)
            {
                ret = true;
                currenttip.add(stored.amount + " / " + tank.getCapacity() + " mB " + stored.getLocalizedName());
            }

            idx++;
        }
        
        return ret;
    }

    @Override
    public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        NBTTagCompound tag = accessor.getNBTData();
        
        if (!getConfig("locomotives"))
        {
            return currenttip;
        }
        
        if (entity instanceof EntityLocomotiveElectric)
        {
            addChargeTooltip(currenttip, tag, true);
        }
        
        if (entity instanceof EntityLocomotiveSteam)
        {
            addHeatTooltip(currenttip, tag);
        }
        
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }
}
