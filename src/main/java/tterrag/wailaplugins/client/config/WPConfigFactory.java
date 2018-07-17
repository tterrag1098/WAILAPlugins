package tterrag.wailaplugins.client.config;

import net.minecraft.client.gui.GuiScreen;

import com.enderio.core.common.config.BaseConfigFactory;

public class WPConfigFactory extends BaseConfigFactory
{
    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) 
    {
        return new WPConfigGui(parentScreen);
    }
}
