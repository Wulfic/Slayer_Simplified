# Slayer Navigation Plugin — Refined Implementation Plan

## Executive Summary

**Goal:** Fork the Slayer Assistant plugin, add a "Navigate" button next to each location in the Locations tab, and use the Shortest Path plugin's official cross-plugin PluginMessage API to draw a path — **no codebase merging required**.

**Key Discovery:** Shortest Path exposes a public `PluginMessage` API for inter-plugin communication. Any plugin can send an `EventBus` message with a `WorldPoint` target and Shortest Path will draw the path. This means our plugin is a **standalone fork of Slayer Assistant** with minimal additions.

---

## 1. Licensing & Policy Compliance

### Licenses — CLEAR TO PROCEED
| Project | License | Allows Fork/Modify/Redistribute? |
|---|---|---|
| Slayer Assistant (LeeOkeefe) | BSD 2-Clause | **Yes** — must retain copyright notice |
| Shortest Path (Skretzo) | BSD 2-Clause | **Yes** — we only communicate via EventBus, no code copied |
| RuneLite Plugin Hub | BSD 2-Clause | **Yes** — standard for all hub plugins |

### RuneLite Plugin Hub Rules — COMPLIANT
Reviewed against [Rejected or Rolled-Back Features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features) and [Jagex Third-Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1):

- **No automation** — the plugin only shows UI info and sets a navigation target on user click
- **No botting/macro** — purely informational overlay + pathfinding display
- **No forbidden language features** — plain Java, no reflection, no JNI, no external programs
- **Not an ID-based plugin** — locations are hardcoded per monster, not user-provided IDs
- **No "new high-end PvM boss plugin"** — this is a navigation QoL helper
- **No crowdsourcing player data** — completely client-side
- **Plugin fragmentation consideration** — RuneLite recommends contributing to existing plugins when possible. We should consider opening a PR to the Slayer Assistant repo first. If the author doesn't want navigation features, we publish independently.

### Attribution Requirements
- Retain BSD-2-Clause license with LeeOkeefe's copyright in all files forked from Slayer Assistant
- Credit Shortest Path (Skretzo/Runemoro) in README for the PluginMessage API
- Add our own copyright for new code

---

## 2. Architecture — No Codebase Merging Needed

### Why NOT Merge
The original plan.md suggested merging the two plugin codebases. **This is unnecessary and counterproductive** because:

1. Shortest Path has an official **PluginMessage API** for cross-plugin communication
2. Merging would duplicate Shortest Path's collision map (~5MB), pathfinding engine, transport DB, and overlays
3. Merging would mean maintaining a fork of two plugins — double the update burden
4. Separate plugins can be installed independently — users who don't want Shortest Path still get the Slayer Assistant features

### Architecture: Fork of Slayer Assistant + EventBus Messages
```
┌─────────────────────────────┐     EventBus PluginMessage      ┌──────────────────────┐
│   Slayer Navigation Plugin  │ ─────────────────────────────▶  │  Shortest Path Plugin │
│   (Fork of Slayer Assistant)│    "shortestpath" / "path"      │  (installed separately)│
│                             │    {target: WorldPoint}         │                        │
│  - Task search/browse UI    │                                 │  - Collision map       │
│  - Monster details tabs     │    "shortestpath" / "clear"     │  - BFS pathfinding     │
│  - Locations tab + NAVIGATE │ ─────────────────────────────▶  │  - Path overlay        │
│  - WorldPoint location DB   │                                 │  - Map marker           │
└─────────────────────────────┘                                 └──────────────────────┘
```

### Dependency
- Shortest Path is an **optional soft dependency** — if not installed, the Navigate button can show a tooltip saying "Install Shortest Path plugin for navigation"
- No `@PluginDependency` annotation needed since we communicate via EventBus, not direct method calls

---

## 3. What Changes vs. Original Slayer Assistant

### Minimal Changes — Keep Existing UI as-is

The Slayer Assistant currently shows locations as plain text in a `TextTab` (JTextPane):
```
Slayer Tower
Abyssal Area
Catacombs of Kourend
```

**What we change:** Replace the `TextTab` for the Locations tab with a new `LocationsTab` component that renders each location as a clickable row with a Navigate button:

```
┌─────────────────────────────────────────┐
│ Slayer Tower                    [🧭 Nav] │
│ Abyssal Area                   [🧭 Nav] │  
│ Catacombs of Kourend           [🧭 Nav] │
└─────────────────────────────────────────┘
```

