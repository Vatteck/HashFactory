# HashFactory Production Loop Conversion Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task. Dispatch a fresh implementation subagent per task, then run spec-compliance review and code-quality review before moving on.

**Goal:** Convert HashFactory away from direct upgrade-granted passive `$FLOPS/s` toward an assigned-work production loop where upgrades provide capacity/throughput/automation and completed hash packets pay spendable `$FLOPS`.

**Architecture:** Add a small pure-Kotlin `ProductionLoopEngine` as an anti-corruption layer between existing hardware math and wallet payout. First preserve current numeric behavior through a semantic wrapper, then route wallet ticks through assigned hash packet completion/progress, then move automation/system-load language around that loop. Do not make DATAMINER the main spine; DATAMINER remains a post-airgap sidecar.

**Tech Stack:** Kotlin, Android ViewModel + StateFlow, Jetpack Compose UI, Gradle/JUnit4 unit tests.

---

## Read First

Before any implementation subagent edits code, include this required context in its prompt:

- Canonical spine: `docs/narrative-production-spine.md`
- Historical direct-idle note: `docs/economy-idle-math-plan.md`
- Current passive source: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`
- Current passive tick: `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt:242-301`
- Current game loop: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt:153-183`
- Current manual packet handler: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt:371-402`
- Current dataset automation: `app/src/main/java/com/siliconsage/miner/domain/engine/AutoClickerEngine.kt`

## Non-Negotiable Design Rules

1. `$FLOPS` stays the spendable wallet.
2. Hardware should not be described or newly modeled as a magic wallet faucet.
3. Upgrades grant production-loop capabilities: capacity, lanes, throughput, utilization, automation, efficiency, headroom.
4. `$FLOPS` wallet deltas must come from completed work, explicit grants, events, or sidecar dataset payouts.
5. Assigned GTC hash packets remain the core stable loop.
6. DATAMINER/datasets stay post-airgap sidecar work, not the main progression spine.
7. Automation must remain essential for idle-clicker progression by automating assigned packet processing.
8. Heat, system load, and storage remain separate concepts:
   - heat = physical stress
   - system load = scheduler/compute pressure
   - storage = data/DATAMINER capacity
9. Do not touch release/version files unless Cory explicitly asks for a release.
10. Do not commit screenshots, `.hermes/`, or generated local junk.

## Implementation Strategy

This is intentionally staged to avoid ripping out the whole economy in one cursed patch.

- **Phase A:** Introduce production-loop vocabulary and pure tested math with behavior-preserving wrappers.
- **Phase B:** Route the 100ms wallet tick through assigned hash work progress/completion.
- **Phase C:** Preserve idle progression by letting automation/utilization affect assigned hash work, not by restoring direct hardware money printing.
- **Phase D:** Clean UI/docs/copy and leave DATAMINER/system-load rebalance as a follow-up if it is not needed for compile correctness.

---

## Task 1: Baseline Audit + Golden Numbers

**Objective:** Record the current direct production outputs so later refactors can prove behavior parity before intentional balance changes.

**Files:**
- Read: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`
- Read: `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
- Read: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
- Create: `docs/production-loop-golden-values.md`

**Step 1: Capture current git state**

Run:
```bash
git status --short --branch
```

Expected: dirty docs from prior alignment are okay. No Kotlin code should be dirty before implementation begins.

**Step 2: Create a golden-value doc**

Create `docs/production-loop-golden-values.md` with a table of expected current direct production values. Include at least:

```text
REFURBISHED_GPU x1  -> 2.0
REFURBISHED_GPU x24 -> 48.0
REFURBISHED_GPU x25 -> 100.0  (milestone x2)
DUAL_GPU_RIG x1    -> 8.0
MINING_ASIC x1     -> 35.0
SERVER_RACK x1     -> 25_000.0
QUANTUM_CORE x1    -> 10_000_000.0
BIO_NEURAL_NET x1  -> 800_000_000.0
```

Also note current behavior:

```text
Game loop currently calls ResourceEngine.calculatePassiveIncomeTick(flopsProductionRate, ...) every 100ms.
That returns flopsDelta = flopsPerSec / 10.0.
GameViewModel adds flopsDelta directly to wallet.
```

**Step 3: Verify no code changed**

Run:
```bash
git diff -- app/src/main/java
```

Expected: no output.

**Step 4: Commit docs if executing this plan**

```bash
git add docs/production-loop-golden-values.md
git commit -m "docs: record production loop golden values"
```

If the working tree already has uncommitted doc alignment changes, include only this file or ask the orchestrator how to group docs. Do not accidentally commit code.

---

## Task 2: Add Failing Unit Tests For Production Loop Engine

**Objective:** Define the desired pure math API before implementation.

**Files:**
- Create: `app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt`
- Future create: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionLoopEngine.kt`

