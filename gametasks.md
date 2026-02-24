# gametasks.md — SUBSTRATE:Miner Development Backlog

## 🔴 Critical (blocking)
- [ ] **Pixel Fold ADB** — Currently stable on `192.168.50.116:34175`. Check for drift.
- [x] **Security Defense Pass (v3.19.0)** — Breach scaling (Hijack/Audit/Diag/Failsafe), Integrity popups/penalties, Compliance Rating passive rep, Admin frequency tuning, Free Stage 2 firewall.
- [x] **Narrative & UI Polish Pass (v3.20.0)** — Identity-aware security nomenclature, Pac-Man chomp animation, Risk-reactive ghosts.
- [x] **UI & DevTools Polish Pass (v3.21.0)** — Reduced Header LED glow, reversed ghost chase direction, broadened audio picker mimetype.

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
- [ ] **Progress Bar/Terminal UI Enhancement Pass** — See `ROADMAP_v3.17.md` Phase A (A1–A5). Build order: v3.17.0 = A1+A2+A3, v3.17.1 = A4, v3.18.0 = A5 (oscilloscope stretch).
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
