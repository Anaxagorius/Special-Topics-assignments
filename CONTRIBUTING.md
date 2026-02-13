# Contributing to Card Games - TB

Thank you for your interest in contributing to this project! This document provides guidelines and standards for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

This project adheres to a Code of Conduct that all contributors are expected to follow. Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## Getting Started

1. **Fork the repository** to your own GitHub account
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/Special-Topics-assignments.git
   cd Special-Topics-assignments
   ```
3. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** following our coding standards
5. **Test thoroughly** before submitting

## Development Workflow

### Branching Strategy

We use a simplified Git Flow model:

- `main` - Production-ready code
- `develop` - Integration branch for features (if applicable)
- `feature/*` - New features or enhancements
- `bugfix/*` - Bug fixes
- `hotfix/*` - Urgent production fixes

**Branch naming convention:**
- `feature/add-poker-game`
- `bugfix/fix-ace-calculation`
- `hotfix/dealer-logic-crash`

### Before You Start

1. Ensure you have the latest code:
   ```bash
   git checkout main
   git pull origin main
   ```
2. Create your branch:
   ```bash
   git checkout -b feature/descriptive-name
   ```

## Coding Standards

### Java Style Guide

Follow standard Java conventions:

#### Naming Conventions
- **Classes**: PascalCase (`BlackjackGame`, `Card`)
- **Methods**: camelCase (`calculateScore`, `dealCard`)
- **Variables**: camelCase (`playerHand`, `dealerScore`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_SCORE`, `DEALER_STAND_VALUE`)

#### Code Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Braces**: Opening brace on same line, closing brace on new line
  ```java
  if (condition) {
      // code here
  }
  ```

#### Documentation
- **All public classes** must have Javadoc comments
- **All public methods** must have Javadoc with:
  - Description
  - `@param` for each parameter
  - `@return` if method returns a value
  - `@throws` for exceptions

Example:
```java
/**
 * Calculates the total score of a hand.
 * Handles Ace values (1 or 11) automatically.
 * 
 * @return The total score of the hand
 */
public int calculateScore() {
    // implementation
}
```

#### Best Practices
- Keep methods short and focused (under 30 lines when possible)
- Use meaningful variable names
- Avoid magic numbers - use named constants
- Handle exceptions appropriately
- Write self-documenting code

### File Organization
```
src/
├── BlackjackGame.java    # Main game class
├── Card.java             # Card representation
├── Deck.java             # Deck management
├── Player.java           # Player/Dealer logic
└── [other game files]
```

## Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```
feat(blackjack): add double-down option

Implement double-down functionality allowing players to double
their bet in exchange for exactly one more card.

Closes #15
```
```
fix(deck): correct shuffle algorithm

Previous implementation had bias. Switched to Fisher-Yates shuffle
for proper randomization.
```
```
docs(readme): update setup instructions

Added Java version requirement and clarified compilation steps.
```

### Commit Best Practices
- Use present tense ("add feature" not "added feature")
- Keep subject line under 50 characters
- Capitalize subject line
- No period at end of subject
- Separate subject from body with blank line
- Wrap body at 72 characters
- Reference issues in footer

## Pull Request Process

### Before Submitting

1. **Update documentation** if you've changed APIs or added features
2. **Test your code** thoroughly
3. **Run all existing tests** (if applicable)
4. **Update CHANGELOG.md** with your changes
5. **Ensure code compiles** without errors or warnings

### Submitting a Pull Request

1. **Push your branch** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open a Pull Request** on GitHub with:
   - Clear title describing the change
   - Reference to related issue (if applicable)
   - Description of what changed and why
   - Screenshots/examples if relevant

3. **PR Description Template**:
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Documentation update
   - [ ] Refactoring

   ## Testing
   Describe testing performed

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Documentation updated
   - [ ] CHANGELOG.md updated
   - [ ] No compiler warnings
   ```

### Code Review

- All PRs require review before merging
- Address review feedback promptly
- Keep discussions professional and constructive
- Reviewers should check:
  - Code quality and style
  - Functionality correctness
  - Test coverage
  - Documentation completeness

## Issue Reporting

### Bug Reports

Use the bug report template and include:
- Clear description of the bug
- Steps to reproduce
- Expected vs. actual behavior
- Environment details (Java version, OS)
- Code snippets or error messages

### Feature Requests

Include:
- Use case and motivation
- Proposed solution
- Alternative solutions considered
- Examples of usage

### Issue Labels

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention needed

## Questions?

If you have questions about contributing, feel free to:
- Open an issue with the `question` label
- Contact the maintainer: Tom Burchell (w0516036@nscc.ca)

---

Thank you for contributing to Card Games - TB! 🎴