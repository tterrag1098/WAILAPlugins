package tterrag.wailaplugins.plugins;

import com.enderio.core.client.render.RenderUtil;
import com.mark719.magicalcrops.blocks.BlockMagicalCrops;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaBlockDecorator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import tterrag.wailaplugins.api.Plugin;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@Plugin(deps = "magicalcrops")
public class PluginMagicalCrops extends PluginBase implements IWailaBlockDecorator {
    private static EntityItem item;

    public void load(IWailaRegistrar registrar) {
        super.load(registrar);

        registerBody(BlockMagicalCrops.class);

        registrar.registerDecorator(this, BlockMagicalCrops.class);

        addConfig("showHover");
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor) {
        int meta = accessor.getMetadata();
        currenttip.add(StatCollector.translateToLocal("hud.msg.growth") + " : " + (meta < 7 ? ((int) (((double) meta / 7) * 100)) + "%" : StatCollector.translateToLocal("hud.msg.mature")));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void decorateBlock(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!getConfig("showHover") || !enabled()) {
            return;
        }

        ItemStack stack = new ItemStack(accessor.getBlock().getItemDropped(7, accessor.getWorld().rand, 0), 1, accessor.getBlock().damageDropped(7));
        Vec3 pos = accessor.getRenderingPosition();
        if (item == null) {
            item = new EntityItem(accessor.getWorld(), 0, 0, 0, stack);
        } else {
            item.setEntityItemStack(stack);
        }

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