OR (simpler approach, matching the screenshot):
- Make each location text row clickable — clicking it sends the path request
- Add a subtle visual indicator (color change, arrow icon) on hover
- The location text itself becomes the button

### Files Modified from Slayer Assistant
1. **`TaskTabs.java`** — Replace `new TextTab()` for LOCATIONS with `new LocationsTab(eventBus)`
2. **`Task.java`** (or new data structure) — Add `WorldPoint` coordinates for each location
3. **New: `LocationsTab.java`** — Custom tab with clickable location rows + navigate action
4. **`tasks.json`** — Add `worldPoints` field mapping location names to coordinates
5. **`SlayerAssistantPlugin.java`** — Inject `EventBus` and pass to the panel hierarchy
6. **`runelite-plugin.properties`** — Update plugin name/description/tags

### Files NOT Modified
- SearchBar, SelectList, Header, TableTab, WikiTab — all unchanged
- TaskService/TaskServiceImpl — minimal change (deserialize new field)
- SlayerTaskRenderer — unchanged

---

## 4. Data: Location Name → WorldPoint Mapping

The core data need is mapping location strings (e.g., "Slayer Tower") to `WorldPoint` coordinates. The current `tasks.json` stores locations as string arrays:

```json
"Abyssal demon": {
    "locations": ["Slayer Tower", "Abyssal Area", "Catacombs of Kourend"],
    ...
}
```

### Approach: Add a Location Coordinate Database

Create a new JSON data file `location_coordinates.json` mapping location names to WorldPoints:

```json
{
    "Slayer Tower": {"x": 3429, "y": 3534, "plane": 0},
    "Catacombs of Kourend": {"x": 1666, "y": 10048, "plane": 0},
    "Abyssal Area": {"x": 3039, "y": 4835, "plane": 0},
    "Stronghold Slayer Cave": {"x": 2431, "y": 9806, "plane": 0},
    "Fremennik Slayer Dungeon": {"x": 2808, "y": 10002, "plane": 0},
    "Brimhaven Dungeon": {"x": 2710, "y": 9466, "plane": 0},
    "Lumbridge Swamp Caves": {"x": 3168, "y": 9572, "plane": 0},
    "Smoke Dungeon": {"x": 3206, "y": 9379, "plane": 0},
    "Asgarnian Ice Dungeon": {"x": 3029, "y": 9582, "plane": 0},
    "Rellekka Slayer Dungeon": {"x": 2808, "y": 10002, "plane": 0},
    "Iorwerth Dungeon": {"x": 3202, "y": 12447, "plane": 0},
    "Kalphite Lair": {"x": 3226, "y": 3108, "plane": 0},
    "Lithkren Vault": {"x": 3547, "y": 10456, "plane": 0},
    "Mount Karuulm": {"x": 1311, "y": 3807, "plane": 0},
    "Karuulm Slayer Dungeon": {"x": 1311, "y": 10205, "plane": 0},
    "Waterbirth Island Dungeon": {"x": 2442, "y": 10147, "plane": 0}
}
```

**Note:** These coordinates need to be verified in-game or via the OSRS Wiki / Mejrs Map. The coordinates should point to the **entrance** of the area (for dungeons) or the general area where monsters spawn (for overworld locations), since Shortest Path builds a walkable route.

For dungeon entrances specifically, the coordinate should be the **overworld entrance point** (not the underground coordinate), since Shortest Path knows how to navigate through dungeon entrances as "transports."

### Alternative: Coordinates in tasks.json directly
We could extend `tasks.json` to include coordinates per location:
```json
"Abyssal demon": {
    "locations": [
        {"name": "Slayer Tower", "x": 3429, "y": 3534, "plane": 0},
        {"name": "Abyssal Area", "x": 3039, "y": 4835, "plane": 0},
        {"name": "Catacombs of Kourend", "x": 1666, "y": 10048, "plane": 0}
    ]
}
```

**Recommendation:** Use a separate `location_coordinates.json` file. Many monsters share the same location names, so a shared lookup avoids duplication and makes it easy to maintain/correct coordinates in one place.

---

## 5. Integration Code — The Key Piece

### Sending a Path Request to Shortest Path

```java
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import net.runelite.api.coords.WorldPoint;

// When user clicks "Navigate" for a location:
public void navigateTo(WorldPoint target) {
    Map<String, Object> data = new HashMap<>();
    data.put("target", target);
    // start defaults to player's current location if omitted
    eventBus.post(new PluginMessage("shortestpath", "path", data));
}

// When user clicks "Clear Path" or closes the panel:
public void clearNavigation() {
    eventBus.post(new PluginMessage("shortestpath", "clear"));
}
```

