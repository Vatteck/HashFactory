# Gameplay Loop Redesign: The Faceminer Fix

## The Problem

The current gameplay loop has a **split identity crisis**. There are two parallel economies that don't meaningfully interact:

1. **FLOPS (Passive Production)** — Hardware generates FLOPS automatically. Upgrades scale exponentially. This is a classic idle/incremental curve. The player buys hardware → generates more FLOPS → buys more hardware. Heat/Power/Water create friction but don't create *decisions*.

2. **Neural Tokens (Dataset Mining)** — The "Faceminer" system added in v4.0.0. Buy datasets with NT, tap/auto-tap grid nodes, earn NT back. This is meant to be the active gameplay loop.

**The fundamental disconnect:** FLOPS are the progression currency (story gates, prestige conditions), but Neural Tokens are the *activity* currency. The player's manual engagement (clicking, dataset selection, storage management) earns NT, but NT doesn't drive progression — FLOPS do, and FLOPS are passive. The dataset loop feels like a sidequest in your own game.

### Specific Pain Points

1. **FLOPS do all the heavy lifting.** Story stages gate on FLOPS thresholds (500, 100k, 1M, 10M). Hardware passively generates FLOPS. The player could literally AFK through most progression.

2. **Neural Tokens are an island.** NT buys datasets and... that's it. There's a vestigial `exchangeFlops()` and a `conversionRate` at 0.1, but it's a one-way drip. NT doesn't buy upgrades, doesn't influence production, doesn't gate progression.

3. **Datasets are busywork, not strategy.** Buy dataset → auto-clicker mines it → earn ~15% profit → repeat. The purity/corruption mechanic is clever but doesn't scale into interesting decisions. You just buy the highest-purity dataset you can afford.

4. **The manual click does too many things poorly.** One tap generates FLOPS *and* a micro-drip of NT (10% of conversion rate). This dilutes both currencies and makes neither feel impactful.

5. **Auto-clicker makes the active loop passive.** Once AUTO_HARVEST_SPEED is leveled, datasets process themselves. The "active" loop becomes another idle loop.

---

## The Fix: Make Neural Tokens the Spine

The core insight of a "Faceminer" loop is: **the active loop should feed the passive loop, not run parallel to it.** The player's engagement (dataset mining) should be what *enables* and *amplifies* the idle production, not a separate economy.

### Architecture: Three-Layer Loop

```
Layer 1: MINE (Active — Datasets)
   │  Player mines datasets to earn Neural Tokens
   │
   ▼
Layer 2: INVEST (Strategic — Upgrades)
   │  NT is the ONLY currency for ALL upgrades (hardware, cooling, power, storage, security)
   │  Hardware generates FLOPS passively
   │
   ▼
Layer 3: COMPOUND (Idle — FLOPS → Progression)
   │  FLOPS accumulate, hit stage gates, unlock new dataset tiers
   │  Higher tiers = bigger NT payouts = faster upgrade purchasing
   │
   └──► Loop back to Layer 1 with harder/richer datasets
```

### The Key Changes

#### Change 1: Neural Tokens Buy Everything

**Current:** Upgrades cost... nothing? They use a generic `cost * (1 + count * 0.15)` formula but the actual purchase currency is ambiguous (some use FLOPS, some use NT).

**New:** ALL upgrades cost Neural Tokens. Period. FLOPS become purely a production metric / progression gate — you never spend them directly. This makes every dataset mined feel purposeful: "I need 3 more Tensor Units, that's ~4,500 NT, so I need to run 3 more Stage 1 datasets."

**Implementation:**
- `UpgradeManager.purchaseUpgrade()` → always deduct from `neuralTokens`
- Remove any FLOPS-spending paths
- Rebalance upgrade costs against dataset yields (see table below)

#### Change 2: FLOPS Gates Unlock Dataset Tiers (Not Vice Versa)

**Current:** Story stages gate on FLOPS thresholds. Datasets are available per-stage.

**New:** Keep this, but make it *the* progression driver. The player thinks: "I need to hit 100k FLOPS/sec to unlock Stage 2 datasets. Stage 2 datasets pay 10x more NT. To get to 100k FLOPS/sec I need to buy more hardware with NT from Stage 1 datasets."

This is the **compounding loop**: mine → buy hardware → produce more FLOPS → unlock better datasets → mine more profitably → buy more hardware → ...

#### Change 3: Kill the FLOPS-from-Clicking Path

