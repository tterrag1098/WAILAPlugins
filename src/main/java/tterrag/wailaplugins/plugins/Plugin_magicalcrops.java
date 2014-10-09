package tterrag.wailaplugins.plugins;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import mcp.mobius.waila.api.IWailaBlockDecorator;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import tterrag.core.client.util.RenderingUtils;

import com.mark719.magicalcrops.crops.BlockMagicalCrops;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Plugin_magicalcrops extends PluginBase implements IWailaBlockDecorator
{    
    public void load(IWailaRegistrar registrar)
    {
        if (Loader.isModLoaded("magicalcrops"))
        {
            registrar.registerBodyProvider(this, BlockMagicalCrops.class);
            registrar.registerDecorator(this, BlockMagicalCrops.class);
        }
    }

    @Override
    public void getBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        int meta = accessor.getMetadata();
        currenttip.add(StatCollector.translateToLocal("hud.msg.growth") + " : " + (meta < 7 ?  ((int) (((double) meta / 7) * 100)) + "%" : StatCollector.translateToLocal("hud.msg.mature")));
    }

    private static EntityItem item;
    @Override
    @SideOnly(Side.CLIENT)
    public void decorateBlock(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        ItemStack stack = new ItemStack(accessor.getBlock().getItemDropped(7, accessor.getWorld().rand, 0), 1, accessor.getBlock().damageDropped(7));
        Vec3 pos = accessor.getRenderingPosition();
        if (item == null)
        {
            item = new EntityItem(accessor.getWorld(), 0, 0, 0, stack);
        }
        else
        {
            item.setEntityItemStack(stack);
        }
        
        glPushMatrix();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glEnable(GL_TEXTURE_2D);
        RenderHelper.enableStandardItemLighting();
        glTranslated(pos.xCoord + 0.5, pos.yCoord + 0.9, pos.zCoord + 0.5);
        glPushMatrix();
        glScalef(0.75f, 0.75f, 0.75f);
        RenderingUtils.render3DItem(item, true);
        glPopMatrix();
        glPopAttrib();
        glPopMatrix();
    }
}