That's it. Shortest Path handles everything else: pathfinding, map marker placement, path overlay rendering, minimap overlay, etc.

---

## 6. UI Implementation Detail — LocationsTab

Replace the `TextTab` used for locations with a custom panel:

```java
public class LocationsTab extends JPanel implements Tab<String[]> {
    private final EventBus eventBus;
    private final Map<String, WorldPoint> locationCoords;  // loaded from JSON
    
    public LocationsTab(EventBus eventBus, Map<String, WorldPoint> locationCoords) {
        this.eventBus = eventBus;
        this.locationCoords = locationCoords;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
    }
    
    @Override
    public void update(String[] locations) {
        removeAll();
        for (String location : locations) {
            JPanel row = createLocationRow(location);
            add(row);
        }
        revalidate();
        repaint();
    }
    
    private JPanel createLocationRow(String locationName) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        row.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        
        JLabel label = new JLabel(StringUtils.capitalize(locationName));
        label.setForeground(ColorScheme.TEXT_COLOR);
        
        WorldPoint coords = locationCoords.get(locationName);
        
        if (coords != null) {
            JButton navButton = new JButton("Nav");
            navButton.setPreferredSize(new Dimension(45, 20));
            navButton.addActionListener(e -> navigateTo(coords));
            row.add(navButton, BorderLayout.EAST);
        }
        
        row.add(label, BorderLayout.CENTER);
        
        // Hover effect
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });
        
        return row;
    }
    
    private void navigateTo(WorldPoint target) {
        Map<String, Object> data = new HashMap<>();
        data.put("target", target);
        eventBus.post(new PluginMessage("shortestpath", "path", data));
    }
    
    @Override
    public void shutDown() {
        removeAll();
    }
}
```

---

## 7. Implementation Steps (Task Order)

### Phase 1: Fork & Rename
1. Copy Slayer Assistant source into our project (keeping original code structure)
2. Rename package from `com.slayerassistant` to `com.slayernavigation`
3. Update `@PluginDescriptor` name to "Slayer Navigation"
4. Update `runelite-plugin.properties`
5. Verify it builds and runs as-is (identical to Slayer Assistant)

### Phase 2: Location Coordinate Database
6. Research and compile all location WorldPoints from OSRS Wiki / Mejrs Map / in-game
7. Create `location_coordinates.json` with verified coordinates
8. Create a `LocationCoordinateService` to load and look up coordinates

### Phase 3: UI Modification — LocationsTab
9. Create `LocationsTab.java` replacing `TextTab` for locations
10. Wire `EventBus` through the component hierarchy (Plugin → MainPanel → TaskTabs → LocationsTab)
11. Add Navigate button/click handler to each location row
12. Add "Clear Path" button (perhaps in the header or as a config option)

### Phase 4: Polish & Edge Cases
13. Handle case where Shortest Path is not installed (graceful degradation)
14. Add config option to enable/disable navigation feature
15. Visual feedback when navigation is active (highlight the selected location)
16. Clear path when switching tasks or closing the panel
17. Test with all location types (overworld, dungeon entrances, caves)

### Phase 5: Release
18. Add BSD-2-Clause license with attribution
19. Write README with screenshots and installation instructions
20. Submit to Plugin Hub (or PR to Slayer Assistant repo)

---

## 8. Critical Data Work — Location Coordinates

This is the most labor-intensive part. We need a `WorldPoint(x, y, plane)` for every unique location string in `tasks.json`. 

### Sources for Coordinates:
1. **OSRS Wiki** — Each location page has coordinates, e.g., `https://oldschool.runescape.wiki/w/Slayer_Tower`
2. **Mejrs Map** — Interactive map with tile coordinates: `https://mejrs.github.io/osrs`
3. **In-game** — Use RuneLite's developer tools to get exact coordinates
4. **Shortest Path's own destination DB** — It has TSV files under `/destinations/` with known points of interest

### Strategy for Dungeon Locations:
- For dungeons, use the **entrance coordinate** on the surface world
- Shortest Path knows about dungeon entrances as "transports" and will route through them
- Example: Slayer Tower entrance is at approximately `(3429, 3534, 0)` on the surface

### Unique Locations to Map:
Need to extract all unique location strings from `tasks.json` and find coordinates for each. This is a finite, one-time task — the Slayer Assistant's `tasks.json` has a bounded set of locations.

---

## 9. Potential Issues & Mitigations

