# ADR 002: Social Feed Design (Privacy, Scam Alerts, Memos, Interactions)

Date: 2025-08-10

## Status
Accepted

## Context
Solana Seeker provides a Venmo-like social feed with likes, comments, and notes around P2P transactions. Solana-specific aspects include on-chain memos for public notes, privacy defaults, and scam alerts. Seeker-first UX guides beginners to safe defaults and clear disclosures.

## Decisions
- Privacy-by-default: All notes are private unless explicitly posted as public.
- Scam alerts: Modals and feed tooltips include warnings (e.g., "Verify sender before accepting" and "Public comment? Visible in feed—use memos wisely").
- Interactions: Likes and comments are stored locally via `SocialRepository` initially; future backend sync planned. Inline actions for quick feedback.
- Public comments via memos: When opted-in, public notes can be encoded as on-chain memos using `MemoProgram`. This is opt-in with clear UX copy.
- Rewards/Boosts: Optional, opt-in setting. Reward hints (e.g., 0.25% SOL back) are shown only when enabled. Future SPL distribution may post a memo referencing the reward.

## Consequences
- Users have safe defaults and can opt-in to public sharing and rewards.
- Social data starts local; transition path to backend.
- On-chain memos are used judiciously to avoid noise and protect privacy.

## Alternatives Considered
- Always-on public feed (rejected for privacy).
- Immediate backend for social (deferred to reduce complexity and time-to-ship).

## Follow-ups
- Backend for social interactions and moderation.
- SPL-based rewards with ledgered accounting.
- Per-contact visibility controls.