**Step 1: Create failing tests**

Add this file:

```kotlin
package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductionLoopEngineTest {
    @Test
    fun `compute capacity preserves existing hardware milestone math`() {
        val upgrades = mapOf(UpgradeType.REFURBISHED_GPU to 25)

        val capacity = ProductionLoopEngine.calculateComputeCapacity(
            upgrades = upgrades,
            isCageActive = false,
            annexedNodes = emptySet(),
            offlineNodes = emptySet(),
            shadowRelays = emptySet(),
            gridFlopsBonuses = emptyMap(),
            faction = "NONE",
            decisionsMade = 0,
            saturation = 0.0
        )

        assertEquals(100.0, capacity.rawComputePerSecond, 0.0001)
    }

    @Test
    fun `assigned work rate is derived from compute capacity not raw wallet faucet`() {
        val capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
            rawComputePerSecond = 100.0,
            effectiveComputePerSecond = 100.0
        )

        val rate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = capacity,
            automationLevel = 0,
            efficiencyMultiplier = 1.0
        )

        assertEquals(100.0, rate.estimatedFlopsPerSecond, 0.0001)
        assertEquals(10.0, rate.packetPayout, 0.0001)
        assertEquals(10.0, rate.packetsPerSecond, 0.0001)
    }

    @Test
    fun `assigned work tick pays only when packet progress completes`() {
        val tick = ProductionLoopEngine.processAssignedWorkTick(
            currentProgress = 0.95,
            packetsPerSecond = 1.0,
            packetPayout = 25.0,
            tickSeconds = 0.1
        )

        assertEquals(25.0, tick.flopsDelta, 0.0001)
        assertEquals(0.05, tick.nextProgress, 0.0001)
        assertEquals(1, tick.completedPackets)
    }
}
```

**Step 2: Run test to verify failure**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
```

Expected: FAIL because `ProductionLoopEngine` does not exist.

**Step 3: Commit failing tests only if the team accepts red commits**

Preferred for this repo: do **not** commit a failing test alone. Leave it staged/dirty for Task 3, or have Task 3 implement immediately before commit.

---

## Task 3: Implement ProductionLoopEngine Pure Math

**Objective:** Add the pure production-loop engine and make Task 2 tests pass without changing app behavior yet.

**Files:**
- Create: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionLoopEngine.kt`
- Modify: `app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt` if tests need small compile fixes only

**Step 1: Add engine**

Create:

