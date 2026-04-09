/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Navigation contributors
 * See LICENSE for details.
 */
package com.slayernavigation.presentation.components.tabs;

import com.slayernavigation.domain.Tab;
import com.slayernavigation.domain.WikiLink;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab that shows clickable buttons linking to the OSRS Wiki pages
 * for the monster and its variants.
 */
@Slf4j
public class WikiTab extends JPanel implements Tab<WikiLink[]>
{
    private final List<JButton> buttons = new ArrayList<>();

    public WikiTab()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
    }

    @Override
    public void update(WikiLink[] wikiLinks)
    {
        removeExistingButtons();

        add(Box.createVerticalStrut(5));

        for (WikiLink wikiLink : wikiLinks)
        {
            JButton button = new JButton(wikiLink.name);
            button.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            button.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            button.setFont(FontManager.getRunescapeSmallFont());
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getMaximumSize().height));
            button.addActionListener(e -> LinkBrowser.browse(wikiLink.url));
            buttons.add(button);

            add(Box.createVerticalStrut(5));
            add(button);
            add(Box.createVerticalStrut(5));
        }

        add(Box.createVerticalStrut(5));
    }

    @Override
    public void shutDown()
    {
        removeExistingButtons();
    }

    private void removeExistingButtons()
    {
        for (JButton button : buttons)
        {
            Arrays.stream(button.getActionListeners())
                    .forEach(button::removeActionListener);
        }
        buttons.clear();
        removeAll();
    }
}
