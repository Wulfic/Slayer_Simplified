/*
 * BSD 2-Clause License
 * Copyright (c) 2022, Lee (original Slayer Assistant plugin)
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 *
 * Combined Info tab — merges Items Required, Combat, and Masters
 * into a single scrollable panel with distinct sections.
 */
package com.slayersimplified.presentation.components.tabs;

import com.slayersimplified.domain.Tab;
import com.slayersimplified.loot.CombatStats;
import com.slayersimplified.loot.WikiScraper;
import com.slayersimplified.presentation.components.ScrollBarStyling;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Scrollable tab combining Items Required, Wiki Combat Stats, and Slayer Masters
 * into one panel with labelled sections. Combat stats are fetched asynchronously
 * from the OSRS Wiki.
 */
@Slf4j
public class InfoTab extends JScrollPane implements Tab<InfoTab.InfoData>
{
    private final JPanel contentPanel = new JPanel();
    private final OkHttpClient okHttpClient;

    /** Placeholder panel that gets replaced when wiki data arrives. */
    private final JPanel wikiCombatPanel = new JPanel();

    private String currentMonster;

    private static final Color SECTION_HEADER_BG = ColorScheme.DARKER_GRAY_COLOR.darker();
    private static final Color STAT_VALUE_COLOR = Color.WHITE;

    public InfoTab(OkHttpClient okHttpClient)
    {
        this.okHttpClient = okHttpClient;

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        wikiCombatPanel.setLayout(new BoxLayout(wikiCombatPanel, BoxLayout.Y_AXIS));
        wikiCombatPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        setViewportView(contentPanel);
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(16);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        ScrollBarStyling.apply(this);
    }

    @Override
    public void update(InfoData data)
    {
        contentPanel.removeAll();

        // -- Items Required section --
        addSectionHeader("Items Required");
        if (data.items == null || data.items.length == 0)
        {
            addTextRow("None");
        }
        else
        {
            for (String item : data.items)
            {
                addTextRow(StringUtils.capitalize(item));
            }
        }

        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // -- Combat section (from task data) --
        addSectionHeader("Combat");
        if (data.combat != null && data.combat.length == 2)
        {
            Object[] attackStyles = data.combat[0];
            Object[] attributes = data.combat[1];

            if (attackStyles.length > 0)
            {
                addSubHeader("Attack Styles");
                for (Object style : attackStyles)
                {
                    addTextRow(style.toString());
                }
            }

            if (attributes.length > 0)
            {
                addSubHeader("Attributes");
                for (Object attr : attributes)
                {
                    addTextRow(attr.toString());
                }
            }

            if (attackStyles.length == 0 && attributes.length == 0)
            {
                addTextRow("None");
            }
        }
        else
        {
            addTextRow("None");
        }

        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // -- Wiki Combat Stats placeholder --
        wikiCombatPanel.removeAll();
        addSectionHeaderTo(wikiCombatPanel, "Wiki Combat Stats");
        addTextRowTo(wikiCombatPanel, "Loading...");
        contentPanel.add(wikiCombatPanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // -- Masters section --
        addSectionHeader("Slayer Masters");
        if (data.masters == null || data.masters.length == 0)
        {
            addTextRow("None");
        }
        else
        {
            for (String master : data.masters)
            {
                addTextRow(StringUtils.capitalize(master));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();

        // Scroll to top
        SwingUtilities.invokeLater(() -> getVerticalScrollBar().setValue(0));

        // Fetch wiki combat stats asynchronously (cached after first lookup)
        if (data.monsterName != null && !data.monsterName.isEmpty())
        {
            currentMonster = data.monsterName;
            fetchCombatStats(data.monsterName);
        }
    }

    private void fetchCombatStats(String monsterName)
    {
        WikiScraper.getCombatStats(okHttpClient, monsterName)
                .whenCompleteAsync((stats, ex) ->
                        SwingUtilities.invokeLater(() -> populateWikiCombatStats(stats)));
    }

    private void populateWikiCombatStats(CombatStats stats)
    {
        wikiCombatPanel.removeAll();
        addSectionHeaderTo(wikiCombatPanel, "Wiki Combat Stats");

        if (stats == null || stats.getCombatLevel().isEmpty())
        {
            addTextRowTo(wikiCombatPanel, "No data found.");
            wikiCombatPanel.revalidate();
            wikiCombatPanel.repaint();
            contentPanel.revalidate();
            revalidate();
            return;
        }

        // Core stats
        addStatRow(wikiCombatPanel, "Combat Level", stats.getCombatLevel());
        addStatRow(wikiCombatPanel, "Hitpoints", stats.getHitpoints());
        addStatRow(wikiCombatPanel, "Max Hit", stats.getMaxHit());
        addStatRow(wikiCombatPanel, "Attack Style", stats.getAttackStyle());

        if (!stats.getAttribute().isEmpty())
        {
            addStatRow(wikiCombatPanel, "Attribute", stats.getAttribute());
        }

        // Weakness
        if (!stats.getElementalWeakness().isEmpty())
        {
            String weakness = stats.getElementalWeakness();
            if (!stats.getElementalWeaknessPercent().isEmpty())
            {
                weakness += " (" + stats.getElementalWeaknessPercent() + ")";
            }
            addStatRow(wikiCombatPanel, "Weakness", weakness);
        }

        // Immunities
        addSubHeaderTo(wikiCombatPanel, "Immunities");
        addImmunityRow(wikiCombatPanel, "Poison", stats.getImmunePoison());
        addImmunityRow(wikiCombatPanel, "Venom", stats.getImmuneVenom());
        addImmunityRow(wikiCombatPanel, "Cannons", stats.getImmuneCannon());
        addImmunityRow(wikiCombatPanel, "Thralls", stats.getImmuneThrall());
        addImmunityRow(wikiCombatPanel, "Burn", stats.getImmuneBurn());

        wikiCombatPanel.revalidate();
        wikiCombatPanel.repaint();
        contentPanel.revalidate();
        revalidate();
    }

    @Override
    public void shutDown()
    {
        contentPanel.removeAll();
        currentMonster = null;
    }

    // ---- Helpers that add to contentPanel ----

    private void addSectionHeader(String text)
    {
        addSectionHeaderTo(contentPanel, text);
    }

    private void addSubHeader(String text)
    {
        addSubHeaderTo(contentPanel, text);
    }

    private void addTextRow(String text)
    {
        addTextRowTo(contentPanel, text);
    }

    // ---- Helpers that add to an arbitrary panel ----

    private void addSectionHeaderTo(JPanel target, String text)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SECTION_HEADER_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        panel.setBorder(new EmptyBorder(4, 8, 4, 4));

        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeBoldFont());
        label.setForeground(ColorScheme.BRAND_ORANGE);
        panel.add(label, BorderLayout.WEST);

        target.add(panel);
    }

    private void addSubHeaderTo(JPanel target, String text)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        panel.setBorder(new EmptyBorder(3, 12, 2, 4));

        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeBoldFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        panel.add(label, BorderLayout.WEST);

        target.add(panel);
    }

