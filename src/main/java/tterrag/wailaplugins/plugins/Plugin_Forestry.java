package tterrag.wailaplugins.plugins;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.common.Lang;
import com.enderio.core.common.util.BlockCoord;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.ITree;
import forestry.api.core.ErrorStateRegistry;
import forestry.api.core.IErrorState;
import forestry.api.genetics.IGenome;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.gadgets.TileAlveary;
import forestry.apiculture.gadgets.TileAlvearyPlain;
import forestry.apiculture.gadgets.TileBeehouse;
import forestry.apiculture.genetics.Bee;
import forestry.arboriculture.gadgets.TileLeaves;
import forestry.arboriculture.gadgets.TileSapling;
import forestry.arboriculture.gadgets.TileTreeContainer;
import forestry.arboriculture.genetics.Tree;
import forestry.core.config.ForestryItem;
import forestry.core.gadgets.Engine;
import forestry.core.gadgets.TileForestry;
import forestry.core.proxy.Proxies;
import forestry.core.utils.StringUtil;
import forestry.plugins.PluginApiculture;

public class Plugin_Forestry extends PluginBase
{
    private static Field _throttle;
    private static Field _maxHeat;
    private static Field _logic;
    private static Field _alvearyLogic;
    private static NumberFormat pctFmt = NumberFormat.getPercentInstance();
    private static Lang forLang = new Lang("for");

