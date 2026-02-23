# gametasks.md — SUBSTRATE:Miner Development Backlog

## 🔴 Critical (blocking)
- [ ] **Pixel Fold ADB** — Currently stable on `192.168.50.116:34175`. Check for drift.

## 🟡 High (next session)
- [x] **Compute Fever Quota System (v3.16.1)** — Stage 0: HASH quota for CRED survival. Personal → Global reveal via logs/chatter. Static for low headroom, +10% CRED "Signal Quality" bonus for quota beat.
- [x] **Data Log & Chatter Overhaul (v3.16.1)** — Rewrite for Compute Fever (Thorne flexes, Barnaby audits, subnet rack mania). Gate by stage.
- [ ] **Substrate Sickness System** — Terminal glitching ("Cascade Desync") when under quota. Clears on rack high/quota beat.
- [ ] **The "Snap" Effect** — UI reboot on stage cross/rack milestones.
- [ ] **Hugging Face Papers RSS** — Fix 401 Auth error.

## 🟢 Medium
- [ ] **ExpansionLogs.kt Refactor** — Split 1164-line file into smaller modules for AI context management.
- [ ] **Admin Subnet Handle Fix** — Admin handle should be Electric Blue with effects, but text should be regular (not glitched).
- [ ] **Notification Bubble Fix** — Notification dot on main screen not clearing properly.
- [ ] **Progress Bar/Terminal UI Enhancement Pass** — Improve visual design of progress bars and command line UI elements.
- [ ] **WAL Protocol Adoption** — standardize one-line pre-delete logging in decisions/.

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
