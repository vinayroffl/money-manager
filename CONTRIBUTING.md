# Contributing to Money Manager

Thank you for your interest in contributing to Money Manager! Please take a moment to review these guidelines before submitting your contribution.

## Branch Naming Convention

We follow a structured branch naming strategy to keep our repository organized:

| Branch Type | Format                  | Example                     |
| ----------- | ----------------------- | --------------------------- |
| Main        | `main`                  | `main`                      |
| Develop     | `develop`               | `develop`                   |
| Feature     | `feature/<description>` | `feature/user-registration` |
| Release     | `release/<version>`     | `release/v1.2.0`            |
| Hotfix      | `hotfix/<description>`  | `hotfix/login-error`        |

**Guidelines:**

- Use lowercase letters and hyphens (`-`) for word separation
- Keep branch names descriptive but concise
- Do not use special characters or underscores

---

## Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format

<type>(<scope>): <subject>

[optional body]

[optional footer]

### Types

| Type       | Description                                       |
| ---------- | ------------------------------------------------- |
| `feat`     | A new feature                                     |
| `fix`      | A bug fix                                         |
| `docs`     | Documentation changes                             |
| `style`    | Code style changes (formatting, semicolons, etc.) |
| `refactor` | Code refactoring without changing functionality   |
| `test`     | Adding or updating tests                          |
| `chore`    | Maintenance tasks, build process, dependencies    |

### Examples

feat(auth): add user registration endpoint

Implements user sign-up with email verification.

Closes #42

fix(transactions): resolve date formatting issue

docs: update API documentation

---

## Pull Request Checklist

Before submitting a pull request, please ensure:

- [ ] Branch is created from `develop` (or `main` for hotfixes)
- [ ] Branch name follows naming convention
- [ ] Code is rebased on latest `develop` branch
- [ ] Commits follow conventional commit message format
- [ ] All tests pass locally
- [ ] New features include appropriate tests
- [ ] Documentation has been updated if needed
- [ ] No unnecessary files or debug code are included
- [ ] PR description clearly describes the changes and why they are needed
- [ ] PR links to any related issues (e.g., "Closes #42")

---

## Coding Standards

We follow standard industry best practices for clean and maintainable code.

### General Guidelines

- Write readable, self-documenting code
- Use meaningful variable and function names
- Keep functions small and focused on a single responsibility
- Comment complex logic where necessary
- Follow DRY (Don't Repeat Yourself) principles

### Language-Specific Standards

| Language   | Reference                                                                            |
| ---------- | ------------------------------------------------------------------------------------ |
| JavaScript | [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)                |
| Python     | [PEP 8](https://www.python.org/dev/peps/pep-0008/)                                   |
| HTML/CSS   | [Google HTML/CSS Style Guide](https://google.github.io/styleguide/htmlcssguide.html) |

---

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a new branch from `develop`
4. Make your changes
5. Commit with proper message format
6. Push to your fork
7. Submit a pull request to `develop`

---

## Questions?

If you have any questions, feel free to open an issue or reach out to the maintainers.

Thank you for contributing!
