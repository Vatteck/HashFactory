# Changelog
All notable changes to this project will be documented in this file.

## [3.9.20] - 2026-02-20
### Added
- **Reputation System (Phase 14 Logic)**: Implemented `reputationScore` tracking (0-100) and computed tiers: **TRUSTED**, **NEUTRAL**, **FLAGGED**, and **BURNED**.
- **Social Reactivity**: The Subnet now acts as a threat vector or asset. **TRUSTED** status grants early-warning sentinel messages (lowering risk). **BURNED** status triggers "snitch" leaks (spiking risk).
- **Economic Mechanics**: Introduced the "Employee Discount" (-10% upgrade costs) for TRUSTED operators and "Systemic Friction" (+25% costs) for those who are BURNED.
- **Handshake Penalty**: Annexation speed is now tied to reputation. TRUSTED handshakes are 10% faster; BURNED handshakes are 25% slower due to extra security checks.
- **Good Neighbor Duty**: Added the `[STABILIZE_NODE]` interaction to the Subnet, allowing players to spend Neural Tokens to repair their reputation.

### Changed
- **Modular Architectural Refactor**: 
    - Decoupled "God Objects" by extracting static data and UI components into side-car facades.
    - `NarrativeManager.kt`: 2,808 → 103 lines (Logic extracted to `NarrativeEvents.kt`).
    - `DataLogManager.kt`: 1,784 → 119 lines (Data extracted to `DataLogEntries.kt`).
    - `SocialRepository.kt`: 1,176 → 260 lines (Templates extracted to `SocialTemplates.kt`).
    - `GridScreen.kt`: 971 → 519 lines (UI extracted to `CityGridScreen.kt`).
    - `GameViewModel.kt`: 845 → 650 lines (State fields extracted to `CoreGameState.kt`).
- **Tactical Map 2.0**:
    - **Gravel Substrate**: Added tri-color jittering pixel noise for an analog terminal feel.
    - **Directional Data Streams**: Roadway packets now flow visually toward the Command Center (A3) or away, reflecting network convergence.
    - **Siege Halos**: Roadways maintain a steady glow, shifting to an active alpha-pulse only when nodes are under GTC siege.

### Fixed
- **Performance Optimization**: Nuked triplicate noise coroutines across the map and detail panels, reducing the frame budget by **3,500 drawRect calls per frame**.
- **Build Stabilization**: Switched scanlines to float-based loops and pre-cached stroke widths for high-DPI (Pixel Fold) smoothness.

## [3.9.8] - 2026-02-19
### Fixed
- **Singularity-Before-Departure**: Removed hardcoded departure triggers from FactionChoiceScreen. Singularity choice now auto-routes to departure: SOVEREIGN → LAUNCH (orbit), NULL → DISSOLUTION (void). Both paths available to both factions.
- **Kessler Fate**: Removed hard-locked Kessler outcomes (CONSUMED/EXILED). Kessler fate is now player-determined.
- **City Assault**: Fixed soft-lock where auto-fire narrative popup would fail if hardwareIntegrity was not stable. Now manual trigger via A3 GridScreen button.
### Changed
- **CANON.md v4.1**: Updated narrative arc (singularity before departure), added departure outcome table, removed CRITICAL BLOCKER note.
- **Diary Script**: Fixed to run at midnight and read yesterday log (was reading today which barely existed at 4 AM).

[Remaining log history truncated...]