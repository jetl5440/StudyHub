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

## Deliverable 3 – Student courses and tasks

Log in as a **Student** or **Study Group Leader** and tap **My Courses** on the welcome screen.

- **Course list** – shows the signed-in student's courses (code, title, term). If there are none, a message explains how to add the first one.
- **Course detail** – tap a course to see its code, title, category, term, location and number of tasks.
- **Create course** – **Add Course** asks for a course code, title, a category (chosen from the categories the admin created), a term (Fall/Winter/Summer + year) and an optional location.
- **Edit course** – **Edit Course** on the detail screen. A student can only see and change their own courses.
- **Delete course** – **Delete Course** on the detail screen. **Rule: deleting a course always deletes all of its tasks.** The confirmation message tells the student how many tasks will be deleted with it.
- **Task list** – **View Tasks** on the detail screen lists the course's tasks (sorted by due date), or a message if there are none.
- **Create task** – **Add Task** asks for a title, description, due date (picked from a calendar) and status (To Do, In Progress, Completed).
- **Edit / complete / delete task** – tap a task to **Edit** it, **Mark as completed** (or not completed), or **Delete** it.
- **Validation**:
  - Course code: required, 3 letters + 4 digits (e.g. `SEG2105`), can't be added twice in the same term.
  - Course title: required, max 80 characters, letters/numbers/basic punctuation.
  - Category: required (if the admin hasn't made any, the student is told to ask the admin).
  - Term year: required, 4 digits between 2000 and 2100.
  - Location: optional, max 50 characters.
  - Task title (max 60), description (max 300) and due date are required.
- When the admin deletes a student account, that student's courses and tasks are deleted too.

The updated domain model is in `UML_Class_Diagram.pdf`.

## Resources

- The SQLite database code (`DatabaseHelper`) was written with help from outside resources, mainly the Android Developers guide on saving data with SQLite: https://developer.android.com/training/data-storage/sqlite
