package tterrag.wailaplugins.config;

import net.minecraft.client.gui.GuiScreen;
import tterrag.core.api.common.config.IConfigHandler;
import tterrag.core.client.config.BaseConfigGui;
import tterrag.wailaplugins.WailaPlugins;

public class WPConfigGui extends BaseConfigGui
{
    public WPConfigGui(GuiScreen parentScreen)
    {
        super(parentScreen);
    }

    @Override
    protected IConfigHandler getConfigHandler()
    {
        return WPConfigHandler.INSTANCE;
    }
    
    @Override
    protected String getTitle()
    {
        return WailaPlugins.lang.localize("config.title");
    }
}
