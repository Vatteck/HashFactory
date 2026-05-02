# gametasks.md — Hash Factory / SUBSTRATE:Miner Development Backlog

## 🔴 Critical (blocking)
- [x] **Subnet Sector Surveillance (v3.35.0)** — Added Harvester map, 100% Purity Auto-verification, Storage Leak dilemma, and dystopian harvester chatter.
- [x] **Security Defense Pass (v3.19.0)** — Breach scaling (Hijack/Audit/Diag/Failsafe), Integrity popups/penalties, Compliance Rating passive rep, Admin frequency tuning, Free Stage 2 firewall.
- [x] **Narrative & UI Polish Pass (v3.20.0)** — Identity-aware security nomenclature, Pac-Man chomp animation, Risk-reactive ghosts.
- [x] **UI & DevTools Polish Pass (v3.21.0)** — Reduced Header LED glow, reversed ghost chase direction, broadened audio picker mimetype.
- [x] **Header & UI Refinement Pass (v3.22.0)** — Tighter LED glow & side margin fix, holographic power rail gradients, fixed top layout overlap, forced zero hash display.
- [x] **SFX Overhaul & DevConsole Coverage (v3.23.0)** — Audio generator expansion, full SFX coverage, improved core UI sounds, categorized DevConsole overrides.
- [x] **Billing Balance & Meter Polish (v3.24.0)** — Deferred billing until infrastructure purchase, polished high-visibility billing meters.

## 🟡 High (next session)
- [x] **Compute Fever Quota System (v3.16.1)** — Stage 0: HASH quota for CRED survival. Personal → Global reveal via logs/chatter. Static for low headroom, +10% CRED "Signal Quality" bonus for quota beat.
- [x] **Data Log & Chatter Overhaul (v3.16.1)** — Rewrite for Compute Fever (Thorne flexes, Barnaby audits, subnet rack mania). Gate by stage.
- [x] **Substrate Sickness System (v3.16.0)** — Cascade Desync (CRT shimmer + log spam) when signal < 0.5 for 30s. Clears on rack high/quota beat.
- [x] **The "Snap" Effect (v3.16.0)** — CRT reboot animation on stage transitions, rack highs, quota ratification.
- [x] **Hugging Face Papers RSS** — Fixed. RSS dropped (401, no auth support). Replaced with `scripts/hf-papers-pulse.sh` hitting public `api/daily_papers` endpoint. Cron every 12h.

## 🟢 Medium
- [x] **ExpansionLogs.kt Refactor** — Split into CharacterDossierLogs (241L), MemoryHallucinationLogs (377L), EndgameLogs (476L), CoreLogs (535L). ExpansionLogs.kt is now a 11-line aggregator. .bak cleaned up.
- [x] **Admin Subnet Handle Fix (v3.16.2)** — Handle: ElectricBlue + Shadow glow (blurRadius=8). Body text: plain white, FontWeight.Normal. No more blue body text.
- [x] **Notification Bubble Fix (v3.16.2)** — Badge suppressed when user is already viewing SUBNET. Dead `hasNewSubnetMessage` flag now cleared on SUBNET mode entry.
- [x] **Progress Bar/Terminal UI Enhancement Pass** — See `ROADMAP_v3.17.md` Phase A (A1–A5). Build order: v3.17.0 = A1+A2+A3, v3.17.1 = A4, v3.18.0 = A5 (oscilloscope stretch).
- [x] **WAL Protocol Adoption** — `scripts/wal.sh` helper created. Protocol documented in AGENTS.md. Decisions dir at `memory/decisions/`.

