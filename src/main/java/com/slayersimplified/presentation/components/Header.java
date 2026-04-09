/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 */
package com.slayersimplified.presentation.components;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

/**
 * Header component showing the monster name and image at the top of the
 * task detail view.
 */
public class Header extends JLabel
{
    public Header()
    {
        setFont(this.getFont().deriveFont(Font.BOLD, 18f));
        setForeground(ColorScheme.BRAND_ORANGE);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.TOP);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setIconTextGap(10);
    }

    public void update(String title, ImageIcon icon)
    {
        setText(title);
        setIcon(icon);
    }
}
