package tterrag.wailaplugins.plugins;

import java.text.DecimalFormat;
import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mods.railcraft.api.electricity.IElectricGrid;
import mods.railcraft.common.blocks.machine.TileMachineBase;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.alpha.TileCokeOven;
import mods.railcraft.common.blocks.machine.beta.TileBoiler;
import mods.railcraft.common.blocks.machine.beta.TileBoilerFirebox;
import mods.railcraft.common.blocks.machine.beta.TileBoilerTank;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tterrag.core.common.util.BlockCoord;
import tterrag.core.common.util.TTItemUtils;
import tterrag.wailaplugins.config.WPConfigHandler;
import cofh.api.energy.IEnergyHandler;

public class Plugin_Railcraft extends PluginBase implements IWailaEntityProvider
{
    private static final DecimalFormat fmtCharge = new DecimalFormat("#.##");
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        
        registerBody(TileMachineBase.class, TileTrack.class);
        
        registerEntityBody(this, EntityLocomotive.class);
                
        registerNBT(TileEngineSteam.class, IElectricGrid.class, TileTrack.class, EntityLocomotive.class, TileMultiBlock.class);
        
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

        if ((tile instanceof TileEngineSteamHobby || tile instanceof TileBoilerFirebox || tile instanceof TileBoilerTank) && getConfig("heat"))
        {
            addHeatTooltip(currenttip, tag);
        }
        
        if (tile instanceof IEnergyHandler && getConfig("energy") && tag.hasKey("Energy"))
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
            int end = currenttip.size();
            TankManager dummy = null;
            Fluids[] fluids = null;

            if (tile instanceof TileCokeOven)
            {
                dummy = ((TileCokeOven) tile).getTankManager();
                fluids = new Fluids[] {Fluids.CREOSOTE};
            }
            else if (tile instanceof TileEngineSteamHobby)
            {                
                dummy = ((TileEngineSteamHobby) tile).getTankManager();
                fluids = new Fluids[] {Fluids.STEAM, Fluids.WATER};
            }
            else if (tile instanceof TileEngineSteam)
            {
                dummy = ((TileEngineSteam) tile).getTankManager();
                fluids = new Fluids[] {Fluids.STEAM};
            }
            // we do ID checks for multiblocks that are passing in the data of the master block
            else if ("RCBoilerFireboxLiquidTile".equals(tag.getString("id")))
            {
                // this needs to be custom for the non-filtered tank
                dummy = ((TileBoiler)tile).getTankManager();
                
                if (dummy != null)
                {
                    TankManager manager = new TankManager(
                            new FilteredTank(dummy.get(0).getCapacity(), Fluids.WATER.get(), tile), 
                            new FilteredTank(dummy.get(1).getCapacity(), Fluids.STEAM.get(), tile), 
                            new BoilerFuelTank(dummy.get(2).getCapacity(), tile)
                    );

                    manager.readTanksFromNBT(tag);
                    if (addTankTooltip(currenttip, manager))
                    {
                        currenttip.add(end, "");
                    }
                }
            }
            else if ("RCBoilerFireboxSoildTile".equals(tag.getString("id"))) // TYPOS D:<
            {
                dummy = ((TileBoiler)tile).getTankManager();
                fluids = new Fluids[] {Fluids.WATER, Fluids.STEAM};
            }
            
            if (fluids != null && dummy != null)
            {
                if (addTankTooltip(currenttip, getTankManager(tile, tag, dummy, fluids)))
                {
                    currenttip.add(end, "");
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
    
    private static TankManager getTankManager(TileEntity tile, NBTTagCompound tag, TankManager dummy, Fluids... fluids)
    {
        FilteredTank[] tanks = new FilteredTank[fluids.length];
        for (int i = 0; i < tanks.length; i++)
        {
            tanks[i] = new FilteredTank(dummy.get(i).getCapacity(), fluids[i].get(), tile);
        }
        TankManager manager = new TankManager(tanks);
        manager.readTanksFromNBT(tag);
        return manager;
    }
    
    private static boolean addTankTooltip(List<String> currenttip, TankManager manager)
    {
        StandardTank tank = null;
        boolean ret = false;
        for (int i = 0; i < manager.getTankInfo().length; i++)
        {
            tank = manager.get(i);

            if (tank != null)
            {
                FluidStack stored = tank.getFluid();

                if (stored != null)
                {
                    ret = true;
                    currenttip.add(stored.amount + " / " + tank.getCapacity() + " mB " + stored.getLocalizedName());
                }
            }
        }
        return ret;
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        if (te instanceof TileMultiBlock && ((TileMultiBlock) te).getMasterBlock() != null)
        {
            ((TileMultiBlock) te).getMasterBlock().writeToNBT(tag);
        }
        else
        {
            te.writeToNBT(tag);
        }
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

    @Override
    public NBTTagCompound getNBTData(Entity ent, NBTTagCompound tag)
    {
        return tag;
    }
}
