package tterrag.wailaplugins.plugins;

import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tterrag.core.common.util.BlockCoord;
import tterrag.wailaplugins.config.WPConfigHandler;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;

import com.google.common.base.Strings;

/**
 * @author Pokefenn (edits by tterrag)
 */
public class Plugin_AWWayofTime extends PluginBase
{
    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        
        registerBody(TEAltar.class, TEWritingTable.class, TEMasterStone.class, TETeleposer.class);

        registerNBT(TEAltar.class, TEWritingTable.class, TEMasterStone.class, TETeleposer.class);
        
        addConfig("altar");
        addConfig("chemistrySet");
        addConfig("masterStone");
        addConfig("teleposer");
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        boolean hasSeer = false;
        boolean hasSigil = false;
        
        switch(WPConfigHandler.sigilBehavior)
        {
        case 0:
            hasSeer = hasSigil = true;
            break;
        case 1:
            hasSeer = searchInventory(ModItems.itemSeerSigil, accessor.getPlayer()) != null;
            hasSigil = hasSeer || searchInventory(ModItems.divinationSigil, accessor.getPlayer()) != null;
            break;
        case 2:
            hasSeer = accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.itemSeerSigil;
            hasSigil = hasSeer || (accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.divinationSigil);
            break;
        default: break;
        }
        
        hasSeer |= !WPConfigHandler.seerBenefit;

        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (te instanceof TEAltar && getConfig("altar"))
        {
            TEAltar altar = (TEAltar) te;
            te.readFromNBT(tag);
            
            if (hasSigil)
            {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
                currenttip.add(lang.localize("currentLP") + (fluid == null ? "0" : fluid.amount));
                currenttip.add(lang.localize("capacity") + tag.getInteger("capacity"));
                currenttip.add(lang.localize("tier") + tag.getInteger("upgradeLevel"));

                if (hasSeer && altar.getStackInSlot(0) != null)
                {
                    int cur = tag.getInteger("progress");
                    int max = tag.getInteger("liquidRequired") * altar.getStackInSlot(0).stackSize;
                    currenttip.add(lang.localize("progress") + ((int) (((double) cur / (double) max) * 100)) + "%");
                }
            }
        }
        
        if (te instanceof TEWritingTable && getConfig("chemistrySet"))
        {
            TEWritingTable chemistrySet = (TEWritingTable) te;
            te.readFromNBT(tag);

            currenttip.add(lang.localize("progress") + tag.getInteger("progress") + "%");

            if (chemistrySet.getResultingItemStack() != null)
            {
                currenttip.add(chemistrySet.getResultingItemStack().getDisplayName());
            }
        }
        
        if (te instanceof TEMasterStone && getConfig("masterStone"))
        {
            String owner = tag.getString("owner");
            if (!Strings.isNullOrEmpty(owner))
            {
                currenttip.add(lang.localize("owner") + tag.getString("owner"));
            }

            String ritualName = tag.getString("currentRitualString");
            if (!Strings.isNullOrEmpty(ritualName))
            {
                currenttip.add(Rituals.getNameOfRitual((ritualName)));
            }
        }
        
        if (te instanceof TETeleposer && getConfig("teleposer"))
        {
            TETeleposer teleposer = (TETeleposer) te;
            te.readFromNBT(tag);

            if (teleposer.getStackInSlot(0) != null)
            {
                currenttip.add(teleposer.getStackInSlot(0).getDisplayName());
            }
        }
    }

    private ItemStack searchInventory(Item item, EntityPlayer player)
    {
        for (ItemStack stack : player.inventory.mainInventory)
        {
            if (stack != null && stack.getItem() == item)
            {
                return stack.copy();
            }
        }
        return null;
    }
    
    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        te.writeToNBT(tag);
    }
}
