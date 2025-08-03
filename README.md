# True Tap - Solana Wallet for Seeker

[![Solana Seeker Optimized](https://img.shields.io/badge/Optimized%20for-Solana%20Seeker-orange.svg)](https://solana.com/seeker)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2035-green.svg)](https://developer.android.com/about/versions/15)

**True Tap** is a Solana wallet application designed for the Solana Seeker device. The app provides secure wallet connection and authentication using the Seeker's hardware-backed Seed Vault and biometric authentication capabilities.

Currently in early development, the app focuses on establishing secure wallet connections through the Mobile Wallet Adapter protocol and integrating with Solana's Seed Vault SDK for hardware-secured key management.

## Current Features
- **Secure Wallet Connection**: Integration with Mobile Wallet Adapter for secure Solana wallet connections
- **Hardware Security**: Utilizes Seeker's Seed Vault for hardware-backed key storage and biometric authentication
- **Account Management**: Basic wallet account creation and session management

## Planned Features
- P2P SOL transfers
- Transaction history
- Multi-account support
- QR code payments

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