package tterrag.wailaplugins.api;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public interface IPlugin extends IWailaDataProvider {

    void load(IWailaRegistrar registrar);

    void postLoad();

}
