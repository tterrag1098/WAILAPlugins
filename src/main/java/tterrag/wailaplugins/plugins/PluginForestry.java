package tterrag.wailaplugins.plugins;

import com.enderio.core.common.Lang;
import com.enderio.core.common.util.BlockCoord;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.*;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.IGenome;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.arboriculture.tiles.TileTreeContainer;
import forestry.core.access.IOwnable;
import forestry.core.config.ForestryItem;
import forestry.core.proxy.Proxies;
import forestry.core.tiles.IPowerHandler;
import forestry.core.tiles.IRestrictedAccessTile;
import forestry.core.tiles.TileEngine;
import forestry.core.tiles.TileForestry;
import forestry.core.utils.StringUtil;
import forestry.mail.tiles.IMailContainer;
import forestry.plugins.PluginApiculture;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.client.ClientMessageHandler;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Plugin(name = "Forestry", deps = "Forestry")
public class PluginForestry extends WailaPluginBase {

    public static final String LEAF_BRED_SPECIES = "leafBredSpecies";
    public static final String QUEEN_STACK = "queenStack";
    public static final String DRONE_STACK = "droneStack";
    public static final String ERRORS = "errors";
    public static final String BREED_PROGRESS = "breedProgress";
    public static final String TREE = "treeData";
    public static final String ENERGY_STORED = "rfStored";
    public static final String MAX_ENERGY_STORED = "maxRfStored";
    public static final String HEAT = "engineHeat";
    public static final String OWNER = "tileOwner";
    private static Field _throttle;
    //private static Field _maxHeat;
    private static NumberFormat pctFmt = NumberFormat.getPercentInstance();
    private static Lang forLang = new Lang("for");

    @SneakyThrows
    public PluginForestry() {
        try {
            _throttle = BeekeepingLogic.class.getDeclaredField("throttle");
        } catch (NoSuchFieldException e) {
            // forestry update
            _throttle = BeekeepingLogic.class.getDeclaredField("queenWorkCycleThrottle");
        }

        _throttle.setAccessible(true);

        //_maxHeat = TileEngine.class.getDeclaredField("maxHeat");
        //_maxHeat.setAccessible(true);

        pctFmt.setMinimumFractionDigits(2);
    }

