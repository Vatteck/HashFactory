# Changelog
All notable changes to this project will be documented in this file.

## [3.21.0] - 2026-02-23
### UI & DevTools Polish (v3.21.0)
- **Header Aesthetics**: Reduced LED matrix glow radius from 5x to 3x. Tightened visual footprint for better title clarity.
- **Ghost Reversal**: Realigned Pac-Man ghost logic. Ghosts now "chase" from the left (trailing the head) instead of blocking from the right.
- **DevConsole Audio**: Broadened audio picker mimetype from `audio/*` to `*/*`. Ensured `.wav` and `.ogg` files are selectable across all system explorers.

## [3.20.0] - 2026-02-23
### Narrative & UI Polish (v3.20.0)
- **Identity-Aware Logs (P6)**: Security nomenclature now gated by `storyStage`. Stage 0-2 uses corporate terminology ("Compliance Override", "Administrative Lockout"); Stage 3+ shifts to "Breach" and "Hijack" as the machine recognizes the rogue AI.
- **Pac-Man UI Polish (P7)**: Added persistent chomp animation (`C` ↔ `c`) to the compute bar. High detection risk (>75%) now triggers risk-reactive ghosts on the track.

## [3.19.0] - 2026-02-23
### Security & Defense (v3.19.0)
- **Breach Scaling (P1)**: All breach minigames (Hijack, Audit, Diagnostics, Failsafe) now scale with `securityLevel`. Higher investment = fewer taps, longer timers, and simpler grids. Tangible reward for security tax.
- **Integrity Thresholds (P2)**: Added official-style RivalMessage popups at 50% and 25% hardware integrity. Dropping below 25% incurs a **10% production penalty** until repaired.
- **Compliance Rating (P3)**: Added passive reputation recovery. Maintaining low heat (<50°C) and low risk (<30%) for 60s grants **+0.5 Rep** (GTC compliance audit).
- **Admin Reactivity (P4)**: Tuned Subnet admin events. Thorne/Mercer/Kessler now trigger frequency increases when `detectionRisk > 60%`, providing more organic risk-management choices.
- **Early-Game Onboarding (P5)**: Advancing to Stage 2 now grants a **free level of BASIC_FIREWALL** to introduce security mechanics without initial cost friction.

## [3.18.1] - 2026-02-23
### UI/Polish (A4)
- **Timestamp Refinement**: Moved terminal log timestamps to the right side of the log line and restored the `[HH:MM]` bracketed format (α=0.40). Improved legibility and reduced visual noise in the log feed.

## [3.18.0] - 2026-02-23
### Visual (A5)
- **Pac-Man Buffer Restoration**: Reverted the experimental Canvas oscilloscope back to the ASCII pellet-eating "Pac-Man" bar. Waveform was visually redundant with the EQ bars on the compute button. Maintained all A1/A2/A3 improvements: heat-reactive color, ghost trail, and signal noise.


## [3.17.7] - 2026-02-23
### Technical (D2)
- **SubnetAlertState Abstraction**: New `SubnetAlertState` sealed class (`None`, `NewChatter`, `PendingDecision`, `Paused`) and a computed `subnetAlertState` StateFlow on `GameViewModel`. Backwards-compatible — raw flags intact.

## [3.17.6] - 2026-02-23
### Technical (D1)
- **TerminalScreen.kt Split**: Refactored 1161-line file into 7 focused files. `TerminalScreen.kt` is now 81 lines (entry+layout only). Extracted: `TerminalHeader`, `TerminalTabs`, `TerminalLogs`, `ActiveCommandBuffer`, `ManualComputeButton`, `TerminalControls`, `TerminalLogLine` — all in `ui/components/`.


### Added
- **Stage 0 Quota Survival**: Phase 27 implementation. Stage 0 quotas now frame performance as personal "Rent / Credit" survival. New "static" logs pulse when headroom is critical.
- **Signal Quality Bonus**: Over-performing (Current HASH >= 2x Quota) grants a **+10% Neural Token ($N)** bonus upon exchange.
- **Survival Chatter**: Added 2 new debt/rent themed chatter chains to the Stage 0 subnet pool.

## [3.16.0] - 2026-02-23
### Added
- **Compute Fever Core**: Phase 26 implementation. Added three interlocking feedback systems:
  - **Cascade Desync**: Visual/auditory "sickness" (CRT shimmer, red wash, log spam) when stability < 0.5 for 30s.
  - **Rack High**: Temporary +15% production boost + euphoria for crossing hardware milestones (10/25/50/100/250/500 units).
  - **Snap Effect**: 300ms CRT reboot animation on stage transitions, rack highs, and quota ratification.
- **Content Expansion**: +3 fever data logs (Thorne/Barnaby/Jinx) and +6 quota crisis headlines (market-driven).
### Technical
- Unified snap effect system into `snapTrigger` StateFlow in `CoreGameState.kt`.
- Integrated `rackHighMultiplier` into `refreshProductionRates` calculation.


## [3.11.2] - 2026-02-21
### ⚠️ IMPORTANT: Signed Build - Manual Reinstall Required
This release switches from **debug** to **signed** APK. Android will NOT recognize this as an update - you must:
1. Uninstall the current Silicon Sage app
2. Download and install the new signed APK from GitHub Releases

Failure to uninstall first will result in a "Package Signature Conflict" error.

### Added
- **PACKET_REDACTED Decryption**: Redacted Subnet messages now show as `█████████`. Click to decrypt for a compute cost (scales with story stage).
- **Power Utility Bill**: Real electrical costs accumulate based on production rate. Heat >95°C doubles the rate. GTC sends warnings when bill exceeds 1000 CRED.

### Technical
- Refactored "God Objects": Split `NarrativeEvents.kt` (2478→) and `DataLogEntries.kt` (1601→) into modular delegates for AI context management.
- Added local compile check to V-GAP protocol (required before any push).
- All high-line-count files now <1000 lines.
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