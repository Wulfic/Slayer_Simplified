/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.presentation.components.tabs;

import com.slayersimplified.domain.Tab;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Two-column table tab used for the Combat tab (Attack Styles vs Attributes).
 */
public class TableTab extends JScrollPane implements Tab<Object[][]>
{
    private final JTable table = new JTable();

    public TableTab(String[] columnNames)
    {
        setTableRenderer(columnNames);
        setHeaderRenderer();

        table.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        table.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        table.setGridColor(ColorScheme.DARK_GRAY_COLOR);
        table.setFont(FontManager.getRunescapeSmallFont());
        table.getTableHeader().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        table.getTableHeader().setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        table.setRowHeight(25);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);

        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(null);
        setFocusable(false);
        setPreferredSize(new Dimension(table.getWidth(), 150));
        setViewportView(table);
    }

    @Override
    public void update(Object[][] data)
    {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        int maxRowCount = Math.max(data[0].length, data[1].length);
        for (int i = 0; i < maxRowCount; i++)
        {
            String attackStyle = i < data[0].length ? data[0][i].toString() : "";
            String attribute = i < data[1].length ? data[1][i].toString() : "";
            model.addRow(new Object[]{attackStyle, attribute});
        }
    }

    @Override
    public void shutDown()
    {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }

    private void setTableRenderer(String[] columnNames)
    {
        table.setModel(new DefaultTableModel(columnNames, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        });
    }

    private void setHeaderRenderer()
    {
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Border border = BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
                        BorderFactory.createEmptyBorder(8, 3, 5, 3)
                );
                c.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                c.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                ((JComponent) c).setBorder(border);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
        {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }
}