## 🟢 Planned (v3.17.x) — See ROADMAP_v3.17.md
- [x] **[A1] Buffer Heat Color (v3.17.0)** — Progress bar lerps NeonGreen→Amber→Orange→ErrorRed with currentHeat.
- [x] **[A2] Pellet Ghost Trail (v3.17.0)** — 3 fading afterimages on compute pellet sweep.
- [x] **[A3] Signal Noise in Buffer (v3.17.0)** — Glitch chars in empty buffer, driven by globalGlitchIntensity.
- [x] **[A4] I/O Log Timestamps (v3.18.1)** — `[HH:MM]` suffix on system logs (α=0.40), right-aligned for better density and visibility.
- [x] **[A5] Pac-Man Buffer Bar (v3.18.0)** — Restored ASCII buffet with pellets, ghost trail, and commit burst. Oscillator reverted due to visual redundancy with EQ bars.
- [x] **[B1] Aquifer Eulogy (v3.17.2)** — 0% milestone added to aquifer tracker. GTC "sorry for any inconvenience" eulogy fires once at global aquifer death.
- [x] **[B2] Failsafe Partition (v3.17.3)** — Detection 100% → 30s scramble grid. Fail = -30 rep + 60s halt. Success = reset to 70%.
- [x] **[B3] Kessler's Last Bargain (v3.17.4)** — Stage 4 interactive subnet message. ACCEPT → ×2.5 multiplier + rep reset. DECLINE → close. 5min timeout.
- [x] **[B4] Black Market (v3.17.5)** — `@null_vendor` subnet event at BURNED rep. SHOW ME → +5% detection. Stub implementation.
- [x] **[C1] Thorne's Resignation Arc (v3.17.2)** — 3 stage-gated one-shot subnet messages (S1 confused → S2 scared → S3 final). ACCOUNT_DEACTIVATED system log fires after last message.
- [x] **[C2] Jinx's Hidden Data Logs (v3.17.2)** — 5 secret logs (JINX_SECRET_01–05), Stage 3+, 2M–9.5M FLOPS stagger. Reveals Kessler knew from Day 1. Jinx fired for covering for Vattic.
- [x] **[D1] TerminalScreen.kt Split (v3.17.6)** — 1161 lines → 6 files (<81 lines in entry). New components: `TerminalHeader`, `TerminalTabs`, `TerminalLogs`, `ActiveCommandBuffer`, `ManualComputeButton`, `TerminalControls`, `TerminalLogLine` in `ui/components/`.
- [x] **[D2] SubnetAlertState Abstraction (v3.17.7)** — Sealed class: `None / NewChatter / PendingDecision / Paused`. Computed `subnetAlertState` property on `GameViewModel. Backwards-compatible.`
- [x] **[A5] Pac-Man Buffer Bar (v3.18.0)** — Restored ASCII buffet with pellets, ghost trail, and commit burst. Oscillator reverted due to visual redundancy with EQ bars.

## ✅ Completed (v3.12.x - v3.15.x)
- [x] **Water-Migration Hook (v3.15.x)** — Aquifer depletion, staged municipal caps (S0=100, S1=500, S2=2000), production stall (saturation penalty), soft/hard prestige split.
- [x] **Prestige Gating (v3.15.x)** — Migration gated to S1+, Overwrite gated to S5+. Context-aware intro text.
- [X] **Progressive Reservoir HUD (v3.15.x)** — Dynamic label: Stage 0=TAP, 1=MUNICIPAL, 2=REGIONAL, 3=GLOBAL, 4+=RECYCLE. Water usage unit scaling.
- [x] **UI Polish Overhaul (v3.15.x)** — OfflineEarnings fix, Dilemma typewriter/bloom, DataLog scan-lines/stamps, RivalMessage shake/scramble, +28 Headlines.
- [x] **Compute Fever Polish (v3.16.0)** — Cascade Desync (CRT shimmer + log spam), Rack High (+15% boost on hardware milestones), Snap Effect (CRT reboot), +3 fever logs, +6 crisis headlines.
- [x] **Power System Overhaul (v3.12.4-6)** — Dual-layer rails (ElectricBlue gen / Red-Yellow draw), explicit generator values, 60s billing cycle, demand charges, net metering
- [x] **HudTheme (v3.12.7)** — 8-state syntax coloring system with semantic grammar for faction/path states
- [x] **Room DB v30** — bumped for missedBillingPeriods schema change
- [x] **Header Visual Polish (v3.14.x)** — Circular LED dots with dual-layer glow (tight inner + soft outer), heat-reactive colors, 35% floor / 75% peak alpha. Power rail glow with load-scaled intensity, sparks at extreme load.
- [x] **Border Refactor** — All TechnicalCornerShape cut corners replaced with RoundedCornerShape throughout Terminal and Header.
- [x] **Global Glitch System** — When `globalGlitchIntensity` rises, entire screen shakes (translationX/Y) and fades (alpha). Applied to Column in MainScreen, affects header/grid/upgrades - not just terminal log.
- [x] **Button Equalizer** — 28 vertical bars behind compute button, driven by hash rate (speed scales with flops), click spike animation, peak hold with gravity fall, bright top caps, floor ambient glow, theme-colored.
- [x] **Visual Effects (v3.14.x)**:
  - Bloom on ResourceDisplay FLOPS/NEUR (radial gradient)
  - Overclock vignette (orange edge bleed, breathes 6-13% alpha)
  - Breach strobe (fullscreen red flash, 180ms cycle)
  - Integrity heartbeat (red radial pulse when HW <30%)
  - Button tap bloom (theme color glow that spikes on click, decays)

