package tterrag.wailaplugins.plugins;

import static net.minecraftforge.common.util.ForgeDirection.*;
import java.util.Arrays;

import mcp.mobius.waila.api.IWailaBlockDecorator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.gui.helpers.UIHelper;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.gates.GateBlock;
import mods.immibis.redlogic.gates.GateTile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class Plugin_RedLogic extends PluginBase implements IWailaBlockDecorator
{   
    // @formatter:off
    byte[][] IOARRAY = {
        {2,1,1,1} /* AND   */, {1,2,1,1} /* OR    */, {2,2,2,1} /* NOT   */, {1,2,1,2} /* Latch */,
        {1,2,1,2} /* T-FF  */, {1,2,1,1} /* NOR   */, {1,2,1,1} /* NAND  */, {1,2,1,0} /* XOR   */,
        {1,2,1,0} /* XNOR  */, {2,2,2,1} /* Buff  */, {1,2,1,1} /* MOX   */, {0,2,0,1} /* Repeat*/,
        {2,2,2,6} /* Timer */, {2,9,2,8} /* Count */, {2,2,2,2} /* Sequen*/, {0,2,0,1} /* Pulse */,
        {2,2,2,1} /* Rand  */, {1,2,2,6} /* State */, {1,2,1,6} /* Sync  */, {2,2,4,5} /*D-Latch*/,
        {2,2,4,5} /* D-FF  */, 
        
        // bundled gates
        {0,2,12,1} /* Latch */, {10,1,10,1} /* Bus */, {11,12,11,12} /* Null  */,
        {11,12,11,12} /* Inver */, {11,12,11,12} /* Buff  */, {1,9,2,8} /* Cmp    */, {1,12,2,12} /* And   */,
    };      
    
    // this is now defined as a compressed 3D array. Each sub-group of numbers is for that state of the cell + 1 (0 is the default defined in the above array)
    byte[][] IOARRAYALTER = {
            {0,2,1,1,  1,2,1,0,  0,2,1,0,  1,2,0,1,  0,2,0,1,  1,2,0,0,  0,2,0,0} /* And */
    };     
    
    static String[] IONAMES={ "", "IN", "OUT", "SWAP", "IN_A", "IN_B", "LOCK", "IO", "POS", "NEG", "BUS", "A", "B", "UNLOCK"};
    // @formatter:on

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registrar.registerDecorator(this, GateBlock.class);
        
        registerBody(GateTile.class);
        
        syncNBT(GateTile.class);
    }

    @Override
    public void decorateBlock(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        NBTTagCompound tag = accessor.getNBTData();

        ForgeDirection vOrient = ForgeDirection.getOrientation(tag.getInteger("side"));
        int f = tag.getInteger("front");
        
        // this block doesn't work
        {
            if (vOrient == UP || vOrient == DOWN) f -=2;
            if (vOrient == NORTH || vOrient == SOUTH) f = f < 2 ? f : f - 2;
            
            if      (f == 0) f = 2;
            else if (f == 1) f--;
            else if (f == 2) f++;
            else             f = 3;
        }

        ForgeDirection front = ForgeDirection.getOrientation(f);

        EnumGates type = EnumGates.VALUES[tag.getInteger("type")];
        int subID = type.ordinal();
        int state = tag.getShort("gateSettings");
        boolean flipped = tag.getBoolean("flipped");

        int hOrient = front.ordinal();

        if (hOrient == -1)
            hOrient = 3;

        String[] IOStr = new String[4];

        int alterID = -1;
        if (state != 0)
        {
            if (subID == 0) alterID = 0; // AND
//            if (subID == 21) alterID = 0; // state cell
            
            if (alterID != -1)
            {
                for (int i = 0; i < 4; i++)
                    IOStr[i] = IONAMES[IOARRAYALTER[alterID][i + (state - 1) * 4]];  
            }
        }
        if (alterID == -1)
        {
            for (int i = 0; i < 4; i++)
                IOStr[i] = IONAMES[IOARRAY[subID][i]];
        }

        String[] IOStrRot = new String[4];

        for (int i = 0; i < 4; i++)
        {
            int j = i + hOrient;
            j %= 4;
            IOStrRot[j] = IOStr[i];
        }
        
        if (flipped)
        {
            String temp = IOStrRot[1];
            IOStrRot[1] = IOStrRot[3];
            IOStrRot[3] = temp;
        }

        System.out.println(tag.getInteger("front") + "  " + vOrient.toString() + "  " + Arrays.toString(IOStrRot));
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
}
