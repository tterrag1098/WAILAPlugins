package tterrag.wailaplugins.config;

import net.minecraftforge.common.config.Property;
import tterrag.wailaplugins.WailaPlugins;

import com.enderio.core.common.config.AbstractConfigHandler;

public class WPConfigHandler extends AbstractConfigHandler
{
    public static final WPConfigHandler INSTANCE = new WPConfigHandler();

    private WPConfigHandler()
    {
        super(WailaPlugins.MODID);
    }
    
    public static final String SECTION_BM = "blood_magic";
    public static final String SECTION_RC = "railcraft";
    
    public static final String SECTION_PLUGINS = "plugins";

    // config keys
    
    // blood magic
    public static int sigilBehavior = 1;
    public static boolean seerBenefit = true;
    
    // railcraft
    public static boolean meterInHand = true;

    @Override
    protected void init()
    {
        addSection(SECTION_BM);
        addSection(SECTION_RC);
        
        addSection(SECTION_PLUGINS);
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
        
        activateSection(SECTION_RC);
        meterInHand = getValue("meterInHand", "Does the player need the Electric Meter in hand to see info about RC charge", meterInHand);
    }

    public boolean isPluginEnabled(String modid)
    {
        activateSection(SECTION_PLUGINS);
        boolean ret = getPropFor(modid).getBoolean();
        saveConfigFile();
        return ret;
    }

    public void disablePlugin(String modid)
    {
        getProperty(modid, false).set(false);
        saveConfigFile();
    }

    private Property getPropFor(String modid)
    {
        activateSection(SECTION_PLUGINS);
        Property prop = getProperty(modid, true);
        prop.comment = "Should the plugin for the mod with modid '" + modid + "' be loaded";
        return prop;
    }
}
