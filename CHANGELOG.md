# Changelog
All notable changes to this project will be documented in this file.

## [3.2.7-dev] - 2026-02-10

### Fixed
- **Purge Sacrifice Restoration**: Increased the FLOPS penalty during heat purging from 90% to 99% reduction to ensure a tangible production sacrifice. Injected immediate rate recalculation upon triggering the purge.
- **Tech Tree Boundary Hardening**: Pulled the node margins even further into the safe zone (`0.15/0.85`) to fix border-bleeding and overlapping issues on high-DPI displays.

## [3.2.6-dev] - 2026-02-10

## [3.2.5-dev] - 2026-02-10

### Changed
- **Zero-Recomposition HUD**: Deep performance refactor to isolate volatile stats (FLOPS, Heat, Power) from the UI layout thread. Values are now read during the draw phase, eliminating lag during high-frequency clicking.
- **Terminal Performance**: Isolated the log list into a sub-composable to prevent full-screen recompositions on manual compute hashes.
- **Visual Purity**: Wider horizontal spread for tech tree nodes to prevent overlap on high-DPI targets.

## [3.2.4-dev] - 2026-02-10

### Added
- **RESEARCH Tab Finalization**: Expanded keyword detection in `UpgradeManager` to ensure all 14+ specialized items (Ghost, Wraith, Citadel, etc.) correctly appear in the path-filtered Research tab.
- **Tech Tree Margins**: Pulled the node boundaries in from `0.05/0.95` to `0.12/0.88` to prevent clipping and border-bleeding on high-DPI targets.

### Changed
- **Root Node Alignment**: Hard-anchored the **Sentience Core** and Tier 0 root to absolute screen center (0.5f) to prevent overlapping with Null-side nodes on high-DPI displays.
- **Log Noise Suppression**: Removed redundant "OPERATIONS SUSPENDED/RESUMED" terminal logs when switching UI screens or backgrounding the app. Renamed the offline progression title to "DATA CONSOLIDATED" and scrubbed all automatic pause/resume telemetry.

## [3.2.3-dev] - 2026-02-10

### Added
- **HUD Identity Restoration**: Re-branded the starting header title to "Terminal OS 1.0" for better early-game immersion.
- **Updater UI Refactor**: Clicking "CHECK FOR UPDATES" in Settings now triggers an on-screen **Snackbar** instead of a terminal log.
- **Log Hygiene**: Removed the redundant "Kernel Boot Complete" terminal log.
- **Settings Clarity**: Restored the dynamic app version number to the footer of the Settings screen.

## [3.2.2-dev] - 2026-02-10

### Fixed
- **Persistence Restoration Leak**: Resolved a major bug where collections (unlocked logs, seen events, annexed nodes) were not being restored upon kernel initialization.
- **State Flow Completeness**: Ensured all 50+ bridge properties are correctly mapped from disk to the active simulation.

## [3.2.1-dev] - 2026-02-10

### Added
- **Persistence Hardening**: Decoupled housekeeping (saves, updates, lore checks) from the simulation pause. 
- **Immediate Data Commits**: Injected forced `saveState()` triggers into the hardware purchase engine and narrative decision branches.

### Fixed
- **UI Precision Leak**: Switched heat, power, and cooling stats to explicit decimal formatting (`%.1f`). 

## [3.0.1] - 2026-02-07
