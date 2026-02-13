# Card Games - TB

A collection of card game implementations in Java, starting with a console-based Blackjack game.

## Project Purpose

This project serves as a learning platform for software development practices including version control, project management, documentation, and collaborative development workflows. The initial implementation is a console-based Blackjack (21) card game that demonstrates object-oriented programming principles in Java.

### Problem Statement

Card games are a universal form of entertainment and an excellent domain for learning programming concepts. This project aims to:
- Provide a working implementation of classic card games
- Demonstrate clean code architecture and documentation practices
- Serve as a foundation for learning Git workflows and GitHub features
- Enable collaborative development and code review practices

## Technologies Used

- **Java 17+** - Primary programming language
- **Git** - Version control system
- **GitHub** - Code hosting and collaboration platform
- **VS Code** - Integrated development environment
- **Maven/Gradle** (optional) - Build automation and dependency management

## Features

### Blackjack Game
- Full implementation of Blackjack rules
- Player vs. Dealer gameplay
- Hit/Stand decision making
- Automatic dealer AI (hits until 17+)
- Score calculation with Ace handling
- Multiple rounds with play-again option

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Git installed on your system
- VS Code (recommended) or any Java IDE

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Anaxagorius/Special-Topics-assignments.git
   cd Special-Topics-assignments
   ```

2. **Verify Java installation**
   ```bash
   java --version
   ```

3. **Compile the game**
   ```bash
   cd src
   javac *.java
   ```

4. **Run the game**
   ```bash
   java BlackjackGame
   ```

### VS Code Setup

If using VS Code, the repository includes configuration for Java development:
- Open the folder in VS Code
- Install the "Extension Pack for Java" from Microsoft
- The `.vscode` folder contains launch and task configurations

## How to Play

1. Run the BlackjackGame class
2. You'll be dealt two cards, and can see one of the dealer's cards
3. Choose to **Hit** (draw another card) or **Stand** (keep current hand)
4. Try to get as close to 21 without going over
5. Dealer plays automatically after your turn
6. Winner is determined by who has the higher score without busting

## Extending the Project

This project is designed to be extended in several ways:

### Adding New Card Games
1. Create new game classes in `src/` directory
2. Reuse existing `Card`, `Deck`, and `Player` classes
3. Implement game-specific rules and logic
4. Examples: Poker, Go Fish, War, Crazy Eights

### Enhancing Blackjack
- Add betting system with virtual currency
- Implement split and double-down options
- Add insurance bet when dealer shows Ace
- Multi-player support
- Save/load game state
- Add statistics tracking

### Technical Improvements
- Implement a GUI using JavaFX or Swing
- Add unit tests with JUnit
- Create a REST API for multiplayer games
- Add database persistence for player profiles
- Implement AI with different difficulty levels

### Project Structure
```
cardgames-tb/
├── src/              # Source code directory
├── docs/             # Additional documentation
├── .vscode/          # VS Code configuration
├── README.md         # This file
├── LICENSE           # Project license
├── CONTRIBUTING.md   # Contribution guidelines
└── CHANGELOG.md      # Version history
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Original Blackjack tutorial: [Console Blackjack in Java (21) - Kevin's Guides](https://kevinsguides.com/guides/code/java/blackjack)
- Created as part of NSCC COGS IT Programming coursework
- Course: INFT4000 - Special Topics

## Author

**Tom Burchell (TB)**  
Student ID: W0516036  
NSCC - COGS IT Programming  
February 2026

---

*Last Updated: 2026-02-13*