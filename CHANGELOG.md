# Changelog
All notable changes to this project will be documented in this file.

## [4.0.1] - 2026-02-27
### FACEMINER Pressure Loop — Phase 2 Complete
**The automation economy overhaul. Every software upgrade now creates hardware demand. The pressure loop is closed.**

- **SystemLoadEngine v2.0**: Three-axis capacity model (CPU/RAM/Storage). Hardware provides capacity, software consumes it. Throttle at >80%, hard lockout at 100%.
- **Leveled Software Upgrades**: Replaced 4-tier auto-clicker with `AUTO_HARVEST_SPEED` and `AUTO_HARVEST_ACCURACY` as standard upgradeable levels.
  - Speed: +0.5 taps/sec per level (8 GHz CPU, 4 GB RAM, 15 kW, 0.3 heat per level)
  - Accuracy: +5% per level (5 GHz CPU, 6 GB RAM, 10 kW, 0.15 heat per level)
- **Pre-Purchase Load Gate**: Software purchases are blocked if they would exceed system capacity. No more blind overloading.
- **Stage Gate**: Automation requires Stage 1+ clearance.
- **Downgrade Fix**: Refund now calculated correctly (40% of current level cost, not next-level).
- **SYS.LOAD Header**: Combined weighted load indicator with OVERLOAD/THROTTLED status tags.
- **Narrative Feedback**: Terminal logs on load state transitions (nominal → throttled → locked and back).
- **SoftwarePanel UI**: Shows all three resource axes (CPU/RAM/DISK) in the load bar.

### Technical
- Refactored demand calculation into SystemLoadEngine's internal maps — no more external parameters.
- Removed `usedCpuCapacity` and `autoClickerTier` from state.
- Clean compile, no regressions.

## [3.35.0] - 2026-02-25
### Surveillance Harvester Expansion
- **Subnet Sector Surveillance**: Added new `SurveillanceVisualizer` UI to the Terminal `SURV` tab. Players can now deploy Harvesters across 12 sectors to passively siphon raw biometric data.
- **High-Purity Contracts**: Harvester buffers at 100% automatically generate "Raw Biometric Bundle" Compute Contracts that inherently bypass the Verification Minigame and apply a 1.5x payout guarantee.
- **Data Leaks**: Added a global data storage cache. Exceeding capacity triggers continuous Detection Risk spikes and Reputation damage until the buffer is cleared.
- **Narrative Integration**: Expanded the Dystopian Subnet chatter with new strings reacting to predictive text and oppressive surveillance. Added the "DATA HEMORRHAGE" dilemma to trigger when storage thresholds hit critical levels.

## [3.27.0] - 2026-02-24
### UI Polish & Chatter Sync Hotfix
- **UI Colors**: Segmented the `ResourceDisplay` string format. Rates are now permanently colored `ElectricBlue`, while efficiency multipliers are dynamically colored (`Yellow` for <1.0, `ElectricBlue` for >=1.0).
- **Pac-Man Animation**: Replaced static time-based logic with an active `rememberInfiniteTransition` in `ActiveCommandBuffer.kt`, ensuring the Pac-Man mouth reliably animates.
- **Subnet Chatter Sync**: Resolved a disparity in `SocialRepository` to properly load the expanded ambient chatter arrays. Adjusted pacing (lowered to 6.5s) and weighted reputation templates to balance the dense expansion.

## [3.26.1] - 2026-02-24
### UI Polish Hotfix
- **Header Alignment**: Mirror-aligned the top header hashes and credits sections by applying `Alignment.Top` to their respective columns.
- **Resource Glow**: Softened the gradient bloom and drop shadows behind the active HUD counters to reduce the over-saturated blockiness of the glowing text.

## [3.26.0] - 2026-02-24
### Dense Subnet Expansion
- **Ambient Chatter Addition**: Injected >150 new lines of stage-specific ambient chatter into `SocialTemplates.kt` (Corpo Grind, Faction Whispers, The Burn) and unique Stage 5 arrays (Sovereign, Null, Unity) to deepen the world building.
- **Interactive Threads**: Expanded the interactive thread pool in `SocialManager.kt`. Players can now trigger Minor Corpo Threads (Lunch Thief), Faction Recruitment Probes, GTC Raid Panic Rooms, Hallucinations, and endgame Final Pleas from Kessler, Thorne, and Mercer.

## [3.25.1] - 2026-02-24
### Power & Cooling Hotfix
- **Cooling Buff**: Increased `BOX_FAN` (to -1.5) and `AC_UNIT` (to -4.5) to prevent immediate thermal lockouts in Stage 0.
- **Power Labels**: Upgrade UI now correctly displays `KW DRAW` instead of `W` to align with the internal simulation and HUD logic.
- **Water UI Arc Fix**: The utility panel's water billing gauge now only progresses when water is actively being consumed or a bill is due, fixing the empty-state progression bug.

## [3.25.0] - 2026-02-24
### Subnet Expansion & Narrative Persistence (v3.25.0)
- **Narrative Persistence**: Implemented `narrativeFlags` in `GameState` to track long-term player choices (e.g., `santos_loyalty`, `skimmer_caught`).
- **Reputation Antagonists**: Added `@the_skimmer` (tokens) and `@snitch_0x` (risk) logic. Antagonists now dynamically messaging when reputation is **BURNED**.
- **Interactive Threads**: Integrated new thread trees for Barnaby audits, skimmer traces, snitch hunts, and character callbacks (Santos).
- **UI Polish**: 
    - **Handle Colors**: GTC staff handles now use `ConvergenceGold`.
    - **Glitch Effects**: Antagonist messages feature a subtle character-scramble effect.
- **Verification**: Verified build stability and updated internal narrative logic.

## [3.24.0] - 2026-02-24
### Billing Balance & Meter Polish (v3.24.0)
- **Balance**: Billing is now deferred until the player purchases their first Grid Capacity (`RESIDENTIAL_TAP`) or Power Generator (`SOLAR_PANEL`). This prevents early-game bankruptcy.
- **UI**: Polished billing meter arcs in the header. Thicker strokes (3.5dp), larger circles (48dp), bigger icons (14dp), and bolder text with a subtle glow for better readability.

## [3.23.0] - 2026-02-24
### SFX Overhaul & DevConsole Coverage (v3.23.0)
- **Audio Synthesis**: Added `generateChord()` and `generateImpact()` to `AudioGenerator` for richer, more complex sounds.
- **SFX Coverage**: Generated 6 previously missing sounds (`startup`, `success`, `victory`, `alert`, `data_recovered`, `climax_impact`).
- **Audio Quality**: Improved 5 existing sounds (`click`, `buy`, `error`, `alarm`, `glitch`) with better layering and frequency control.
- **DevTools**: Expanded DevConsole SFX override list to cover all 19 game sounds, categorized for easy testing.

## [3.22.0] - 2026-02-23
### Header & UI Refinement (v3.22.0)
- **LED Aesthetics**: Reduced glow radius (2.2x/1.5x) and softened alpha (0.12f). Added 8dp side margins to the LED matrix to prevent border overlap.
- **Power Rails**: Replaced blocky side-glows with `Brush.horizontalGradient` for a holographic, soft-tapered effect.
- **Header Layout**: Increased top padding to 6dp to prevent Title/Timer/Badge from overlapping the top border.
- **Data Clarity**: ResourceDisplay now consistently shows hash/compute rate even when at 0.0.

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