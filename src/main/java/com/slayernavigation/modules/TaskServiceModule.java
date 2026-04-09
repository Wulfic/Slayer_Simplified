/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 */
package com.slayernavigation.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.slayernavigation.services.TaskService;
import com.slayernavigation.services.TaskServiceImpl;

/**
 * Guice module that configures bindings for the task data service
 * and provides named constants (resource paths, URLs) used at injection time.
 */
public class TaskServiceModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(TaskService.class).to(TaskServiceImpl.class);
    }

    @Provides
    @Named("dataPath")
    String provideJsonDataPath()
    {
        return "/data/tasks.json";
    }

    @Provides
    @Named("baseWikiUrl")
    String provideBaseWikiUrl()
    {
        return "https://oldschool.runescape.wiki/w/";
    }

    @Provides
    @Named("baseImagesPath")
    String provideBaseImagesPath()
    {
        return "/images/monsters/";
    }

    /** Path to the location coordinates JSON for the NavigationService. */
    @Provides
    @Named("locationDataPath")
    String provideLocationDataPath()
    {
        return "/data/location_coordinates.json";
    }
}
