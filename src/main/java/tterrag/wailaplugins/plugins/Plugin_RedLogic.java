package tterrag.wailaplugins.plugins;

import java.text.NumberFormat;
import java.util.List;

import mcp.mobius.waila.api.IWailaBlockDecorator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.gui.helpers.UIHelper;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.gates.GateBlock;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Flippable;
import mods.immibis.redlogic.gates.GateTile;
import mods.immibis.redlogic.gates.TimedGateLogic;
import mods.immibis.redlogic.gates.types.GateCounter;
import mods.immibis.redlogic.gates.types.GateTimer;
import mods.immibis.redlogic.wires.EnumWireType;
import mods.immibis.redlogic.wires.RedAlloyTile;
import mods.immibis.redlogic.wires.WireTile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.BlockCoord;

import static net.minecraftforge.common.util.ForgeDirection.*;

/**
 * @author ProfMobius, ported and adapted to RedLogic by tterrag
 */
public class Plugin_RedLogic extends PluginBase implements IWailaBlockDecorator
{
    // @formatter:off
    byte[][] IOARRAY = {
            {2,1,1,1} /* AND   */, {2,1,1,1} /* OR    */, {2,2,1,2} /* NOT   */, {2,1,2,1} /* Latch */,
            {2,1,2,1} /* T-FF  */, {2,1,1,1} /* NOR   */, {2,1,1,1} /* NAND  */, {2,1,0,1} /* XOR   */,
            {2,1,0,1} /* XNOR  */, {2,2,1,2} /* Buff  */, {2,1,1,1} /* MOX   */, {2,0,1,0} /* Repeat*/,
            {2,2,6,2} /* Timer */, {9,2,8,2} /* Count */, {2,2,2,2} /* Sequen*/, {2,0,1,0} /* Pulse */,
            {2,2,1,2} /* Rand  */, {2,2,6,1} /* State */, {2,1,6,1} /* Sync  */, {2,4,5,2} /*D-Latch*/,
            {2,4,5,2} /* D-FF  */, 
            
            // bundled gates
            {2,13,1,0} /* Latch */, {2,13,1,0} /* Relay */, {2,1,3,1} /* MOX  */, {2,1,1,1} /* AND */,
            {2,1,1,1}  /* OR    */, {2,2,1,2}  /* NOT   */, {2,1,1,1} /* XOR  */, {0,0,0,0} /* it skips a number here... */,
            {2,14,1,14}/* Comparator */
    };      
        