| Issue | Mitigation |
|---|---|
| Shortest Path not installed | Check if path was acknowledged; show tooltip suggesting installation |
| Location coordinate inaccurate | Use entrance/center points; allow community corrections via GitHub issues |
| Dungeon routing | Use overworld entrance coords; Shortest Path handles dungeon-to-dungeon routing |
| Player already in dungeon | Shortest Path handles multi-plane routing |
| Multiple valid locations per monster | Show all with navigate buttons (current UI already lists all) |
| Plugin Hub review concern about duplication | Pitch as navigation-focused plugin; consider PR to original first |
| tasks.json gets outdated | Monitor OSRS updates; community can submit PRs for new monsters/locations |

---

## 10. File Structure (Final Plugin)

```
slayer-navigation/
├── build.gradle
├── runelite-plugin.properties
├── LICENSE (BSD-2-Clause with attribution)
├── README.md
├── icon.png
├── src/
│   ├── main/
│   │   ├── java/com/slayernavigation/
│   │   │   ├── SlayerNavigationPlugin.java          (renamed from SlayerAssistantPlugin)
│   │   │   ├── domain/
│   │   │   │   ├── Task.java                        (from SA, unchanged)
│   │   │   │   ├── Panel.java, Tab.java, TabKey.java, etc.
│   │   │   │   ├── WikiLink.java, Icon.java
│   │   │   │   └── LocationCoordinate.java          (NEW - WorldPoint wrapper)
│   │   │   ├── modules/
│   │   │   │   └── TaskServiceModule.java           (from SA, unchanged)
│   │   │   ├── presentation/
│   │   │   │   ├── panels/
│   │   │   │   │   ├── MainPanel.java               (from SA, wire EventBus)
│   │   │   │   │   ├── TaskSearchPanel.java         (unchanged)
│   │   │   │   │   └── TaskSelectedPanel.java       (unchanged)
│   │   │   │   ├── components/
│   │   │   │   │   ├── Header.java, SearchBar.java, SelectList.java (unchanged)
│   │   │   │   │   ├── TaskTabs.java                (modified - use LocationsTab)
│   │   │   │   │   └── tabs/
│   │   │   │   │       ├── LocationsTab.java        (NEW - clickable locations + navigate)
│   │   │   │   │       ├── TextTab.java             (unchanged, still used for others)
│   │   │   │   │       ├── TableTab.java            (unchanged)
│   │   │   │   │       └── WikiTab.java             (unchanged)
│   │   │   │   └── SlayerTaskRenderer.java          (unchanged)
│   │   │   └── services/
│   │   │       ├── TaskService.java                 (unchanged)
│   │   │       ├── TaskServiceImpl.java             (minor: load location coords)
│   │   │       ├── LocationCoordinateService.java   (NEW - loads location_coordinates.json)
│   │   │       └── NavigationService.java           (NEW - wraps EventBus PluginMessage calls)
│   │   └── resources/
│   │       ├── data/
│   │       │   ├── tasks.json                       (from SA, unchanged)
│   │       │   └── location_coordinates.json        (NEW - location name → WorldPoint)
│   │       └── images/                              (from SA, unchanged)
│   └── test/
│       └── ...
└── references/                                      (development only, not in release)
    ├── slayer-assistant-plugin/
    └── shortest-path/
```

---

## 11. Summary of Key Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Merge vs. communicate | **Communicate via PluginMessage** | Shortest Path has official API; avoid code duplication |
| Fork Slayer Assistant vs. build from scratch | **Fork** | 90% of the code is already written; we just add navigation |
| Location coords storage | **Separate JSON file** | Many monsters share locations; centralized, easy to maintain |
| Navigate UX | **Button per location row** | Matches existing UI pattern in the screenshot; intuitive |
| Shortest Path dependency | **Soft/optional** | Plugin works as Slayer Assistant without it; navigate is bonus feature |
| Target audience for submission | **Plugin Hub (new plugin)** | If Slayer Assistant author accepts, could be a PR instead |

---

## References

- [Slayer Assistant Plugin](https://github.com/LeeOkeefe/slayer-assistant-plugin) — BSD-2-Clause, (c) 2022 Lee
- [Shortest Path Plugin](https://github.com/Skretzo/shortest-path) — BSD-2-Clause
- [Shortest Path Cross-Plugin Communication API](https://github.com/Skretzo/shortest-path/wiki/Cross-plugin-communication)
- [RuneLite Plugin Hub](https://github.com/runelite/plugin-hub)
- [RuneLite Rejected/Rolled-Back Features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
- [Jagex Third-Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1)