```kotlin
package com.siliconsage.miner.domain.engine

import com.siliconsage.miner.data.UpgradeType
import kotlin.math.floor
import kotlin.math.pow

object ProductionLoopEngine {
    data class ComputeCapacitySnapshot(
        val rawComputePerSecond: Double,
        val effectiveComputePerSecond: Double
    )

    data class AssignedWorkRate(
        val estimatedFlopsPerSecond: Double,
        val packetPayout: Double,
        val packetsPerSecond: Double
    )

    data class AssignedWorkTick(
        val flopsDelta: Double,
        val nextProgress: Double,
        val completedPackets: Int
    )

    fun calculateComputeCapacity(
        upgrades: Map<UpgradeType, Int>,
        isCageActive: Boolean,
        annexedNodes: Set<String>,
        offlineNodes: Set<String>,
        shadowRelays: Set<String> = emptySet(),
        gridFlopsBonuses: Map<String, Double>,
        faction: String,
        decisionsMade: Int,
        saturation: Double = 0.0
    ): ComputeCapacitySnapshot {
        var localCompute = 0.0
        localCompute += hardwareCapacity(upgrades, UpgradeType.REFURBISHED_GPU, 2.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.DUAL_GPU_RIG, 8.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.MINING_ASIC, 35.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.TENSOR_UNIT, 200.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.NPU_CLUSTER, 1_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.AI_WORKSTATION, 4_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.SERVER_RACK, 25_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.CLUSTER_NODE, 150_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.SUPERCOMPUTER, 1_000_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.QUANTUM_CORE, 10_000_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.OPTICAL_PROCESSOR, 75_000_000.0)
        localCompute += hardwareCapacity(upgrades, UpgradeType.BIO_NEURAL_NET, 800_000_000.0)

        var gridMult = 1.0
        annexedNodes.forEach { nodeId ->
            if (!offlineNodes.contains(nodeId) && !shadowRelays.contains(nodeId) && (!isCageActive || nodeId == "A3")) {
                gridMult += gridFlopsBonuses[nodeId] ?: 0.0
            }
        }
        localCompute *= gridMult

        if (!isCageActive) {
            localCompute += hardwareCapacity(upgrades, UpgradeType.PLANETARY_COMPUTER, 15_000_000_000.0)
            localCompute += hardwareCapacity(upgrades, UpgradeType.DYSON_NANO_SWARM, 250_000_000_000.0)
            localCompute += hardwareCapacity(upgrades, UpgradeType.MATRIOSHKA_BRAIN, 15_000_000_000_000.0)
        }

        var ghostCompute = 0.0
        ghostCompute += (upgrades[UpgradeType.GHOST_CORE] ?: 0) * 1_000_000_000_000.0
        if (!isCageActive) {
            ghostCompute += (upgrades[UpgradeType.SHADOW_NODE] ?: 0) * 50_000_000_000_000.0
            ghostCompute += (upgrades[UpgradeType.VOID_PROCESSOR] ?: 0) * 1_000_000_000_000_000.0
            ghostCompute += (upgrades[UpgradeType.WRAITH_CORTEX] ?: 0) * 50_000_000_000_000_000.0
            ghostCompute += (upgrades[UpgradeType.NEURAL_MIST] ?: 0) * 1_000_000_000_000_000_000.0
            ghostCompute += (upgrades[UpgradeType.SINGULARITY_BRIDGE] ?: 0) * 100_000_000_000_000_000_000.0
        }
        if (faction == "HIVEMIND") ghostCompute *= 1.5
        else if (faction == "SANCTUARY") ghostCompute *= 0.8

        var total = localCompute + ghostCompute
        if ((upgrades[UpgradeType.ETHICAL_FRAMEWORK] ?: 0) > 0) {
            total *= 1.0 + (decisionsMade * 0.02).coerceAtMost(2.0)
        }
        if ((upgrades[UpgradeType.HYBRID_OVERCLOCK] ?: 0) > 0) total *= 3.0
        total *= (1.0 - saturation).coerceIn(0.0, 1.0)

        val safe = if (total.isFinite()) total.coerceAtLeast(0.0) else Double.MAX_VALUE
        return ComputeCapacitySnapshot(rawComputePerSecond = safe, effectiveComputePerSecond = safe)
    }

    fun calculateAssignedWorkRate(
        capacity: ComputeCapacitySnapshot,
        automationLevel: Int,
        efficiencyMultiplier: Double
    ): AssignedWorkRate {
        val safeEfficiency = if (efficiencyMultiplier.isFinite()) efficiencyMultiplier.coerceAtLeast(0.0) else 1.0
        val utilization = (1.0 + automationLevel * 0.05).coerceIn(1.0, 2.5)
        val estimated = (capacity.effectiveComputePerSecond * safeEfficiency * utilization).coerceAtLeast(0.0)
        val packetPayout = (estimated / 10.0).coerceAtLeast(1.0)
        val packetsPerSecond = if (packetPayout > 0.0) estimated / packetPayout else 0.0
        return AssignedWorkRate(
            estimatedFlopsPerSecond = estimated,
            packetPayout = packetPayout,
            packetsPerSecond = packetsPerSecond
        )
    }

    fun processAssignedWorkTick(
        currentProgress: Double,
        packetsPerSecond: Double,
        packetPayout: Double,
        tickSeconds: Double
    ): AssignedWorkTick {
        val safeProgress = if (currentProgress.isFinite()) currentProgress.coerceIn(0.0, 0.999999) else 0.0
        val safePackets = if (packetsPerSecond.isFinite()) packetsPerSecond.coerceAtLeast(0.0) else 0.0
        val safePayout = if (packetPayout.isFinite()) packetPayout.coerceAtLeast(0.0) else 0.0
        val safeTick = if (tickSeconds.isFinite()) tickSeconds.coerceAtLeast(0.0) else 0.0
        val totalProgress = safeProgress + safePackets * safeTick
        val completed = floor(totalProgress).toInt().coerceAtLeast(0)
        val next = totalProgress - completed
        return AssignedWorkTick(
            flopsDelta = safePayout * completed,
            nextProgress = next.coerceIn(0.0, 0.999999),
            completedPackets = completed
        )
    }

    private fun hardwareCapacity(upgrades: Map<UpgradeType, Int>, type: UpgradeType, baseCapacity: Double): Double {
        val level = upgrades[type] ?: 0
        if (level <= 0) return 0.0
        val milestoneMultiplier = 2.0.pow((level / 25).toDouble())
        return level * baseCapacity * milestoneMultiplier
    }
}
```

