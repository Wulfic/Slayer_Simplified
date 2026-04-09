/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.domain;

import java.util.Objects;

/**
 * Represents a link to the OSRS Wiki for a monster or variant.
 */
public class WikiLink
{
    public final String name;
    public final String url;

    public WikiLink(String name, String url)
    {
        this.name = Objects.requireNonNull(name, "wiki name cannot be null");
        this.url = Objects.requireNonNull(url, "wiki url cannot be null");
    }
}
