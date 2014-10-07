package tterrag.wailaplugins.api;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public interface IPlugin extends IWailaDataProvider
{
    public void load(IWailaRegistrar registrar);
}
