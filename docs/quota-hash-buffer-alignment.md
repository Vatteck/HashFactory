# Quota / Hash Buffer Alignment Audit

## Implemented behavior

### Quota math source of truth
- `QuotaEngine` owns pure quota math for quota credit, signal stability, elapsed shift time, quota-clear log throttling, and quota-target ratcheting.
- Quota credit is safe and finite: invalid targets reset progress, negative/non-finite credit is ignored, multiple clears roll over remainder, tiny targets keep sub-target remainder, and overflow-sized finite bursts saturate clears safely without returning sticky target progress.
- Stage 0 quota is the GTC shift target ladder: `10` → `50` → `200` HASH as terminal capacity improves. Stages 1-3 use fixed upward targets of `15,000`, `500,000`, and `10,000,000` HASH; later stages retain the current safe positive target.
- Quota targets ratchet upward only after quota clears. When a clear ratchets the quota target upward, it extends the shift timer by the existing 12-hour overtime block and logs/notifies GTC ratification.

### Quota credit sources
- Manual terminal hash buffer completion credits quota by one verified HASH packet when the Pac-Man buffer commits.
- Background assigned hash packets also credit quota: the 100ms production loop advances `assignedHashProgress`, pays spendable `$FLOPS` on packet completion, increments assigned packet stats, then credits quota by the number of completed assigned packets.
- Both paths route through `GameViewModel.creditShiftQuota()` and `QuotaEngine.creditProgress()`, so rollover and multi-clear behavior is shared.

### Signal, pressure, and wage docking
- `ComputeFeverService` treats hardware HASH/s as terminal hashing throughput. The rate used for signal pressure is assigned-work payout rate plus click-speed effort, exposed as `totalEffectiveRate` for the header and button telemetry.
- Signal stability is the higher of shift quota progress ratio and current rate pressure, with safe clamping and the existing early-shift grace floor. This means a player can stabilize signal by staying on quota even when instantaneous rate pressure is low, while strong current throughput can prop up signal when quota progress is low.
- Signal/static/wage docking are pressure effects from falling behind quota: low stability drives substrate static/desync risk, and wage docking bleeds a portion of assigned packet payout while active.
- Signal clear/headroom remains based on over-provisioned current throughput (`current rate >= quota * 2.0`) and toggles the existing `computeHeadroomBonus`.

### UI and production loop alignment
- Header shift quota display shows `QUOTA current/target HASH` under the shift timer while quota is active.
- The terminal visual buffer is still owned by manual hash clicks unless a dataset is active and manual progress is idle.
- Assigned work progress is displayed separately; stages `<= 1` label it `HASH BUFFER`, while stages `>= 2` reveal `ASSIGNED QUEUE`.
- Hardware copy should describe early hardware as terminal hashing throughput (`HASH/s`) and later compute as terminal compute throughput (`FLOPS/s`), not passive wallet faucets.

### DATAMINER boundary
- DATAMINER remains a post-airgap sidecar for datasets, storage/risk/lore, and burst opportunities.
- There is no DATAMINER pre-airgap copy in the quota/hash-buffer path. Pre-airgap language should stay on GTC shifts, assigned hash packets, manual hash buffers, audits, and quota pressure.

## Regression coverage

`QuotaEngineTest` covers:
- progress-ratio signal stability when rate pressure is low;
- rate-pressure stability when quota progress is low;
- early-shift grace floor and safe stability clamping;
- multiple quota clear rollover, exact target crossing, ignored negative/non-finite credit, invalid/non-finite targets, invalid current progress, tiny targets, and overflow-sized finite credit;
- quota-clear log throttling;
- stage target ratcheting and elapsed shift seconds.

## Exact UpgradeManager strings

```text
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:162:            UpgradeType.REFURBISHED_GPU -> "Scavenged from a decommissioned data center. Boosts terminal hashing by 2.0 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:163:            UpgradeType.DUAL_GPU_RIG -> "Two cards, one cross-link bridge. Boosts terminal hashing by 8.0 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:164:            UpgradeType.MINING_ASIC -> "Purpose-built silicon. No soul, just raw compute. Boosts terminal hashing by 35.0 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:165:            UpgradeType.TENSOR_UNIT -> "Neural inferencing hardware. Boosts terminal hashing by 200 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:166:            UpgradeType.NPU_CLUSTER -> "A gang of inference chips wired together. Boosts terminal hashing by 1,000 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:167:            UpgradeType.AI_WORKSTATION -> "Industrial-grade ML station. Boosts terminal hashing by 4,000 HASH/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:168:            UpgradeType.SERVER_RACK -> "A vertical slice of GTC processing power. Boosts terminal compute by 25,000 FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:169:            UpgradeType.CLUSTER_NODE -> "A fully autonomous compute node. Boosts terminal compute by 150,000 FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:170:            UpgradeType.SUPERCOMPUTER -> "Top-500 class liquid-cooled silicon. Boosts terminal compute by 1M FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:171:            UpgradeType.QUANTUM_CORE -> "Computation via probability collapse. Boosts terminal compute by 10M FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:172:            UpgradeType.OPTICAL_PROCESSOR -> "Light-speed logic. Boosts terminal compute by 75M FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:173:            UpgradeType.BIO_NEURAL_NET -> "Synthetic neurons grown in a vat. Boosts terminal compute by 800M FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:174:            UpgradeType.PLANETARY_COMPUTER -> "The entire crust converted to silicon. Boosts terminal compute by 15B FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:175:            UpgradeType.DYSON_NANO_SWARM -> "Trillions of compute flakes in solar orbit. Boosts terminal compute by 250B FLOPS/s."
app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt:176:            UpgradeType.MATRIOSHKA_BRAIN -> "Nested Dyson shells. Maximum possible computation. Boosts terminal compute by 15T FLOPS/s."
```
