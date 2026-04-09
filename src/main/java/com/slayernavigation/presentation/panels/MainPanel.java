/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 *
 * Main plugin panel. Adds a "Quick Navigate" button that auto-routes
 * based on the player's current slayer task state.
 */
package com.slayernavigation.presentation.panels;

import com.slayernavigation.SlayerNavigationConfig;
import com.slayernavigation.domain.Panel;
import com.slayernavigation.domain.SlayerMaster;
import com.slayernavigation.domain.Task;
import com.slayernavigation.services.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Main plugin panel showing either the task search list or the selected
 * task detail view. Contains a "Quick Navigate" button that:
 *   - No task → navigates to the configured preferred slayer master
 *   - Has task + favorite location → auto-navigates to that location
 *   - Has task + no favorite → opens the monster's locations tab
 */
@Slf4j
public class MainPanel extends PluginPanel
{
    private final TaskService taskService;
    private final NavigationService navigationService;
    private final LocationCoordinateService locationCoordinateService;
    private final SlayerTaskTracker taskTracker;
    private final FavoriteLocationService favoriteService;
    private final SlayerNavigationConfig config;

    private final TaskSearchPanel taskSearchPanel;
    private final TaskSelectedPanel taskSelectedPanel;

    private final Map<Panel, JPanel> panels = new HashMap<>();
    private final JPanel currentPanelContainer = new JPanel(new BorderLayout());
    private final JButton quickNavButton = new JButton("Quick Navigate");

    @Inject
    public MainPanel(
            TaskService taskService,
            NavigationService navigationService,
            LocationCoordinateService locationCoordinateService,
            SlayerTaskTracker taskTracker,
            FavoriteLocationService favoriteService,
            SlayerNavigationConfig config)
    {
        this.taskService = taskService;
        this.navigationService = navigationService;
        this.locationCoordinateService = locationCoordinateService;
        this.taskTracker = taskTracker;
        this.favoriteService = favoriteService;
        this.config = config;

        this.taskSearchPanel = new TaskSearchPanel(this::onSearchBarChanged, this::onTaskSelected);
        this.taskSelectedPanel = new TaskSelectedPanel(
                this::onTaskClosed, navigationService, locationCoordinateService, favoriteService);

        Task[] orderedTasks = taskService.getAll(Comparator.comparing(t -> t.name));
        taskSearchPanel.updateTaskList(orderedTasks);

        // Quick Navigate button styling — match RuneLite panel aesthetic
        quickNavButton.setFont(FontManager.getRunescapeSmallFont());
        quickNavButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        quickNavButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        quickNavButton.setFocusPainted(false);
        quickNavButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        quickNavButton.setToolTipText("Navigate based on current slayer task");
        quickNavButton.addActionListener(e -> onQuickNavigate());

        setLayout(new BorderLayout(0, 0));

        panels.put(Panel.TASK_SEARCH, taskSearchPanel);
        panels.put(Panel.TASK_SELECTED, taskSelectedPanel);

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        currentPanelContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(quickNavButton, BorderLayout.NORTH);
        add(currentPanelContainer, BorderLayout.CENTER);
        showPanel(Panel.TASK_SEARCH);
    }

    public void shutDown()
    {
        navigationService.clearNavigation();
        taskSearchPanel.shutDown();
        taskSelectedPanel.shutDown();
    }

    /**
     * Smart navigation based on current slayer task state:
     * 1. No task → navigate to preferred slayer master
     * 2. Has task + favorite set → navigate to favorite location
     * 3. Has task + no favorite → open that monster's locations tab
     */
    private void onQuickNavigate()
    {
        if (!taskTracker.hasTask())
        {
            // No active task — navigate to preferred slayer master
            SlayerMaster master = config.preferredMaster();
            navigationService.navigateTo(master.getWorldPoint());
            log.info("Quick Nav: no task, navigating to {}", master.getDisplayName());
            return;
        }

        String taskName = taskTracker.getCurrentTaskName();
        if (taskName == null)
        {
            log.info("Quick Nav: task detected but creature unknown. Check your slayer gem or talk to your master.");
            return;
        }

        // Find the matching task in our data
        Task task = taskService.get(taskName);
        if (task == null)
        {
            // Try partial match as fallback
            Task[] matches = taskService.searchPartialName(taskName);
            if (matches.length > 0)
            {
                task = matches[0];
            }
        }

        if (task == null)
        {
            log.warn("Quick Nav: could not find task matching '{}'", taskName);
            return;
        }

        // Check for a favorite location
        String favLocation = favoriteService.getFavorite(task.name);
        if (favLocation != null)
        {
            WorldPoint coords = locationCoordinateService.getCoordinates(favLocation);
            if (coords != null)
            {
                navigationService.navigateTo(coords);
                log.info("Quick Nav: navigating to favorite '{}' for {}", favLocation, task.name);
                return;
            }
        }

        // No favorite — open the monster detail with Locations tab selected
        taskSelectedPanel.update(task);
        showPanel(Panel.TASK_SELECTED);
        SwingUtilities.invokeLater(() -> taskSelectedPanel.selectLocationsTab());
        log.info("Quick Nav: showing locations for {} (no favorite set)", task.name);
    }

    private void onSearchBarChanged(String searchTerm)
    {
        Task[] matchedTasks = searchTerm.isBlank()
                ? taskService.getAll(Comparator.comparing(t -> t.name))
                : taskService.searchPartialName(searchTerm.trim());

        taskSearchPanel.updateTaskList(matchedTasks);
    }

    private void onTaskSelected(Task task)
    {
        taskSelectedPanel.update(task);
        showPanel(Panel.TASK_SELECTED);
    }

    private void onTaskClosed()
    {
        navigationService.clearNavigation();
        showPanel(Panel.TASK_SEARCH);
    }

    private void showPanel(Panel panel)
    {
        SwingUtilities.invokeLater(() ->
        {
            currentPanelContainer.removeAll();
            currentPanelContainer.add(panels.get(panel), BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }
}
