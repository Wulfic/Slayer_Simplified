/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.presentation.panels;

import com.slayersimplified.domain.Task;
import com.slayersimplified.presentation.SlayerTaskRenderer;
import com.slayersimplified.presentation.components.SearchBar;
import com.slayersimplified.presentation.components.SelectList;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel containing the search bar and task list.
 * Lets the user search for and select a Slayer monster.
 */
public class TaskSearchPanel extends JPanel
{
    private final SearchBar searchBar;
    private final SelectList<Task> selectList;
    private final SlayerTaskRenderer taskRenderer = new SlayerTaskRenderer();

    public TaskSearchPanel(Consumer<String> onSearch, Consumer<Task> onSelect)
    {
        searchBar = new SearchBar(onSearch);
        selectList = new SelectList<>(taskRenderer, onSelect, this::onTaskHover);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(searchBar, BorderLayout.NORTH);
        add(selectList, BorderLayout.CENTER);
    }

    public void shutDown()
    {
        searchBar.shutDown();
        selectList.shutDown();
    }

    public void updateTaskList(Task[] tasks)
    {
        selectList.update(tasks);
    }

    private void onTaskHover(int index)
    {
        taskRenderer.setHoverIndex(index);
        if (index != -1)
        {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else
        {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