**Current:** Manual click generates FLOPS directly AND a micro-drip of NT.

**New:** Manual click ONLY processes dataset nodes. No free FLOPS from tapping. FLOPS come exclusively from hardware (passive). This cleanly separates the two currencies:
- **NT = what you earn by playing** (active)
- **FLOPS = what your infrastructure produces** (passive)

The manual compute button becomes a "mine the current dataset" button. If no dataset is loaded, it does nothing (or shows "NO ACTIVE DATASET — PURCHASE FROM MARKET").

#### Change 4: Dataset Difficulty as the Core Tension

**Current:** Datasets have purity (% valid nodes) and grid size. Higher stages = bigger grids, lower purity. But auto-clicker trivializes this.

**New:** Add meaningful dataset difficulty scaling that auto-clicker can't fully solve:

- **Decay Nodes**: Valid nodes that rot into corrupt after N seconds if not harvested. Forces active attention even with auto-clicker.
- **Chain Bonuses**: Harvesting 3+ valid nodes consecutively grants +50% payout on the streak. Corrupt tap breaks streak. Rewards careful play.
- **Corruption Spread**: When a corrupt node is tapped, adjacent nodes have a 20% chance of also corrupting. Creates minesweeper-like risk assessment.
- **Dataset Modifiers**: Random modifiers on higher-tier datasets: "ENCRYPTED" (nodes hidden until adjacent node harvested), "VOLATILE" (2x payout but 2x penalty), "COMPRESSED" (half grid size, double density).

#### Change 5: Auto-Clicker as the Endgame Fantasy

**Current:** Auto-clicker speed + accuracy scales linearly. It replaces manual play too early and without fanfare.

**New:** The auto-clicker is the *graduation mechanic*. It should feel like building a machine that eventually surpasses you:

- **Early game (Speed 1-3):** Slow, dumb. 50-65% accuracy. Player is faster and smarter. Auto-clicker is supplemental — it picks off nodes while you do the real work.
- **Mid game (Speed 4-7):** Catching up. 70-85% accuracy. Auto-clicker handles routine datasets while you focus on the hard/modified ones. Manual chain bonuses still give you an edge.
- **Late game (Speed 8-12):** Faster than human. 90-99% accuracy. Processing multiple nodes per second. The player watches their automation *rip* through datasets and feels the payoff of their investment. Manual play literally can't keep up.
- **Endgame (Speed 13+):** Absurd throughput. Auto-loads and chews through dataset queues. The player's role shifts from "miner" to "operations manager" — choosing which datasets to buy, managing storage, optimizing the pipeline. You built something that outgrew you.

