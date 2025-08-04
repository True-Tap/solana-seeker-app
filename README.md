# True Tap - Revolutionizing Solana Payments on Seeker

[![Solana Seeker Optimized](https://img.shields.io/badge/Optimized%20for-Solana%20Seeker-orange.svg)](https://solana.com/seeker)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2035-green.svg)](https://developer.android.com/about/versions/15)

**True Tap** is the ultimate Solana-native payment app, engineered exclusively for the Solana Seeker device. We're unleashing lightning-fast, near-zero-fee transactions with taps so smooth, they feel like magic. Think Venmo meets Cash App meets AliPay—powered by Solana's speed and Seeker's hardware security. Eventually, we'll evolve into the Stripe of Solana, empowering devs with seamless payment APIs.

Transform your Seeker into a decentralized finance beast: Send SOL with emoji flair, split bills in groups, stash in high-yield DeFi vaults, trade NFTs on the fly, set up merchant subscriptions in AI-driven marketplaces, scan QR codes for instant in-person buys (biometrics + Seed Vault locked), and surprise friends with virtual holiday envelopes. True Tap isn't just an app—it's your gateway to intuitive, secure, revolutionary mobile money where every tap unlocks borderless commerce on Solana.

## Why True Tap? 🚀
- **Blazing Solana Speed**: Near-instant txns with fees under a penny—say goodbye to legacy banking delays.
- **Seeker Superpowers**: Hardware-backed Seed Vault + biometrics for unbreakable security.
- **Fun & Social**: Emoji notes, group splits, virtual gifting—crypto payments that feel human.
- **DeFi & Commerce Hub**: Vaults for yields, NFT flips, merchant tools, AI markets—all in one app.
- **Global & Borderless**: Tap-to-pay anywhere, QR magic for IRL buys, no banks needed.
- **Dev-Friendly Future**: Building toward Stripe-like APIs for Solana ecosystem builders.

We're not building another wallet—we're crafting the future of Web3 payments.

## Key Features
- **Tap-to-Pay & QR Magic**: Effortless NFC/QR transactions with biometric confirmation.
- **P2P Transfers**: Send/split SOL/tokens with emojis, group tracking, and zero hassle.
- **DeFi Vaults**: Lock in high yields with one tap.
- **NFT Trading**: Buy/sell digital assets instantly.
- **Merchant Suite**: Subscriptions, AI marketplaces, borderless sales.
- **Social Gifting**: Virtual envelopes for holidays or fun surprises.

(Initial release focuses on wallet connection/auth; full features rolling out soon!)

## Tech Stack
Lean, mean, and modern for Seeker performance:
- **Language**: Kotlin 2.2.0
- **Architecture**: MVVM
- **DI**: Hilt 2.52
- **Async/State**: Coroutines 1.9.0 + Flow
- **Networking**: Retrofit 3.0.0 + OkHttp 5.1.0
- **Persistence**: Room 2.6.1 + DataStore
- **Jetpack**: Lifecycle/ViewModel 2.8.6, Compose BOM 2025.08.00 (Compiler 1.6.0)
- **Solana Magic**: Seed Vault SDK 0.3.2, Mobile Wallet Adapter 2.1.0, Solana-KMP 0.3.0-beta1
- **Build**: AGP 8.6.0, Gradle 8.9, KSP 2.2.0-2.0.2
- **SDK**: Target 35 (Android 15), Min 31 (Android 12)

Dependencies locked for rock-solid builds—see `gradle/libs.versions.toml`.

## Getting Started
### Prerequisites
- Android Studio (Narwhal Feature Drop 2025.1+)
- Solana Seeker or emulator (API 34+ ARM64)
- Git installed

### Installation & Setup
1. Clone the repo (private access required):
   ```
   git clone https://github.com/True-Tap/solana-seeker-app.git
   cd solana-seeker-app
   ```
2. Open in Android Studio, sync Gradle.
3. Build & run on your Seeker.

If initializing from scratch:
```
git init
git add .
git commit -m "Initial commit: True Tap foundation"
git branch -M main
git remote add origin https://github.com/True-Tap/solana-seeker-app.git
git push -u origin main
```

### Usage
- Launch & connect wallet via Seed Vault (biometric auth).
- Tap to pay, send SOL, or dive into DeFi—seamless from day one.
- Devs: Hook into our backend for custom integrations (docs incoming).

## Architecture Highlights
- **WalletRepository**: Core logic for MWA connection & session mgmt (`connectAndAuthWallet()` returns `Result<WalletAccount>`).
- **SeedVaultService**: Biometric signing, account derivation, emulator fallback.
- **WalletViewModel**: Flows for status, auth results—ready for UI hookup.
- Models: `WalletAccount`, `AuthState`, `WalletResult<T>` for clean data handling.

Security: Hardware-enforced, no seed exposure, auto-session restore.

## Contributing
Join the tap revolution! Fork, branch, PR. Check [CONTRIBUTING.md](CONTRIBUTING.md) for code style & guidelines.
- Bugs/Features: Open issues.
- Community: Stay tuned for Discord!

## Contact
- Website: [truetap.app](https://truetap.app)

Tap in, level up Solana—let's make payments epic! 💥