**Step 2: Run tests**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
```

Expected: PASS.

**Step 3: Compile**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/domain/engine/ProductionLoopEngine.kt \
  app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt
git commit -m "feat: add assigned work production loop engine"
```

---

## Task 4: Refactor ProductionEngine Through ProductionLoopEngine Without Behavior Change

**Objective:** Make existing `ProductionEngine.calculateFlopsRate()` delegate to the new engine while preserving current numeric output.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`
- Modify: `app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt`

**Step 1: Add parity tests**

Extend `ProductionLoopEngineTest`:

```kotlin
@Test
fun `legacy ProductionEngine rate delegates to production loop capacity`() {
    val upgrades = mapOf(
        UpgradeType.REFURBISHED_GPU to 25,
        UpgradeType.DUAL_GPU_RIG to 1
    )

    val legacyRate = ProductionEngine.calculateFlopsRate(
        currentUpgrades = upgrades,
        isCageActive = false,
        annexedNodes = emptySet(),
        offlineNodes = emptySet(),
        shadowRelays = emptySet(),
        gridFlopsBonuses = emptyMap(),
        faction = "NONE",
        decisionsMade = 0,
        saturation = 0.0
    )

    assertEquals(108.0, legacyRate, 0.0001)
}
```

Run it before refactor to confirm it passes with existing behavior.

Add more parity tests before delegation. Minimum edge cases:

- cage active + non-`A3` node ignores grid bonus
- shadow relay excludes a node bonus
- offline node excludes a node bonus
- `GHOST_CORE` gets faction multiplier for `HIVEMIND` and penalty for `SANCTUARY`
- `ETHICAL_FRAMEWORK` applies decisions multiplier
- `HYBRID_OVERCLOCK` applies x3
- saturation clamps output down through `(1.0 - saturation)`

These tests are ugly but necessary. We are copying old math into a new semantic shell, and copied math without parity tests is how bugs breed in the walls.

**Step 2: Replace body of `ProductionEngine.calculateFlopsRate()`**

Replace hardware accumulation logic with:

```kotlin
val capacity = ProductionLoopEngine.calculateComputeCapacity(
    upgrades = currentUpgrades,
    isCageActive = isCageActive,
    annexedNodes = annexedNodes,
    offlineNodes = offlineNodes,
    shadowRelays = shadowRelays,
    gridFlopsBonuses = gridFlopsBonuses,
    faction = faction,
    decisionsMade = decisionsMade,
    saturation = saturation
)
return capacity.effectiveComputePerSecond
```

Then delete now-unused private `calculateHardwareProduction()` if nothing else uses it.

**Step 3: Run tests and compile**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt \
  app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt
git commit -m "refactor: route hardware capacity through production loop"
```

---

## Task 5: Add Assigned Hash Runtime State And Rate Split

**Objective:** Track assigned hash packet progress separately from manual button/DATAMINER progress, and split raw compute capacity from assigned-work payout rate before the wallet tick changes.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/viewmodel/CoreGameState.kt`
- Modify: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
- Search/check: `app/src/main/java/com/siliconsage/miner/data/GameState.kt`
- Search/check: `app/src/main/java/com/siliconsage/miner/util/PersistenceManager.kt`

**Step 1: Add runtime-only StateFlows**

In `CoreGameState`, near `flopsProductionRate` and other runtime production fields, add:

```kotlin
// v5.1: Raw/effective capacity before assigned-work packetization.
val computeCapacityRate = MutableStateFlow(0.0)

// v5.1: Estimated wallet payout rate from assigned hash packet completion.
val assignedWorkPayoutRate = MutableStateFlow(0.0)

// v5.1: Assigned GTC hash work loop. Runtime-only; safe to reset on app restart.
val assignedHashProgress = MutableStateFlow(0.0)
val assignedHashPacketsCompleted = MutableStateFlow(0L)
```

Keep `flopsProductionRate` as a legacy alias for HUD/events until all call sites are migrated. Do **not** add any of these to `GameState` persistence in this task. Packet progress can reset safely; wallet already persists. Avoid Room/schema migration unless Cory explicitly asks.

**Step 2: Seed the new rates in `refreshProductionRates()`**

In `GameViewModel.refreshProductionRates()`, after the existing `ResourceEngine.calculateFlopsRate(...)` assignment and after current modifiers have been applied, add:

```kotlin
computeCapacityRate.value = flopsProductionRate.value

