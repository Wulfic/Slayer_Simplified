/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 */
package com.slayernavigation.domain;

/**
 * Generic interface for tab components that display typed data.
 * Each tab can update its contents and clean up resources on shutdown.
 *
 * @param <T> the type of data this tab displays
 */
public interface Tab<T>
{
    void update(T data);
    void shutDown();
}
