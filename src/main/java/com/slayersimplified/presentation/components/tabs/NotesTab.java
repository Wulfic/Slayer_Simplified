package com.slayersimplified.presentation.components.tabs;

import com.slayersimplified.domain.Tab;
import com.slayersimplified.presentation.components.ScrollBarStyling;
import com.slayersimplified.services.MonsterNotesService;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Tab that allows players to write and save notes for a specific monster.
 * Notes are auto-saved when the text changes and persisted via ConfigManager.
 */
@Slf4j
public class NotesTab extends JPanel implements Tab<String>
{
    private final MonsterNotesService notesService;
    private final JTextArea textArea;
    private String currentMonster;
    private boolean suppressSave = false;

    public NotesTab(MonsterNotesService notesService)
    {
        this.notesService = notesService;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Header with label
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

        JLabel headerLabel = new JLabel("Player Notes");
        headerLabel.setFont(FontManager.getRunescapeBoldFont());
        headerLabel.setForeground(ColorScheme.BRAND_ORANGE);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(FontManager.getRunescapeSmallFont());
        clearButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        clearButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        clearButton.setFocusPainted(false);
        clearButton.setBorder(new EmptyBorder(2, 8, 2, 8));
        clearButton.addActionListener(e -> clearNotes());
        headerPanel.add(clearButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Text area for notes
        textArea = new JTextArea();
        textArea.setBackground(ColorScheme.DARKER_GRAY_COLOR.brighter());
        textArea.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        textArea.setCaretColor(ColorScheme.LIGHT_GRAY_COLOR);
        textArea.setFont(FontManager.getRunescapeSmallFont());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Auto-save on text change
        textArea.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e) { saveNotes(); }

            @Override
            public void removeUpdate(DocumentEvent e) { saveNotes(); }

            @Override
            public void changedUpdate(DocumentEvent e) { saveNotes(); }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new EmptyBorder(0, 4, 4, 4));
        scrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        ScrollBarStyling.apply(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void update(String monsterName)
    {
        if (monsterName == null || monsterName.isEmpty())
        {
            return;
        }

        // Save any pending notes for previous monster before switching
        currentMonster = monsterName;

        // Load notes for the new monster without triggering save
        suppressSave = true;
        String notes = notesService.getNotes(monsterName);
        textArea.setText(notes);
        textArea.setCaretPosition(0);
        suppressSave = false;
    }

    @Override
    public void shutDown()
    {
        saveNotes();
        currentMonster = null;
        suppressSave = true;
        textArea.setText("");
        suppressSave = false;
    }

    private void saveNotes()
    {
        if (suppressSave || currentMonster == null)
        {
            return;
        }
        notesService.setNotes(currentMonster, textArea.getText());
    }

    private void clearNotes()
    {
        textArea.setText("");
        // saveNotes() will be triggered by the document listener
    }
}
