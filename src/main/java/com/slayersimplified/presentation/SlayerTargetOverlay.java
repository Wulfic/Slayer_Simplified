package com.slayersimplified.presentation;

import com.slayersimplified.SlayerSimplifiedConfig;
import com.slayersimplified.domain.Task;
import com.slayersimplified.services.SlayerTaskTracker;
import com.slayersimplified.services.TaskService;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class SlayerTargetOverlay extends Overlay
{
    private final Client client;
    private final SlayerSimplifiedConfig config;
    private final SlayerTaskTracker taskTracker;
    private final TaskService taskService;
    private final ModelOutlineRenderer modelOutlineRenderer;

    private final Set<String> targetNames = new HashSet<>();
    private String lastTaskName;

    @Inject
    public SlayerTargetOverlay(
            Client client,
            SlayerSimplifiedConfig config,
            SlayerTaskTracker taskTracker,
            TaskService taskService,
            ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.config = config;
        this.taskTracker = taskTracker;
        this.taskService = taskService;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.highlightTarget())
        {
            return null;
        }

        String currentTask = taskTracker.getCurrentTaskName();
        if (currentTask == null || currentTask.isEmpty())
        {
            targetNames.clear();
            lastTaskName = null;
            return null;
        }

        // Rebuild target name set when task changes
        if (!currentTask.equals(lastTaskName))
        {
            lastTaskName = currentTask;
            rebuildTargetNames(currentTask);
        }

        if (targetNames.isEmpty())
        {
            return null;
        }

        Color color = config.highlightColor();

        for (NPC npc : client.getNpcs())
        {
            if (npc.getName() != null && targetNames.contains(npc.getName().toLowerCase()))
            {
                modelOutlineRenderer.drawOutline(npc, 2, color, 4);
            }
        }

        return null;
    }

    private void rebuildTargetNames(String taskName)
    {
        targetNames.clear();

        // Add the task name itself
        targetNames.add(taskName.toLowerCase());

        // Look up the task to get variants
        Task task = taskService.get(taskName);
        if (task != null)
        {
            if (task.variants != null)
            {
                for (String variant : task.variants)
                {
                    targetNames.add(variant.toLowerCase());
                }
            }
        }
    }
}
