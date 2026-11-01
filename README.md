# StudyHub – SEG2105 Fall 2026 Project

**Team:** Group 122

**GitHub repository:** https://github.com/jetl5440/StudyHub

## Team members

| Name | Student number |
|------|----------------|
| Jet Lin | 300471562 |

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

## Deliverable 2 – Admin functionality

Log in as the admin (`admin` / `XPI76SZUqyCjVxgnUjm0`) to see two extra buttons on the welcome screen:

- **Manage Categories** – lists all course categories. Tap **Add Category** to create one (name and description). Tap a category to **edit** or **delete** it.
- **Manage Users** – lists every account registered in the app. Tap a student or study group leader to **disable/enable** or **delete** their account. A disabled account can't log in. The admin account can't be disabled or deleted.
- Category fields are validated: name and description are required, names must be unique and can only use letters, numbers, spaces, & and -.

## Resources

- The SQLite database code (`DatabaseHelper`) was written with help from outside resources, mainly the Android Developers guide on saving data with SQLite: https://developer.android.com/training/data-storage/sqlite
