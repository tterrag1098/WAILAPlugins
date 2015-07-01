package tterrag.wailaplugins.client.config;

import net.minecraft.client.gui.GuiScreen;

import com.enderio.core.common.config.BaseConfigFactory;

public class WPConfigFactory extends BaseConfigFactory
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return WPConfigGui.class;
    }
}
