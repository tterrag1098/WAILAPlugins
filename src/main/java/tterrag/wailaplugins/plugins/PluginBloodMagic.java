package tterrag.wailaplugins.plugins;

import java.util.ArrayList;
import java.util.List;

import WayofTime.bloodmagic.altar.BloodAltar;
import WayofTime.bloodmagic.api.recipe.AlchemyTableRecipe;
import WayofTime.bloodmagic.api.registry.AlchemyTableRecipeRegistry;
import WayofTime.bloodmagic.block.BlockAltar;
import WayofTime.bloodmagic.registry.ModItems;
import WayofTime.bloodmagic.tile.TileAlchemyTable;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.tile.TileTeleposer;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.config.WPConfigHandler;

/**
 * @author Pokefenn (edits by tterrag)
 */
@Plugin(name = "Blood Magic", deps = "BloodMagic", order = 1 /* After Forge Plugin */)
public class PluginBloodMagic extends PluginBase
{
    private static final String KEY_CURRENT_LP = "lp";
    private static final String KEY_CAPACITY = "cap";
    private static final String KEY_TIER = "tier";
    private static final String KEY_PROGRESS = "prog";
    private static final String KEY_RESULT_STACK = "resStack";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_RITUAL_NAME = "ritName";
    private static final String KEY_FOCUS_STACK = "stackName";

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileAltar.class, TileAlchemyTable.class, TileTeleposer.class);

        registerNBT(TileAltar.class, TileAlchemyTable.class, TileTeleposer.class);

        addConfig("altar");
        addConfig("chemistrySet");
        addConfig("masterStone");
        addConfig("teleposer");
    }
    
    @Override
    public void postLoad() 
    {
        // Remove BM's altar stuff
        ModuleRegistrar.instance().bodyBlockProviders.remove(BlockAltar.class);
        ModuleRegistrar.instance().NBTDataProviders.remove(BlockAltar.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        ((ITaggedList<String, String>)currenttip).removeEntries("IFluidHandler");
        
        boolean hasSeer = false;
        boolean hasSigil = false;

        switch (WPConfigHandler.sigilRequirement)
        {
        case 0:
            hasSeer = hasSigil = true;
            break;
        case 1:
            hasSeer = searchInventory(ModItems.SIGIL_SEER, accessor.getPlayer()) != null;
            hasSigil = hasSeer || searchInventory(ModItems.SIGIL_DIVINATION, accessor.getPlayer()) != null;
            break;
        case 2:
            hasSeer = accessor.getPlayer().getHeldItemMainhand() != null && accessor.getPlayer().getHeldItemMainhand().getItem() == ModItems.SIGIL_SEER;
            hasSigil = hasSeer
                    || (accessor.getPlayer().getHeldItemMainhand() != null && accessor.getPlayer().getHeldItemMainhand().getItem() == ModItems.SIGIL_DIVINATION);
            break;
        default:
            break;
        }

        hasSeer |= !WPConfigHandler.seerBenefit;

        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (hasSigil && te instanceof TileAltar && getConfig("altar"))
        {
            currenttip.add(lang.localize("currentLP") + tag.getInteger(KEY_CURRENT_LP));
            currenttip.add(lang.localize("capacity") + tag.getInteger(KEY_CAPACITY));
            currenttip.add(lang.localize("tier") + tag.getInteger(KEY_TIER));

            if (hasSeer && tag.hasKey(KEY_PROGRESS))
            {
                currenttip.add(lang.localize("progress") + tag.getInteger(KEY_PROGRESS) + "%");
            }
        }

        if (te instanceof TileAlchemyTable && getConfig("chemistrySet"))
        {
            currenttip.add(lang.localize("progress") + tag.getInteger(KEY_PROGRESS) + "%");

            if (tag.hasKey(KEY_RESULT_STACK))
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(KEY_RESULT_STACK));
                currenttip.add(stack.getDisplayName());
            }
        }

        if (te instanceof TileTeleposer && getConfig("teleposer"))
        {
            if (tag.hasKey(KEY_FOCUS_STACK)) 
            {
                currenttip.add(ItemStack.loadItemStackFromNBT(tag.getCompoundTag(KEY_FOCUS_STACK)).getDisplayName());
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
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (te instanceof TileAltar)
        {
            TileAltar altar = (TileAltar) te;
            tag.setInteger(KEY_CURRENT_LP, altar.getCurrentBlood());
            tag.setInteger(KEY_CAPACITY, altar.getCapacity());
            tag.setInteger(KEY_TIER, altar.getTier().ordinal());

            if (altar.isActive())
            {
                int cur = altar.getProgress();
                int max = ((BloodAltar)altar.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)).getLiquidRequired() * altar.getStackInSlot(0).stackSize;
                tag.setInteger(KEY_PROGRESS, (int) (((double) cur / (double) max) * 100));
            }
        }
        if (te instanceof TileAlchemyTable)
        {
            if (((TileAlchemyTable)te).isSlave()){
                te = world.getTileEntity(((TileAlchemyTable)te).getConnectedPos());
            }

            tag.setInteger(KEY_PROGRESS, (int) (((TileAlchemyTable)te).getProgressForGui() * 100));
            NBTTagCompound stack = new NBTTagCompound();
            List<ItemStack> inputs = new ArrayList<>();
            for (int i = 0; i < ((TileAlchemyTable)te).getSizeInventory() - 3; i++) {
                ItemStack input = ((TileAlchemyTable)te).getStackInSlot(i);
                if (input != null) {
                    inputs.add(input.copy());
                }
            }
            AlchemyTableRecipe recipe = AlchemyTableRecipeRegistry.getMatchingRecipe(inputs, world, pos);
            if (recipe != null && recipe.getRecipeOutput(inputs) != null)
            {
                recipe.getRecipeOutput(inputs).writeToNBT(stack);
                tag.setTag(KEY_RESULT_STACK, stack);
            }
        }
        if (te instanceof TileTeleposer)
        {
            if (((TileTeleposer)te).getStackInSlot(0) != null)
            {
                tag.setTag(KEY_FOCUS_STACK, ((TileTeleposer)te).getStackInSlot(0).writeToNBT(new NBTTagCompound()));
            }
        }
    }
}
