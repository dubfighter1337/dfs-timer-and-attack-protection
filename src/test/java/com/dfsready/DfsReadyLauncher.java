package com.dfsready;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class DfsReadyLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(DfsReadyPlugin.class);
        RuneLite.main(args);
    }
}