    private void addTextRowTo(JPanel target, String text)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setBorder(new EmptyBorder(2, 16, 2, 4));

        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        row.add(label, BorderLayout.WEST);

        target.add(row);
    }

    private void addStatRow(JPanel target, String label, String value)
    {
        if (value == null || value.isEmpty())
        {
            return;
        }

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setBorder(new EmptyBorder(2, 16, 2, 8));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(FontManager.getRunescapeSmallFont());
        nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        row.add(nameLabel, BorderLayout.WEST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FontManager.getRunescapeBoldFont());
        valueLabel.setForeground(STAT_VALUE_COLOR);
        row.add(valueLabel, BorderLayout.EAST);

        target.add(row);
    }

    private void addImmunityRow(JPanel target, String label, String value)
    {
        if (value == null || value.isEmpty())
        {
            return;
        }

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setBorder(new EmptyBorder(2, 20, 2, 8));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(FontManager.getRunescapeSmallFont());
        nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        row.add(nameLabel, BorderLayout.WEST);

        // Color-code: green for immune, red-ish for not immune
        boolean isImmune = value.toLowerCase().startsWith("immune");
        boolean isNotImmune = value.toLowerCase().startsWith("not immune");
        Color valueColor = isImmune ? new Color(220, 60, 60)
                : isNotImmune ? new Color(100, 200, 100) : ColorScheme.LIGHT_GRAY_COLOR;

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FontManager.getRunescapeSmallFont());
        valueLabel.setForeground(valueColor);
        row.add(valueLabel, BorderLayout.EAST);

        target.add(row);
    }

    /**
     * Data holder for the combined info tab.
     */
    public static class InfoData
    {
        public final String monsterName;
        public final String[] items;
        public final Object[][] combat;
        public final String[] masters;

        public InfoData(String monsterName, String[] items, Object[][] combat, String[] masters)
        {
            this.monsterName = monsterName;
            this.items = items;
            this.combat = combat;
            this.masters = masters;
        }
    }
}
