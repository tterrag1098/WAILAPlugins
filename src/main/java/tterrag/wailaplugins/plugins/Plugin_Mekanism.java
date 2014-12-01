package tterrag.wailaplugins.plugins;

import java.lang.reflect.Field;
import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.tile.TileEntitySalinationController;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

public class Plugin_Mekanism extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileEntitySalinationController.class, TileEntityPortableTank.class, IGasHandler.class, TileEntityElectricBlock.class);

        syncNBT(TileEntityElectricBlock.class);

        addConfig("salination");
        addConfig("portableTank");
        addConfig("gas");
        addConfig("factoryType");
        addConfig("energy");
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (tile instanceof TileEntitySalinationController)
        {
            currenttip.add(EnumChatFormatting.GREEN.toString() + ((float) Math.round(((TileEntitySalinationController) tile).getTempMultiplier() * 10)) / 10f + "x");
        }

        if (tile instanceof TileEntityPortableTank)
        {
            FluidStack fluid = ((TileEntityPortableTank) tile).getFluidStack();
            addFluidTooltip(currenttip, fluid);
        }

        if (tile instanceof IGasHandler)
        {
            // unholy mess because IGasHandler lacks getters -.-
            Field tankF = null;
            try
            {
                tankF = tile.getClass().getDeclaredField("gasTank");
            }
            catch (NoSuchFieldException e)
            {
                try
                {
                    tankF = tile.getClass().getDeclaredField("inputTank");
                }
                catch (NoSuchFieldException e1)
                {
                    // nothing we can do :(
                }
            }
            if (tankF != null)
            {
                try
                {
                    tankF.setAccessible(true);
                    GasTank tank = (GasTank) tankF.get(tile);
                    GasStack gas = tank == null ? null : tank.getGas();
                    if (gas != null)
                    {
                        currenttip.add(EnumChatFormatting.GOLD.toString() + gas.amount + " " + gas.getGas().getLocalizedName());
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (tile instanceof TileEntityFactory)
        {
            RecipeType type = RecipeType.values()[tag.getInteger("recipeType")];
            currenttip.add(EnumChatFormatting.AQUA + type.getName());
        }

        if (tile instanceof TileEntityElectricBlock)
        {
            double power = tag.getDouble("electricityStored");
            MachineType type = MachineType.get(accessor.getBlock(), accessor.getMetadata());
            double maxPower = type == null ? tile instanceof TileEntityEnergyCube ? EnergyCubeTier.getFromName(tag.getString("tier")).MAX_ELECTRICITY
                    : ((TileEntityElectricBlock) tile).getMaxEnergy() : type.baseEnergy;

            currenttip.add(String.format("%s%s %s/ %s%s", EnumChatFormatting.WHITE, MekanismUtils.getEnergyDisplay(power), EnumChatFormatting.GRAY, EnumChatFormatting.WHITE,
                    MekanismUtils.getEnergyDisplay(maxPower)));
        }
    }

    private void addFluidTooltip(List<String> tt, FluidStack fluid)
    {
        if (fluid != null)
        {
            tt.add(EnumChatFormatting.AQUA.toString() + fluid.amount + " " + fluid.getLocalizedName());
        }
    }
}
