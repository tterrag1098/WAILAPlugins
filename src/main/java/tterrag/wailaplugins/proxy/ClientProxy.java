package tterrag.wailaplugins.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class ClientProxy extends CommonProxy
{
    @Override
    public MovingObjectPosition getMouseOver()
    {
        return Minecraft.getMinecraft().objectMouseOver;
    }
}
