# Production Loop Conversion Plan Review

Reviewer: delegate_task subagent  
Date: 2026-05-02

## Verdict

Revise before approval.

## High-severity findings

1. Task 6 passing `0.0` to `ResourceEngine.calculatePassiveIncomeTick()` would stop substrate progression because substrate is derived from the same `flopsPerSec` argument.
2. Assigned-work payout rate could diverge from advertised `flopsProductionRate` because automation utilization is applied during tick but not in the rate state.
3. Current system load still multiplies `flopsProductionRate`, so the new loop would keep global load throttling unless explicitly treated as a temporary compatibility shim or refactored.

## Required revisions applied

- Task 6 now keeps assigned-work throughput as the non-wallet substrate basis and ignores `res.flopsDelta` instead of passing `0.0`.
- Task 7 now introduces `computeCapacityRate` and `assignedWorkPayoutRate`, with `flopsProductionRate` kept as a legacy alias.
- Task 4 now requires broader parity coverage for cage/grid/shadow/offline/ghost/faction/saturation cases.
- Task 9 is reframed as assigned-hash automation coverage/cleanup because behavior is introduced earlier.
- Task 10 now prefers `ActiveCommandBuffer.kt` for progress UI to avoid header churn.
- System-load handling is explicitly called out as compatibility behavior to remove in follow-up unless Cory expands scope.
