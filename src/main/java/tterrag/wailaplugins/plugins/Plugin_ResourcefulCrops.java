package tterrag.wailaplugins.plugins;

import mcp.mobius.waila.api.IWailaBlockDecorator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import tehnut.resourceful.crops.api.base.Seed;
import tehnut.resourceful.crops.api.registry.SeedRegistry;
import tehnut.resourceful.crops.block.BlockRCrop;
import tehnut.resourceful.crops.registry.ItemRegistry;
import tehnut.resourceful.crops.tile.TileRCrop;
import tehnut.resourceful.crops.util.Utils;

import com.enderio.core.client.render.RenderUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

public class Plugin_ResourcefulCrops extends PluginBase implements IWailaBlockDecorator {

    private static EntityItem item;

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        registrar.registerDecorator(this, BlockRCrop.class);
        addConfig("showHover");
        addConfig("showOutputItem", false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void decorateBlock(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (!getConfig("showHover") || !enabled())
            return;

        Vec3 pos = accessor.getRenderingPosition();

        if (accessor.getBlock() instanceof BlockRCrop)
        {
            TileEntity cropTile = accessor.getTileEntity();
            if (cropTile != null && cropTile instanceof TileRCrop)
            {
                Seed seed = SeedRegistry.getSeed(((TileRCrop) cropTile).getSeedName());
                ItemStack hoverStack = Utils.isValidSeed(seed) ? new ItemStack(ItemRegistry.shard, 1, SeedRegistry.getIndexOf(seed)) : Utils.getInvalidSeed(ItemRegistry.shard);
                
                if (getConfig("showOutputItem"))
                    hoverStack = new ItemStack(seed.getOutput().getItem(), 1, seed.getOutput().getItemDamage());
                
                new ItemStack(ItemRegistry.shard, 1, SeedRegistry.getIndexOf(seed));

                if (item == null)
                    item = new EntityItem(accessor.getWorld(), 0, 0, 0, hoverStack);
                else
                    item.setEntityItemStack(hoverStack);

                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glEnable(GL_TEXTURE_2D);
                RenderHelper.enableStandardItemLighting();
                glTranslated(pos.xCoord + 0.5, pos.yCoord + 0.9, pos.zCoord + 0.5);
                glPushMatrix();
                glScalef(0.75f, 0.75f, 0.75f);
                RenderUtil.render3DItem(item, true);
                glPopMatrix();
                glPopAttrib();
                glPopMatrix();
            }
        }
    }
}