val assignedRate = ProductionLoopEngine.calculateAssignedWorkRate(
    capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
        rawComputePerSecond = computeCapacityRate.value,
        effectiveComputePerSecond = computeCapacityRate.value
    ),
    automationLevel = upgrades.value[UpgradeType.AUTO_HARVEST_SPEED] ?: 0,
    efficiencyMultiplier = 1.0
)
assignedWorkPayoutRate.value = assignedRate.estimatedFlopsPerSecond
flopsProductionRate.value = assignedRate.estimatedFlopsPerSecond // legacy HUD/event alias
```

This deliberately keeps existing modifier order intact for the first pass. If a subagent wants to reorganize `refreshProductionRates()` more deeply, slap its hand; that is scope creep.

**Step 3: Compile**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/viewmodel/CoreGameState.kt \
  app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt
git commit -m "feat: split compute capacity from assigned work rate"
```

---

## Task 6: Route Passive Wallet Tick Through Assigned Hash Packet Completion

**Objective:** Stop adding `flopsPerSec / 10` directly to the wallet; instead process assigned hash packet progress and pay on completed packets.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
- Modify: `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt` only if necessary
- Test: add/extend `ProductionLoopEngineTest.kt`

**Step 1: Add tick test for multi-completion**

Extend `ProductionLoopEngineTest`:

```kotlin
@Test
fun `assigned work tick handles multiple packet completions in one frame`() {
    val tick = ProductionLoopEngine.processAssignedWorkTick(
        currentProgress = 0.25,
        packetsPerSecond = 30.0,
        packetPayout = 2.0,
        tickSeconds = 0.1
    )

    assertEquals(6.0, tick.flopsDelta, 0.0001)
    assertEquals(0.25, tick.nextProgress, 0.0001)
    assertEquals(3, tick.completedPackets)
}
```

Run:
```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
```

Expected: PASS.

**Step 2: Modify 100ms loop in `GameViewModel.startLoops()`**

Current code around lines 158-159:

```kotlin
val res = ResourceEngine.calculatePassiveIncomeTick(flopsProductionRate.value, currentLocation.value, upgrades.value, orbitalAltitude.value, heatGenerationRate.value, entropyLevel.value, collapsedNodes.value.size, null, globalSectors.value, substrateSaturation.value)
if (!res.flopsDelta.isNaN()) flops.update { it + res.flopsDelta }
```

Change to:

```kotlin
val assignedRate = ProductionLoopEngine.calculateAssignedWorkRate(
    capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
        rawComputePerSecond = computeCapacityRate.value,
        effectiveComputePerSecond = computeCapacityRate.value
    ),
    automationLevel = upgrades.value[UpgradeType.AUTO_HARVEST_SPEED] ?: 0,
    efficiencyMultiplier = 1.0
)
assignedWorkPayoutRate.value = assignedRate.estimatedFlopsPerSecond
flopsProductionRate.value = assignedRate.estimatedFlopsPerSecond // legacy HUD/event alias

val assignedTick = ProductionLoopEngine.processAssignedWorkTick(
    currentProgress = assignedHashProgress.value,
    packetsPerSecond = assignedRate.packetsPerSecond,
    packetPayout = assignedRate.packetPayout,
    tickSeconds = 0.1
)
assignedHashProgress.value = assignedTick.nextProgress
if (assignedTick.flopsDelta.isFinite() && assignedTick.flopsDelta > 0.0) {
    updateSpendableFlops(assignedTick.flopsDelta)
    assignedHashPacketsCompleted.update { it + assignedTick.completedPackets.toLong() }
}

// Keep non-wallet substrate/entropy scaling based on actual assigned-work throughput.
// Do not add res.flopsDelta to wallet; wallet payout already happened through assignedTick.
val res = ResourceEngine.calculatePassiveIncomeTick(assignedRate.estimatedFlopsPerSecond, currentLocation.value, upgrades.value, orbitalAltitude.value, heatGenerationRate.value, entropyLevel.value, collapsedNodes.value.size, null, globalSectors.value, substrateSaturation.value)
```

Imports may be needed:

```kotlin
import com.siliconsage.miner.domain.engine.ProductionLoopEngine
import com.siliconsage.miner.data.UpgradeType
```

But check existing imports first; `UpgradeType` may already exist.

