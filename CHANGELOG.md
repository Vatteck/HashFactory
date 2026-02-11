# Changelog
All notable changes to this project will be documented in this file.

## [3.2.2-dev] - 2026-02-10

### Fixed
- **Persistence Restoration Leak**: Resolved a major bug where collections (unlocked logs, seen events, annexed nodes) were not being restored upon kernel initialization. This was causing previously seen logs to re-trigger on every app restart.
- **State Flow Completeness**: Ensured all 50+ bridge properties, including launch progress, altitude, and global sectors, are correctly mapped from disk to the active simulation.

## [3.2.1-dev] - 2026-02-10

### Added
- **Persistence Hardening**: Decoupled housekeeping (saves, updates, lore checks) from the simulation pause. The 1s save loop now persists even when the game is "paused" in menus.
- **Immediate Data Commits**: Injected forced `saveState()` triggers into the hardware purchase engine and narrative decision branches to resolve "unstable save" reports.
- **Kernel Initialization Handshake**: Added `repository.ensureInitialized()` to the boot sequence to prevent race conditions on fresh installs.

### Fixed
- **UI Precision Leak**: Switched heat, power, and cooling stats to explicit decimal formatting (`%.1f`). Low-tier hardware like the Refurbished GPU now correctly displays its `+0.5°C/s` signature instead of `+0/s`.
- **Header Readability**: Updated the thermal display to show one decimal place for current temperature to match the rate indicator's precision.

## [3.2.0-dev] - 2026-02-10

### Added
- **Phase 14: Substrate Hardening**: Complete re-integration of core simulation logic following the modular refactor. 100ms Production and 1s Thermal/Power loops are now fully operational.
- **The Overwrite Conduit**: Formalized the NG+ mechanism. Kernel migration now correctly updates `completedFactions` to track run history (Sovereign/Null paths).
- **Unity Path Hard-Lock**: The Unity path is now strictly locked until the persistence engine detects both **SOVEREIGN** and **NULL_OVERWRITE** completions in your history.
- **Subagent Routing Protocol**: Verified and locked local routing for Ollama models (Qwen/DeepSeek). Cloud fallbacks are disabled to ensure local processing.
- **Auto-Updater Restoration**: Fully implemented the GitHub-based update checker and in-app download/install flow.
- **Bridge Completion**: All previously hollow methods in `GameViewModel` are now functional:
    - `initializeGlobalGrid()`: Path-aware sector mapping for Phase 13.
    - `resolveRaidSuccess/Failure()`: Tactical node defense logic for city raids.
    - `triggerSystemCollapse()`: Node-by-node reality dissolution for the Null path.
    - `onDiagnosticTap()`: Repaired the network maintenance mini-game.

### Changed
- **UI Architecture**: Hoisted `DataLogArchiveScreen` navigation to the top-level `MainScreen` to fix a critical layout nesting crash.
- **Archive Alignment**: Re-aligned the Data Log Archive button to the Settings page (under "Dangerous Actions") per user preference.
- **ADB Monitoring**: Updated the sentinel loop to treat Pixel Fold disconnection as nominal behavior (suppressed failure alerts).
- **HUD Performance**: Performance pass for high-DPI targets; verified 120Hz stability on the Foldable substrate.

### Fixed
- **Settings UI Reactivity**: Refactored `SoundManager` and `SettingsScreen` to use `StateFlow`, ensuring audio sliders and toggles update instantly without requiring a screen swap.
- **Manual Click Engine**: Fixed `trainModel()` to correctly generate atomic heat (+0.5°C) and trigger terminal shell logs.
- **Thermal Gauge Rendering**: Switched the gauge drawing to a dedicated Canvas element to prevent layout engine culling.
- **Updater Visibility**: Fixed the "Check for Updates" button text color for better contrast against dark backgrounds.

## [3.1.8-dev] - 2026-02-09

### Added
- **Modular Architecture (Phase 14)**: Surgically extracted `ResourceEngine.kt`, `UpgradeManager.kt`, `PersistenceManager.kt`, `TerminalDispatcher.kt`, and `NarrativeManagerService.kt` to reduce ViewModel bloat.
- **Zero-Recomposition Architecture**: Isolated volatile stats (FLOPS/Heat) into providers to achieve 120Hz stability.
- **Database v18 Migration**: Bumped Room DB version to persist `cdLifetime`, `vfLifetime`, and `peakResonanceTier`.

### Fixed
- **Singularity Trigger**: Bypassed narrative queues for the Singularity event to force immediate UI transition.
- **News Ticker**: Implemented a 15s scroll debounce to fix ticker jumping.
