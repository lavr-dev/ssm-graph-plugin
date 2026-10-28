# Spring State Machine Graph

Streamline your development process by visualizing complex **Spring State Machine (SSM)** configurations directly within your IDE. This plugin provides a clear, graphical representation of your state machine logic, eliminating the need to manually trace transitions through lines of code.

## 🚀 How it Works

Unlike traditional tools that rely on static code analysis, this plugin utilizes a **Java Agent** to capture the actual creation of State Machines during runtime. This ensures that the visualization reflects the *real* structure of your SSM, exactly as it is initialized by the Spring context.

It works seamlessly with:
*   **Local application startups**
*   **Unit Tests** involving SSM

## ✨ Key Features

*   **Interactive Graph Rendering:** Automatically transforms states and transitions into an intuitive visual map.
*   **Runtime Capture:** Captures the SSM structure at the moment of creation via a dedicated agent.
*   **Advanced Toolset:** A dedicated UI panel to organize, and analyze your structure.
*   **Direct DOT Integration:** Generates standard `.dot` files for manual edits or version control.

## 🛠 Getting Started

Follow these steps to visualize your configuration:

1.  **Enable Capture:** Open the plugin panel and check the **"Catch the SSM launch and draw graph"** box.
2.  **Launch Your Project:** Run your application locally or execute a **Unit Test**. The Java Agent will intercept the State Machine creation.
3.  **Access Diagrams:** A folder named `ssmGraph` will be created in your project's root directory. The generated `.dot` files will open automatically in the IDE panel.
4.  **Customize:** Use the built-in toolbar to:
    *   **Exclusions:** Hide specific states to simplify the diagram.
    *   **Colors:** Assign custom colors to important states.
    *   **Manual Edit:** Modify the `.dot` source code directly.
5.  **Synchronize:** Click the **"Sync Dot"** button to apply changes and refresh the visual diagram.