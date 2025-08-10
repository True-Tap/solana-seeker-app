# ADR 001: Payment Features Design (Venmo/Cash App Inspired for Solana)

Date: 2025-08-10

## Status
Accepted

## Context
We are building Solana Seeker as a Seeker-first Venmo/Cash App–style experience: P2P payments with social feed, bill splits, requests, messages/notes, and optional rewards. Solana-specific considerations: on-chain memos for notes, SPL tokens for rewards/splits, compute budget for reliability, and outbox/retry for offline.

## Decision
- Implement Split Pay, Request Payments, and Social Feed enhancements with privacy-by-default.
- Use outbox for offline queue (WorkManager constraints: network + battery-not-low).
- Use fee presets (Normal, Fast, Express) and compute budget (330k CU + priority price) to improve reliability.
- Social data initially stored in-app (`SocialRepository`) with future backend sync; memos can be posted on-chain for public notes.
- Rewards are UX-only initially (0.25% SOL-back suggestion), with path to SPL distribution later.

## Consequences
- Users can request/split/like/comment with guided tooltips and scam warnings.
- We keep builds green and CI checks running.
- Future: replace local social store with backend; enable SPL rewards and on-chain public memos gated by user privacy.

## Alternatives Considered
- Immediate backend for social: deferred to reduce complexity.
- Full on-chain comments: privacy and cost concerns; opt-in memos preferred.


