package tterrag.wailaplugins.plugins;

import static tterrag.wailaplugins.WailaPlugins.*;

import java.lang.reflect.Field;
import java.util.List;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import tterrag.wailaplugins.config.WPConfigHandler;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.common.block.BlockAltar;
import WayofTime.alchemicalWizardry.common.block.BlockMasterStone;
import WayofTime.alchemicalWizardry.common.block.BlockTeleposer;
import WayofTime.alchemicalWizardry.common.block.BlockWritingTable;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;

/**
 * @author Pokefenn (edits by tterrag)
 */
public class Plugin_AWWayofTime extends PluginBase
{
    private Field liquidRequired;
    private Field chemistryProgress;
    private Field currentRitualString;

    public Plugin_AWWayofTime()
    {
        try
        {
            liquidRequired = TEAltar.class.getDeclaredField("liquidRequired");
            liquidRequired.setAccessible(true);

            chemistryProgress = TEWritingTable.class.getDeclaredField("progress");
            chemistryProgress.setAccessible(true);

            currentRitualString = TEMasterStone.class.getDeclaredField("currentRitualString");
            currentRitualString.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void load(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(this, BlockAltar.class);
        registrar.registerBodyProvider(this, BlockWritingTable.class);
        registrar.registerBodyProvider(this, BlockMasterStone.class);
        registrar.registerBodyProvider(this, BlockTeleposer.class);
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        boolean hasSeer = accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.itemSeerSigil;
        boolean hasSigil = hasSeer || !WPConfigHandler.doNeedDiviniation
                || (accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.divinationSigil);

        TileEntity te = accessor.getTileEntity();
        if (te instanceof TEAltar)
        {
            TEAltar altar = (TEAltar) te;
            if (hasSigil)
            {
                currenttip.add(lang.localize("currentLP") + altar.getFluidAmount());
                currenttip.add(lang.localize("capacity") + altar.getCapacity());
                currenttip.add(lang.localize("tier") + altar.getTier());

                if (liquidRequired != null && hasSeer && altar.getStackInSlot(0) != null)
                {
                    try
                    {
                        int cur = altar.getProgress();
                        int max = liquidRequired.getInt(altar) * altar.getStackInSlot(0).stackSize;
                        currenttip.add(lang.localize("progress") + ((int)(((double)cur / (double)max) * 100)) + "%");
                    }
                    catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                        liquidRequired = null;
                    }
                }
            }
        }
        else if (te instanceof TEWritingTable)
        {
            TEWritingTable chemistrySet = (TEWritingTable) te;

            if (chemistryProgress != null)
            {
                try
                {
                    currenttip.add(lang.localize("progress") + chemistryProgress.getInt(chemistrySet) + "%");
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                    chemistryProgress = null;
                }
            }

            if (chemistrySet.getResultingItemStack() != null)
            {
                currenttip.add(chemistrySet.getResultingItemStack().getDisplayName());
            }
        }
        else if (te instanceof TEMasterStone)
        {
            TEMasterStone ritualStone = (TEMasterStone) te;

            if (!ritualStone.getOwner().equals(""))
            {
                currenttip.add(lang.localize("owner") + ritualStone.getOwner());
            }

            if (currentRitualString != null)
            {
                try
                {
                    String ritualName = (String) currentRitualString.get(ritualStone);
                    if (!ritualName.equals(""))
                    {
                        currenttip.add(Rituals.getNameOfRitual((ritualName)));
                    }
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                    currentRitualString = null;
                }
            }
        }
        else if (te instanceof TETeleposer)
        {
            TETeleposer teleposer = (TETeleposer) te;

            if (teleposer.getStackInSlot(0) != null)
            {
                currenttip.add(teleposer.getStackInSlot(0).getDisplayName());
            }
        }
    }
}
