# **Assignment Solver**

*A Java library for solving the Assignment Problem using cost minimization or productivity maximization.*

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)  
[![Java](https://img.shields.io/badge/java-21%2B-orange.svg)](https://www.oracle.com/java/)

## **Overview**

The **Assignment Solver** is a Java library that solves the **Assignment Problem** efficiently using the **Hungarian
Algorithm**. It supports both:

✔ **Cost minimization** (e.g., minimizing task assignment costs).  
✔ **Productivity maximization** (e.g., maximizing worker efficiency).

---

## **Installation**

To include this library in your project, add the following dependency to your build file:

### **Maven**

Add the JitPack repository to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then, add the dependency:

```xml

<dependency>
    <groupId>io.github.rivon0507</groupId>
    <artifactId>or-assignment-problem</artifactId>
    <version>0.1.0</version>
</dependency>
```

### **Gradle**

Add the JitPack repository to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Then, add the dependency:

```groovy
dependencies {
    implementation 'io.github.rivon0507:or-assignment-problem:0.1.0'
}
```

### **Manual**

1. Download the latest **JAR** from [Releases](https://github.com/rivon0507/assignment-solver/releases).
2. Add it to your **classpath**.

---

## **Usage**

### **Basic Example**

```java
import io.github.rivon0507.or.assignmentproblem.AssignmentSolver;

public class Main {
    public static void main(String[] args) {
        AssignmentSolver solver = new AssignmentSolver();
        int[][] costMatrix = {{9, 2, 7}, {6, 4, 3}, {5, 8, 1}};
        solver.configure(costMatrix, AssignmentSolver.OptimizationType.MINIMIZE);
        solver.solve();
        if (solver.isSolved()) {
            int[] optimalAssignment = solver.getSolution();
            for (int i = 0; i < optimalAssignment.length; i++) {
                System.out.printf("Employee %d is assigned to task %d%n", i, optimalAssignment[i]);
            }
            System.out.println("Optimal value: " + solver.getOptimalValue());
        }
    }
}
```

---

## **Features**

✔ Supports **both cost minimization & productivity maximization**

✔ Works with **square cost matrices**

✔ Simple, intuitive API

---

## **License**

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

## **Contributing**

1. Fork the repository
2. Create a new branch (`git checkout -b feat/feature-name`)
3. Commit changes (`git commit -m "Add new feature"`)
4. Push to your branch (`git push origin feat/feature-name`)
5. Open a **pull request**

---

## **Author**

Developed by **[rivon0507](https://github.com/rivon0507)**.
