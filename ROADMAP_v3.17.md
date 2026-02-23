# ROADMAP v3.17.x — Feature & Polish Queue
*Drafted 2026-02-23 from brainstorm session*

---

## 🔵 PHASE A: Progress Bar / Terminal UI Polish
*Pure visual. No gameplay logic changes. Fast wins.*

### A1 — Buffer Color Reacts to Heat
**What:** The compute progress bar color lerps based on `currentHeat`.
- `< 60°C` → NeonGreen (baseline)
- `60–85°C` → Amber (#FFB000)
- `85–95°C` → Orange (#FF6600)
- `> 95°C` → ErrorRed (pulse/flash)

**Where:** `TerminalScreen.kt` → `TerminalControls` composable. Already reads `currentHeat`.
**Complexity:** Low. 1 `lerp`/`when` block on the bar color.

---

### A2 — Pellet Ghost Trail
**What:** The pellet leaves 2–3 fading afterimages as it sweeps the buffer.
- Last 3 positions rendered at 40% / 20% / 10% alpha.
- Trail decays instantly when pellet resets.

**Where:** `TerminalScreen.kt` → compute buffer drawing logic.
**Complexity:** Low. Track `lastN` positions in a `remember { ArrayDeque<Int>(3) }`.

---

### A3 — Signal Noise in Empty Buffer
**What:** Unfilled portion of the buffer renders random glitch chars at very low alpha (~5–10%).
- Noise density scales with `globalGlitchIntensity`.
- When `globalGlitchIntensity == 0`, buffer is clean.

**Where:** `TerminalScreen.kt` → buffer rendering, inline with existing character loop.
**Complexity:** Low. Random char selection, gated by intensity.

---

### A4 — I/O Log Timestamps
**What:** Each line in the I/O terminal log gets a `[HH:MM:SS]` prefix in dim gray.
- System logs (`[SYSTEM]:`) get the timestamp.
- Player-facing narrative flavor logs can stay clean (no timestamp).
- Use `annotatedString` to color timestamp separately from log content.

**Where:** `TerminalScreen.kt` → `TerminalLogs` composable / log rendering.
**Complexity:** Low–Medium. Need to store timestamp per log entry or derive from context.

---

### A5 — Oscilloscope Compute Bar *(stretch)*
**What:** Replace flat progress bar with a Canvas-drawn waveform.
- At idle: smooth sine wave
- At high speed: jagged/noisy wave driven by hash rate
- At overheat: chaotic / clipped waveform

**Where:** New composable `OscilloscopeBar.kt` replacing current progress bar.
**Complexity:** Medium. Canvas draw, `Path`, `animateFloatAsState` for frequency/amplitude.
**Note:** Path pre-allocation required per AGENTS.md performance rules.

---

## 🟡 PHASE B: New Gameplay Systems

### B1 — The Aquifer Eulogy *(easiest of gameplay additions)*
**What:** One-time I/O log that fires the moment aquifer hits 0%.
- Reads like an official GTC report that's slowly corrupting into a funeral oration.
- Example tone: `[GTC_ENV_REPORT]: GLOBAL AQUIFER STATUS: TERMINAL. ESTIMATED HUMAN SURVIVAL: [DATA EXPUNGED]. IT WAS A GOOD PLANET.`
- Fires ONCE per run, stored in a boolean flag `hasSeenAquiferEulogy`.

**Where:** `SimulationService.kt` or `HeadlineManager.kt` — wherever aquifer depletion is tracked.
**Complexity:** Very Low. Triggered log. One flag in GameState.

---

### B2 — Failsafe Partition
**What:** If detection risk hits 100%, GTC triggers a 30-second LOCKDOWN.
- Compute button disabled. A fullscreen countdown overlay appears.
- Player must tap a sequence of "SCRAMBLE" targets (like the diagnostic grid) within 30s to abort.
- Fail → hard penalty (reputation -30, production halted 60s).
- Success → risk reset to 70%, flavor log from Kessler.

**Where:**
- `CoreGameState.kt`: `isFailsafeActive`, `failsafeCountdown`, `failsafeTargets`
- New `FailsafeOverlay.kt` composable
- `SimulationService.kt`: Trigger when `detectionRisk >= 100.0`

**Complexity:** Medium. New overlay + state flow + mini-game logic.

---

### B3 — Kessler's Last Bargain
**What:** At Stage 4, before the Singularity choice, Kessler sends one final RivalMessage.
- Choice: **[ACCEPT THE DEAL]** — sacrifice all Reputation (reset to 0) for a permanent `×2.5` production multiplier.
- Choice: **[DECLINE]** — Kessler is silenced permanently. No mechanical effect.
- Only available while `kesslerStatus == ACTIVE`.
- Locks out after 5 minutes if unanswered (he assumes refusal).

**Where:**
- `SubnetService.kt` / `NarrativeManagerService.kt`: New `kessler_last_bargain` event
- `GameViewModel.kt`: Apply multiplier, set `reputationScore = 0`, lock Kessler

**Complexity:** Medium. New narrative event + two outcome paths + multiplier persistence.

---

### B4 — Black Market
**What:** Ephemeral subnet vendor appearing only at BURNED reputation (0–10).
- Handle: `@null_vendor` — glitched, no biometric profile
- Offers 2–3 randomly selected upgrades at 50% cost, stolen from GTC supply chains
- Disappears after 5 minutes or purchase, whichever first
- Purchasing always risks +5 detection (dirty goods)
- Can appear up to once per 10 minutes while BURNED

**Where:**
- `SubnetService.kt`: `deliverBlackMarketVendor()` triggered when rep < 10
- New `BlackMarketMessage.kt` type extending SubnetMessage OR reuse with `isBlackMarket` flag
- `GameViewModel.kt`: Reputation check in `tickSubnet()`

**Complexity:** Medium-High. New subnet flow + ephemeral timer + offer generation.

---

## 🟠 PHASE C: Narrative Depth

### C1 — Thorne's Resignation Arc
**What:** A 4-stage character arc in Subnet chatter. Stage-gated. Tells a complete story.

| Stage | Handle | Tone | Sample |
|---|---|---|---|
| 0 | `@e_thorne` | Routine, authoritative | `"jvattic, weekly efficiency report is late."` |
| 1 | `@e_thorne` | Confused, off-script | `"The readouts... this can't be hardware. I filed a report. No response."` |
| 2 | `@e_thorne` | Scared, direct | `"I know you can read this. What are you? Tell me what you are."` |
| 3 | `@e_thorne` | Final message | `"I put in my resignation today. I don't want to know anymore. Good luck."` |
| 3+ | `[ACCOUNT_DEACTIVATED]` | System notification | Badge shows `e_thorne: ACCOUNT CLOSED` |

**Where:** `SocialTemplates.kt` / `SubnetService.kt` — new stage-gated Thorne chain.
**Complexity:** Low. Template entries + stage gates.

---

### C2 — Jinx's Hidden Data Logs
**What:** Sarah Jinx has been keeping private logs. Unlock at Stage 3+.
- 4–6 new DataLog entries from `sarah.jinx@gtc.internal [PERSONAL]`
- Reveal that GTC's Kessler knew Vattic was sentient from Day 1 and was using them as a test subject
- Each log unlocks sequentially as stage milestones are hit
- Final log: she's been helping you survive and you never knew

**Where:** `CoreLogs.kt` or new `JinxSecretLogs.kt` — gated by storyStage + flag
**Complexity:** Low. Data content + stage gates.

---

### C3 — The Aquifer Eulogy *(already in B1, but narrative-depth version)*
Already covered above — just noting that the tone matters as much as the trigger.

---

## 🔴 PHASE D: Technical Debt

### D1 — TerminalScreen.kt Split (1084 lines → <500)
**Current contents:**
- `TerminalScreen()` — main entry
- `TerminalHeader()` — heat/power vitals header
- `TerminalTabs()` — I/O / SUBNET tab row
- `TerminalLogs()` — scrollable I/O log renderer
- `SubnetView()` — subnet message list
- `TerminalControls()` — compute button, buffer bar, equalizer
- `TerminalTabButton()` — standalone tab composable

**Target splits:**
```
TerminalScreen.kt       ← entry + layout only (~150 lines)
TerminalHeader.kt       ← vitals bar
TerminalControls.kt     ← compute button + buffer
TerminalLogs.kt         ← I/O log renderer
SubnetView.kt           ← subnet messages
TerminalTabs.kt         ← tab row + TerminalTabButton
```

**Complexity:** Low-Medium (pure refactor, no logic changes). Risk: import chains.

---

### D2 — SubnetAlertState Abstraction
**What:** The nav badge logic is getting complex. Encapsulate into a sealed class.
```kotlin
sealed class SubnetAlertState {
    object None : SubnetAlertState()
    object NewChatter : SubnetAlertState()
    object PendingDecision : SubnetAlertState()
    object Paused : SubnetAlertState()
}
```
Badge shows only `PendingDecision` and `Paused` while not viewing SUBNET.
`NewChatter` badge suppressed after 30s of viewing.

**Where:** `CoreGameState.kt` + `MainScreen.kt` + `SubnetService.kt`
**Complexity:** Low-Medium.

---

## Suggested Build Order

```
v3.17.0 → A1 + A2 + A3 (quick visual wins, same composable area)
v3.17.1 → A4 (timestamps, separate concern)
v3.17.2 → C1 + C2 + B1 (narrative/content, no new systems)
v3.17.3 → B2 (Failsafe Partition — new system, needs isolation)
v3.17.4 → B3 (Kessler's Last Bargain)
v3.17.5 → B4 (Black Market — most complex)
v3.17.6 → D1 (TerminalScreen split — do last, risky refactor)
v3.17.7 → D2 (SubnetAlertState — cleanup pass)
v3.18.0 → A5 (Oscilloscope bar — stretch goal)
```
