# True Tap - Solana Wallet for Seeker

[![Build Status](https://img.shields.io/github/actions/workflow/status/True-Tap/solana-seeker-app/build.yml?label=Build)](https://github.com/True-Tap/solana-seeker-app/actions)
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
- **Language**: Kotlin 2.2.0
- **Architecture**: MVVM
- **DI**: Hilt 2.56.2
- **Async/State**: Coroutines 1.9.0 + Flow
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0
- **Persistence**: Room 2.6.1 + DataStore
- **Jetpack**: Lifecycle/ViewModel 2.8.6, Compose BOM 2025.07.00 (Compiler 1.6.0)
- **Solana**: Seed Vault SDK 0.3.2, Mobile Wallet Adapter 2.0.7, Solana-KMP 0.3.0-beta1
- **Build**: AGP 8.6.0, KSP 2.2.0-2.0.2
- **SDK**: Target 35 (Android 15), Min 31 (Android 12)

See `gradle/libs.versions.toml` for complete dependency versions.

## Getting Started
### Prerequisites
- Android Studio (latest stable version)
- Solana Seeker device or emulator (API 31+)
- Git installed

### Installation & Setup
1. Clone the repository:
   ```
   git clone https://github.com/True-Tap/solana-seeker-app.git
   cd solana-seeker-app
   ```
2. Open in Android Studio and sync Gradle
3. Build and run on your Seeker device

### Usage
- Launch the app and connect your wallet via Seed Vault
- Complete biometric authentication setup
- The app currently provides basic wallet connection and account management

## Architecture
- **WalletRepository**: Handles Mobile Wallet Adapter connections and session management
- **SeedVaultService**: Manages biometric authentication and account derivation with hardware security
- **WalletViewModel**: Provides reactive state management for wallet operations
- **Models**: `WalletAccount`, `AuthState`, `WalletResult<T>` for type-safe data handling

The app uses hardware-enforced security with no private key exposure and automatic session restoration.

## Contributing
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request.
- Report bugs or request features by opening issues
- Follow existing code style and conventions

## Contact
- Website: [truetap.app](https://truetap.app)