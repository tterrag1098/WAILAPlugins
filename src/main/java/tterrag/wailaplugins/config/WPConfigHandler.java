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
    public static int sigilBehavior = 1;
    public static boolean seerBenefit = true;

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
        sigilBehavior = getValue("sigilRequirement", "Determines the behavior of the shown info.\n\n0 - No sigil needed at all.\n1 - Need sigil in your inventory\n2 - Need sigil in your hand.", sigilBehavior);
        seerBenefit = getValue("seerBenefit", "Determines how the Sigil of Sight affects the altar info.\n\ntrue - Sigil of Sight shows altar progress while Divination Sigil will not.\nfalse - Sigil of Sight provides no extra info and Divination sigil shows all information.", seerBenefit);
    }
}
