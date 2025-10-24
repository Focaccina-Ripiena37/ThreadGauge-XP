# ThreadGauge XP 🧵💻

**A Java desktop application for exploring thread behavior and system limits with a classic Windows XP-style interface.**

![Java](https://img.shields.io/badge/Java-21_LTS-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)

---

## 📖 Overview

ThreadGauge XP is a lightweight desktop application that helps developers and system administrators understand Java thread behavior, memory consumption, and system limits. Built with **Java 21 LTS** and **Swing**, it features a nostalgic Windows XP-inspired user interface while providing powerful thread analysis capabilities.

### Key Features

- **Max Thread Discovery**: Empirically find the maximum number of threads your system can handle
- **Memory Estimation**: Calculate per-thread memory consumption with configurable stack sizes
- **Stress Testing**: Run controlled load tests with customizable thread counts and durations
- **Live Telemetry**: Real-time monitoring of active threads, heap usage, and CPU load
- **Export Results**: Save test results to TXT or CSV format for analysis
- **Safe Operation**: Built-in safety caps and graceful error handling (OutOfMemoryError, interruptions)
- **Classic XP UI**: Tahoma fonts, soft blue color palette, and familiar Windows XP styling

---

## 🎯 What It Does

ThreadGauge XP helps answer questions like:

- How many threads can my JVM create before hitting system limits?
- How much memory does each thread consume with different stack sizes?
- What happens to CPU load under thread stress?
- How do different JVM configurations affect thread capacity?

The app is designed for:
- **Java developers** optimizing multi-threaded applications
- **System administrators** capacity planning for Java deployments
- **Educators** teaching concurrency concepts
- **Researchers** studying JVM behavior across platforms

---

## 📋 Requirements

- **Java Development Kit (JDK) 21 LTS** or later
  - Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://jdk.java.net/25/)
- **Operating System**: Windows, macOS, or Linux
- **No additional dependencies** - uses only standard JDK libraries

### Verify Java Installation

```bash
java -version
```

Expected output should show Java 21 or higher:
```
java version "21" 2023-09-19
Java(TM) SE Runtime Environment (build 21+35)
```

---

## 🚀 Quick Start

### Option 1: Run with Gradle Wrapper (Recommended)

```bash
# Clone or download the repository
git clone https://github.com/Focaccina-Ripiena37/ThreadGauge-XP.git
cd ThreadGauge-XP

# Build
.\gradlew.bat clean build

# Build and run (Unix/macOS/Linux)
./gradlew run

# Build and run (Windows)
.\gradlew.bat run

# Verify Java in use by Gradle (should resolve/download JDK 21 automatically)
.\gradlew.bat -version
```

### Option 2: Build JAR and Run

```bash
# Build the executable JAR
./gradlew clean build

# Run the JAR
java -jar build/libs/ThreadGauge-XP.jar
```

### Option 3: Run from IDE

1. Import the project into your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Ensure Java 21 toolchain is configured
3. Run the main class: `dev.threadgaugexp.MainWindow`

---

## 🔧 Build Instructions

### Building from Source

The project uses Gradle with the **Gradle Wrapper**, so you don't need Gradle pre-installed.

```bash
# Clean and build
./gradlew clean build

# (Tests removed)
No unit tests are shipped in this release build.

# Create distribution
./gradlew jar
```

The compiled JAR will be located at:
```
build/libs/ThreadGauge-XP.jar
```

### Manual Compilation (without Gradle)

If you prefer to compile manually:

```bash
# Create output directory
mkdir -p build/classes

# Compile all Java files
javac -d build/classes \
   --release 21 \
  $(find src/main/java -name "*.java")

# Create JAR with manifest
jar cfe build/libs/ThreadGauge-XP.jar \
  dev.threadgaugexp.MainWindow \
  -C build/classes .

# Run
java -jar build/libs/ThreadGauge-XP.jar
```

---

## 📱 User Interface Guide

### System Information Panel (Top)
- Displays OS, JVM (App) version + implementation (HotSpot/OpenJ9/GraalVM), CPU cores, physical RAM (total/free), and System Java (dal PATH, se abilitata)
- Updates on startup

### Controls Panel (Middle Left)
- **Stack Size**: Configure thread stack size (128 KB - 8192 KB)
- **Find Max Threads**: Start maximum thread discovery test
- **Stress Threads/Duration**: Configure stress test parameters
- **Run Stress Test**: Execute controlled load test
- **Stop Test**: Cancel running test (graceful shutdown)
- **Export Results**: Save test data to file

### Telemetry Panel (Middle Right)
- **Active Threads**: Current approximate JVM thread count (updates every 500ms)
- **Heap Used/Committed/Max**: JVM heap usage; progress shows used/max and used/committed
- **CPU Load**: System CPU usage with visual indicator
- Color-coded warnings (green → yellow → red based on usage)

### Output Log (Bottom)
- Timestamped event log
- Test results and error messages
- Clear Log button for cleanup

### Status Bar (Bottom)
- Shows current application state
- "Ready" when idle, descriptive messages during tests

---

## 🧪 Usage Examples

### Finding Maximum Threads

1. Set desired **Stack Size** (default: 512 KB)
2. Click **Find Max Threads**
3. Wait for test to complete (may take 1-3 minutes)
4. Results show:
   - Maximum thread count reached
   - Memory per thread estimate
   - Stop reason (safety cap, memory limit, etc.)

**Note**: The test respects safety caps (50,000 threads) and stops when free heap drops below 50 MB.

### Running a Stress Test

1. Configure:
   - **Stress Threads**: Number of worker threads (10-10,000)
   - **Duration**: Test length in seconds (1-60)
2. Click **Run Stress Test**
3. Monitor CPU and memory in real-time
4. Results show:
   - Actual duration
   - Average CPU load during test
   - Thread lifecycle stats

### Exporting Results

1. Click **Export Results**
2. Choose format:
   - **TXT**: Detailed report with full log
   - **CSV**: Structured data for spreadsheet analysis
3. Select save location
4. File contains:
   - System information
   - Test parameters
   - Results and metrics
   - Timestamp

---

## 🛡️ Safety Features

ThreadGauge XP includes multiple safety mechanisms:

1. **Upper Bounds**
   - 50,000 thread safety cap (configurable in code)
   - Minimum free heap threshold (50 MB)

2. **Error Handling**
   - Catches `OutOfMemoryError` gracefully
   - Handles `StackOverflowError` without crashing
   - Continues operation after test failures

3. **Cancellation**
   - All tests can be stopped mid-execution
   - Threads are interrupted cleanly
   - Partial results are reported

4. **Non-Blocking UI**
   - All tests run in background workers (`SwingWorker`)
   - UI remains responsive during tests
   - Progress indicators show activity

---

## 📊 Understanding Results

### Max Thread Count

The maximum number of threads varies based on:
- **Stack size**: Smaller stacks → more threads
- **Heap size**: More heap → more threads
- **OS limits**: Windows, Linux, macOS have different caps
- **System RAM**: Physical memory constraints

**Typical ranges:**
- Small stack (128 KB): 10,000 - 50,000+ threads
- Default stack (512 KB - 1 MB): 5,000 - 20,000 threads
- Large stack (2+ MB): 1,000 - 5,000 threads

### Memory Per Thread

Includes:
- Thread object overhead (~1 KB)
- Stack size (as configured)
- JVM internal structures

**Example**: With 512 KB stack, expect ~520-550 KB per thread.

### CPU Load

- **0-30%**: Light load, normal operation
- **30-60%**: Moderate load, efficient threading
- **60-80%**: Heavy load, potential contention
- **80-100%**: Maximum load, possible thrashing

---

## 🔍 Technical Details

### Threading Model

- **Test Threads**: Minimal `Runnable` with sleep loop
- **Background Execution**: `SwingWorker` for non-blocking tests
- **Interruption**: Cooperative cancellation via interrupt flags
- **Cleanup**: All threads joined/terminated after tests

### Memory Measurement

1. Force garbage collection (`System.gc()`)
2. Wait for GC to stabilize (100-200ms)
3. Record heap usage before thread creation
4. Create N threads
5. Record heap usage after creation
6. Calculate delta: `(after - before) / N`

**Limitations**: GC timing is not guaranteed; results are estimates.

### CPU Monitoring

Uses `com.sun.management.OperatingSystemMXBean`:
- `getCpuLoad()`: System-wide CPU usage (0.0 - 1.0)
- Sampled every 500ms
- May not be available on all platforms (shows "N/A")

---

## 📸 Screenshots

### Main Window
<img width="879" height="684" alt="image" src="https://github.com/user-attachments/assets/0dc6aa73-d7f2-40f8-ac6f-12da614aa936" />

---

## 🤝 Contributing

Contributions are welcome! This is an educational tool, and improvements are encouraged:

- **Bug reports**: Open an issue with details
- **Feature requests**: Describe the use case
- **Pull requests**: Fork, create a branch, submit PR

### Development Setup

1. Fork and clone the repository
2. Ensure Java 21 LTS is installed
3. Import into your IDE
4. Make changes
5. Test thoroughly on multiple platforms
6. Submit PR with clear description

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2025 ThreadGauge XP

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📚 Additional Resources

- **Java Concurrency**: [Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- **Thread Stack Size**: [JVM Options Reference](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html)
- **Swing Tutorial**: [Oracle Swing Guide](https://docs.oracle.com/javase/tutorial/uiswing/)

---

## 🙏 Acknowledgments

- Inspired by classic Windows XP design language
- Built with Java 21 LTS and Swing
- Uses only standard JDK libraries (no external dependencies)

---

## 📧 Support

For issues, questions, or feedback:
- **GitHub Issues**: [Report a bug](https://github.com/Focaccina-Ripiena37/ThreadGauge-XP/issues)
- **Discussions**: Share ideas and ask questions

---

**Made with ☕ and nostalgia for Windows XP** 🧵💻