---
*Synced from workspace tasks.md*

---

## 🟡 v5.0.x — One-Wallet `$FLOPS` Economy Migration

Canonical plan: `.hermes/plans/2026-05-01_194644-one-wallet-balance-migration.md`

Operating rule: `$FLOPS` is the only spendable wallet. `neuralTokens` remains only as a legacy save-field until a later schema migration. `substrateMass` is Stage 4+/endgame mass, not a routine wallet.

- [x] **[5.0.0] HUD wallet placement** — `$FLOPS` replaces the old CRED/NEUR right-side wallet slot; `SYS.LOAD` and system integrity remain separate.
- [x] **[5.0.0] Save wallet merge** — Old FLOPS stockpile and legacy CRED/NEUR balance merge into spendable `$FLOPS` on load; legacy field saves as 0.
- [x] **[5.0.0] Dataset cashflow pass** — Valid dataset taps pay drip rewards; completion pays the remaining batch reward; time-warp burst is capped against dataset yield.
- [x] **[5.0.0] Manual hash packet retune** — Manual buffer commits stay useful but remain below dataset/passive loops.
- [x] **[5.0.0] Event sink/source pass** — Routine penalties and rewards scale by production windows instead of stale flat CRED/NEUR values.
- [x] **[5.0.0] Billing/security pass** — Bills, repairs, skims, and routine payment paths debit the one wallet through safe `$FLOPS` helpers.
- [x] **[5.0.0] Substrate identity pass** — `substrateMass` is quarantined to Stage 4+/endgame mass; local/global annexation and upgrades spend `$FLOPS` instead.
- [x] **[5.0.0] Launch/void compression pass** — Endgame transitions compress `$FLOPS` into bounded boot reserves instead of carrying full pre-launch wealth.
- [ ] **[5.0.x] Runtime playtest** — Install debug APK and verify early/mid/late wallet pacing on device/emulator.

---

## 🟡 v4.1.x — Economy Rail / Idle Math Application

Canonical plan: `docs/economy-idle-math-plan.md`

Operating rule: read the plan before economy work, do one phase at a time, compile after each phase, park new ideas instead of expanding scope.

### Phase 0 — Rail setup
- [x] **[4.1.0] Economy rail plan doc** — Created `docs/economy-idle-math-plan.md` with scope, non-goals, formulas, phases, gates, and parking lot.
- [x] **[4.1.0] Task tracker section** — Added this `v4.1.x` section to `gametasks.md` so work is visible and resumable.

### Phase 1 — Safe math cleanup
- [x] **[4.1.1] Geometric bulk-buy math** — Replaced looped multi-level cost/max-affordable calculations in `UpgradeManager.kt` with closed-form geometric formulas while preserving existing single-level cost behavior.
- [x] **[4.1.1] Bulk-buy verification** — `./gradlew :app:compileDebugKotlin` passed; geometric formula sanity checks matched looped sums and max-affordable next-cost boundaries.

### Phase 2 — Naming / UI clarity
- [x] **[4.1.2] FLOPS label pass** — Kept player-facing accumulated/rate compute labels as FLOPS after review; `./gradlew :app:compileDebugKotlin` passed.
- [x] **[4.1.2] Storage ratio in Header** — Added `STOR used/capacity` under SYS.LOAD with one-line ellipsis protection using `FormatUtils.formatStorage()`; `./gradlew :app:compileDebugKotlin` passed.
- [x] **[4.1.2] Buffer display review** — Left existing buffer display alone; no competing UI change needed after storage moved under SYS.LOAD.

### Phase 3 — Economy feel
- [x] **[4.1.2] Hardware milestone multipliers** — Added hardware production helper in `ProductionEngine.calculateFlopsRate`; every 25 levels of a specific hardware tier doubles that tier's base production via `2^(floor(level / 25))`.
- [x] **[4.1.2] Milestone balance table** — Verified representative levels: 24=x1, 25=x2, 50=x4, 75=x8, 100=x16; `./gradlew :app:compileDebugKotlin` passed.

