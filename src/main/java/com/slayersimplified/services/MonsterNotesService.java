/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.services;

import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Stores and retrieves player notes for each Slayer monster.
 * Notes are persisted via RuneLite's ConfigManager so they survive
 * client restarts.
 */
@Singleton
public class MonsterNotesService
{
    private static final String CONFIG_GROUP = "slayersimplified";
    private static final String NOTES_PREFIX = "notes_";

    private final ConfigManager configManager;

    @Inject
    public MonsterNotesService(ConfigManager configManager)
    {
        this.configManager = configManager;
    }

    /**
     * Get the notes for a monster.
     *
     * @param monsterName the monster name (e.g. "Abyssal demon")
     * @return the notes text, or empty string if none saved
     */
    public String getNotes(String monsterName)
    {
        String notes = configManager.getConfiguration(CONFIG_GROUP, NOTES_PREFIX + normalize(monsterName));
        return notes != null ? notes : "";
    }

    /**
     * Save notes for a monster.
     */
    public void setNotes(String monsterName, String notes)
    {
        if (notes == null || notes.trim().isEmpty())
        {
            configManager.unsetConfiguration(CONFIG_GROUP, NOTES_PREFIX + normalize(monsterName));
        }
        else
        {
            configManager.setConfiguration(CONFIG_GROUP, NOTES_PREFIX + normalize(monsterName), notes);
        }
    }

    private String normalize(String name)
    {
        return name.toLowerCase().replace(' ', '_').replace("'", "");
    }
}
