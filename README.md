# IJSON - IntelliJ IDEA JSON

Project name: **IJSON**

A full-featured IntelliJ IDEA plugin for validating, formatting, interactively editing, creating, and generating JSON.

## Features

### 1. Validate JSON
- Validate whether the selected text or current file contents contain valid JSON.
- Supports lenient parsing (comments, single quotes, and more).
- Shortcut: `Ctrl + Alt + J`, then `V`
- Menu: Tools → IJSON → Validate JSON, or the editor context menu

### 2. Format JSON (Interactive Tree Editing)
- Pretty-print JSON.
- **Interactive tree view**:
  - **Collapse and expand** nodes.
  - **Add nodes** (Object / Array / String / Number / Boolean / Null).
  - **Delete nodes**.
  - Double-click a leaf node to edit its value.
  - Use the context menu for quick actions.
- Write the formatted result back to the editor, or continue editing in the IJSON tool window on the right.
- Shortcut: `Ctrl + Alt + J`, then `B`

### 3. Create / Generate JSON
- **Generate from templates**: sample JSON or an empty Object.
- **Generate from input**:
  - Supports `key=value` or `key:value` syntax (one entry per line or comma-separated).
  - Automatically detects strings, numbers, booleans, and null.
  - Plain descriptive text generates JSON with a sample structure.
- Insert the generated JSON into the current editor, open it as a new file, or copy it to the clipboard.
- Shortcut: `Ctrl + Alt + J`, then `N`

### 4. IJSON Tool Window
- Open the **IJSON** tool window on the right.
- Includes an input area, interactive tree editor, and one-click validation, formatting, export, copy, and generation.

## Project Structure

```
IJSON/
├── build.gradle.kts          # Gradle build script (IntelliJ Platform Plugin 2.x)
├── settings.gradle.kts
├── gradle.properties
├── src/main/
│   ├── java/com/ijson/plugin/
│   │   ├── actions/          # Menu and shortcut actions
│   │   │   ├── ValidateJsonAction.java
│   │   │   ├── BeautifyJsonAction.java
│   │   │   ├── NewJsonAction.java
│   │   │   └── OpenJsonToolWindowAction.java
│   │   ├── model/
│   │   │   └── JsonTreeNode.java   # Editable tree node model
│   │   ├── ui/
│   │   │   ├── JsonTreePanel.java          # Core interactive tree panel
│   │   │   ├── JsonToolWindowFactory.java
│   │   │   ├── JsonToolWindowPanel.java
│   │   │   └── NewJsonDialog.java
│   │   └── utils/
│   │       └── JsonUtils.java              # Jackson utilities
│   └── resources/META-INF/
│       └── plugin.xml
└── README.md
```

## Requirements

- JDK 17+
- IntelliJ IDEA 2024.3 or later (since-build 243)
- Gradle 8.2+ (using the Wrapper is recommended)

## Build and Run

### 1. Import the project
Open the `IJSON` directory in IntelliJ IDEA and import it as a Gradle project.

### 2. Run the plugin (development)
In the Gradle tool window, find and run:

```
intellijPlatform → runIde
```

Alternatively, run the preconfigured **Run Plugin** configuration. This starts an IDE sandbox with the plugin installed.

### 3. Build the plugin package
```bash
./gradlew buildPlugin
```
The generated ZIP is located at `build/distributions/IJSON-1.0.0.zip`. Install it in the IDE via **Settings → Plugins → gear icon → Install Plugin from Disk**.

### 4. Verify the plugin
```bash
./gradlew verifyPlugin
```

## Dependencies

- Jackson Databind (JSON parsing and serialization)
- IntelliJ Platform SDK

## Usage Examples

1. Paste JSON into the editor, select it, then right-click → **IJSON → Format JSON (Interactive Tree Editing)**.
2. In the tree dialog, expand or collapse nodes, right-click to add or delete fields, then confirm to write the result back to the editor.
3. Go to Tools → IJSON → Create / Generate JSON and enter `name=John Doe,age=25,active=true` to generate JSON.
4. Open the **IJSON** tool window on the right for continued editing and validation.

## License

MIT (modifiable as needed)

---

**IJSON Team**  
Enjoy a more efficient JSON workflow!