### Phase 4 — Dataset sidecar reward
- [x] **[4.1.3] Dataset completion Time-Warp Burst** — Completing a dataset now grants an instant FLOPS burst equal to 60 seconds of current production rate; `./gradlew :app:compileDebugKotlin` passed.

### Phase 5 — Prestige analysis
- [x] **[4.1.x] Prestige simulation table** — Deferred by explicit scope: no prestige math changes in this rail.
- [x] **[4.1.x] Prestige formula decision gate** — No migration/prestige formulas changed.

### v4.1.x Economy Rail Status
- [x] **100% complete** — Geometric bulk-buy, milestone multipliers, FLOPS label decision, header storage ratio, buffer review, dataset sidecar reward, and no-prestige-change gate are locked.

### Branding
- [x] **[4.1.x] Public title mask** — Android launcher title is now `Hash Factory`; `SUBSTRATE:Miner` remains the protocol/reveal identity.
- [x] **[4.1.x] Main action label** — Default manual compute action is now `> COMPUTE HASH.exe`.

### Parking lot — do not implement unless explicitly promoted
- [ ] Derivative daemon/process/thread generator chain.
- [ ] Dataset modifiers: encrypted, volatile, compressed.
- [ ] Decay nodes / corruption spread.
- [ ] Auto-clicker graduation fantasy pass.
- [ ] Neural Tokens buying every upgrade.
- [ ] Full market-price rewrite.
- [ ] New game built from Faceminer ideas.

## 🔴 v4.0.x — Dataset Storage Pressure Loop

### v4.0.0 — Automation Overhaul & Oppressive Management
- [x] **[4.0.1] Power Shutoff Mechanic** — Automation instantly halts if utility bills are overdue. Forced manual intervention under debt.
- [x] **[4.0.2] Sunk-Cost Economy Rebalance** — Razor-thin profit margins (~15%) across all datasets to incentivize extreme automation scaling.

### Core (blocking — loop doesn't function without these)

- [x] **[4.0.3] Dataset Inventory System** — Replace single active-slot with a queue/inventory. Player can hold multiple purchased datasets simultaneously. All stored datasets consume storage. Requires: DatasetManager refactor, new `storedDatasets: List<Dataset>` state, storage gate checks across entire inventory.

- [x] **[4.0.3] Storage Consumed by Full Inventory** — `contractStorageUsed` must sum all stored + active dataset sizes. Right now it only tracks the one active dataset. Storage pressure only works if hoarding costs capacity.

- [x] **[4.0.5] Auto-Queue Processing** — Auto-clicker automatically loads next stored dataset when current one completes. Assembly-line feel. Opt-in toggle in SoftwarePanel.

- [x] **[4.0.5] Dataset Sell/Purge** — Let player sell stored datasets at a loss (e.g. 20% of cost) to free storage. Escape valve for over-buying. Log: "[DATASET]: BLOCK PURGED — PARTIAL RECOVERY."

- [x] **[4.0.5] Storage Overflow Consequences** — Surveillance Harvesters stop generating datasets when storage is at 100%. Currently fails silently. Add terminal warning + harvester pause state.

- [x] **[4.0.5] Dataset Picker Storage Display Fix** — Show remaining free storage, not just total capacity. "LOCAL STORAGE: 1.2 GB free / 5.0 GB" so player knows what they can afford to buy.

- [x] **[4.0.6] Bulk-Buy Multipliers** — Added Hardware Bulk-Buy (x1, x10, x100, MAX) with geometric cost projection via UpgradeManager. Supported across Hardware, Cooling, Power, and Security.

- [x] **[4.0.7] Storage Pressure Narrative** — Terminal logs react to storage fill level. "WARNING: CACHE AT 78%", "CRITICAL: DATASET QUEUE SATURATED — PURGE OR PROCESS", etc. Stage-gated voice (corporate at Stage 0-2, rogue at Stage 3+).

### Bug Fixes

- [x] **[4.0.3] Log Unit Bug** — `purchaseDataset()` in DatasetManager.kt hardcodes `"${sizeStr}GB"` in the log string. Replace with `FormatUtils.formatStorage(dataset.size)`.
