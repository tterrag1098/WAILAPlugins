package tterrag.wailaplugins.config;

import tterrag.core.common.config.AbstractConfigHandler;
import tterrag.wailaplugins.WailaPlugins;

public class WPConfigHandler extends AbstractConfigHandler
{
    public static final WPConfigHandler INSTANCE = new WPConfigHandler();

    private WPConfigHandler()
    {
        super(WailaPlugins.MODID);
    }
    
    public static final String SECTION_BM = "blood_magic";

    // config keys
    
    // blood magic
    public static boolean doNeedDiviniation = true;

    @Override
    protected void init()
    {
        addSection(SECTION_BM, SECTION_BM);
    }

    @Override
    protected void reloadNonIngameConfigs()
    {
        // none
    }

    @Override
    protected void reloadIngameConfigs()
    {
        activateSection(SECTION_BM);
        doNeedDiviniation = getValue("needDivinationSigil", "Does the player need the divination sigil in hand to see info", doNeedDiviniation);
    }

}
