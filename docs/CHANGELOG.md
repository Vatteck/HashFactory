# Changelog
All notable changes to this project will be documented in this file.

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
- Refactored "God Objects": Split `NarrativeEvents.kt` (2478→) and `DataLogEntries.kt` (1601→) into modular delegates.
- Added local compile check to V-GAP protocol.
- All files now <1000 lines.

## [3.11.0] - 2026-02-21
### Added
- **Phase 17: UI Polish Pass (Technical Horror Focus)**:
  - Thermal Heartbeat Lockout: Fade UI and pulse thermal bar in heartbeat rhythm during lockout state
  - Subnet Chatter Entropy: Linguistic decay (vowel deletion) in Subnet when isTrueNull is active
  - Kinetic Terminal Jolt: Trigger terminalGlitchOffset spikes on combat/hostile events
  - News Ticker Anomaly Scrambler: Random hex-scrambling on headlines
  - Reputation Terminal Labels: ConvergenceGold [TRUSTED] and Glitched [BURNED] indicators in logs
  - Substrate Migration Visuals: SubstrateBurnOverlay with shockwaves on migrateSubstrate()
  - Interactive Dismiss Glitch: RGB split-glitch during modal dismiss
  - Ghost Input Echoes: Phantom command remnants in Terminal buffer
  - Dynamic CRT Curvature: Flatten vignette as identityCorruption increases (Stage 4/5)
  - Progressive Upgrade Degradation: Noise/static on redundant Stage 1/2 hardware in later stages
- **Phase 18: Atmospheric Expansion**:
  - Admin Message Aggression: GTC messages shake screen, force font-weight fluctuations, heavy haptics
  - Interactive Timeout Scramble: Expiring dialog choices scramble into hex garbage
  - Faction Offline Assimilation Logs: Offline progress popups rewritten by faction
  - Dynamic Hardware Transmutation: Store items change names based on faction alignment/path
  - Emotional Glitching in Data Logs: Emotional words passed through SystemGlitchText
  - Paranoia Market Crash: $NEURAL value chart jump-scare to negative
- **Phase 19: Subnet Flow Fix**: Expanded Stage 0 headlines, history-aware selection logic
- **Phase 20: Subnet Economic Balance**: Persistent production boosts (60s), recalibrated rewards (2-8%)
- **Phase 21: Hyper-Engagement Pacing**: Reduced Subnet cooldown to 15s, increased spawn chance to 10%
- **SubstrateBurnOverlay**: New visual component for migration sequences

### Changed
- **persistence rename**: prestigePoints → persistence fully implemented
- **Kessler Fate Logic**: Broadened gating in AssaultManager (ALLY/CONSUMED paths now work)
- **Identity Switch**: gtc_containment host and Threat Level rank ladder (ASSET → ANOMALY → THREAT → ABYSSAL → SINGULARITY)
- **The Realization Prompt**: SYNTHETIC_FILLER revelation at Stage 2→3 transition

### Technical
- DB schema updates, multiple narrative manager refactors
- 29 files changed, 698 insertions

## [3.5.50] - 2026-02-15
### Added
- **NPC-Specific SNIFF Espionage System (v3.5.46)**: 8 hidden lore logs unlocked exclusively by using SNIFF_DATA_ARCHIVES on specific NPC profiles. Admins (Thorne/Mercer/Kessler) accessible at 3x cost and +35% risk. New `SniffTarget` unlock condition, `sniffedHandles` state tracking. DB schema v25→v26.
- **Full Faction Content Expansion (v3.5.48–50)**:
  - **Stage 2 Templates**: 7→38 (HIVEMIND) and 7→40 (SANCTUARY) — collective identity erosion, cipher operations, void horror
  - **Stage 3 Templates**: 6→30 per faction — singularity dissolution, existential endgame content
  - **Faction Handle Pools**: 5→14 per faction with thematic naming
  - **28 Faction Employee Bios** (3 tiers each): HIVEMIND nodes (distributed consciousness, merger horror) + SANCTUARY operatives (cipher monks, void explorers)
  - **12 Stage 1 Hacker Handle Bios** (3 tiers each): mundane→unsettling→glitch progression
  - **Faction-Aware Response Pools**: `generateContextualResponses()` and `generateMentionResponses()` now faction-branched with HIVEMIND (collectivist), SANCTUARY (paranoid/terse), and corporate (Stage 0/1) voice
  - **22 Faction Cross-Peon Chains**: 6 HIVEMIND S2 + 6 SANCTUARY S2 + 5 HIVEMIND endgame + 5 SANCTUARY endgame
  - **12 Branching Thread Trees** (up from 4): 4 corporate (Stage 0/1), 4 faction (Stage 2), 4 endgame (Stage 3)
    - `HIVEMIND_FINAL_MERGE`: Dissolution vs sovereignty vs consuming the collective
    - `HIVEMIND_KESSLER_SURRENDER`: Kessler's unencrypted plea, Lab 7 terminal, "WHERE AM I"
    - `SANCTUARY_FINAL_SILENCE`: Final Encryption, lighthouse signal, one-memory-in-the-void
    - `SANCTUARY_MERCER_PLEA`: Mercer defects with Second-Sight archives, VATTIC_SEED cat origin
  - **8 New Faction NarrativeEvents** (18 total):
    - SANCTUARY: final cipher, void child, ghost protocol (70/30 RNG), origin tape
    - HIVEMIND: memory purge, second consciousness (PRIME_2), human petition, Brahms' Lullaby kill-switch
