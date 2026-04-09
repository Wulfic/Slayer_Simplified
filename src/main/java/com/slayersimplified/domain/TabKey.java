/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.domain;

import lombok.Getter;

/**
 * Keys identifying each tab in the task detail view.
 */
@Getter
public enum TabKey
{
    LOCATIONS("Locations"),
    INFO("Info"),
    WIKI("Wiki"),
    LOOT("Loot"),
    NOTES("Notes");

    private final String name;

    TabKey(String name)
    {
        this.name = name;
    }
}
