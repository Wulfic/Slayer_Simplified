/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 *
 * MODIFIED from original: TaskTabs constructor now requires NavigationService
 * and LocationCoordinateService for the LocationsTab integration.
 */
package com.slayernavigation.presentation.panels;

import com.slayernavigation.domain.Task;
import com.slayernavigation.presentation.components.Header;
import com.slayernavigation.presentation.components.TaskTabs;
import com.slayernavigation.services.FavoriteLocationService;
import com.slayernavigation.services.LocationCoordinateService;
import com.slayernavigation.services.NavigationService;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel showing the selected task's details with a header (name + image),
 * tabbed info sections, and a close button.
 */
public class TaskSelectedPanel extends JPanel
{
    private final Header header = new Header();
    private final TaskTabs taskTabs;
    private final JButton closeButton = new JButton("Close");

    private final ActionListener onClickListener;

    /**
     * @param onClose                   callback to invoke when the close button is clicked
     * @param navigationService         navigation service for path requests
     * @param locationCoordinateService coordinate lookup service
     * @param favoriteService           favorite location persistence service
     */
    public TaskSelectedPanel(
            Runnable onClose,
            NavigationService navigationService,
            LocationCoordinateService locationCoordinateService,
            FavoriteLocationService favoriteService)
    {
        // Pass all services down to TaskTabs → LocationsTab
        this.taskTabs = new TaskTabs(navigationService, locationCoordinateService, favoriteService);
        this.onClickListener = e -> onClose.run();
        closeButton.addActionListener(this.onClickListener);
        closeButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        closeButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        closeButton.setFont(FontManager.getRunescapeSmallFont());
        closeButton.setFocusPainted(false);

        setLayout(new BorderLayout(0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(5, 0, 0, 0));

        add(header, BorderLayout.NORTH);
        add(taskTabs, BorderLayout.CENTER);
        add(closeButton, BorderLayout.SOUTH);
    }

    public void shutDown()
    {
        taskTabs.shutDown();
        closeButton.removeActionListener(onClickListener);
    }

    public void update(Task task)
    {
        header.update(task.name, new ImageIcon(task.image));
        SwingUtilities.invokeLater(() -> taskTabs.update(task));
    }

    /**
     * Programmatically switch to the Locations tab (used by Quick Navigate).
     */
    public void selectLocationsTab()
    {
        taskTabs.selectLocationsTab();
    }
}