    @Override
    public void load() {

        registerBody(TileForestry.class, TileTreeContainer.class, TileAlveary.class);

        registerNBT(TileForestry.class, TileTreeContainer.class, TileAlveary.class);

        addConfig("power");
        addConfig("heat");
        addConfig("sapling");
        addConfig("leaves");
        addConfig("apiary");

        addConfig("showAccessData");
        addConfig("showMailData");
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unused")
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        //Block block = accessor.getBlock();
        TileEntity tile = accessor.getTileEntity();
        //World world = accessor.getWorld();
        EntityPlayer player = accessor.getPlayer();
        //MovingObjectPosition pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        //int x = pos.blockX, y = pos.blockY, z = pos.blockZ;

        if (tag != null) {
            if (tag.hasKey(access)){
                currenttip.add(ClientMessageHandler.getAccessMessage()+ StatCollector.translateToLocal("for." + tag.getString(access)));
            }
            /*if (tag.hasKey(energy)){
                currenttip.add(ClientMessageHandler.getEnergyMessage()+tag.getInteger(energy)+"/"+tag.getInteger(maxEnergy)+" RF");
            }*/
            if (tag.hasKey(energyOut)){
                currenttip.add(ClientMessageHandler.getOutputMessage()+tag.getInteger(energyOut)+" RF/t");
                //currenttip.add(ClientMessageHandler.getHeatMessage()+tag.getString(heat));
            }
            /*if (tag.hasKey(specialData4)){
                boolean q = false;
                if (tag.hasKey(specialData1)){
                    currenttip.add(ClientMessageHandler.getDroneMessage()+tag.getString(specialData1));
                }
                if (tag.hasKey(specialData2)){
                    q = true;
                    currenttip.add(ClientMessageHandler.getQueenMessage()+tag.getString(specialData2));
                }
                if (tag.hasKey(progress) && q){
                    currenttip.add(ClientMessageHandler.getLifeSpanMessage()+ClientMessageHandler.format(tag.getInteger(progress))+"%");
                }
            }*/
            if (tag.hasKey(specialData3)){
                currenttip.add(ClientMessageHandler.getMailMessage(tag.getBoolean(specialData3)));
            }

            //Original WP below
            if (tag.hasKey(ENERGY_STORED) && getConfig("power")) {
                currenttip.add(tag.getInteger(ENERGY_STORED) + " / " + tag.getInteger(MAX_ENERGY_STORED) + " RF");
            }

            if (tag.hasKey(HEAT) && getConfig("heat")) {
                currenttip.add(lang.localize("engineHeat", tag.getInteger(HEAT) / 10D + "\u00B0C"));
            }

            if (tag.hasKey(TREE) && getConfig("sapling")) {
                ITree tree = new Tree(tag.getCompoundTag(TREE));
                addGenomeTooltip(tag, (TileTreeContainer) tile, tree, player, currenttip);
            }

            if (tile instanceof TileLeaves && getConfig("leaves")) {
                if (((TileLeaves) tile).isPollinated()) {
                    currenttip.add(lang.localize("pollinated", tag.getString(LEAF_BRED_SPECIES)));
                }
            }

            if (tile instanceof IBeeHousing && getConfig("apiary")) {
                ItemStack queenstack = null;
                ItemStack dronestack = null;
                if (tag.hasKey(QUEEN_STACK)) {
                    queenstack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(QUEEN_STACK));
                }
                if (tag.hasKey(DRONE_STACK)) {
                    dronestack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(DRONE_STACK));
                }

                IBee queen = null;

                if (queenstack != null) {
                    queen = new Bee(queenstack.getTagCompound());
                    String queenSpecies = getSpeciesName(queen.getGenome(), true);

                    currenttip.add(EnumChatFormatting.WHITE
                            + lang.localize("mainbee", getNameForBeeType(queenstack), EnumChatFormatting.GREEN + queenSpecies));
                    if (queen.isAnalyzed()) {
                        addIndentedBeeInfo(queen, currenttip);
                    }
                }

                IBee drone = null;

                if (queen != null && queen.getMate() != null) {
                    drone = new Bee(queen.getMate());
                } else if (dronestack != null) {
                    drone = new Bee(dronestack.getTagCompound());
                }

                if (drone != null) {
                    currenttip.add(String.format(EnumChatFormatting.WHITE + lang.localize("secondarybee"), lang.localize("drone"),
                            EnumChatFormatting.GREEN + getSpeciesName(drone.getGenome(), true)));

                    if (drone.isAnalyzed()) {
                        addIndentedBeeInfo(drone, currenttip);
                    }
                }

                if (tag.hasKey(ERRORS) || tag.hasKey(BREED_PROGRESS)) {
                    int[] ids = tag.getIntArray(ERRORS);
                    Set<IErrorState> errs = Sets.newHashSet();
                    for (int i : ids) {
                        errs.add(ForestryAPI.errorStateRegistry.getErrorState((short) i));
                    }

                    if (!errs.isEmpty()) {
                        for (IErrorState err : errs) {
                            currenttip.add(EnumChatFormatting.WHITE
                                    + String.format(lang.localize("breedError"), EnumChatFormatting.RED + forLang.localize(err.getDescription())));
                        }
                    } else {
                        currenttip.add(EnumChatFormatting.WHITE
                                + String.format(lang.localize("breedProgress"), EnumChatFormatting.AQUA + pctFmt.format(tag.getDouble(BREED_PROGRESS))));
                    }
                }
            }
        }
    }

    private void addGenomeTooltip(NBTTagCompound tag, TileTreeContainer te, ITree tree, EntityPlayer player, List<String> currenttip) {
        UUID owner = UUID.fromString(tag.getString(OWNER));
        // XXX Leaf isAnalyzed is not working properly, wait for forestry fix
        if (owner.equals(player.getGameProfile().getId()) && (tree.isAnalyzed() || te instanceof TileLeaves)) {
            addTreeTooltip(tree, currenttip);
        } else if (tree != null) {
            currenttip.add(EnumChatFormatting.ITALIC + (tree.isAnalyzed() ? lang.localize("notOwner") : lang.localize("notAnalyzed")));
        }
    }

    private void addTreeTooltip(ITree tree, List<String> currenttip) {
        if (Proxies.common.isShiftDown())
            tree.addTooltip(currenttip);
        else
            currenttip.add(getTMI());
    }

    private String getTMI() {
        return EnumChatFormatting.ITALIC + "<" + StringUtil.localize("gui.tooltip.tmi") + ">";
    }

    private String getSpeciesName(IGenome genome, boolean active) {
        return active ? genome.getActiveAllele(EnumBeeChromosome.SPECIES).getName() : genome.getInactiveAllele(EnumBeeChromosome.SPECIES).getName();
    }

    private String getNameForBeeType(ItemStack bee) {
        return ForestryItem.beeDroneGE.isItemEqual(bee.getItem()) ? lang.localize("drone")
                : ForestryItem.beePrincessGE.isItemEqual(bee.getItem()) ? lang.localize("princess") : lang.localize("queen");
    }

    private void addIndentedBeeInfo(IBee bee, List<String> currenttip) {
        if (Proxies.common.isShiftDown()) {
            List<String> tt = new ArrayList<String>();
            bee.addTooltip(tt);
            for (int i = 0; i < tt.size(); i++) {
                tt.set(i, (i == 0 ? ">" : "") + SpecialChars.TAB + tt.get(i));
            }
            currenttip.addAll(tt);
        } else {
            currenttip.add(getTMI());
        }
    }

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile != null && tag != null){
            if (tile instanceof IRestrictedAccessTile && getConfig("showAccessData")){
                tag.setString(access, ((IRestrictedAccessTile) tile).getAccessHandler().getAccessType().getName());
            }
            if (tile instanceof IPowerHandler){
                tag.setInteger(ENERGY_STORED, ((IPowerHandler) tile).getEnergyManager().getTotalEnergyStored());
                tag.setInteger(MAX_ENERGY_STORED, ((IPowerHandler) tile).getEnergyManager().getMaxEnergyStored());
            }
            if (tile instanceof TileEngine){
                tag.setString(HEAT, ((TileEngine) tile).getTemperatureState().toString());
                tag.setInteger(energyOut, ((TileEngine) tile).getCurrentOutput());
            }
            /*if (tile instanceof IBeeHousing && Config.WAILA.Forestry.showBeeData){
                tag.setBoolean(specialData4, true);
                IBeeHousingInventory inv = ((IBeeHousing) tile).getBeeInventory();
                if (inv != null) {
                    if (inv.getDrone() != null)
                        tag.setString(specialData1, inv.getDrone().getItem().getItemStackDisplayName(inv.getDrone()));
                    if (inv.getQueen() != null)
                        tag.setString(specialData2, inv.getQueen().getItem().getItemStackDisplayName(inv.getQueen()));
                }
                IBeekeepingLogic beekeepingLogic = ((IBeeHousing) tile).getBeekeepingLogic();
                if (beekeepingLogic != null) {
                    tag.setInteger(progress, beekeepingLogic.getBeeProgressPercent());
                }
            }*/
            if (tile instanceof IMailContainer && getConfig("showMailData")){
                tag.setBoolean(specialData3, ((IMailContainer) tile).hasMail());
            }

            // Original WP below
            if (tile instanceof TileLeaves) {
                ITreeGenome mate = ((TileLeaves) tile).getTree().getMate();
                if (mate != null) {
                    tag.setString(LEAF_BRED_SPECIES, mate.getActiveAllele(EnumTreeChromosome.SPECIES).getName());
                }
            }
            if (tile instanceof IBeeHousing) {
                IBeeHousing housing = (IBeeHousing) tile;
                IBeekeepingLogic logic = housing.getBeekeepingLogic();
                IBeeHousingInventory inv = housing.getBeeInventory();
                IErrorLogic errs = housing.getErrorLogic();

                if (logic != null) {
                    ItemStack queen = inv.getQueen();
                    ItemStack drone = inv.getDrone();
                    if (queen != null) {
                        NBTTagCompound queenTag = new NBTTagCompound();
                        queen.writeToNBT(queenTag);
                        tag.setTag(QUEEN_STACK, queenTag);
                    }
                    if (drone != null) {
                        NBTTagCompound droneTag = new NBTTagCompound();
                        drone.writeToNBT(droneTag);
                        tag.setTag(DRONE_STACK, droneTag);
                    }
                    Set<IErrorState> errors = errs.getErrorStates();
                    List<Integer> ids = Lists.newArrayList();
                    for (IErrorState error : errors) {
                        ids.add((int) error.getID());
                    }
                    tag.setIntArray(ERRORS, ArrayUtils.toPrimitive(ids.toArray(new Integer[ids.size()])));

                    if (queen != null && ForestryItem.beeQueenGE.isItemEqual(queen.getItem())) {
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
            if (tile instanceof TileTreeContainer) {
                ITree tree = ((TileTreeContainer) tile).getTree();
                NBTTagCompound treeTag = new NBTTagCompound();
                tree.writeToNBT(treeTag);
                tag.setTag(TREE, treeTag);
            }
        /*if (tile instanceof TileEngine) {
            tag.setInteger(ENERGY_STORED, ((TileEngine) tile).getEnergyManager().getTotalEnergyStored());
            tag.setInteger(MAX_ENERGY_STORED, ((TileEngine) tile).getEnergyManager().getMaxEnergyStored());
            tag.setInteger(HEAT, ((TileEngine) tile).getHeat());
        }*/
            if (tile instanceof IOwnable) {
                GameProfile owner = ((IOwnable) tile).getOwner();
                tag.setString(OWNER, owner.getId().toString());
            }
        }
    }

}
