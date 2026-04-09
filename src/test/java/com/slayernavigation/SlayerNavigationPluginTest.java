package com.slayernavigation;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Test launcher that boots RuneLite with the Slayer Navigation plugin loaded.
 * Run via: ./gradlew run
 */
public class SlayerNavigationPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SlayerNavigationPlugin.class);
		RuneLite.main(args);
	}
}
