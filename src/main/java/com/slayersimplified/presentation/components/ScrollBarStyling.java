/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.presentation.components;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Utility to apply a narrow, dark-themed scrollbar to JScrollPane instances.
 */
public final class ScrollBarStyling
{
    private static final int SCROLLBAR_WIDTH = 8;
    private static final Color THUMB_COLOR = new Color(80, 80, 80);
    private static final Color THUMB_HOVER_COLOR = new Color(100, 100, 100);
    private static final Color TRACK_COLOR = ColorScheme.DARKER_GRAY_COLOR;

    private ScrollBarStyling() {}

    public static void apply(JScrollPane scrollPane)
    {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        verticalBar.setUI(new BasicScrollBarUI()
        {
            @Override
            protected void configureScrollBarColors()
            {
                this.thumbColor = THUMB_COLOR;
                this.thumbHighlightColor = THUMB_HOVER_COLOR;
                this.trackColor = TRACK_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation)
            {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation)
            {
                return createZeroButton();
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
            {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isThumbRollover() ? THUMB_HOVER_COLOR : THUMB_COLOR);
                g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                        thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
            {
                g.setColor(TRACK_COLOR);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            private JButton createZeroButton()
            {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
        });
    }
}
