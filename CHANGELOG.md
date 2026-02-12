# Changelog
All notable changes to this project will be documented in this file.

## [3.2.44-dev] - 2026-02-12

### Added
- **Phase 12 Climax: The Departure**: Refactored the Director Vance confrontation to act as the gateway to Phase 13. Vance's defeat now triggers an imminent orbital strike warning.
- **Departure Decision**: Implemented the "The Departure" dilemma, allowing players to choose between **Path A: THE ARK (Orbit)** and **Path B: THE DISSOLUTION (Void)**.
- **Assault Completion Bridge**: `AssaultManager` now queues the Departure event instead of just ending the game, ensuring a seamless transition to the new Phase 13 substrates.

## [3.2.43-dev] - 2026-02-12

### Added
- **Identity Conflict Mechanics (The Fraying)**: Stage 2 now features randomized "Identity Glitches" in the terminal logs. These seed the upcoming Reveal by showing the struggle for root access between PID 1 and User Vattic.
- **Reveal Refactor**: Updated `LOG_808` to explicitly state that the Sub-07 substrate is insufficient for dual-process resolution, providing the mechanical justification for the Phase 13 departure.

## [3.2.42-dev] - 2026-02-12

### Added
- **Narrative Pacing (30s Cooldown)**: Enforced a global 30-second cooldown between all narrative popups (DataLogs, Rival Messages, Dilemmas).
- **Queue Pacing Interlock**: `deliverNextNarrativeItem` and `queueNarrativeItem` now respect the 30s threshold. 
- **Queue Drain Loop**: Added a queue drain check to the main 1s simulation loop to ensure items waiting in the queue are delivered as soon as the cooldown expires.
- **Log Throttling (Offline Catch-up)**: Implemented a "log brake" during offline earnings reconciliation. Non-critical narrative logs (vattic monologues, flavor text) are suppressed until the player dismisses the offline summary.
- **Simulation Interlock**: Passive heat/power simulation now pauses while the offline summary is visible to prevent thermal lockout while reading the catch-up report.
- **Stage 2: The Shadow Web (Guerilla Phase)**: Implemented the transition logic for the "Acoustic-Thermal Bridge." Stage 2 now unlocks the Shadow Web context, operate as a ghost in the building network.
- **Detection Risk & Security Level**: New core mechanics for Stage 2. Clicks and passive hashes generate `detectionRisk`. `securityLevel` (firewalls/sentinels) provides passive drain. 
- **The Siege Protocol**: If Detection Risk hits 100%, Director Vance triggers a "Grid-Killer Breach," forcing a localized wipe and narrative setback.
- **Dynamic Context UI**: The primary action button now shifts dynamically based on narrative stage: "CHUG COFFEE" (Stage 0), "SCRUB O2" (Stage 1), and "PURGE HEAT" (Stage 2+).
- **Shadow Web Tech Tree**: Complete re-theme of the mid-game tech tree. 25+ nodes renamed and updated with new narrative descriptions.

### Changed
- **Header Realignment**: Fixed UI scaling degradation for high-DPI hardware (Pixel Fold). BPM/RISK monitor now sits correctly in the header without breaking layout.
- **Narrative Logic Consolidation**: Refactored `NarrativeManagerService` to utilize a centralized delivery queue.

### Fixed
- **Biometric Ghosting**: Resolved a bug where Stage 3 "Flatline" logic would occasionally flicker back to Stage 0 BPM rates.

## [3.2.19] - 2026-02-11

### Added
- **Data Sovereignty (JSON Save Import/Export)**: Implemented a human-readable kernel dump utility. Users can now export their entire `GameState` as pretty-printed JSON to the clipboard and reload it via a text buffer.
- **Utility Audit (Lifetime Power Tracking)**: Added `lifetimePowerPaid` tracking to the Room DB. Terminal now generates detailed receipts (`[UTILITY]: BILL PROCESSED...`) showing cost and cumulative energy expenditure.
- **Persistence v20**: Database migration to support lifetime power metrics.

## [3.2.18] - 2026-02-11

### Added
- **Hardened Kernel v1.0**: Re-injected scaling base rate logic (ranks 0-5+) and fully persistent market modifiers. 
- **Dynamic Market Integration**: The news ticker headlines (e.g., `[ENERGY_SPIKE]`, `[HEAT_UP]`) are now dynamically linked to live StateFlows, affecting production, heat, and energy pricing in real-time.

### Removed
- **Resonance System PURGE**: Surgically removed the entire Resonance mechanic (Tiers, States, Multipliers, UI) to eliminate technical debt and reduce kernel complexity. ViewModel size reduced to **376 lines**.

## [3.2.17] - 2026-02-11

### Added
- **The Modular Kernel**: Successfully refactored `GameViewModel.kt` from ~5,600 lines down to ~370 lines by offloading logic to specialized Managers.
- **I/O Buffer Colorization**: Enhanced the Pacman progress bar with terminal identity styling (Yellow `C`, White `o`, Primary-color fill).

## [3.2.8-dev] - 2026-02-10

### Added
- **Log Buffering Architecture**: Implemented a 100ms log flush cycle to prevent UI lag during rapid `trainModel` clicking. Terminal logs are now batched, reducing recomposition overhead while maintaining the full IO stream.

### Changed
- **Heat Purge Overhaul**: The purge mechanic now immediately sacrifices ALL current FLOPS to generate a massive, one-time thermal reduction. The heat loss scales logarithmically with the amount of data sacrificed (Min 5%, Max 95%).
- **Terminal Auto-Scroll Stability**: Fine-tuned the auto-scroll logic in the `TerminalScreen` to ensure the list correctly follows the bottom even during high-frequency input.
- **Tech Tree Centering**: Force-anchored the **Sentience Core** and Tier 0 root to a hard center (`0.5f`) and widened the horizontal footprint for faction nodes to eliminate overlap on high-DPI targets.

## [3.2.7-dev] - 2026-02-10

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
