package tterrag.wailaplugins.plugins;

import java.util.List;

import com.google.common.base.Strings;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import tterrag.wailaplugins.config.WPConfigHandler;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;

/**
 * @author Pokefenn (edits by tterrag)
 */
public class Plugin_AWWayofTime extends PluginBase
{
    public void load(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(this, TEAltar.class);
        registrar.registerBodyProvider(this, TEWritingTable.class);
        registrar.registerBodyProvider(this, TEMasterStone.class);
        registrar.registerBodyProvider(this, TETeleposer.class);

        registrar.registerSyncedNBTKey("*", TEAltar.class);
        registrar.registerSyncedNBTKey("*", TEWritingTable.class);
        registrar.registerSyncedNBTKey("*", TEMasterStone.class);
        registrar.registerSyncedNBTKey("*", TETeleposer.class);
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        boolean hasSeer = accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.itemSeerSigil;
        boolean hasSigil = hasSeer || !WPConfigHandler.doNeedDiviniation
                || (accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.divinationSigil);

        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (te instanceof TEAltar)
        {
            TEAltar altar = (TEAltar) te;
            te.readFromNBT(tag);
            
            if (hasSigil)
            {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
                currenttip.add(lang.localize("currentLP") + (fluid == null ? "0" : fluid.amount));
                currenttip.add(lang.localize("capacity") + tag.getInteger("capacity"));
                currenttip.add(lang.localize("tier") + tag.getInteger("upgradeLevel"));

                if (altar.getStackInSlot(0) != null)
                {
                    int cur = tag.getInteger("progress");
                    int max = tag.getInteger("liquidRequired") * altar.getStackInSlot(0).stackSize;
                    currenttip.add(lang.localize("progress") + ((int) (((double) cur / (double) max) * 100)) + "%");
                }
            }
        }
        else if (te instanceof TEWritingTable)
        {
            TEWritingTable chemistrySet = (TEWritingTable) te;
            te.readFromNBT(tag);

            currenttip.add(lang.localize("progress") + tag.getInteger("progress") + "%");

            if (chemistrySet.getResultingItemStack() != null)
            {
                currenttip.add(chemistrySet.getResultingItemStack().getDisplayName());
            }
        }
        else if (te instanceof TEMasterStone)
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
        else if (te instanceof TETeleposer)
        {
            TETeleposer teleposer = (TETeleposer) te;
            te.readFromNBT(tag);

            if (teleposer.getStackInSlot(0) != null)
            {
                currenttip.add(teleposer.getStackInSlot(0).getDisplayName());
            }
        }
    }
}
