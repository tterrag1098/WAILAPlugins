package tterrag.wailaplugins.plugins;

import static forestry.apiculture.gadgets.TileBeehouse.*;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.ITree;
import forestry.api.genetics.IGenome;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.gadgets.TileApiary;
import forestry.apiculture.genetics.Bee;
import forestry.arboriculture.gadgets.TileLeaves;
import forestry.arboriculture.gadgets.TileSapling;
import forestry.arboriculture.gadgets.TileTreeContainer;
import forestry.core.config.ForestryItem;
import forestry.core.gadgets.Engine;
import forestry.core.gadgets.TilePowered;
import forestry.core.proxy.Proxies;
import forestry.core.utils.InventoryAdapter;
import forestry.core.utils.StringUtil;
import forestry.plugins.PluginApiculture;

public class Plugin_Forestry extends PluginBase
{
    private static Field _throttle;
    private static Field _maxHeat;
    private static NumberFormat pctFmt = NumberFormat.getPercentInstance();

    @SneakyThrows
    public Plugin_Forestry()
    {
        try
        {
            _throttle = BeekeepingLogic.class.getDeclaredField("throttle");
        }
        catch (NoSuchFieldException e)
        {
            _throttle = BeekeepingLogic.class.getDeclaredField("queenWorkCycleThrottle"); // forestry update
        }
        
        _throttle.setAccessible(true);

        _maxHeat = Engine.class.getDeclaredField("maxHeat");
        _maxHeat.setAccessible(true);

        pctFmt.setMinimumFractionDigits(2);
    }

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);
        
        registerBody(TilePowered.class, Engine.class, TileSapling.class, TileLeaves.class, TileApiary.class);
        
        syncNBT(TilePowered.class, Engine.class, TileApiary.class);
        
        addConfig("power");
        addConfig("heat");
        addConfig("sapling");
        addConfig("leaves");
        addConfig("apiary");
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unused")
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        Block block = accessor.getBlock();
        TileEntity tile = accessor.getTileEntity();
        World world = accessor.getWorld();
        EntityPlayer player = accessor.getPlayer();
        MovingObjectPosition pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        int x = pos.blockX, y = pos.blockY, z = pos.blockZ;

        if ((tile instanceof TilePowered || tile instanceof Engine) && getConfig("power"))
        {
            EnergyStorage storage = new EnergyStorage(Integer.MAX_VALUE).readFromNBT(tag.getCompoundTag("EnergyManager").getCompoundTag("EnergyStorage"));
            currenttip.add(storage.getEnergyStored() + " / " + ((IEnergyHandler)tile).getMaxEnergyStored(accessor.getSide()) + " RF");
            
            if (tile instanceof Engine && getConfig("heat"))
            {
                double heat = tag.getInteger("EngineHeat");
                double maxHeat = _maxHeat.getInt(tile);
                currenttip.add(String.format(lang.localize("engineHeat"), pctFmt.format(heat / maxHeat)));
            }
        }
        
        if (tile instanceof TileSapling && getConfig("sapling"))
        {
            addGenomeTooltip((TileSapling) tile, player, currenttip);
        }
        
        if (tile instanceof TileLeaves && getConfig("leaves"))
        {
            TileLeaves leaf = (TileLeaves) tile;
            if (leaf.isPollinated())
            {
                currenttip.add(String.format(lang.localize("pollinated"), leaf.getTree().getMate().getActiveAllele(EnumTreeChromosome.SPECIES.ordinal()).getName()));
            }
        }
        
        if (tile instanceof TileApiary && getConfig("apiary"))
        {
            TileApiary apiary = (TileApiary) tile;
            InventoryAdapter inv = new InventoryAdapter(12, "Items");
            inv.readFromNBT(tag);

            ItemStack queenstack = inv.getStackInSlot(SLOT_QUEEN);
            ItemStack dronestack = inv.getStackInSlot(SLOT_DRONE);

            Bee queen = null;

            if (inv.getStackInSlot(SLOT_QUEEN) != null)
            {
                queen = new Bee(queenstack.getTagCompound());
                String queenSpecies = getSpeciesName(queen.getGenome(), true);

                currenttip.add(EnumChatFormatting.WHITE + String.format(lang.localize("mainbee"), getNameForBeeType(queenstack), EnumChatFormatting.GREEN + queenSpecies));
                if (queen.isAnalyzed())
                {
                    addIndentedBeeInfo(queen, currenttip);
                }
            }

            Bee drone = null;

            if (queen != null && queen.getMate() != null)
            {
                drone = new Bee(queen.getMate());
            }
            else if (dronestack != null)
            {
                drone = new Bee(dronestack.getTagCompound());
            }

            if (drone != null)
            {
                currenttip.add(String.format(EnumChatFormatting.WHITE + lang.localize("secondarybee"), lang.localize("drone"),
                        EnumChatFormatting.GREEN + getSpeciesName(drone.getGenome(), true)));
                
                if (drone.isAnalyzed())
                {
                    addIndentedBeeInfo(drone, currenttip);
                }
            }

            if (queen != null && ForestryItem.beeQueenGE.isItemEqual(queenstack.getItem()))
            {
                IBeekeepingLogic logic = new BeekeepingLogic(apiary);
                logic.readFromNBT(tag);
                float throttle = _throttle.getInt(logic);
                float maxAge = queen.getMaxHealth();
                float age = Math.abs(queen.getHealth() - maxAge);                    // inverts the progress

                float step = (1 / maxAge);                                           // determines the amount of percentage points between each breed tick
                float progress = step * (throttle / PluginApiculture.beeCycleTicks); // interpolates between 0 and step

                currenttip.add(String.format(EnumChatFormatting.WHITE + lang.localize("breedProgress"), EnumChatFormatting.AQUA + pctFmt.format((age / maxAge) + progress)));
            }
        }
    }

    private void addGenomeTooltip(TileTreeContainer te, EntityPlayer player, List<String> currenttip)
    {
        ITree tree = te.getTree();
        if (te.isOwner(player) && (tree.isAnalyzed() || te instanceof TileLeaves))
        {
            addTreeTooltip(tree, currenttip);
        }
        else if (tree != null)
        {
            currenttip.add(EnumChatFormatting.ITALIC + (tree.isAnalyzed() ? lang.localize("notOwner") : lang.localize("notAnalyzed")));
        }
    }

    private void addTreeTooltip(ITree tree, List<String> currenttip)
    {
        if (Proxies.common.isShiftDown())
            tree.addTooltip(currenttip);
        else
            currenttip.add(getTMI());
    }

    private String getTMI()
    {
        return EnumChatFormatting.ITALIC + "<" + StringUtil.localize("gui.tooltip.tmi") + ">";
    }

    private String getSpeciesName(IGenome genome, boolean active)
    {
        return active ? genome.getActiveAllele(EnumBeeChromosome.SPECIES.ordinal()).getName() : genome.getInactiveAllele(EnumBeeChromosome.SPECIES.ordinal()).getName();
    }

    private String getNameForBeeType(ItemStack bee)
    {
        return ForestryItem.beeDroneGE.isItemEqual(bee.getItem())       ? lang.localize("drone")    :  
               ForestryItem.beePrincessGE.isItemEqual(bee.getItem())    ? lang.localize("princess") : lang.localize("queen");
    }

    private void addIndentedBeeInfo(Bee bee, List<String> currenttip)
    {
        if (Proxies.common.isShiftDown())
        {
            List<String> tt = new ArrayList<String>();
            bee.addTooltip(tt);
            for (int i = 0; i < tt.size(); i++)
            {
                tt.set(i, (i == 0 ? ">" : "") + SpecialChars.TAB + tt.get(i));
            }
            currenttip.addAll(tt);
        }
        else
        {
            currenttip.add(getTMI());
        }
    }
}
