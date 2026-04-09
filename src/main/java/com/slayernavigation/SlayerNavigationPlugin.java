/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 *
 * Main plugin entry point for Slayer Navigation. Extends the Slayer Assistant
 * plugin with navigate-to-location functionality via the Shortest Path plugin's
 * PluginMessage API. Tracks the current slayer task via chat message parsing
 * and provides a Quick Navigate feature.
 */
package com.slayernavigation;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.slayernavigation.domain.Icon;
import com.slayernavigation.modules.TaskServiceModule;
import com.slayernavigation.presentation.panels.MainPanel;
import com.slayernavigation.services.SlayerTaskTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Slayer Navigation",
        description = "Slayer task assistant with Shortest Path navigation integration",
        tags = {"slay", "slayer", "navigation", "path", "shortest"}
)
public class SlayerNavigationPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private MainPanel mainPanel;

    @Inject
    private SlayerTaskTracker taskTracker;

    private NavigationButton navButton;

    @Override
    public void configure(Binder binder)
    {
        binder.install(new TaskServiceModule());
    }

    @Provides
    SlayerNavigationConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(SlayerNavigationConfig.class);
    }

    @Override
    protected void startUp()
    {
        navButton = NavigationButton.builder()
                .tooltip("Slayer Navigation")
                .icon(Icon.SLAYER_SKILL.getImage())
                .priority(10)
                .panel(mainPanel)
                .build();

        clientToolbar.addNavigation(navButton);
        log.info("Slayer Navigation started");
    }

    @Override
    protected void shutDown()
    {
        clientToolbar.removeNavigation(navButton);
        mainPanel.shutDown();
        log.info("Slayer Navigation stopped");
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE
                && event.getType() != ChatMessageType.SPAM)
        {
            return;
        }

        String message = Text.removeTags(event.getMessage());
        taskTracker.parseChatMessage(message);
    }
}
