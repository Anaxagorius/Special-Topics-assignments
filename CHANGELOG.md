# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Split**: Players can split two equal-rank starting cards into two independent hands; an extra bet equal to the original is placed for the second hand
- **Double Down**: Players can double their bet on the first action to receive exactly one additional card and automatically stand
- Betting system with virtual currency (player starts with $100 per session)
- Statistics tracking: wins, losses, and ties displayed at end of session
- Input validation with clear error messages for invalid hit/stand input
- `Player` class overloaded constructor accepting an initial balance
- `Player.getBalance()`, `addBalance()`, and `deductBalance()` methods
- 8 new unit tests covering `Player` balance management

## [0.1.0] - 2026-02-13

### Added
- Console-based Blackjack game
- Player vs Dealer gameplay
- Hit/Stand mechanics
- Automatic dealer AI
- Score calculation with Ace handling
- Play again functionality
- Complete project documentation
- MIT License
- Contributing guidelines
- Code of Conduct

### Technical
- Java 17+ implementation
- Object-oriented design with Card, Deck, Player classes
- VS Code configuration

[Unreleased]: https://github.com/Anaxagorius/Special-Topics-assignments/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/Anaxagorius/Special-Topics-assignments/releases/tag/v0.1.0