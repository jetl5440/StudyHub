# StudyHub – SEG2105 Fall 2026 Project

**Team:** group1   <!-- replace with your team name -->

**GitHub repository:** https://github.com/YOUR-ORG/YOUR-REPO   <!-- replace -->

## Team members

| Name | Student number |
|------|----------------|
| Member 1 | 000000000 |

## Deliverable 1 – User accounts

- SQLite database (`DatabaseHelper`) stores all accounts.
- One predefined admin account is created automatically:
  - username: `admin`
  - password: `XPI76SZUqyCjVxgnUjm0`
- Anyone can register as a **Student** or a **Study Group Leader** (as many as they want).
- After logging in, the user sees: `Welcome firstname! You are logged in as "role".`
- Log Out returns to the login screen.
- All registration fields are validated (names, email, username, student number, password, password confirmation). Login checks for empty fields.
- Optional: the admin's welcome screen lists every user created so far.
