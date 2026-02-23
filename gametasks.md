# gametasks.md — SUBSTRATE:Miner Development Backlog

## 🔴 Critical (blocking)
- [x] **Pixel Fold ADB** — Reconnected at `192.168.50.116:34663`. Fixed.

## 🟡 High (next session)
- [x] **Phase 23 Water System Fixes** — Fixed `isWaterCooling` logic (removed cryogenic/nitrogen, added chiller), added faction renames for large coolers, fixed `[RATE_LIMITED]` header status.
- [ ] **Hugging Face Papers RSS** — Fix 401 Auth error.

## 🟢 Medium
- [ ] **ExpansionLogs.kt Refactor** — Split 1164-line file into smaller modules for AI context management.
- [ ] **Admin Subnet Handle Fix** — Admin handle should be Electric Blue with effects, but text should be regular (not glitched).
- [ ] **Notification Bubble Fix** — Notification dot on main screen not clearing properly.
- [ ] **Progress Bar/Terminal UI Enhancement Pass** — Improve visual design of progress bars and command line UI elements.
- [ ] **WAL Protocol Adoption** — standardize one-line pre-delete logging in decisions/.

## ✅ Completed (v3.12.x)
- [x] **Power System Overhaul (v3.12.4-6)** — Dual-layer rails (ElectricBlue gen / Red-Yellow draw), explicit generator values, 60s billing cycle, demand charges, net metering
- [x] **HudTheme (v3.12.7)** — 8-state syntax coloring system with semantic grammar for faction/path states
- [x] **Room DB v30** — bumped for missedBillingPeriods schema change

---
*Synced from workspace tasks.md*
