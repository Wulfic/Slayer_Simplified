/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.presentation.components;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Generic selectable list component with hover support.
 * Used for the task search results list.
 *
 * @param <T> the type of items displayed in the list
 */
public class SelectList<T> extends JList<T>
{
    public SelectList(ListCellRenderer<T> renderer, Consumer<T> onSelect)
    {
        ListSelectionListener onSelectListener = e ->
        {
            T selectedValue = getSelectedValue();
            if (e.getValueIsAdjusting() || selectedValue == null)
            {
                return;
            }
            onSelect.accept(selectedValue);
            clearSelection();
        };

        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        setCellRenderer(renderer);
        addListSelectionListener(onSelectListener);
    }

    public SelectList(ListCellRenderer<T> renderer, Consumer<T> onSelect, Consumer<Integer> onHoverHandler)
    {
        this(renderer, onSelect);

        MouseMotionListener onHoverListener = createOnHoverListener(onHoverHandler);
        addMouseMotionListener(onHoverListener);
    }

    public void shutDown()
    {
        Arrays.stream(getListSelectionListeners())
                .forEach(this::removeListSelectionListener);

        Arrays.stream(getMouseMotionListeners())
                .forEach(this::removeMouseMotionListener);

        setModel(new DefaultListModel<>());
    }

    public void update(T[] items)
    {
        SwingUtilities.invokeLater(() -> setListData(items));
    }

    private MouseMotionListener createOnHoverListener(Consumer<Integer> onHoverHandler)
    {
        return new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e) { }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                int index = locationToIndex(e.getPoint());
                onHoverHandler.accept(index);
                repaint();
            }
        };
    }
}
