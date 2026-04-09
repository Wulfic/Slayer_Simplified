/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified;

import com.slayersimplified.domain.SlayerMaster;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Plugin configuration for Slayer Simplified. Exposes settings in
 * RuneLite's plugin configuration panel.
 */
@ConfigGroup(SlayerSimplifiedConfig.CONFIG_GROUP)
public interface SlayerSimplifiedConfig extends Config
{
    String CONFIG_GROUP = "slayersimplified";

    @ConfigItem(
            keyName = "preferredMaster",
            name = "Preferred Slayer Master",
            description = "The slayer master to navigate to when you have no active task",
            position = 1
    )
    default SlayerMaster preferredMaster()
    {
        return SlayerMaster.DURADEL;
    }

    // Hidden config keys used to persist internal state across sessions

    @ConfigItem(
            keyName = "currentTaskName",
            name = "",
            description = "",
            hidden = true
    )
    default String currentTaskName()
    {
        return "";
    }

    @ConfigItem(
            keyName = "currentTaskName",
            name = "",
            description = ""
    )
    void setCurrentTaskName(String taskName);
}