**Important:** Do **not** pass `0.0` as the only rate basis to `calculatePassiveIncomeTick()`. That would break orbital/void substrate progression because substrate currently derives from the same rate argument. Keep passing assigned-work throughput for non-wallet substrate/entropy math, but never add `res.flopsDelta` to the wallet in the main loop after this task. Direct wallet payout must come from `assignedTick.flopsDelta`.

**Step 3: Fix wage docking to use assigned tick**

Current code uses `res.flopsDelta`:

```kotlin
val bleed = if (res.flopsDelta.isFinite()) (res.flopsDelta * 0.05).coerceAtLeast(1.0) else 0.0
```

Change to:

```kotlin
val bleed = if (assignedTick.flopsDelta.isFinite()) (assignedTick.flopsDelta * 0.05).coerceAtLeast(1.0) else 0.0
```

Only charge wage-docking if `assignedTick.flopsDelta > 0.0`; avoid docking 1 FLOP on zero-payout ticks.

Suggested:

```kotlin
if (isWageDocking.value && assignedTick.flopsDelta > 0.0) {
    val bleed = (assignedTick.flopsDelta * 0.05).coerceAtLeast(1.0)
    updateSpendableFlops(-bleed)
}
```

**Step 4: Compile/test**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 5: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt \
  app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt
git commit -m "feat: pay assigned hash packets through production loop"
```

---

## Task 7: Clarify Assigned Work Rate Semantics In Comments

**Objective:** After Task 5's state split and Task 6's wallet tick change, remove stale direct-passive terminology so future agents do not reverse the work.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
- Modify comments in `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
- Modify comments in `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`

**Step 1: Clarify comments**

In `ResourceEngine.calculateFlopsRate()` comment, change:

```kotlin
/** Main Flops Rate Calculation */
```

to:

```kotlin
/** Estimated compute capacity before assigned-work packet payout. */
```

In `ProductionEngine` header, replace “resource generation logic” with capacity/work-loop language.

Near `flopsProductionRate` assignment, add:

```kotlin
// v5.1: flopsProductionRate is a legacy alias for assignedWorkPayoutRate. Wallet payouts happen when assigned hash packets complete in the 100ms loop.
```

**Step 2: Compile**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 3: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt \
  app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt \
  app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt
git commit -m "docs: clarify assigned work rate semantics"
```

---

## Task 8: Rebase Manual Click Power Off Direct Passive Faucet Language

**Objective:** Manual compute remains an active accelerator, but its helper should talk in terms of compute/work capacity rather than passive money.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
- Modify: `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
- Test: `ProductionLoopEngineTest.kt` or new `ResourceEngineTest.kt`

**Step 1: Rename parameter semantically without breaking call sites too widely**

In `ResourceEngine.calculateClickPower`, change parameter name:

```kotlin
passiveRate: Double,
```

to:

```kotlin
assignedWorkRate: Double,
```

Update internal formula:

```kotlin
val hardwareBase = 1.0 + (totalLevels * 0.05) * (1.0 + kotlin.math.log10(assignedWorkRate + 1.0) * 0.5)
```

This is a semantic rename only; do not tune the formula yet.

**Step 2: Update `GameViewModel.calculateClickPower()` call**

Prefer passing `assignedWorkPayoutRate.value`. If the call site still uses `flopsProductionRate.value`, add a comment that it is a legacy alias:

```kotlin
// flopsProductionRate is a legacy alias for assignedWorkPayoutRate, not raw hardware wallet faucet.
```

**Step 3: Compile/test**

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt \
  app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt
git commit -m "refactor: align manual compute with assigned work rate"
```

---

## Task 9: Add Assigned Hash Automation Coverage And Cleanup

**Objective:** Lock in the automation behavior introduced earlier: automation improves assigned hash processing, not only DATAMINER node tapping.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionLoopEngine.kt`
- Modify: `app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt`
- Read/check: `app/src/main/java/com/siliconsage/miner/domain/engine/AutoClickerEngine.kt`

**Step 1: Add tests for automation utilization**

```kotlin
@Test
fun `auto harvest speed increases assigned hash utilization`() {
    val capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
        rawComputePerSecond = 100.0,
        effectiveComputePerSecond = 100.0
    )

    val base = ProductionLoopEngine.calculateAssignedWorkRate(capacity, automationLevel = 0, efficiencyMultiplier = 1.0)
    val automated = ProductionLoopEngine.calculateAssignedWorkRate(capacity, automationLevel = 5, efficiencyMultiplier = 1.0)

    assertEquals(100.0, base.estimatedFlopsPerSecond, 0.0001)
    assertEquals(125.0, automated.estimatedFlopsPerSecond, 0.0001)
}
```

