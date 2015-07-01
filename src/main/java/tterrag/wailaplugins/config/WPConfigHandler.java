package tterrag.wailaplugins.config;

import net.minecraftforge.common.config.Property;
import tterrag.wailaplugins.WailaPlugins;

import com.enderio.core.common.config.AbstractConfigHandler;
import com.enderio.core.common.config.ConfigProcessor;
import com.enderio.core.common.config.annot.Comment;
import com.enderio.core.common.config.annot.Config;
import com.enderio.core.common.config.annot.Range;

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
    @Config(SECTION_BM)
    @Range(min = 0, max = 2)
    @Comment({ "Determines the behavior of the shown info.\n", 
            "0 - No sigil needed at all.", 
            "1 - Need sigil in your inventory",
            "2 - Need sigil in your hand." })
    public static int sigilRequirement = 1;
    @Config(SECTION_BM)
    @Comment({ "Determines how the Sigil of Sight affects the altar info.\n",
            "true - Sigil of Sight shows altar progress while Divination Sigil will not.",
            "false - Sigil of Sight provides no extra info and Divination sigil shows all information." })
    public static boolean seerBenefit = true;

    // railcraft
    @Config(SECTION_RC)
    @Comment("Does the player need the Electric Meter in hand to see info about RC charge")
    public static boolean meterInHand = true;

    @Override
    protected void init()
    {
        addSection(SECTION_BM);
        addSection(SECTION_RC);
        addSection(SECTION_PLUGINS);

        new ConfigProcessor(getClass(), this).process(true);
    }

    @Override
    protected void reloadNonIngameConfigs()
    {
        ;
    }

    @Override
    protected void reloadIngameConfigs()
    {
        ;
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
