package tterrag.wailaplugins.config;

import net.minecraft.client.gui.GuiScreen;
import tterrag.wailaplugins.WailaPlugins;

import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.client.config.BaseConfigGui;

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
        return WailaPlugins.lang.localize(WailaPlugins.MODID + ".config.title", false);
    }
    
    @Override
    protected String getLangPrefix()
    {
        return WailaPlugins.MODID + ".config";
    }
}