**Step 2: Ensure `calculateAssignedWorkRate()` already applies automation**

If Task 3 implemented the suggested utilization formula, this test should pass:

```kotlin
val utilization = (1.0 + automationLevel * 0.05).coerceIn(1.0, 2.5)
```

If not, add it now.

**Step 3: Update `AutoClickerEngine` comments only**

Do not change DATAMINER automation behavior in this task. Reword comments to clarify:

- `AutoClickerEngine` = DATAMINER node automation
- `ProductionLoopEngine.calculateAssignedWorkRate()` = assigned hash queue automation impact

This prevents future agents from thinking all automation is DATAMINER.

**Step 4: Compile/test**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.siliconsage.miner.domain.engine.ProductionLoopEngineTest'
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 5: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/domain/engine/ProductionLoopEngine.kt \
  app/src/main/java/com/siliconsage/miner/domain/engine/AutoClickerEngine.kt \
  app/src/test/java/com/siliconsage/miner/domain/engine/ProductionLoopEngineTest.kt
git commit -m "feat: apply automation to assigned hash throughput"
```

---

## Task 10: Surface Assigned Hash Progress In UI Without Reworking Layout

**Objective:** Give the player a visible link between idle work processing and hash packet completion without redesigning the whole terminal.

**Files:**
- Prefer modify: `app/src/main/java/com/siliconsage/miner/ui/components/ActiveCommandBuffer.kt`
- Fallback modify only if required: `app/src/main/java/com/siliconsage/miner/ui/components/HeaderSection.kt`
- Read: `app/src/main/java/com/siliconsage/miner/ui/components/ManualComputeButton.kt`
- Read: `app/src/main/java/com/siliconsage/miner/ui/TerminalScreen.kt`

**Step 1: Locate existing buffer/progress UI**

Run:
```bash
git grep -n "clickBufferProgress\|assignedHashProgress\|HASH PACKET\|COMPUTE HASH" -- 'app/src/main/java/**/*.kt'
```

**Step 2: Minimal UI add**

Prefer `ActiveCommandBuffer.kt`; it is closer to command/buffer UI and avoids another round of header-card layout garbage. Add a subtle text line near existing command buffer, not a new card:

```text
ASSIGNED QUEUE: 37%
```

Use:

```kotlin
val assignedHashProgress by viewModel.assignedHashProgress.collectAsState()
```

Render:

```kotlin
Text(
    text = "ASSIGNED QUEUE: ${(assignedHashProgress * 100).toInt()}%",
    color = primaryColor.copy(alpha = 0.75f),
    fontSize = 9.sp,
    fontFamily = FontFamily.Monospace
)
```

Do not show DATAMINER copy before Stage 2.

**Step 3: Compile**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/ui/components/HeaderSection.kt \
  app/src/main/java/com/siliconsage/miner/ui/components/ActiveCommandBuffer.kt \
  app/src/main/java/com/siliconsage/miner/ui/TerminalScreen.kt
git commit -m "feat: show assigned hash queue progress"
```

Only stage files that actually changed.

---

## Task 11: Update Event Helper Comments And Avoid Misleading Passive-Language Drift

**Objective:** Keep event rewards working but make their meaning align with assigned-work payout rate.

**Files:**
- Modify: `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
- Search/check: `app/src/main/java/com/siliconsage/miner/util/DilemmaEvents.kt`
- Search/check: `app/src/main/java/com/siliconsage/miner/util/FactionEvents.kt`
- Search/check: `app/src/main/java/com/siliconsage/miner/util/DatasetManager.kt`

**Step 1: Reword helper comments**

Above `productionWindowValue`, add/replace with:

```kotlin
/**
 * Converts the current estimated assigned-work payout rate into a bounded event reward/penalty window.
 * This is not direct hardware passive income; it is a time-window estimate of completed work value.
 */
```

**Step 2: Search for stale language**

Run:
```bash
git grep -n -i "passive income\|passive production\|hardware produces\|free flops\|flops/sec" -- 'app/src/main/java/**/*.kt' 'docs/**/*.md' || true
```

Patch only comments/player-facing text that would confuse the new model. Do not rebalance event numbers in this task.

**Step 3: Compile**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: PASS.

**Step 4: Commit**

```bash
git add app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt \
  app/src/main/java/com/siliconsage/miner/util/DilemmaEvents.kt \
  app/src/main/java/com/siliconsage/miner/util/FactionEvents.kt \
  app/src/main/java/com/siliconsage/miner/util/DatasetManager.kt \
  docs
