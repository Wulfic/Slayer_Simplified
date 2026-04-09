package com.slayersimplified;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Test launcher that boots RuneLite with the Slayer Simplified plugin loaded.
 * Run via: ./gradlew run
 */
public class SlayerSimplifiedPluginTest
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SlayerSimplifiedPlugin.class);
		RuneLite.main(args);
	}
}