    @SneakyThrows
    public Plugin_Forestry()
    {
        try
        {
            _throttle = BeekeepingLogic.class.getDeclaredField("throttle");
        }
        catch (NoSuchFieldException e)
        {
            // forestry update
            _throttle = BeekeepingLogic.class.getDeclaredField("queenWorkCycleThrottle");
        }

        _throttle.setAccessible(true);

        _maxHeat = Engine.class.getDeclaredField("maxHeat");
        _maxHeat.setAccessible(true);

        _logic = TileBeehouse.class.getDeclaredField("logic");
        _logic.setAccessible(true);

        _alvearyLogic = TileAlvearyPlain.class.getDeclaredField("beekeepingLogic");
        _alvearyLogic.setAccessible(true);

        pctFmt.setMinimumFractionDigits(2);
    }

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileForestry.class, TileTreeContainer.class);

        registerNBT(TileForestry.class, TileTreeContainer.class);

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

        if (tag.hasKey(ENERGY_STORED) && getConfig("power"))
        {
            currenttip.add(tag.getInteger(ENERGY_STORED) + " / " + tag.getInteger(MAX_ENERGY_STORED) + " RF");
        }

        if (tag.hasKey(HEAT) && getConfig("heat"))
        {
            currenttip.add(lang.localize("engineHeat", tag.getInteger(HEAT) / 10D + "\u00B0C"));
        }

        if (tag.hasKey(TREE) && getConfig("sapling"))
        {
            ITree tree = new Tree(tag.getCompoundTag(TREE));
            addGenomeTooltip((TileSapling) tile, tree, player, currenttip);
        }

        if (tile instanceof TileLeaves && getConfig("leaves"))
        {
            if (((TileLeaves) tile).isPollinated())
            {
                currenttip.add(lang.localize("pollinated", tag.getString(LEAF_BRED_SPECIES)));
            }
        }

        if ((tile instanceof IBeeHousing || tile instanceof TileAlveary) && getConfig("apiary"))
        {
            ItemStack queenstack = null;
            ItemStack dronestack = null;
            if (tag.hasKey(QUEEN_STACK))
            {
                queenstack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(QUEEN_STACK));
            }
            if (tag.hasKey(DRONE_STACK))
            {
                dronestack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(DRONE_STACK));
            }

            Bee queen = null;

            if (queenstack != null)
            {
                queen = new Bee(queenstack.getTagCompound());
                String queenSpecies = getSpeciesName(queen.getGenome(), true);

                currenttip.add(EnumChatFormatting.WHITE
                        + lang.localize("mainbee", getNameForBeeType(queenstack), EnumChatFormatting.GREEN + queenSpecies));
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

            if (tag.hasKey(ERRORS) || tag.hasKey(BREED_PROGRESS))
            {
                int[] ids = tag.getIntArray(ERRORS);
                Set<IErrorState> errs = Sets.newHashSet();
                for (int i : ids)
                {
                    errs.add(ErrorStateRegistry.getErrorState((short) i));
                }

                if (!errs.isEmpty())
                {
                    for (IErrorState err : errs)
                    {
                        currenttip.add(EnumChatFormatting.WHITE
                                + String.format(lang.localize("breedError"), EnumChatFormatting.RED + forLang.localize(err.getDescription())));
                    }
                }
                else
                {
                    currenttip.add(EnumChatFormatting.WHITE
                            + String.format(lang.localize("breedProgress"), EnumChatFormatting.AQUA + pctFmt.format(tag.getDouble(BREED_PROGRESS))));
                }
            }
        }
    }

    private void addGenomeTooltip(TileTreeContainer te, ITree tree, EntityPlayer player, List<String> currenttip)
    {
        if (/* XXX BUGGED Uncomment when using 4.x te.isOwner(player) && */ (tree.isAnalyzed() || te instanceof TileLeaves))
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
        return active ? genome.getActiveAllele(EnumBeeChromosome.SPECIES).getName() : genome.getInactiveAllele(EnumBeeChromosome.SPECIES).getName();
    }

    private String getNameForBeeType(ItemStack bee)
    {
        return ForestryItem.beeDroneGE.isItemEqual(bee.getItem()) ? lang.localize("drone")
                : ForestryItem.beePrincessGE.isItemEqual(bee.getItem()) ? lang.localize("princess") : lang.localize("queen");
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

    public static final String LEAF_BRED_SPECIES = "leafBredSpecies";
    public static final String QUEEN_STACK = "queenStack";
    public static final String DRONE_STACK = "droneStack";
    public static final String ERRORS = "errors";
    public static final String BREED_PROGRESS = "breedProgress";
    public static final String TREE = "treeData";
    public static final String ENERGY_STORED = "rfStored";
    public static final String MAX_ENERGY_STORED = "maxRfStored";
    public static final String HEAT = "engineHeat";

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        if (te instanceof TileLeaves)
        {
            tag.setString(LEAF_BRED_SPECIES, ((TileLeaves) te).getTree().getMate().getActiveAllele(EnumTreeChromosome.SPECIES).getName());
        }
        if (te instanceof IBeeHousing || te instanceof TileAlveary)
        {
            BeekeepingLogic logic = null;
            if (te instanceof TileBeehouse)
            {
                logic = (BeekeepingLogic) _logic.get(te);
            }
            else if (te instanceof TileAlveary)
            {
                te = (TileEntity) ((TileAlveary) te).getCentralTE();
                if (te != null)
                {
                    logic = (BeekeepingLogic) _alvearyLogic.get(te);
                }
            }

            if (logic != null)
            {
                IBeeHousing beehouse = (IBeeHousing) te;
                
                ItemStack queen = beehouse.getQueen();
                ItemStack drone = beehouse.getDrone();
                if (queen != null)
                {
                    NBTTagCompound queenTag = new NBTTagCompound();
                    queen.writeToNBT(queenTag);
                    tag.setTag(QUEEN_STACK, queenTag);
                }
                if (drone != null)
                {
                    NBTTagCompound droneTag = new NBTTagCompound();
                    drone.writeToNBT(droneTag);
                    tag.setTag(DRONE_STACK, droneTag);
                }
                Set<IErrorState> errors = beehouse.getErrorStates();
                List<Integer> ids = Lists.newArrayList();
                for (IErrorState error : errors)
                {
                    ids.add((int) error.getID());
                }
                tag.setIntArray(ERRORS, ArrayUtils.toPrimitive(ids.toArray(new Integer[0])));

                if (queen != null && ForestryItem.beeQueenGE.isItemEqual(queen.getItem()))
                {
                    Bee queenBee = new Bee(queen.getTagCompound());
                    float throttle = _throttle.getInt(logic);
                    float maxAge = queenBee.getMaxHealth();
                    float age = Math.abs(queenBee.getHealth() - maxAge); // inverts the progress

                    // determines the amount of percentage points between each breed tick
                    float step = (1 / maxAge);

                    // interpolates between 0 and step
                    float progress = step * (throttle / PluginApiculture.ticksPerBeeWorkCycle);

                    tag.setDouble(BREED_PROGRESS, (age / maxAge) + progress);
                }
            }
        }
        if (te instanceof TileSapling)
        {
            ITree tree = ((TileSapling) te).getTree();
            NBTTagCompound treeTag = new NBTTagCompound();
            tree.writeToNBT(treeTag);
            tag.setTag(TREE, treeTag);
        }
        if (te instanceof Engine)
        {
            tag.setInteger(ENERGY_STORED, ((Engine) te).getEnergyManager().getTotalEnergyStored());
            tag.setInteger(MAX_ENERGY_STORED, ((Engine) te).maxEnergy);
            tag.setInteger(HEAT, ((Engine) te).getHeat());
        }
    }
}
