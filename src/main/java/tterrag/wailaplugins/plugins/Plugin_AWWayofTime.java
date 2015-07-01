package tterrag.wailaplugins.plugins;

import java.lang.reflect.Field;
import java.util.List;

import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tterrag.wailaplugins.config.WPConfigHandler;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TETeleposer;
import WayofTime.alchemicalWizardry.common.tileEntity.TEWritingTable;

import com.enderio.core.common.util.BlockCoord;

import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * @author Pokefenn (edits by tterrag)
 */
public class Plugin_AWWayofTime extends PluginBase
{
    private static final String KEY_CURRENT_LP = "lp";
    private static final String KEY_CAPACITY = "cap";
    private static final String KEY_TIER = "tier";
    private static final String KEY_PROGRESS = "prog";
    private static final String KEY_RESULT_STACK = "resStack";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_RITUAL_NAME = "ritName";
    private static final String KEY_STACK_NAME = "stackName";

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

        switch (WPConfigHandler.sigilRequirement)
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
            hasSigil = hasSeer
                    || (accessor.getPlayer().getHeldItem() != null && accessor.getPlayer().getHeldItem().getItem() == ModItems.divinationSigil);
            break;
        default:
            break;
        }

        hasSeer |= !WPConfigHandler.seerBenefit;

        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (hasSigil && te instanceof TEAltar && getConfig("altar"))
        {
            currenttip.add(lang.localize("currentLP") + tag.getInteger(KEY_CURRENT_LP));
            currenttip.add(lang.localize("capacity") + tag.getInteger(KEY_CAPACITY));
            currenttip.add(lang.localize("tier") + tag.getInteger(KEY_TIER));

            if (hasSeer && tag.hasKey(KEY_PROGRESS))
            {
                currenttip.add(lang.localize("progress") + tag.getInteger(KEY_PROGRESS) + "%");
            }
        }

        if (te instanceof TEWritingTable && getConfig("chemistrySet"))
        {
            currenttip.add(lang.localize("progress") + tag.getInteger(KEY_PROGRESS) + "%");

            if (tag.hasKey(KEY_RESULT_STACK))
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(KEY_RESULT_STACK));
                currenttip.add(stack.getDisplayName());
            }
        }

        if (te instanceof TEMasterStone && getConfig("masterStone"))
        {
            String owner = tag.getString(KEY_OWNER);
            if (!owner.isEmpty())
            {
                currenttip.add(lang.localize("owner") + tag.getString("owner"));
            }

            String ritualName = tag.getString(KEY_RITUAL_NAME);
            if (!ritualName.isEmpty())
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

    private static final Field liquidRequired = ReflectionHelper.findField(TEAltar.class, "liquidRequired");

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        if (!liquidRequired.isAccessible())
        {
            liquidRequired.setAccessible(true);
        }

        if (te instanceof TEAltar)
        {
            TEAltar altar = (TEAltar) te;
            tag.setInteger(KEY_CURRENT_LP, altar.getCurrentBlood());
            tag.setInteger(KEY_CAPACITY, altar.getCapacity());
            tag.setInteger(KEY_TIER, altar.getTier());

            if (altar.isActive())
            {
                int cur = altar.getProgress();
                int max = liquidRequired.getInt(altar) * altar.getStackInSlot(0).stackSize;
                tag.setInteger(KEY_PROGRESS, (int) (((double) cur / (double) max) * 100));
            }
        }
        if (te instanceof TEWritingTable)
        {
            NBTTagCompound datahack = new NBTTagCompound();
            te.writeToNBT(datahack);
            tag.setInteger(KEY_PROGRESS, datahack.getInteger("progress"));
            NBTTagCompound stack = new NBTTagCompound();
            if (((TEWritingTable) te).getResultingItemStack() != null)
            {
                ((TEWritingTable) te).getResultingItemStack().writeToNBT(stack);
                tag.setTag(KEY_RESULT_STACK, stack);
            }
        }
        if (te instanceof TEMasterStone) 
        {
            tag.setString(KEY_OWNER, ((TEMasterStone)te).getOwner());
            tag.setString(KEY_RITUAL_NAME, ((TEMasterStone)te).getCurrentRitual());
        }
        if (te instanceof TETeleposer)
        {
            if (((TETeleposer)te).getStackInSlot(0) != null)
            {
                tag.setString(KEY_STACK_NAME, ((TETeleposer)te).getStackInSlot(0).getDisplayName());
            }
        }
    }
}
