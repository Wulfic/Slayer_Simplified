/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 *
 * MODIFIED from original: LOCATIONS tab now uses LocationsTab (with navigate
 * buttons) instead of the plain TextTab.
 */
package com.slayernavigation.presentation.components;

import com.slayernavigation.domain.Icon;
import com.slayernavigation.domain.Tab;
import com.slayernavigation.domain.TabKey;
import com.slayernavigation.domain.Task;
import com.slayernavigation.presentation.components.tabs.LocationsTab;
import com.slayernavigation.presentation.components.tabs.TableTab;
import com.slayernavigation.presentation.components.tabs.TextTab;
import com.slayernavigation.presentation.components.tabs.WikiTab;
import com.slayernavigation.services.FavoriteLocationService;
import com.slayernavigation.services.LocationCoordinateService;
import com.slayernavigation.services.NavigationService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.laf.RuneLiteTabbedPaneUI;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabbed pane containing the task detail tabs.
 * The Locations tab uses a NavigationService-aware LocationsTab with
 * favorite star toggles and "Nav" buttons.
 */
@Slf4j
public class TaskTabs extends JTabbedPane
{
    private final Map<TabKey, Tab<?>> tabMap = new HashMap<>();
    private final LocationsTab locationsTab;

    /**
     * @param navigationService         service for sending path requests to Shortest Path
     * @param locationCoordinateService service for resolving location names to WorldPoints
     * @param favoriteService           service for persisting favorite locations
     */
    public TaskTabs(
            NavigationService navigationService,
            LocationCoordinateService locationCoordinateService,
            FavoriteLocationService favoriteService)
    {
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setUI(new RuneLiteTabbedPaneUI()
        {
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics)
            {
                return getWidth() / getTabCount();
            }
        });

        TabKey locations = TabKey.LOCATIONS;
        TabKey items = TabKey.ITEMS_REQUIRED;
        TabKey combat = TabKey.COMBAT;
        TabKey masters = TabKey.MASTERS;
        TabKey wiki = TabKey.WIKI;

        locationsTab = new LocationsTab(navigationService, locationCoordinateService, favoriteService);

        setTab(locations, Icon.COMPASS.getIcon(), locationsTab, locations.getName());
        setTab(items, Icon.INVENTORY.getIcon(), new TextTab(), items.getName());
        setTab(combat, Icon.COMBAT.getIcon(), new TableTab(new String[]{"Attack Styles", "Attributes"}), combat.getName());
        setTab(masters, Icon.SLAYER_SKILL.getIcon(), new TextTab(), masters.getName());
        setTab(wiki, Icon.WIKI.getIcon(), new WikiTab(), wiki.getName());
    }

    public void shutDown()
    {
        tabMap.values().forEach(Tab::shutDown);
    }

    /**
     * Programmatically switch to the Locations tab.
     */
    public void selectLocationsTab()
    {
        int index = indexOfComponent((Component) tabMap.get(TabKey.LOCATIONS));
        if (index >= 0)
        {
            setSelectedIndex(index);
        }
    }

    public void update(Task task)
    {
        // Set the current monster name on LocationsTab before updating
        locationsTab.setCurrentMonster(task.name);

        updateTab(TabKey.LOCATIONS, task.locations);
        updateTab(TabKey.ITEMS_REQUIRED, task.itemsRequired);
        updateTab(TabKey.COMBAT, new Object[][]{task.attackStyles, task.attributes});
        updateTab(TabKey.MASTERS, task.masters);
        updateTab(TabKey.WIKI, task.wikiLinks);
    }

    private <T> void updateTab(TabKey key, T data)
    {
        Tab<?> rawTab = tabMap.get(key);

        if (rawTab == null)
        {
            log.error("No tab found with key {}", key.toString());
            return;
        }

        @SuppressWarnings("unchecked")
        Tab<T> tab = (Tab<T>) rawTab;
        tab.update(data);
    }

    private void setTab(TabKey key, ImageIcon icon, Tab<?> tab, String tip)
    {
        tabMap.put(key, tab);
        addTab(null, icon, (Component) tab, tip);
    }
}