    // this is now defined as a compressed 3D array. The first byte is the subID, and each sub-group of numbers is for that state of the cell + 1 (0 is the default defined in the above array)
    byte[][] IOARRAYALTER = {
            {0,  2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* AND */
            {1,  2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* OR  */
            {2,  2,2,1,0,  0,2,1,2,  0,2,1,0,  2,0,1,2,  2,0,1,0,  0,0,1,2,  0,0,1,0}, /* NOT */
            {5,  2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* NOR */
            {6,  2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* NAND */
            
            // bundled gates
            {24, 2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* AND */
            {25, 2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* OR  */
            {26, 2,2,1,0,  0,2,1,2,  0,2,1,0,  2,0,1,2,  2,0,1,0,  0,0,1,2,  0,0,1,0}, /* NOT */
            {27, 2,1,1,0,  2,1,0,1,  2,1,0,0,  2,0,1,1,  2,0,1,0,  2,0,0,1,  2,0,0,0}, /* OR  */
            {29, 2,15,1,15} /* Comparator */
    };
    // @formatter:on

    String[] IONAMES = { "", "IN", "OUT", "SWAP", "IN_A", "IN_B", "LOCK", "IO", "POS", "NEG", "BUS", "A", "B", "UNLOCK", "COMPARE", "SUBTRACT" };

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registrar.registerDecorator(this, GateBlock.class);

        registerBody(GateTile.class, WireTile.class);

        registerNBT(GateTile.class, WireTile.class);

        addConfig("overlay");
        addConfig("data");
        addConfig("strength");
    }

    @Override
    public void decorateBlock(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (!getConfig("overlay")) return;
        
        NBTTagCompound tag = accessor.getNBTData();

        ForgeDirection vOrient = ForgeDirection.getOrientation(tag.getInteger("side"));
        int front = tag.getInteger("front");

        // @formatter:off
        // let's fix up the orientations
        {
            // offset everything to 0-3
            if (vOrient == UP || vOrient == DOWN) front -=2;
            if (vOrient == NORTH || vOrient == SOUTH) front = front < 2 ? front : front - 2;
            if (vOrient == EAST || vOrient == WEST) front = (front + 2) % 4;
            
            // make them rotate in the ForgeDirection order instead of circularly
            if      (front == 0) front = 2;
            else if (front == 1) front = 0;
            else if (front == 2) front = 1;
            else if (front == 3) front = 3;
            
            if (vOrient == WEST || vOrient == NORTH || vOrient == UP) front = front == 3 ? 1 : front == 1 ? 3 : front; // 3 and 1 must be swapped on these directions
        }
        // @formatter:on

        EnumGates type = EnumGates.VALUES[tag.getInteger("type")];
        int subID = type.ordinal(); // equivalent to stack damage of gate
        int state = tag.getShort("gateSettings"); // the "sub-state" of the gate
        boolean flipped = tag.getBoolean("flipped");

        int hOrient = front;

        String[] IOStr = new String[4];

        int alterID = -1;

        // don't bother checking alterations if state is default
        if (state != 0)
        {
            for (int i = 0; i < IOARRAYALTER.length; i++)
            {
                // if the first index of the alter array matches our subID, we use the alterations
                if (IOARRAYALTER[i][0] == subID)
                {
                    alterID = IOARRAYALTER[i][0];
                    for (int j = 0; j < 4; j++)
                        IOStr[j] = IONAMES[IOARRAYALTER[i][1 + j + (state - 1) * 4]];
                }
            }
        }

        // don't overwrite the alterations if we found one
        if (alterID == -1)
        {
            for (int i = 0; i < 4; i++)
                IOStr[i] = IONAMES[IOARRAY[subID][i]];
        }

        String[] IOStrRot = new String[4];

        // rotate the strings around for the orientation
        for (int i = 0; i < 4; i++)
        {
            int j = i + hOrient;
            j %= 4;
            IOStrRot[j] = IOStr[i];
        }

        // flip the string array
        if (flipped)
        {
            // we must flip the sides perpendicular to the front
            int idx = front % 2 == 0 ? 1 : 0;
            String temp = IOStrRot[idx];
            IOStrRot[idx] = IOStrRot[idx + 2];
            IOStrRot[idx + 2] = temp;
        }

        // below code taken straight from WAILA with <3
        switch (vOrient)
        {
        case DOWN:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.5F, 0.2F, 1.4F, 90F, 0F, 0F);  // Orient 0
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), -0.4F, 0.2F, 0.5F, 90F, 270F, 0F);  // Orient 1
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.5F, 0.2F, -0.4F, 90F, 180F, 0F);  // Orient 2
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), 1.4F, 0.2F, 0.5F, 90F, 90F, 0F);  // Orient 3
            break;
        case EAST:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.8F, 0.5F, 1.4F, 0F, 90F, 90F);
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), 0.8F, -0.4F, 0.5F, 0F, 90F, 180F);
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.8F, 0.5F, -0.4F, 0F, 90F, 270F);
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), 0.8F, 1.4F, 0.5F, 0F, 90F, 0F);
            break;
        case NORTH:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.5F, 1.4F, 0.2F, 0F, 180F, 0F);
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), 1.4F, 0.5F, 0.2F, 0F, 180F, 90F);
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.5F, -0.4F, 0.2F, 0F, 180F, 180F);
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), -0.4F, 0.5F, 0.2F, 0F, 180F, 270F);
            break;
        case SOUTH:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.5F, 1.4F, 0.8F, 0F, 0F, 0F);
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), -0.4F, 0.5F, 0.8F, 0F, 0F, 90F);
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.5F, -0.4F, 0.8F, 0F, 0F, 180F);
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), 1.4F, 0.5F, 0.8F, 0F, 0F, 270F);
            break;
        case UP:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.5F, 0.8F, 1.4F, 270F, 180F, 0F);
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), 1.4F, 0.8F, 0.5F, 270F, 270F, 0F);
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.5F, 0.8F, -0.4F, 270F, 0F, 0F);
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), -0.4F, 0.8F, 0.5F, 270F, 90F, 0F);
            break;
        case WEST:
            UIHelper.drawFloatingText(IOStrRot[0], accessor.getRenderingPosition(), 0.2F, 0.5F, 1.4F, 180F, 90F, 90F);
            UIHelper.drawFloatingText(IOStrRot[1], accessor.getRenderingPosition(), 0.2F, 1.4F, 0.5F, 180F, 90F, 180F);
            UIHelper.drawFloatingText(IOStrRot[2], accessor.getRenderingPosition(), 0.2F, 0.5F, -0.4F, 180F, 90F, 270F);
            UIHelper.drawFloatingText(IOStrRot[3], accessor.getRenderingPosition(), 0.2F, -0.4F, 0.5F, 180F, 90F, 0F);
            break;
        default:
            break;
        }

        double x = accessor.getRenderingPosition().xCoord;
        double y = accessor.getRenderingPosition().yCoord;
        double z = accessor.getRenderingPosition().zCoord;

        switch (vOrient)
        {
        case DOWN:
            UIHelper.drawRectangle(x - 0.1, y + 0.1, z, x, y + 0.1, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangle(x - 0.1, y + 0.1, z + 0.65, x, y + 0.1, z + 1.0, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.0, y + 0.1, z, x + 1.1, y + 0.1, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.0, y + 0.1, z + 0.65, x + 1.1, y + 0.1, z + 1.0, 255, 255, 255, 150);

            UIHelper.drawRectangle(x - 0.1, y + 0.1, z - 0.1, x + 0.35, y + 0.1, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.65, y + 0.1, z - 0.1, x + 1.1, y + 0.1, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x - 0.1, y + 0.1, z + 1.0, x + 0.35, y + 0.1, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.65, y + 0.1, z + 1.0, x + 1.1, y + 0.1, z + 1.1, 255, 255, 255, 150);
            break;
        case EAST:
            UIHelper.drawRectangleEW(x + 0.9, y, z - 0.1, x + 0.9, y + 0.35, z, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y + 0.65, z - 0.1, x + 0.9, y + 1.0, z, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y, z + 1.0, x + 0.9, y + 0.35, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y + 0.65, z + 1.0, x + 0.9, y + 1.0, z + 1.1, 255, 255, 255, 150);

            UIHelper.drawRectangleEW(x + 0.9, y - 0.1, z - 0.1, x + 0.9, y, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y - 0.1, z + 0.65, x + 0.9, y, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y + 1.0, z - 0.1, x + 0.9, y + 1.1, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.9, y + 1.0, z + 0.65, x + 0.9, y + 1.1, z + 1.1, 255, 255, 255, 150);
            break;
        case NORTH:
            UIHelper.drawRectangle(x - 0.1, y, z + 0.1, x, y + 0.35, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x - 0.1, y + 0.65, z + 0.1, x, y + 1.0, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.0, y, z + 0.1, x + 1.1, y + 0.35, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.0, y + 0.65, z + 0.1, x + 1.1, y + 1.0, z + 0.1, 255, 255, 255, 150);

            UIHelper.drawRectangle(x - 0.1, y - 0.1, z + 0.1, x + 0.35, y, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.65, y - 0.1, z + 0.1, x + 1.1, y, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x - 0.1, y + 1.0, z + 0.1, x + 0.35, y + 1.1, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.65, y + 1.0, z + 0.1, x + 1.1, y + 1.1, z + 0.1, 255, 255, 255, 150);
            break;
        case SOUTH:
            UIHelper.drawRectangle(x, y, z + 0.9, x - 0.1, y + 0.35, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x, y + 0.65, z + 0.9, x - 0.1, y + 1.0, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y, z + 0.9, x + 1.0, y + 0.35, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 0.65, z + 0.9, x + 1.0, y + 1.0, z + 0.9, 255, 255, 255, 150);

            UIHelper.drawRectangle(x + 0.35, y - 0.1, z + 0.9, x - 0.1, y, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.10, y - 0.1, z + 0.9, x + 0.65, y, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.35, y + 1.0, z + 0.9, x - 0.1, y + 1.1, z + 0.9, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 1.0, z + 0.9, x + 0.65, y + 1.1, z + 0.9, 255, 255, 255, 150);
            break;
        case UP:
            UIHelper.drawRectangle(x, y + 0.9, z, x - 0.1, y + 0.9, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangle(x, y + 0.9, z + 0.65, x - 0.1, y + 0.9, z + 1.0, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 0.9, z, x + 1.0, y + 0.9, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 0.9, z + 0.65, x + 1.0, y + 0.9, z + 1.0, 255, 255, 255, 150);

            UIHelper.drawRectangle(x + 0.35, y + 0.9, z - 0.1, x - 0.1, y + 0.9, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 0.9, z - 0.1, x + 0.65, y + 0.9, z + 0.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 0.35, y + 0.9, z + 1.0, x - 0.1, y + 0.9, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangle(x + 1.1, y + 0.9, z + 1.0, x + 0.65, y + 0.9, z + 1.1, 255, 255, 255, 150);

            break;
        case WEST:
            UIHelper.drawRectangleEW(x + 0.1, y + 0.35, z - 0.1, x + 0.1, y, z, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y + 1.0, z - 0.1, x + 0.1, y + 0.65, z, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y + 0.35, z + 1.0, x + 0.1, y, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y + 1.0, z + 1.0, x + 0.1, y + 0.65, z + 1.1, 255, 255, 255, 150);

            UIHelper.drawRectangleEW(x + 0.1, y, z - 0.1, x + 0.1, y - 0.1, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y, z + 0.65, x + 0.1, y - 0.1, z + 1.1, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y + 1.1, z - 0.1, x + 0.1, y + 1.0, z + 0.35, 255, 255, 255, 150);
            UIHelper.drawRectangleEW(x + 0.1, y + 1.1, z + 0.65, x + 0.1, y + 1.0, z + 1.1, 255, 255, 255, 150);
            break;
        default:
            break;

        }
    }

    // repeater delays
    private static final int[] DELAYS = { 1, 2, 4, 8, 16, 32, 64, 128 }; // in redstone ticks

    private static final NumberFormat secFmt = NumberFormat.getNumberInstance();
    static
    {
        secFmt.setMinimumFractionDigits(2);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        NBTTagCompound tag = accessor.getNBTData();
        TileEntity tile = accessor.getTileEntity();

        if (tile instanceof GateTile && getConfig("data"))
        {
            EnumGates type = EnumGates.VALUES[tag.getInteger("type")];
            GateLogic logic = type.createLogic();
            int state = tag.getShort("gateSettings"); // the "sub-state" of the gate
            boolean flipped = tag.getBoolean("flipped");

            logic.configure(state);
            logic.read(tag.getCompoundTag("logic"));

            if (logic instanceof Flippable)
            {
                currenttip.add(String.format(lang.localize("flipped"), lang.localize(flipped ? "yes" : "no")));
            }

            if (logic instanceof TimedGateLogic)
            {
                double interval = ((TimedGateLogic) logic).getInterval();
                double seconds = interval / 20d;
                currenttip.add(String.format(lang.localize("interval"), secFmt.format(seconds) + "s"));
            }

            switch (type)
            {
            case Repeater:
                currenttip.add(String.format(lang.localize("delay"), DELAYS[state]));
                break;
            case Timer:
                double time = ((GateTimer.Logic) logic).ticksLeft;
                double interval = ((GateTimer.Logic) logic).intervalTicks;
                time = (interval - time) / 20d;
                currenttip.add(String.format(lang.localize("currentTime"), secFmt.format(time)));
                break;
            case Counter:
                GateCounter.Logic counter = (GateCounter.Logic) logic;
                currenttip.add(""); // blank line after flipped
                currenttip.add(String.format(lang.localize("count"), counter.value));
                currenttip.add(String.format(lang.localize("countMax"), counter.max));
                currenttip.add(String.format(lang.localize("incrAmnt"), counter.incr));
                currenttip.add(String.format(lang.localize("decrAmnt"), counter.decr));
                break;
            case Comparator:
                currenttip.add(String.format(lang.localize("mode"), lang.localize(state == 1 ? "subtract" : "comparat")));
            default:
                break;
            }
        }
        
        if (tile instanceof WireTile && getConfig("strength"))
        {
            EnumWireType type = EnumWireType.VALUES[tag.getByte("type")];
            
            switch(type)
            {
            case RED_ALLOY:
                int str = ((RedAlloyTile)tile).getRedstoneSignalStrength();
                currenttip.add(String.format(lang.localize("strength"), Integer.toString(str)));
                break;
            default:
                break;
            }
        }
    }
    
    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos)
    {
        te.writeToNBT(tag);
    }
}
