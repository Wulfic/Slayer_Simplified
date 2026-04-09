/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 *
 * Tracks the player's current slayer task by parsing game chat messages
 * and reading the task counter varbit. The plugin class subscribes to
 * ChatMessage events and delegates to this tracker.
 */
package com.slayernavigation.services;

import com.slayernavigation.SlayerNavigationConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.VarPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the player's current Slayer task. Uses VarPlayer to check
 * if a task is active, and parses chat messages to identify the
 * creature name. The task name is persisted in config so it survives
 * client restarts.
 */
@Slf4j
@Singleton
public class SlayerTaskTracker
{
    // Common chat message patterns for slayer task assignment
    private static final Pattern NEW_TASK_PATTERN =
            Pattern.compile("(?:Your new task is to kill|You are to bring balance to) (\\d+) (.+)\\.");
    private static final Pattern CURRENT_TASK_PATTERN =
            Pattern.compile("You're (?:still hunting|assigned to kill) (.+?); (?:you have|only) (\\d+)(?: more)? to go\\.");
    private static final Pattern TASK_COMPLETE_PATTERN =
            Pattern.compile("You have completed your task!");
    private static final Pattern NO_TASK_PATTERN =
            Pattern.compile("You need something new to hunt\\.");

    private final Client client;
    private final SlayerNavigationConfig config;

    @Inject
    public SlayerTaskTracker(Client client, SlayerNavigationConfig config)
    {
        this.client = client;
        this.config = config;
    }

    /**
     * Check if the player currently has an active slayer task by reading
     * the task remaining count from game state.
     *
     * @return true if the player has a task with remaining kills > 0
     */
    public boolean hasTask()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return false;
        }
        return client.getVarpValue(VarPlayer.SLAYER_TASK_SIZE) > 0;
    }

    /**
     * Get the current task creature name as parsed from chat messages.
     * May return null if no task has been detected this session and
     * none was persisted from a previous session.
     */
    public String getCurrentTaskName()
    {
        String name = config.currentTaskName();
        return (name == null || name.isEmpty()) ? null : name;
    }

    /**
     * Called by the plugin's ChatMessage subscriber. Parses the message
     * to detect task assignments, completions, and status checks.
     *
     * @param message the stripped (tag-free) chat message text
     */
    public void parseChatMessage(String message)
    {
        // New task assignment: "Your new task is to kill 130 abyssal demons."
        Matcher newTask = NEW_TASK_PATTERN.matcher(message);
        if (newTask.find())
        {
            String creatureName = normalizeCreatureName(newTask.group(2));
            config.setCurrentTaskName(creatureName);
            log.debug("Detected new slayer task: {}", creatureName);
            return;
        }

        // Current task check: "You're still hunting abyssal demons; you have 45 to go."
        Matcher currentTask = CURRENT_TASK_PATTERN.matcher(message);
        if (currentTask.find())
        {
            String creatureName = normalizeCreatureName(currentTask.group(1));
            config.setCurrentTaskName(creatureName);
            log.debug("Confirmed current slayer task: {}", creatureName);
            return;
        }

        // Task complete
        if (TASK_COMPLETE_PATTERN.matcher(message).find())
        {
            config.setCurrentTaskName("");
            log.debug("Slayer task completed");
            return;
        }

        // No task
        if (NO_TASK_PATTERN.matcher(message).find())
        {
            config.setCurrentTaskName("");
            log.debug("No slayer task active");
        }
    }

    /**
     * Normalize a creature name from a chat message to match our tasks.json keys.
     * Chat messages use lowercase plural names like "abyssal demons".
     * Our tasks.json uses title case singular like "Abyssal demon".
     */
    private String normalizeCreatureName(String raw)
    {
        String name = raw.trim();

        // Remove trailing 's' for simple plurals (but not "ss" like "boss")
        if (name.endsWith("s") && !name.endsWith("ss") && !name.endsWith("us"))
        {
            name = name.substring(0, name.length() - 1);
        }

        // Title-case the first character
        if (!name.isEmpty())
        {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        return name;
    }
}
