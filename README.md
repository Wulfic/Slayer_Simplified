# Slayer Simplified

A RuneLite plugin that combines a slayer task assistant with one-click navigation via the [Shortest Path](https://github.com/Skretzo/shortest-path) plugin. Browse monsters, view drop tables and combat stats from the OSRS Wiki, manage favorite locations, take personal notes, and navigate to any task location — all from a single panel. When you type !task in-game, the plugin automatically detects your current slayer assignment and provides a quick Nav button to your task location. If no task is active, it will navigate to your slayer master instead. Whether you're a beginner or a seasoned slayer, Slayer Simplified streamlines your workflow and keeps all the essential information at your fingertips.

## Features

### Task Browser & Search
- Searchable list of 100+ slayer monsters with icons
- Select a monster to view its locations, loot, combat info, and more

### Target Highlighting
- Highlights the current task monster for easy identification
- Customizable highlight color and style in settings

### Navigation
- One-click **Nav** buttons on every mapped location to draw a path via Shortest Path's PluginMessage API
- 194 location-to-coordinate mappings (dungeons use overworld entrance coords so Shortest Path handles routing)
- Navigate to any of the 8 slayer masters (Turael, Mazchna, Vannaka, Chaeldar, Konar, Nieve, Duradel, Krystilia)

### Active Task Tracking
- Automatically detects your current slayer task from in-game chat messages
- Recognizes new assignments, ongoing tasks, completions, and "no task" states
- Quick Navigate to your active task's location

### Locations Tab
- Each location displayed as a row with a favorite toggle (★/☆) and Nav button
- Favorite locations are persisted and sorted to the top

### Loot Tab (OSRS Wiki Scraper)
- Scrapes drop tables directly from the OSRS Wiki
- Displays collapsible sections with item name, quantity, rarity (color-coded), and price
- Results cached per monster to avoid redundant fetches

### Info Tab
- Combined view of items required, combat info (from task data), wiki combat stats, and slayer master assignments
- Wiki stats loaded asynchronously: combat level, HP, max hit, attack style, elemental weakness, immunities

### Notes Tab
- Editable per-monster notes saved automatically
- Persisted via RuneLite's ConfigManager

## Requirements
- [Shortest Path](https://github.com/Skretzo/shortest-path) plugin installed in RuneLite

## Building
```
./gradlew build
./gradlew run
```

## Credits
Built on top of these excellent plugins:
- [Slayer Assistant](https://github.com/LeeOkeefe/slayer-assistant-plugin) by LeeOKeefe — task data and original UI foundation
- [Loot Lookup](https://github.com/donth77/loot-lookup-plugin) by donth77 — OSRS Wiki drop table scraping
- [Shortest Path](https://github.com/Skretzo/shortest-path) by Skretzo — pathfinding engine and PluginMessage API

## License
BSD 2-Clause — see [LICENSE](LICENSE) for details.