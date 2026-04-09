/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 */
package com.slayernavigation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

/**
 * Enum of all Slayer masters with their overworld WorldPoint coordinates.
 * Used for "navigate to master" when the player has no active task.
 */
@Getter
@RequiredArgsConstructor
public enum SlayerMaster
{
    TURAEL("Turael", new WorldPoint(2931, 3536, 0)),
    MAZCHNA("Mazchna", new WorldPoint(3510, 3507, 0)),
    VANNAKA("Vannaka", new WorldPoint(3145, 9914, 0)),
    CHAELDAR("Chaeldar", new WorldPoint(2445, 4431, 0)),
    KONAR("Konar quo Maten", new WorldPoint(1308, 3786, 0)),
    NIEVE("Nieve / Steve", new WorldPoint(2432, 3424, 0)),
    DURADEL("Duradel", new WorldPoint(2869, 2982, 0)),
    KRYSTILIA("Krystilia", new WorldPoint(3109, 3514, 0));

    private final String displayName;
    private final WorldPoint worldPoint;

    @Override
    public String toString()
    {
        return displayName;
    }
}
