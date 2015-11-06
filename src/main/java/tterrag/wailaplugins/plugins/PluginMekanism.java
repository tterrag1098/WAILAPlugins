package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mekanism.api.gas.GasStack;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.*;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tterrag.wailaplugins.api.Plugin;

import java.util.List;

@Plugin(deps = "Mekanism")
public class PluginMekanism extends WailaPluginBase {

    @Override
    public void load() {

        registerBody(TileEntitySolarEvaporationController.class, TileEntityPortableTank.class, TileEntityGasTank.class, TileEntityElectricBlock.class);

        registerNBT(TileEntityElectricBlock.class);

        addConfig("salination");
        addConfig("portableTank");
        addConfig("gas");
        addConfig("factoryType");
        addConfig("energy");
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (tile instanceof TileEntitySolarEvaporationController) {
            currenttip.add(EnumChatFormatting.GREEN.toString() + ((float) Math.round(((TileEntitySolarEvaporationController) tile).getTempMultiplier() * 10)) / 10f + "x");
        }

        if (tile instanceof TileEntityPortableTank) {
            FluidStack fluid = ((TileEntityPortableTank) tile).getFluidStack();
            addFluidTooltip(currenttip, fluid);
        }

        if (tile instanceof TileEntityGasTank) {
            GasStack gas = ((TileEntityGasTank) tile).gasTank.getGas();
            currenttip.add(EnumChatFormatting.GOLD.toString() + gas.amount + " " + gas.getGas().getLocalizedName());
        }

        if (tile instanceof TileEntityFactory) {
            RecipeType type = RecipeType.values()[tag.getInteger("recipeType")];
            currenttip.add(EnumChatFormatting.AQUA + type.getLocalizedName());
        }

        if (tile instanceof TileEntityElectricBlock) {
            double power = tag.getDouble("electricityStored");
            MachineType type = MachineType.get(accessor.getBlock(), accessor.getMetadata());
            double maxPower = type == null ? tile instanceof TileEntityEnergyCube ? EnergyCubeTier.getFromName(tag.getString("tier")).maxEnergy
                    : ((TileEntityElectricBlock) tile).getMaxEnergy() : type.baseEnergy;

            currenttip.add(String.format("%s%s %s/ %s%s", EnumChatFormatting.WHITE, MekanismUtils.getEnergyDisplay(power), EnumChatFormatting.GRAY, EnumChatFormatting.WHITE,
                    MekanismUtils.getEnergyDisplay(maxPower)));
        }
    }

    private void addFluidTooltip(List<String> tt, FluidStack fluid) {
        if (fluid != null) {
            tt.add(EnumChatFormatting.AQUA.toString() + fluid.amount + " " + fluid.getLocalizedName());
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        te.writeToNBT(tag);
    }
}
