package tterrag.wailaplugins.plugins;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.common.Lang;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.IGenome;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.arboriculture.tiles.TileTreeContainer;
import forestry.core.owner.IOwnedTile;
import forestry.core.tiles.TileEngine;
import forestry.core.tiles.TileForestry;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tterrag.wailaplugins.WailaPlugins;
import tterrag.wailaplugins.api.Plugin;

@Plugin(name = "Forestry", deps = "forestry")
public class PluginForestry extends PluginBase
{
    private static Field _throttle;
    private static Field _maxHeat;
    private static NumberFormat pctFmt = NumberFormat.getPercentInstance();
    private static Lang forLang = new Lang("for");

    @SneakyThrows
    public PluginForestry()
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

        _maxHeat = TileEngine.class.getDeclaredField("maxHeat");
        _maxHeat.setAccessible(true);

        pctFmt.setMinimumFractionDigits(2);
    }

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registerBody(TileForestry.class, TileTreeContainer.class, TileAlveary.class);

        registerNBT(TileForestry.class, TileTreeContainer.class, TileAlveary.class);

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
        BlockPos pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        if (tag.hasKey(ENERGY_STORED) && getConfig("power"))
        {
            currenttip.add(tag.getInteger(ENERGY_STORED) + " / " + tag.getInteger(MAX_ENERGY_STORED) + " RF");
        }

        if (tag.hasKey(HEAT) && getConfig("heat"))
        {
            int heat = tag.getInteger(HEAT);
            int maxHeat = tag.getInteger(MAX_HEAT);
            currenttip.add(lang.localize("engineHeat", pctFmt.format((double) heat / maxHeat)));
        }

        if (tag.hasKey(TREE) && getConfig("sapling"))
        {
            ITree tree = new Tree(tag.getCompoundTag(TREE));
            addGenomeTooltip(tag, (TileTreeContainer) tile, tree, player, currenttip);
        }

        if (tile instanceof TileLeaves && getConfig("leaves"))
        {
            if (((TileLeaves) tile).isPollinated())
            {
                currenttip.add(lang.localize("pollinated", tag.getString(LEAF_BRED_SPECIES)));
            }
        }

        if (tile instanceof IBeeHousing && getConfig("apiary"))
        {
            ItemStack queenstack = null;
            ItemStack dronestack = null;
            if (tag.hasKey(QUEEN_STACK))
            {
                queenstack = new ItemStack(tag.getCompoundTag(QUEEN_STACK));
            }
            if (tag.hasKey(DRONE_STACK))
            {
                dronestack = new ItemStack(tag.getCompoundTag(DRONE_STACK));
            }

            IBee queen = null;

            if (queenstack != null)
            {
                queen = new Bee(queenstack.getTagCompound());
                String queenSpecies = getSpeciesName(queen.getGenome(), true);

                currenttip.add(TextFormatting.WHITE
                        + lang.localize("mainbee", getNameForBeeType(queenstack), TextFormatting.GREEN + queenSpecies));
                if (queen.isAnalyzed())
                {
                    addIndentedBeeInfo(queen, currenttip);
                }
            }

            IBee drone = null;

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
                currenttip.add(String.format(TextFormatting.WHITE + lang.localize("secondarybee"), lang.localize("drone"),
                        TextFormatting.GREEN + getSpeciesName(drone.getGenome(), true)));

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
                    errs.add(ForestryAPI.errorStateRegistry.getErrorState((short) i));
                }

                if (!errs.isEmpty())
                {
                    for (IErrorState err : errs)
                    {
                        currenttip.add(TextFormatting.WHITE
                                + String.format(lang.localize("breedError"), TextFormatting.RED + I18n.format(err.getUnlocalizedDescription())));
                    }
                }
                else
                {
                    currenttip.add(TextFormatting.WHITE
                            + String.format(lang.localize("breedProgress"), TextFormatting.AQUA + pctFmt.format(tag.getDouble(BREED_PROGRESS))));
                }
            }
        }
    }

    private void addGenomeTooltip(NBTTagCompound tag, TileTreeContainer te, ITree tree, EntityPlayer player, List<String> currenttip)
    {
        UUID owner = UUID.fromString(tag.getString(OWNER));
        // XXX Leaf isAnalyzed is not working properly, wait for forestry fix
        if (owner.equals(player.getGameProfile().getId()) && tree.isAnalyzed())
        {
            addTreeTooltip(tree, currenttip);
        }
        else if (tree != null)
        {
            currenttip.add(TextFormatting.ITALIC + (tree.isAnalyzed() ? lang.localize("notOwner") : lang.localize("notAnalyzed")));
        }
    }

    @SideOnly(Side.CLIENT)
    private void addTreeTooltip(ITree tree, List<String> currenttip)
    {
        if (WailaPlugins.proxy.isShiftKeyDown())
            tree.addTooltip(currenttip);
        else
            currenttip.add(getTMI());
    }

    private String getTMI()
    {
        return TextFormatting.ITALIC + "<" + forLang.localize("gui.tooltip.tmi") + ">";
    }

    private String getSpeciesName(IGenome genome, boolean active)
    {
        return active ? genome.getActiveAllele(EnumBeeChromosome.SPECIES).getName() : genome.getInactiveAllele(EnumBeeChromosome.SPECIES).getName();
    }

    private String getNameForBeeType(ItemStack bee)
    {
        return ModuleApiculture.getItems().beeDroneGE == bee.getItem() ? lang.localize("drone")
                : ModuleApiculture.getItems().beePrincessGE == bee.getItem() ? lang.localize("princess") : lang.localize("queen");
    }

    private void addIndentedBeeInfo(IBee bee, List<String> currenttip)
    {
        if (WailaPlugins.proxy.isShiftKeyDown())
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
    public static final String MAX_HEAT = "engineMaxHeat";
    public static final String OWNER = "tileOwner";

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
    {
        if (te instanceof TileLeaves)
        {
            ITreeGenome mate = ((TileLeaves) te).getTree().getMate();
            if (mate != null)
            {
                tag.setString(LEAF_BRED_SPECIES, mate.getActiveAllele(EnumTreeChromosome.SPECIES).getName());
            }
        }
        if (te instanceof IBeeHousing)
        {
            IBeeHousing housing = (IBeeHousing) te;
            IBeekeepingLogic logic = housing.getBeekeepingLogic();
            IBeeHousingInventory inv = housing.getBeeInventory();
            IErrorLogic errs = housing.getErrorLogic();

            if (logic != null)
            {
                ItemStack queen = inv.getQueen();
                ItemStack drone = inv.getDrone();
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
                Set<IErrorState> errors = errs.getErrorStates();
                List<Integer> ids = Lists.newArrayList();
                for (IErrorState error : errors)
                {
                    ids.add((int) error.getID());
                }
                tag.setIntArray(ERRORS, ArrayUtils.toPrimitive(ids.toArray(new Integer[0])));

                if (queen != null && ModuleApiculture.getItems().beeQueenGE == queen.getItem())
                {
                    Bee queenBee = new Bee(queen.getTagCompound());
                    float throttle = _throttle.getInt(logic);
                    float maxAge = queenBee.getMaxHealth();
                    float age = Math.abs(queenBee.getHealth() - maxAge); // inverts the progress

                    // determines the amount of percentage points between each breed tick
                    float step = (1 / maxAge);

                    // interpolates between 0 and step
                    float progress = step * (throttle / ModuleApiculture.ticksPerBeeWorkCycle);

                    tag.setDouble(BREED_PROGRESS, (age / maxAge) + progress);
                }
            }
        }
        if (te instanceof TileTreeContainer)
        {
            ITree tree = ((TileTreeContainer) te).getTree();
            NBTTagCompound treeTag = new NBTTagCompound();
            tree.writeToNBT(treeTag);
            tag.setTag(TREE, treeTag);
        }
        if (te instanceof TileEngine)
        {
            tag.setInteger(ENERGY_STORED, ((TileEngine) te).getEnergyManager().getEnergyStored());
            tag.setInteger(MAX_ENERGY_STORED, ((TileEngine) te).getEnergyManager().getMaxEnergyStored());
            tag.setInteger(HEAT, ((TileEngine) te).getHeat());
            tag.setInteger(MAX_HEAT, _maxHeat.getInt(te));
        }
        if (te instanceof IOwnedTile)
        {
            GameProfile owner = ((IOwnedTile) te).getOwnerHandler().getOwner();
            if (owner != null) {
                tag.setString(OWNER, owner.getId().toString());
            }
        }
    }
}
