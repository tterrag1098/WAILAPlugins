package tterrag.wailaplugins.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.StatCollector;

import java.text.DecimalFormat;

import static tterrag.wailaplugins.client.ClientMessageHandler.WailaSpecialChars.ITALIC;

/**
 * Created by Elec332 on 6-10-2015.
 */
@SideOnly(Side.CLIENT)
public class ClientMessageHandler {

    private static final DecimalFormat format = new DecimalFormat("####.##");

    public static String getEmptyMessage(){
        return ITALIC + localise("cmip.message.empty");
    }

    public static String getLiquidMessage(){
        return localise("cmip.message.liquid") + ": ";
    }

    public static String getAmountMessage(){
        return localise("cmip.message.amount") + ": ";
    }

    public static String getDimensionMessage(){
        return localise("cmip.message.dimension") + ": ";
    }

    public static String getNameMessage(){
        return localise("cmip.message.name") + ": ";
    }

    public static String getProfessionMessage(){
        return localise("cmip.message.profession") + ": ";
    }

    public static String getEnergyMessage(){
        return localise("cmip.message.energy") + ": ";
    }

    public static String getEnergyTierMessage(){
        return localise("cmip.message.energy") + localise("cmip.message.tier") + ": ";
    }

    public static String getTierMessage(){
        return localise("cmip.message.tier") + ": ";
    }

    public static String getOutputMessage(){
        return localise("cmip.message.output") + ": ";
    }

    public static String getProgressMessage(){
        return localise("cmip.message.progress") + ": ";
    }

    public static String getTargetMessage(){
        return localise("cmip.message.target") + ": ";
    }

    @Deprecated
    public static String getHeatMessage(){
        return localise("cmip.message.heat") + ": ";
    }

    public static String getMaxMessage(){
        return localise("cmip.message.max");
    }

    public static String getMinMessage(){
        return localise("cmip.message.min");
    }

    public static String getAverageMessage(){
        return localise("cmip.message.average");
    }

    public static String getNoAccessMessage(){
        return ITALIC + localise("cmip.message.noAccess");
    }

    public static String getOwnerMessage(){
        return localise("cmip.message.owner") + ": ";
    }

    public static String getActiveMessage(){
        return localise("cmip.message.active") + ": ";
    }

    public static String getRangeMessage(){
        return localise("cmip.message.range") + ": ";
    }

    public static String getLoadedChunksMessage(){
        return localise("cmip.message.loadedChunks") + ": ";
    }

    public static String getAccessMessage(){
        return localise("cmip.message.access") + ": ";
    }

    public static String getDroneMessage(){
        return localise("cmip.message.forestry.drone") + ": ";
    }

    public static String getQueenMessage(){
        return localise("cmip.message.forestry.queen") + ": ";
    }

    public static String getMailMessage(boolean mail){
        return mail ? localise("cmip.message.forestry.newMail") + "!" : ITALIC + localise("cmip.message.forestry.noNewMail");
    }

    public static String getLifeSpanMessage(){
        return localise("cmip.message.lifeSpan") + ": ";
    }

    public static String getSearchLocationMessage(){
        return localise("cmip.message.searchLocation") + ": ";
    }

    public static String getFrequencyMessage(){
        return localise("cmip.message.frequency") + ": ";
    }

    public static String getPressureMessage(){
        return localise("cmip.message.pressure") + ": ";
    }

    public static String getFluidMessage(){
        return localise("cmip.message.fluid") + ": ";
    }

    public static String getTemperatureMessage(){
        return localise("cmip.message.temperature") + ": ";
    }

    public static String getVoltageMessage(){
        return localise("cmip.message.voltage") + ": ";
    }

    public static String getResistanceMessage(){
        return localise("cmip.message.resistance") + ": ";
    }

    public static String getConnectedMachinesMessage(){
        return localise("cmip.message.connectedMachines") + ": ";
    }

    public static String getModeMessage(){
        return localise("cmip.message.mode") + ": ";
    }

    public static String getOutPutMessage(){
        return localise("cmip.message.input") + ": ";
    }

    public static String getInPutMessage(){
        return localise("cmip.message.output") + ": ";
    }

    public static String getNiceInputModeMessage(boolean output){
        return getModeMessage()+removeColon(output?getOutputMessage():getInPutMessage());
    }

    public static String getNoConnectionMessage(){
        return ITALIC + localise("cmip.message.noConnection");
    }


    private static String localise(String s){
        return StatCollector.translateToLocal(s);
    }

    public static String format(double d){
        return format.format(d);
    }

    public static String removeColon(String s){
        return s.replace(": ", "");
    }
    /**
     * Copied from WAILA-API to avoid crashes when WAILA is not loaded
     */
    public static class WailaSpecialChars {

        public static String MCStyle  = "\u00A7";

        public static String BLACK    = MCStyle + "0";
        public static String DBLUE    = MCStyle + "1";
        public static String DGREEN   = MCStyle + "2";
        public static String DAQUA    = MCStyle + "3";
        public static String DRED     = MCStyle + "4";
        public static String DPURPLE  = MCStyle + "5";
        public static String GOLD     = MCStyle + "6";
        public static String GRAY     = MCStyle + "7";
        public static String DGRAY    = MCStyle + "8";
        public static String BLUE     = MCStyle + "9";
        public static String GREEN    = MCStyle + "a";
        public static String AQUA     = MCStyle + "b";
        public static String RED      = MCStyle + "c";
        public static String LPURPLE  = MCStyle + "d";
        public static String YELLOW   = MCStyle + "e";
        public static String WHITE    = MCStyle + "f";

        public static String OBF      = MCStyle + "k";
        public static String BOLD     = MCStyle + "l";
        public static String STRIKE   = MCStyle + "m";
        public static String UNDER    = MCStyle + "n";
        public static String ITALIC   = MCStyle + "o";
        public static String RESET    = MCStyle + "r";

        public static String WailaStyle     = "\u00A4";
        public static String WailaIcon      = "\u00A5";
        public static String WailaRenderer  = "\u00A6";
        public static String TAB         = WailaStyle + WailaStyle +"a";
        public static String ALIGNRIGHT  = WailaStyle + WailaStyle +"b";
        public static String ALIGNCENTER = WailaStyle + WailaStyle +"c";
        public static String HEART       = WailaStyle + WailaIcon  +"a";
        public static String HHEART      = WailaStyle + WailaIcon  +"b";
        public static String EHEART      = WailaStyle + WailaIcon  +"c";
        public static String RENDER      = WailaStyle + WailaRenderer +"a";

    }
}