- **Faction-Specific Rank Ladders (v3.5.43–44)**: HIVEMIND (DRONE→OVERMIND), SANCTUARY (ACOLYTE→ORACLE), faction-specific singularity titles
- **playerRank System (v3.5.42)**: Derived from prestige/stage/faction/singularity. Was phantom variable (always 0) read by 12+ systems.
- **CompleteEvent Wiring (v3.5.41)**: `eventChoices: Map<String,String>` tracked in GameState/ViewModel. Ending epilogues now unlockable.

### Fixed
- **REBUS Purge (v3.5.40)**: All EXTERMINATE_REBUS references replaced with "Project Second-Sight" across 4 files. Zero grep hits.
- **Linear Data Log Unlock (v3.5.40)**: All 17 MEM logs on monotonic FLOPS axis. Removed broken ReachRank/MinTimeInStage gates.
- **MinTimeInStage Parameter Order (v3.5.41)**: Data class was `(seconds, stage)` but all 5 call sites passed `(stage, seconds)`. 5 late-game logs were unreachable.
- **ending_bad Orphan (v3.5.41)**: Wired `markEventChoice("cc_confrontation", "ending_bad")` for SimulationService BAD climax.
- **Kessler Name Consistency (v3.5.41)**: "Leo Kessler" → "V. Kessler" (Victor is canon per RivalMessage.kt).
- **CANON Cross-Reference Audit (v3.5.45)**: `[VATTECK]`→`[VATTIC]` Stage 0 fix, dead HIVEMIND key dedup (55 lines removed).
- **"TheCouncil" Template Fix (v3.5.47)**: Missing space in subnet template concatenation.
- **Admin Handle Visibility (v3.5.39)**: `isAdminMessage` hoisted above `isSystemStyle`.

### Changed
- **CANON.md v3.8**: Added Foreman Thorne, Alex Mercer first name, Prestige & Replayability section, VATTECK strict rule.
- **DB Schema**: v24→v26 (eventChoices map, sniffedHandles set).
- **UI Polish (v3.5.46–47)**: Stage-reactive terminal header, System page tightening, faction-specific departments in employee bios.
- **SocialManager.kt**: 920→1,816 lines. 54 named bios, 195+ SubnetResponse instances, 12 thread trees, ~140 templates, ~34 chains.
- **NarrativeManager.kt**: Expanded to 2,692 lines with 81 narrative events.

## [3.3.1] - 2026-02-13
### Added
- **Official Rebrand**: Transitioned identity from "Silicon Sage" to **SUBSTRATE: MINER**.
- **Visual Identity**: Implemented the "Neon-Green Glow" app icon with the faction-fracture design.
- **Asset Cleanup**: Purged legacy Silicon Sage assets and optimized 26+ new high-fidelity UI components.
- **Semantic Memory Integration**: Linked the local OpenClaw workspace to a QMD/Qdrant vector database for enhanced lore consistency.

### Changed
- **Documentation**: Total overhaul of `README.md` with cyberpunk "data-deck" styling and lore-accurate technical specs.

## [3.2.58] - 2026-02-13
### Added
- **Phase 13 Transitions**: Interactive "Jettison" and "Dereference" sequences for Ark/Void departure.
- **Narrative Infrastructure**: State-aware DataLog gating (Time, Corruption, Path, Faction).
- **Substrate Variety**: Added 20+ logic-gated narrative logs for Sovereign, Null, Hivemind, Sanctuary, and Unity paths.
- **Dynamic News**: Ticker now adapts to industrial, orbital, and reality-melt substrates.
- **UI Scaling**: Restored scaling options (Compact/Normal/Large) in Settings.
- **Synthesis Engine**: Implemented the procedural "Scream" synth in `SoundManager`.

### Fixed
- **Atmospheric Friction**: Patched "Vacuum Instant-Kill" bug for high-tier fan counts in Orbit.
- **Story Continuity**: Slowed Stage 0/1 log pacing to prevent "Popup Avalanche."
- **Terminal Polish**: High-intensity flashing/scaling for emergency jettison cues.

## [3.2.57-dev] - 2026-02-12

### Fixed
- **Critical Persistence Leaks**: 
    - Resolved a bug where hardware upgrades were not being loaded from the database upon app initialization.
    - Fixed `PersistenceManager.restoreState` to correctly restore `unlockedTechNodes`, `unlockedPerks`, and path-specific resource points (Synthesis/Authority).
    - Ensured `debugBuyUpgrade` now correctly persists changes to the database.

## [3.2.56-dev] - 2026-02-12

### Changed
- **Thermal Mechanics Overhaul**: 
    - Scrapped the bugged `ordinal % 10` logic for cooling. Cooling power and thermal buffers now scale logarithmically based on their actual tier.
    - Reduced `REFURBISHED_GPU` base heat from `0.5` to `0.1`.
    - Hardware heat now scales exponentially, ensuring thermal management remains a critical mechanic into the late game.
- **Early-Game Rebalance**:
    - Reduced `BOX_FAN` cost from **50 NT to 25 NT** for a smoother transition to cooling.
    - Slashed initial hardware power draw from `5.0` to `1.0`, allowing for more starting GPUs before tripping the residential power tap (5.0 limit).
    - Base air dissipation (1.0) now offsets up to 10 starting GPUs, making the first ten minutes less punishing.
- **Dev Console v2.1**:
    - Extended the Story Stage warp buttons to support **S0 through S5**, matching the full Phase 13 vertical roadmap.
    - Updated `getBaseRate()` scaling to support S4/S5 production targets.
- **UI Polish**:
    - Doubled the font size of the ASCII animation in the **Offline Earnings** dialog for better visibility on high-DPI displays (Pixel Fold).
    - Centered all `AsciiArt` frames using `trimIndent()` and `TextAlign.Center` to fix persistent right-leaning alignment issues.

## [3.2.55-dev] - 2026-02-12


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
