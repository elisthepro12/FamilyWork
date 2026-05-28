# 1. General description of the project
The FamilyWork app is a management tool designed to help family members organize their household life in one shared place. The main purpose of the app is to sync tasks, shopping lists, and history between different devices used by the same family. It solves the common problem of forgotten chores or buying the same grocery item twice by providing a real-time database. The users are typically family members who want a simple way to assign tasks and manage shopping needs. The app features a categorization system for chores, a shopping list with text-to-speech capabilities, and a history log that keeps track of completed shopping items for up to seven days.

# 2. Application structure
The project follows a standard Android architecture using Activities for main navigation and Fragments for specific content areas.
- **Activities:** The app starts with `MainActivity`, which acts as a gateway. `LoginActivity` handles the logic for identifying users and linking them to a family. `StartActivity` serves as the main hub of the application.
- **Fragments:** Inside `StartActivity`, the user can switch between `fragment_tasks` (for chores), `ShoppingListFragment` (for groceries), and `HistoryFragment` (for logs).
- **Interaction:** The classes interact mainly through a shared Firebase Realtime Database. Transitions between screens happen using `Intent` objects, while transitions between fragments are managed by the `FragmentManager` inside `StartActivity`.

# 3. User Interface (UI)
- **MainActivity:** A clean splash screen with a background image and a "Start" button to begin the login process.
- **LoginActivity:** Contains fields for "Your Name", "Phone Number", "Family Name", and "Family Code". It has buttons to either "Join Existing Family" or "Create New Family".
- **StartActivity:** Features a top "Family Spinner" allowing users to switch between different family codes they belong to (displaying "Code - Family Name"). It also has a Bottom Navigation Bar to switch views.
- **fragment_tasks:** Displays a list of chores grouped by categories (e.g., Dog, Kitchen, Personal Room). Includes an "Add Task" button and a "Delete Done" button.
- **ShoppingListFragment:** Shows grocery items with their quantities and images. Includes a "Speak" button to read the list aloud and an "Add" button.
- **HistoryFragment:** Shows a ץvertical list of completed shopping items, separated by date headers.
- ":? "

# 4. Class Descriptions

## LoginActivity
This class manages how a user enters the app. It checks if the user is already logged in by looking at the phone's memory. If not, it allows the user to create a new family group with a unique 6-character code or join an existing one. It uses `TextInputEditText` for professional data entry and interacts with `SharedPreferences` for local data and `FirebaseDatabase` for cloud sync.

## StartActivity
This is the "Control Center" of the app. It holds the fragments and manages the top menu. When a user picks a different family from the top list, this class refreshes to show that specific family's data. It also initializes background services for notifications.

## fragment_tasks
This fragment is responsible for showing and managing household chores. It retrieves task data from Firebase and tells the `TaskAdapter` how to display it. It also handles the "Add Task" dialog where users choose who does the task and what category it belongs to.

## Task
This is a data class (Model). It holds information about a single chore, such as its ID, title, whether it is finished, if it repeats daily, its category, and a map of family members assigned to it.

## TaskAdapter
This class acts as a bridge between the data (the list of tasks) and the screen. It decides how each chore card looks, applies different colors to them, and handles the grouping of chores under category headers.

# 5. Description of methods

### LoginActivity: private void generateNewFamily()
- **Parameters:** None.
- **Description:** It collects the user's name, phone, and desired family name from the screen. It generates a random 6-character code and immediately calls the `saveAndGo` method to create the family group.
- **Called when:** The user clicks the "Create New Family" button.

### LoginActivity: private void joinExistingFamily()
- **Parameters:** None.
- **Description:** It collects the user's name, phone, and the family code they entered. It looks up the family name in the Firebase database. If found, it continues to the main app; otherwise, it shows an error message.
- **Called when:** The user clicks the "Join Existing Family" button.

### LoginActivity: private void saveAndGo(String code, String fName, String userName, String userPhone)
- **Parameters:** `code` (Family ID), `fName` (Family Name), `userName` (User's Name), `userPhone` (User's Phone).
- **Description:** Saves the family information into `SharedPreferences` in a "Code:Name" format. It also uploads the user's details to the family group in Firebase and starts `StartActivity`.
- **Called when:** A user successfully creates or joins a family.

### fragment_tasks: private void showTaskDialog(@Nullable Task taskToEdit)
- **Parameters:** `taskToEdit` (The task object if editing, or null if creating new).
- **Description:** Opens a ScrollView-based dialog with fields for the task name, category, and dynamic checkboxes for all family members. If editing, it fills the fields with the current task details.
- **Called when:** The user clicks the "+" button or clicks on an existing task card.

### TaskAdapter: public void updateTasks(List<Task> tasks)
- **Parameters:** `tasks` (A list of Task objects from Firebase).
- **Description:** Clears the current list and sorts the new tasks into a map by category. It then builds a display list that includes both category headers and the tasks themselves.
- **Called when:** Task data in Firebase is added, changed, or removed.

# 6. Application workflow
1. **Launch:** The app starts at `MainActivity`. It checks `SharedPreferences` for a saved phone number.
2. **Auto-Login:** If data is found, it automatically moves to `LoginActivity`, which then finishes and opens `StartActivity`.
3. **Registration:** If no data is found, the user enters their details. They can generate a new family code or use one from a relative.
4. **Main Hub:** In `StartActivity`, the user sees the family name at the top. The bottom bar allows switching between tasks and shopping.
5. **Action:** A user adds a task. The `fragment_tasks` class creates a `Task` object, sends it to Firebase, and the `TaskAdapter` immediately updates the screen for everyone in that family.

# 7. Extras
- **Database:** Uses Firebase Realtime Database for instant synchronization across all family members.
- **Local Storage:** `SharedPreferences` are used to remember login status and the list of joined families.
- **Categorization:** Tasks are sorted automatically into headers like "Kitchen" or "Dog" to keep the UI organized.
- **Text-to-Speech:** Integrated in the shopping list to assist users by reading out items during shopping.
