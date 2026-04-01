# Card Games – TB: Documentation

This directory contains supplementary documentation for the **Card Games – TB** project.

---

## Architecture Overview

The project is structured as a standard Maven Java application:

```
Special-Topics-assignments/
├── src/
│   ├── main/java/           # Application source code
│   │   ├── BlackjackGame.java       # Console Blackjack game logic
│   │   ├── Card.java                # Card model (suit + rank)
│   │   ├── Deck.java                # 52-card deck with shuffle
│   │   ├── Player.java              # Player/Dealer state and balance
│   │   ├── HomeGUI.java             # JavaFX launcher / main menu
│   │   ├── PokerGame.java           # Texas Hold'em Poker implementation
│   │   ├── PokerHandEvaluator.java  # Hand ranking logic for Poker
│   │   ├── GoFishGame.java          # Go Fish card game implementation
│   │   ├── WarGame.java             # War card game implementation
│   │   ├── CrazyEightsGame.java     # Crazy Eights card game implementation
│   │   ├── Difficulty.java          # Difficulty level enum
│   │   ├── GameApiServer.java       # Lightweight REST API for multiplayer
│   │   ├── GameSession.java         # Active game session management
│   │   ├── GameState.java           # Serializable game state model
│   │   ├── PlayerProfile.java       # Persistent player profile model
│   │   └── PlayerRepository.java   # H2 database persistence layer
│   └── test/java/           # JUnit 5 unit tests (mirrors main structure)
├── docs/                    # Project documentation (you are here)
├── .vscode/                 # VS Code workspace configuration
├── pom.xml                  # Maven build descriptor
├── Dockerfile               # Docker container definition
├── README.md                # Top-level project overview
├── CONTRIBUTING.md          # Contribution guidelines
├── CHANGELOG.md             # Version history
├── CODE_OF_CONDUCT.md       # Community standards
└── LICENSE                  # MIT License
```

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| Maven for build | Industry-standard dependency management; easy CI integration |
| JUnit 5 | Modern test framework with parameterized tests and extensions |
| H2 embedded DB | Zero-setup persistence for player profiles; no external server needed |
| Conventional Commits | Enables automated changelog generation and semantic versioning |

---

## Game Classes

### `BlackjackGame`
Console-based Blackjack (21). Supports hit/stand, betting with virtual currency, statistics, and input validation.

### `PokerGame` / `PokerHandEvaluator`
Texas Hold'em implementation with hand ranking (pair → royal flush).

### `GoFishGame`
Classic Go Fish with ask-for-rank and book-completion detection.

### `WarGame`
Simple War card game: highest card wins each round.

### `CrazyEightsGame`
Crazy Eights with suit/rank matching and eight-as-wildcard logic.

### `GameApiServer`
Lightweight HTTP server exposing game state as JSON for potential multiplayer or UI extensions.

---

## Setup Guide

See [../README.md](../README.md#setup-instructions) for full setup instructions including Java/Maven prerequisites and VS Code configuration.

Quick start:
```bash
git clone https://github.com/Anaxagorius/Special-Topics-assignments.git
cd Special-Topics-assignments
mvn compile
mvn exec:java
```

Run with Docker (no local Java required):
```bash
docker build -t cardgames-tb .
docker run --rm -it cardgames-tb
```

---

## AI Usage

AI tools (GitHub Copilot) were used to assist with:
- Boilerplate Javadoc generation
- Unit test scaffolding
- Documentation drafting

All AI-generated code was reviewed and validated by the author before committing.

---

## Pages in This Docs Folder

| File | Contents |
|---|---|
| `README.md` | Architecture, design decisions, and quick-start (this file) |
| `CONTRIBUTING.md` | Coding standards, branching strategy, and PR process |
| `CODE_OF_CONDUCT.md` | Community standards for contributors |

---

*Maintained by Tom Burchell (W0516036) — NSCC INFT4000*