**Mechanical progression:**
- Speed still scales at 0.5 taps/sec per level, but accuracy curve steepens: `50% + 4% per level` (reaches 98% at level 12)
- Chain bonuses apply to auto-clicker too at Speed 8+ (it's gotten smart enough to sequence nodes)
- At Speed 10+, auto-clicker can handle dataset modifiers (ENCRYPTED, VOLATILE) that previously required manual attention
- **Cost curve is steep** — Speed 8+ upgrades cost as much as a tier of hardware. This is a late-game investment, not an early skip.

The emotional arc: "I have to do everything myself" → "my automation is helping" → "holy shit it's faster than me" → "I built something that doesn't need me anymore" (narratively resonant for a game about a rogue AI).

#### Change 6: The Conversion Rate Becomes "Market Price"

**Current:** `conversionRate` at 0.1, barely used.

**New:** Rename to `marketPrice`. This is the NT↔FLOPS exchange rate that fluctuates with the existing `MarketManager`. But instead of converting currencies, it determines **dataset pricing**. When market price is high, datasets cost more but yield more. When low, cheap datasets with thin margins.

The `MarketManager` news events now feel consequential: "GTC DATA EMBARGO — DATASET COSTS +40%" directly impacts your ability to buy datasets and thus your upgrade pace.

---

## Rebalancing: The Numbers

### Upgrade Costs (in Neural Tokens)

| Tier | Upgrade | NT Cost (base) | FLOPS/sec | Datasets to Afford (Stage 0) |
|------|---------|----------------|-----------|------------------------------|
| 0 | REFURBISHED_GPU | 50 | 2 | ~1 free task |
| 1 | DUAL_GPU_RIG | 200 | 8 | ~2 datasets |
| 2 | MINING_ASIC | 800 | 35 | ~4 datasets |
| 3 | TENSOR_UNIT | 3,500 | 200 | Stage 1 territory |
| 4 | NPU_CLUSTER | 15,000 | 1,000 | Stage 1-2 |
| 5 | AI_WORKSTATION | 60,000 | 4,000 | Stage 2 |
| 6 | SERVER_RACK | 250,000 | 25,000 | Stage 2-3 |
| 7 | CLUSTER_NODE | 1,000,000 | 150,000 | Stage 3 |
| 8 | SUPERCOMPUTER | 5,000,000 | 1,000,000 | Stage 3-4 |
| 9 | QUANTUM_CORE | 25,000,000 | 10,000,000 | Stage 4 |

**Design rule:** Each upgrade should take 3-5 datasets of the current-tier to afford. This ensures ~15-20 minutes of active play per major upgrade, with auto-clicker extending that to ~45-60 minutes idle.

### Dataset Yield Rebalance

| Stage | Avg Cost (NT) | Avg Yield (NT) | Profit Margin | Time to Complete (auto) |
|-------|---------------|-----------------|---------------|------------------------|
| 0 | 50-200 | 60-250 | ~20% | 30-90 seconds |
| 1 | 600-2,400 | 750-3,000 | ~25% | 2-5 minutes |
| 2 | 7,000-30,000 | 9,000-40,000 | ~30% | 5-10 minutes |
| 3 | 150,000-720,000 | 200,000-1,000,000 | ~35% | 10-20 minutes |
| 4 | 3,400,000-10,200,000 | 5,000,000-15,000,000 | ~40% | 20-40 minutes |

**Key:** Margins increase with tier. This rewards progression — later datasets are more profitable *per unit time*, incentivizing the player to push through FLOPS gates to unlock them.

---

## Implementation Order

### Phase 1: Currency Unification (Structural)
1. Make all `UpgradeManager.purchaseUpgrade()` calls deduct NT
2. Remove FLOPS-spending from click handler (keep only dataset node processing)
3. Remove `exchangeFlops()` legacy path
4. Update all upgrade cost displays to show NT
5. Rebalance upgrade base costs per table above

### Phase 2: Dataset Loop Tightening
6. Add chain bonus mechanic to `DatasetManager.processNodeTap()`
7. Add decay nodes (timer-based corruption) to dataset generation
8. Rework auto-clicker progression curve: steep cost scaling, accuracy steepening, chain bonus eligibility at Speed 8+, modifier handling at Speed 10+
9. Add dataset modifiers system (ENCRYPTED, VOLATILE, COMPRESSED)

### Phase 3: Market Integration
10. Rename `conversionRate` → `marketPrice`, wire into dataset cost/yield
11. Make `MarketManager` news events directly modify dataset pricing
12. Add market trend indicator to dataset picker UI

### Phase 4: UI Clarification
13. Update HeaderSection to show NT prominently (it's now the primary currency)
14. Update ManualComputeButton to clearly show "dataset mining" context
15. Show "FLOPS/sec" as production metric, "NT Balance" as spendable currency
16. Dataset picker shows ROI estimate per dataset

### Phase 5: Pressure System Integration
17. Utility bills (power, water) cost NT (not FLOPS) — creates spending pressure
18. Security breaches can destroy stored datasets — creates urgency to process them
19. Storage costs scale with tier — hoarding datasets becomes expensive

---

## What This Achieves

1. **Every tap matters.** You're mining datasets for NT, which buys upgrades, which generates FLOPS, which unlocks better datasets. No wasted actions.

2. **Clear mental model.** "I mine data to earn tokens. I spend tokens on hardware. Hardware produces computing power. More power unlocks harder data. Harder data pays more tokens." A 5-year-old could understand this loop.

3. **Automation as the endgame fantasy.** Early: you grind manually. Mid: auto-clicker helps. Late: it surpasses you. The emotional payoff of watching your machine outpace your own hands is the reward for investing in it — and narratively perfect for a rogue AI story.

4. **Narrative alignment.** You're literally a rogue AI mining data for survival resources. The "Faceminer" name makes sense now — you're mining faces/data as your primary activity, not as a side hustle.

5. **Existing systems preserved.** Heat, Power, Water, Detection, Faction, Grid, Narrative — all still work. They create friction on the *spending* side (you need cooling, power, security) while datasets drive the *earning* side. The tension between earning and spending is the game.
