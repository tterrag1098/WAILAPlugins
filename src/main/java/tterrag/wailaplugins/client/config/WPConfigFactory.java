package tterrag.wailaplugins.client.config;

import com.enderio.core.common.config.BaseConfigFactory;
import net.minecraft.client.gui.GuiScreen;

public class WPConfigFactory extends BaseConfigFactory {
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return WPConfigGui.class;
    }
}
