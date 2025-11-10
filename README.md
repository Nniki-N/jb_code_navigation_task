# jb_code_navigation_task

# Code Search IntelliJ Plugin

A simple IntelliJ IDEA plugin that allows searching for a string in all files of a given directory. Results are shown in a custom Tool Window in real-time.

## Build & Run Instructions

All commands for building and running the plugin should be executed from the root directory of the project. You do not need to go into Task3 directory.

### 1. Clean old Build outputs

```bash
./gradlew clean
```

### 2. (Optional) Refresh Dependencies
```bash
./gradlew --refresh-dependencies
```

### 3. Build the Plugin

```bash
./gradlew buildPlugin
```

### 4. Run the Plugin
```bash
./gradlew runIde
```

You can find the plugin Tool Window “Text Search” on the right panel.

### 5. Using the Plugin

- Enter the directory path in the first text field.

- Enter the string to search in the second text field.

- Click Start Search to begin searching.

- Results will appear in real-time in the text area.

- Click Cancel Search to stop searching.
