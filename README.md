# Card Games - TB

## Project Overview

A console-based Blackjack card game implementation in Java, developed as part of the INFT4000 course at NSCC. This project demonstrates object-oriented programming principles, game logic implementation, and Java best practices.

## Problem Statement

Many developers learning Java struggle to apply object-oriented concepts in practical scenarios. This project provides a hands-on implementation of a classic card game (Blackjack/21) that demonstrates:

- Class design and encapsulation
- Game state management
- User input handling
- Random number generation and shuffling algorithms
- Score calculation logic

## Technologies Used

- **Java 17** - Primary programming language
- **IntelliJ IDEA Ultimate** - Integrated Development Environment
- **Git** - Version control
- **GitHub** - Repository hosting and collaboration
- **VS Code** - Alternative code editor with Java extensions

## Features

- ✅ Full Blackjack game implementation
- ✅ Player vs Dealer gameplay
- ✅ Deck shuffling and card dealing
- ✅ Score calculation with Ace handling
- ✅ Hit/Stand player actions
- ✅ Automatic dealer logic (hits until 17+)
- ✅ Win/Loss determination
- ✅ Multiple rounds support

## Setup Instructions

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Git
- A Java IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Anaxagorius/Special-Topics-assignments.git
   cd Special-Topics-assignments
   ```

2. **Compile the Java files**
   ```bash
   cd src
   javac *.java
   ```

3. **Run the game**
   ```bash
   java BlackjackGame
   ```

### Using VS Code

1. Open the project folder in VS Code
2. Install the "Extension Pack for Java" if not already installed
3. Open `src/BlackjackGame.java`
4. Click the "Run" button above the `main` method or press `F5`

### Using IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select "Open" and navigate to the project directory
3. Right-click on `BlackjackGame.java` in the `src` folder
4. Select "Run 'BlackjackGame.main()'"

## How to Play

1. Run the program
2. You'll be dealt two cards, and the dealer gets two cards (one visible)
3. Choose to **Hit** (h) to draw another card or **Stand** (s) to keep your current hand
4. Try to get as close to 21 as possible without going over
5. If you go over 21, you bust and lose
6. The dealer must hit until reaching 17 or higher
7. The player with the score closest to 21 without busting wins

## Extending the Project

This project is designed to be extensible. Here are some ideas for enhancement:

### Beginner Extensions
- Add bet tracking and player balance
- Implement splitting pairs
- Add double down functionality
- Create different difficulty levels

### Intermediate Extensions
- Implement multiple players
- Add a GUI using JavaFX or Swing
- Create different card game variations (Poker, Go Fish, etc.)
- Add save/load game state functionality

### Advanced Extensions
- Implement network multiplayer
- Add AI opponents with different strategies
- Create a web-based version using Spring Boot
- Add card counting detection
- Implement authentication and user profiles

### How to Contribute Extensions

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/new-game`)
3. Make your changes following the coding standards in [CONTRIBUTING.md](CONTRIBUTING.md)
4. Test thoroughly
5. Submit a pull request with a clear description

## Project Structure

```
cardgames-TB/
├── README.md                # This file
├── LICENSE                  # MIT License
├── CHANGELOG.md             # Version history
├── CONTRIBUTING.md          # Contribution guidelines
├── CODE_OF_CONDUCT.md       # Community standards
│
├── src/                     # Source code
│   ├── BlackjackGame.java   # Main game class
│   ├── Deck.java            # Deck management
│   ├── Card.java            # Card representation
│   └── Player.java          # Player/Dealer logic
│
├── docs/                    # Documentation
│   ├── ARCHITECTURE.md      # System architecture
│   ├── AI_USAGE.md          # AI assistance documentation
│   └── SETUP_GUIDE.md       # Detailed setup instructions
│
└── .vscode/                 # VS Code configuration
    └── settings.json        # Java project settings
```

## Credits

- **Original Tutorial**: [Console Blackjack in Java (21) - Kevin's Guides](https://kevinsguides.com/guides/code/java/blackjack)
- **Developer**: Tom Burchell (W0516036)
- **Course**: INFT4000 - Special Topics in IT
- **Institution**: NSCC COGS - IT Programming

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

The original tutorial content is used with attribution as per educational fair use.

## Contact

- **GitHub**: [@Anaxagorius](https://github.com/Anaxagorius)
- **Organization**: [NSCC COGS IT Programming Team](https://github.com/NSCC-AVC-PROG2200)

## Acknowledgments

- Kevin's Guides for the excellent Blackjack tutorial
- NSCC instructors and classmates for feedback and support
- JetBrains for IntelliJ IDEA
- Oracle for the Java Development Kit

---

**Last Updated**: February 2026
