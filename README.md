# SP25_Gruppe6_fshaye_geilmann_manafov

## Development Setup

This project requires Java 24 and Maven. Follow these steps to set up your development environment:

### 1. Install JDK 24

*   Download and install JDK 24. You can find installers for your operating system from sources like:
    *   [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
    *   [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=24)
    *   [OpenJDK builds](https://jdk.java.net/24/)
*   Ensure that JDK 24 is correctly installed and that the `JAVA_HOME` environment variable is set, or that the JDK's `bin` directory is in your system's `PATH`.

### 2. Configure Maven Toolchains

Maven needs to know where your JDK 24 installation is located. This is done using a `toolchains.xml` file.

*   **Locate your Maven settings directory:**
    *   macOS/Linux: `~/.m2/`
    *   Windows: `%USERPROFILE%\\.m2\\` (e.g., `C:\\Users\\YourUsername\\.m2\\`)

*   **Create or edit `toolchains.xml`:**
    If the file doesn't exist, create it. Add the following content, replacing `/path/to/your/jdk-24` with the actual installation path of your JDK 24.

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <toolchains>
      <toolchain>
        <type>jdk</type>
        <provides>
          <version>24</version>
        </provides>
        <configuration>
          <jdkHome>/path/to/your/jdk-24</jdkHome>
        </configuration>
      </toolchain>
    </toolchains>
    ```

*   **Finding your JDK 24 installation path (`jdkHome`):**
    *   **macOS:** Typically, JDKs are installed in `/Library/Java/JavaVirtualMachines/`. For JDK 24, the path might look like `/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home`. You can often find it by running `/usr/libexec/java_home -v 24` in the terminal.
    *   **Linux:** Paths vary depending on the installation method (e.g., package manager, manual download). Common locations include `/usr/lib/jvm/`, `/opt/jdk/`, or a user-specific directory. Use commands like `update-alternatives --config java` (Debian/Ubuntu) or check where your package manager installed it.
    *   **Windows:** Typically, JDKs are installed in `C:\\Program Files\\Java\\` or `C:\\Program Files (x86)\\Java\\`. The path might look like `C:\\Program Files\\Java\\jdk-24`.

### 3. Build and Run the Project

Once JDK 24 is installed and `toolchains.xml` is configured, you can build and run the project using Maven:

1.  Navigate to the project's root directory (where `pom.xml` is located, i.e., `SoSe25` in this repository).
    ```bash
    cd SoSe25
    ```
2.  Clean and run the JavaFX application:
    ```bash
    mvn clean javafx:run
    ```

### 4. Alternative Ways to Run the Application

#### Using the launch script

We've created a convenient launch script that makes it easy to run the application:

1. Navigate to the SoSe25 directory:
   ```bash
   cd /Users/zuhalsicim/sp25_gruppe6_fshaye_geilmann_manafov/SoSe25
   ```

2. Run the launch script:
   ```bash
   ./launch.sh
   ```

The script will automatically compile the application if needed and launch it using Maven's JavaFX plugin.

#### Using the VS Code Task

If you're using Visual Studio Code, you can run the application using the predefined task:

1. Open the Command Palette (Cmd+Shift+P)
2. Type "Tasks: Run Task" and select it
3. Choose "Run JavaFX Application"

#### Using IntelliJ IDEA

If you're using IntelliJ IDEA, follow these steps to run the application:

1. Open the project in IntelliJ IDEA:
   - Select "Open" or "Import Project"
   - Navigate to the `/Users/zuhalsicim/sp25_gruppe6_fshaye_geilmann_manafov/SoSe25` directory
   - Select the `pom.xml` file and choose "Open as Project"

2. Create a Run Configuration:
   - Click on "Add Configuration" or "Edit Configurations" in the top-right corner
   - Click the "+" button to add a new configuration
   - Select "Maven"
   - Set the following parameters:
     - Name: `Run UserStoryApp`
     - Command line: `javafx:run`
     - Working directory: `/Users/zuhalsicim/sp25_gruppe6_fshaye_geilmann_manafov/SoSe25`
   - Click "OK" to save the configuration

3. Run the application:
   - Click the green "Run" button in the toolbar
   - Alternatively, use the keyboard shortcut Ctrl+R (Windows/Linux) or Cmd+R (macOS)

This setup ensures that Maven uses JDK 24 for compiling and running the project, as specified in the `pom.xml`.

## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.uni-marburg.de/manafov/sp25_gruppe6_fshaye_geilmann_manafov.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitlab.uni-marburg.de/manafov/sp25_gruppe6_fshaye_geilmann_manafov/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
