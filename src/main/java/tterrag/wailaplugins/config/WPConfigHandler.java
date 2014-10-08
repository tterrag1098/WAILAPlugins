package tterrag.wailaplugins.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class WPConfigHandler
{
    public static boolean doNeedDiviniation = true;

    public static final String SECTION_BM = "blood_magic";

    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        config.load();
        doNeedDiviniation = config.get(SECTION_BM, "needDivinationSigil", doNeedDiviniation, "Does the player need the divination sigil in hand to see info").getBoolean();

        config.save();
    }

}