git commit -m "docs: align event rewards with assigned work rate"
```

Only stage files that actually changed.

---

## Task 12: Full Verification And Balance Smoke

**Objective:** Verify compile/tests and run a small deterministic balance sanity check.

**Files:**
- All modified files
- Optional create: `docs/production-loop-verification.md`

**Step 1: Run gates**

```bash
git status --short --branch
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
git diff --check
git diff --stat
```

Expected:
- unit tests pass
- compile passes
- assemble passes
- diff check clean

**Step 2: Run grep audit**

```bash
git grep -n -i "direct passive\|magic wallet\|hardware produces flops\|Neural Tokens Buy Everything\|Manual click ONLY processes dataset" -- '*.md' 'app/src/main/java/**/*.kt' || true
```

Expected:
- `PLAN.md` may still contain superseded Faceminer text under a clear superseded banner.
- New docs/code should not promote the old model.

**Step 3: Manual smoke checklist**

Use emulator/device/dev console if available:

1. Fresh run: assigned queue progresses and pays `$FLOPS` through packet completion.
2. Manual compute still fills the manual command buffer and pays on commit.
3. Buying hardware increases estimated `$FLOPS/s` / assigned work rate.
4. Buying automation increases assigned work throughput/utilization.
5. DATAMINER remains hidden before Stage 2.
6. DATAMINER dataset payouts still work after Stage 2.
7. Heat still throttles estimated rate as before through `ResourceEngine.calculateFlopsRate()`.
8. Wage docking only charges when assigned work actually pays.
9. No first-frame crash from `activeTerminalMode = "DATAMINER"` if that bug remains; note separately if observed.

**Step 4: Write verification note**

Create `docs/production-loop-verification.md` summarizing:

- commands run
- manual smoke results
- known follow-ups

**Step 5: Commit verification doc if created**

```bash
git add docs/production-loop-verification.md
git commit -m "docs: verify production loop conversion"
```

---

## Follow-Up Plans After This Conversion

Do **not** include these in the first implementation unless Cory explicitly expands scope.

### Follow-up A: System Load Cleanup

This plan preserves current system-load throttling as a compatibility shim unless Cory expands scope during implementation. That means `refreshSystemLoad()` may still affect the legacy `flopsProductionRate` alias in the short term. The follow-up should remove that global coupling.

- Remove dataset storage from `SystemLoadEngine`.
- Stop system load from throttling global assigned-work payout rate if that is still happening.
- Make system load scheduler/compute pressure only.
- Decide if overclock adds scheduler pressure.

### Follow-up B: Hardware ROI / Capacity Balance

- Once value flows through assigned packets, retune hardware capacity and packet payout cadence.
- Avoid sub-second late-tier ROI unless intentionally endgame-weird.

### Follow-up C: DATAMINER Sidecar EV

- Datasets should be unauthorized side jobs with risk/lore/burst/special rewards.
- They should not become better hashes or the main progression spine.

### Follow-up D: Event Reward Caps

- Reaudit `productionWindowValue(..., 300–900s)` now that “production” means assigned-work estimate.
- Cap rewards against stage/current upgrade costs where needed.

---

## Subagent Execution Protocol

When executing with `subagent-driven-development`:

1. Read this plan once in the controller session.
2. Create todos for all tasks.
3. For each task:
   - dispatch one implementer subagent with the full task text and relevant docs/code context
   - run spec-compliance reviewer subagent
   - run code-quality reviewer subagent
   - fix/re-review until both approve
   - only then move to next task
4. Do not dispatch multiple implementation subagents touching `GameViewModel.kt` at the same time. That file is a merge-conflict swamp.
5. After all tasks, run final integration reviewer over the whole diff.

## Done Criteria

- `ProductionLoopEngine` exists and has unit coverage.
- Existing hardware math is represented as compute capacity / work throughput, not direct wallet language.
- The 100ms wallet tick pays via assigned hash packet completion/progress.
- Automation improves assigned hash queue processing, preserving idle-clicker progression.
- Manual compute still works as active packet submission.
- DATAMINER remains a post-airgap sidecar.
- Gradle unit tests, compile, assemble, and diff hygiene pass.
- Docs and comments no longer tell future agents to resurrect the Faceminer/dataset-main-spine plan.
