package tterrag.wailaplugins.plugins;

import java.util.List;

import mekanism.api.MekanismConfig;
import tterrag.wailaplugins.api.Plugin;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mekanism.api.gas.GasStack;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import com.enderio.core.common.util.BlockCoord;

@Plugin(deps = "Mekanism")
public class PluginMekanism extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileEntityThermalEvaporationController.class, TileEntityFluidTank.class, TileEntityGasTank.class, TileEntityElectricBlock.class);

        registerNBT(TileEntityElectricBlock.class);

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

        if (tile instanceof TileEntityThermalEvaporationController)
        {
            TileEntityThermalEvaporationController controller = ((TileEntityThermalEvaporationController) tile);
            double multiplier = controller.biomeTemp * (controller.getActiveSolars() / 4);
            currenttip.add(Math.round(multiplier * 10) / 10f+"x");
        }

        if (tile instanceof TileEntityFluidTank)
        {
            FluidStack fluid = ((TileEntityFluidTank) tile).getFluidStack();
            addFluidTooltip(currenttip, fluid);
        }

        if (tile instanceof TileEntityGasTank)
        {
            GasStack gas = ((TileEntityGasTank) tile).gasTank.getGas();
            currenttip.add(EnumChatFormatting.GOLD.toString() + gas.amount + " " + gas.getGas().getLocalizedName());
        }
        
        if (tile instanceof TileEntityFactory)
        {
            RecipeType type = RecipeType.values()[tag.getInteger("recipeType")];
            currenttip.add(EnumChatFormatting.AQUA + type.getLocalizedName());
        }
        
        if (tile instanceof TileEntityElectricBlock)
        {
            double power = tag.getDouble("electricityStored");
            MachineType type = MachineType.get(accessor.getBlock(), accessor.getMetadata());
            double maxPower = type == null ? tile instanceof TileEntityEnergyCube ? EnergyCubeTier.getFromName(tag.getString("tier")).maxEnergy
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
    
    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        te.writeToNBT(tag);
    }
}
