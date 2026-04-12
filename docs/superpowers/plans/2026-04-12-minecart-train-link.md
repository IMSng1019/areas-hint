# Minecart Train Link Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a server-side minecart train-link subsystem that measures link length along live rail paths, rebinds to current switch state, breaks immediately on topology loss, and suppresses traction while a car is moving opposite the furnace-minecart direction.

**Architecture:** Keep the path, history, evaluation, and traction rules in pure Java classes under `areahint.train` so they can be tested with JUnit without a Minecraft runtime. Add a thin world adapter that turns live rails and minecart snapshots into `RailPathResult` values, then wire it into a single mixin on `AbstractMinecartEntity` so every server-side minecart tick records history and applies link logic through one runtime manager.

**Tech Stack:** Fabric Loom 1.20.4, Java 17, Sponge Mixin, JUnit 5

---

## Scope and assumptions

- This repository contains no existing minecart-link code, so this plan creates a greenfield subsystem under `src/main/java/areahint/train/` and only touches the existing mod bootstrap and mixin config.
- User-facing coupling creation is intentionally out of scope. Other code can establish or remove links later through `TrainLinkService.link(...)` and `TrainLinkService.unlink(...)`.
- `TrainLinkRules.defaults()` uses a local concrete default (`8.0D`) so the subsystem is runnable in this repository. If this subsystem is later transplanted into the actual train mod that already has a straight-line break constant, replace only that literal and keep the rest of the plan unchanged.
- No client code is needed for this feature.

## File structure

### Existing files to modify

- `build.gradle:36-47` and append a new `test { useJUnitPlatform() }` block near the bottom — add JUnit 5 dependencies and enable the test task.
- `src/main/java/areahint/Areashint.java:45-136, 176-183` — initialize and shut down the train-link bootstrap with the rest of the server lifecycle.
- `src/main/resources/areas-hint.mixins.json:5-7` — register the minecart runtime mixin.

### New pure model and logic files

- `src/main/java/areahint/train/model/LinkStatus.java` — enum for `FOLLOWING`, `REBINDING`, `REVERSE_COAST`, `BROKEN_BY_LENGTH`, `BROKEN_BY_TOPOLOGY`.
- `src/main/java/areahint/train/model/TrainLinkRules.java` — immutable runtime thresholds (`maxPathLength`, `reverseEpsilon`, `historyWindowSize`).
- `src/main/java/areahint/train/model/RailAnchor.java` — current rail block plus entry direction.
- `src/main/java/areahint/train/model/RailContinuation.java` — next anchor, exit direction, and segment length.
- `src/main/java/areahint/train/model/RailPathSegment.java` — recorded history segment for one rail block.
- `src/main/java/areahint/train/model/RailPathResult.java` — connected/disconnected path result with total length and first-step direction.
- `src/main/java/areahint/train/model/LinkEvaluation.java` — final per-tick decision produced by the evaluator.
- `src/main/java/areahint/train/logic/HistoryPathBuffer.java` — bounded rail-history cache keyed by visited rail block.
- `src/main/java/areahint/train/logic/RailPathWalker.java` — walks a current live rail path using a `RailLookup` interface.
- `src/main/java/areahint/train/logic/LinkEvaluator.java` — classifies topology break, length break, rebind, follow, and reverse-coast states.
- `src/main/java/areahint/train/logic/TractionController.java` — converts a `LinkEvaluation` into either zero acceleration or a normalized traction vector.

### New runtime integration files

- `src/main/java/areahint/train/TrainLinkService.java` — public API for future coupling code.
- `src/main/java/areahint/train/TrainLinkBootstrap.java` — singleton bootstrap that owns the runtime manager.
- `src/main/java/areahint/train/runtime/MinecartRuntimeView.java` — small interface the manager can tick without depending on raw entities in tests.
- `src/main/java/areahint/train/runtime/LinkedCartState.java` — immutable follower→leader/furnace relationship.
- `src/main/java/areahint/train/runtime/LinkRegistry.java` — stores active links.
- `src/main/java/areahint/train/runtime/RailPathService.java` — world-facing interface that resolves current paths and history directions.
- `src/main/java/areahint/train/runtime/TrainLinkManager.java` — orchestrates history recording, path resolution, evaluation, unlinking, and velocity updates.
- `src/main/java/areahint/train/runtime/MinecraftMinecartView.java` — adapts `AbstractMinecartEntity` into `MinecartRuntimeView`.
- `src/main/java/areahint/train/runtime/WorldRailPathService.java` — resolves live rail continuations from `ServerWorld` block states and maintains history per cart.
- `src/main/java/areahint/mixin/AbstractMinecartEntityMixin.java` — server-side tick hook that forwards each minecart tick to `TrainLinkBootstrap.get().tick(...)`.

### New test files

- `src/test/java/areahint/train/model/RailPathResultTest.java`
- `src/test/java/areahint/train/logic/HistoryPathBufferTest.java`
- `src/test/java/areahint/train/logic/RailPathWalkerTest.java`
- `src/test/java/areahint/train/logic/LinkEvaluatorTest.java`
- `src/test/java/areahint/train/runtime/TrainLinkManagerTest.java`
- `src/test/java/areahint/train/TrainLinkBootstrapTest.java`

---

### Task 1: Enable tests and add the immutable path model

**Files:**
- Modify: `build.gradle:36-47`
- Modify: `build.gradle` (append a `test` block near the end)
- Create: `src/main/java/areahint/train/model/LinkStatus.java`
- Create: `src/main/java/areahint/train/model/TrainLinkRules.java`
- Create: `src/main/java/areahint/train/model/RailAnchor.java`
- Create: `src/main/java/areahint/train/model/RailContinuation.java`
- Create: `src/main/java/areahint/train/model/RailPathSegment.java`
- Create: `src/main/java/areahint/train/model/RailPathResult.java`
- Test: `src/test/java/areahint/train/model/RailPathResultTest.java`

- [ ] **Step 1: Write the failing model test**

```java
package areahint.train.model;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RailPathResultTest {
    @Test
    void brokenByTopologyCreatesDisconnectedResult() {
        RailPathResult result = RailPathResult.brokenByTopology();

        assertFalse(result.connected());
        assertEquals(0.0D, result.length());
        assertEquals(Vec3d.ZERO, result.directionTowardTarget());
        assertTrue(result.segments().isEmpty());
    }

    @Test
    void connectedFactoryPreservesLengthAndSegments() {
        RailPathSegment segment = new RailPathSegment(
            new BlockPos(1, 64, 1),
            Direction.WEST,
            Direction.EAST,
            1.0D,
            20L
        );

        RailPathResult result = RailPathResult.connected(
            List.of(segment),
            1.0D,
            new Vec3d(1.0D, 0.0D, 0.0D)
        );

        assertTrue(result.connected());
        assertEquals(1.0D, result.length());
        assertEquals(Direction.EAST, result.segments().get(0).exitDirection());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "areahint.train.model.RailPathResultTest"`
Expected: FAIL with messages like `package areahint.train.model does not exist` and `cannot find symbol RailPathResult`.

- [ ] **Step 3: Add JUnit and the new immutable model files**

`build.gradle`

```groovy
dependencies {
	// To change the versions see the gradle.properties file
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
	modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
	compileOnly "net.luckperms:api:5.4"
	compileOnly "de.bluecolored.bluemap:BlueMapAPI:2.7.2"

	testImplementation platform("org.junit:junit-bom:5.10.2")
	testImplementation "org.junit.jupiter:junit-jupiter"
}

test {
	useJUnitPlatform()
}
```

`src/main/java/areahint/train/model/LinkStatus.java`

```java
package areahint.train.model;

public enum LinkStatus {
    FOLLOWING,
    REBINDING,
    REVERSE_COAST,
    BROKEN_BY_LENGTH,
    BROKEN_BY_TOPOLOGY
}
```

`src/main/java/areahint/train/model/TrainLinkRules.java`

```java
package areahint.train.model;

public record TrainLinkRules(double maxPathLength, double reverseEpsilon, int historyWindowSize) {
    public static TrainLinkRules defaults() {
        return new TrainLinkRules(8.0D, 0.02D, 64);
    }
}
```

`src/main/java/areahint/train/model/RailAnchor.java`

```java
package areahint.train.model;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record RailAnchor(BlockPos railPos, Direction entryDirection) {
}
```

`src/main/java/areahint/train/model/RailContinuation.java`

```java
package areahint.train.model;

import net.minecraft.util.math.Direction;

public record RailContinuation(RailAnchor nextAnchor, Direction exitDirection, double segmentLength) {
}
```

`src/main/java/areahint/train/model/RailPathSegment.java`

```java
package areahint.train.model;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record RailPathSegment(
    BlockPos railPos,
    Direction entryDirection,
    Direction exitDirection,
    double length,
    long tick
) {
}
```

`src/main/java/areahint/train/model/RailPathResult.java`

```java
package areahint.train.model;

import net.minecraft.util.math.Vec3d;

import java.util.List;

public record RailPathResult(
    boolean connected,
    List<RailPathSegment> segments,
    double length,
    Vec3d directionTowardTarget
) {
    public static RailPathResult brokenByTopology() {
        return new RailPathResult(false, List.of(), 0.0D, Vec3d.ZERO);
    }

    public static RailPathResult connected(List<RailPathSegment> segments, double length, Vec3d directionTowardTarget) {
        return new RailPathResult(true, List.copyOf(segments), length, directionTowardTarget);
    }
}
```

- [ ] **Step 4: Run the model test again**

Run: `./gradlew test --tests "areahint.train.model.RailPathResultTest"`
Expected: PASS.

- [ ] **Step 5: Commit the model foundation**

```bash
git add build.gradle \
  src/main/java/areahint/train/model/LinkStatus.java \
  src/main/java/areahint/train/model/TrainLinkRules.java \
  src/main/java/areahint/train/model/RailAnchor.java \
  src/main/java/areahint/train/model/RailContinuation.java \
  src/main/java/areahint/train/model/RailPathSegment.java \
  src/main/java/areahint/train/model/RailPathResult.java \
  src/test/java/areahint/train/model/RailPathResultTest.java

git commit -m "test: add train link path model foundation"
```

### Task 2: Add the bounded history buffer

**Files:**
- Create: `src/main/java/areahint/train/logic/HistoryPathBuffer.java`
- Test: `src/test/java/areahint/train/logic/HistoryPathBufferTest.java`

- [ ] **Step 1: Write the failing history-buffer test**

```java
package areahint.train.logic;

import areahint.train.model.RailPathSegment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryPathBufferTest {
    @Test
    void keepsOnlyNewestSegmentsInsideWindow() {
        HistoryPathBuffer buffer = new HistoryPathBuffer();

        buffer.append(new RailPathSegment(new BlockPos(0, 64, 0), Direction.WEST, Direction.EAST, 1.0D, 1L), 2);
        buffer.append(new RailPathSegment(new BlockPos(1, 64, 0), Direction.WEST, Direction.EAST, 1.0D, 2L), 2);
        buffer.append(new RailPathSegment(new BlockPos(2, 64, 0), Direction.WEST, Direction.EAST, 1.0D, 3L), 2);

        assertEquals(2, buffer.snapshot().size());
        assertEquals(new BlockPos(1, 64, 0), buffer.snapshot().get(0).railPos());
        assertEquals(new BlockPos(2, 64, 0), buffer.snapshot().get(1).railPos());
    }

    @Test
    void returnsNewestDirectionForMatchingRailBlock() {
        HistoryPathBuffer buffer = new HistoryPathBuffer();
        BlockPos railPos = new BlockPos(5, 64, 5);

        buffer.append(new RailPathSegment(railPos, Direction.WEST, Direction.EAST, 1.0D, 10L), 8);

        assertEquals(new Vec3d(1.0D, 0.0D, 0.0D), buffer.directionAt(railPos).orElseThrow());
        assertTrue(buffer.directionAt(new BlockPos(99, 64, 99)).isEmpty());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "areahint.train.logic.HistoryPathBufferTest"`
Expected: FAIL with `cannot find symbol HistoryPathBuffer`.

- [ ] **Step 3: Implement the bounded history buffer**

`src/main/java/areahint/train/logic/HistoryPathBuffer.java`

```java
package areahint.train.logic;

import areahint.train.model.RailPathSegment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HistoryPathBuffer {
    private final ArrayDeque<RailPathSegment> segments = new ArrayDeque<>();

    public void append(RailPathSegment segment, int maxSize) {
        segments.addLast(segment);
        while (segments.size() > maxSize) {
            segments.removeFirst();
        }
    }

    public Optional<Vec3d> directionAt(BlockPos railPos) {
        List<RailPathSegment> snapshot = snapshot();
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            RailPathSegment segment = snapshot.get(i);
            if (segment.railPos().equals(railPos)) {
                return Optional.of(Vec3d.of(segment.exitDirection().getVector()));
            }
        }
        return Optional.empty();
    }

    public List<RailPathSegment> snapshot() {
        return new ArrayList<>(segments);
    }
}
```

- [ ] **Step 4: Run the history-buffer test again**

Run: `./gradlew test --tests "areahint.train.logic.HistoryPathBufferTest"`
Expected: PASS.

- [ ] **Step 5: Commit the history-buffer work**

```bash
git add src/main/java/areahint/train/logic/HistoryPathBuffer.java \
  src/test/java/areahint/train/logic/HistoryPathBufferTest.java

git commit -m "test: add bounded train history buffer"
```

### Task 3: Implement live rail-path walking

**Files:**
- Create: `src/main/java/areahint/train/logic/RailPathWalker.java`
- Test: `src/test/java/areahint/train/logic/RailPathWalkerTest.java`

- [ ] **Step 1: Write the failing rail-walker test**

```java
package areahint.train.logic;

import areahint.train.model.RailAnchor;
import areahint.train.model.RailContinuation;
import areahint.train.model.RailPathResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RailPathWalkerTest {
    @Test
    void walksUntilTargetAndAccumulatesTrackLength() {
        RailPathWalker walker = new RailPathWalker();
        FakeLookup lookup = new FakeLookup(new BlockPos(2, 64, 0));

        lookup.put(new RailAnchor(new BlockPos(0, 64, 0), Direction.WEST), new RailContinuation(new RailAnchor(new BlockPos(1, 64, 0), Direction.WEST), Direction.EAST, 1.0D));
        lookup.put(new RailAnchor(new BlockPos(1, 64, 0), Direction.WEST), new RailContinuation(new RailAnchor(new BlockPos(2, 64, 0), Direction.WEST), Direction.EAST, 1.0D));

        RailPathResult result = walker.walk(new RailAnchor(new BlockPos(0, 64, 0), Direction.WEST), lookup, 8, 100L);

        assertTrue(result.connected());
        assertEquals(2.0D, result.length());
        assertEquals(new Vec3d(1.0D, 0.0D, 0.0D), result.directionTowardTarget());
        assertEquals(2, result.segments().size());
    }

    @Test
    void returnsTopologyBreakWhenLookupRunsOutBeforeTarget() {
        RailPathWalker walker = new RailPathWalker();
        FakeLookup lookup = new FakeLookup(new BlockPos(5, 64, 0));

        lookup.put(new RailAnchor(new BlockPos(0, 64, 0), Direction.WEST), new RailContinuation(new RailAnchor(new BlockPos(1, 64, 0), Direction.WEST), Direction.EAST, 1.0D));

        RailPathResult result = walker.walk(new RailAnchor(new BlockPos(0, 64, 0), Direction.WEST), lookup, 8, 100L);

        assertFalse(result.connected());
        assertEquals(0.0D, result.length());
    }

    private static final class FakeLookup implements RailPathWalker.RailLookup {
        private final Map<RailAnchor, RailContinuation> continuations = new HashMap<>();
        private final BlockPos target;

        private FakeLookup(BlockPos target) {
            this.target = target;
        }

        private void put(RailAnchor anchor, RailContinuation continuation) {
            continuations.put(anchor, continuation);
        }

        @Override
        public Optional<RailContinuation> next(RailAnchor anchor) {
            return Optional.ofNullable(continuations.get(anchor));
        }

        @Override
        public boolean isTarget(RailAnchor anchor) {
            return anchor.railPos().equals(target);
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "areahint.train.logic.RailPathWalkerTest"`
Expected: FAIL with `cannot find symbol RailPathWalker`.

- [ ] **Step 3: Implement the rail walker**

`src/main/java/areahint/train/logic/RailPathWalker.java`

```java
package areahint.train.logic;

import areahint.train.model.RailAnchor;
import areahint.train.model.RailContinuation;
import areahint.train.model.RailPathResult;
import areahint.train.model.RailPathSegment;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RailPathWalker {
    public interface RailLookup {
        Optional<RailContinuation> next(RailAnchor anchor);
        boolean isTarget(RailAnchor anchor);
    }

    public RailPathResult walk(RailAnchor start, RailLookup lookup, int maxSegments, long tick) {
        RailAnchor current = start;
        List<RailPathSegment> segments = new ArrayList<>();
        double totalLength = 0.0D;
        Vec3d firstDirection = Vec3d.ZERO;

        for (int i = 0; i < maxSegments; i++) {
            if (lookup.isTarget(current)) {
                return RailPathResult.connected(segments, totalLength, firstDirection);
            }

            Optional<RailContinuation> continuation = lookup.next(current);
            if (continuation.isEmpty()) {
                return RailPathResult.brokenByTopology();
            }

            RailContinuation next = continuation.get();
            segments.add(new RailPathSegment(current.railPos(), current.entryDirection(), next.exitDirection(), next.segmentLength(), tick));
            totalLength += next.segmentLength();

            if (segments.size() == 1) {
                firstDirection = Vec3d.of(next.exitDirection().getVector());
            }

            current = next.nextAnchor();
        }

        return RailPathResult.brokenByTopology();
    }
}
```

- [ ] **Step 4: Run the rail-walker test again**

Run: `./gradlew test --tests "areahint.train.logic.RailPathWalkerTest"`
Expected: PASS.

- [ ] **Step 5: Commit the path-walker work**

```bash
git add src/main/java/areahint/train/logic/RailPathWalker.java \
  src/test/java/areahint/train/logic/RailPathWalkerTest.java

git commit -m "test: add live rail path walker"
```

### Task 4: Implement link evaluation and traction suppression

**Files:**
- Create: `src/main/java/areahint/train/model/LinkEvaluation.java`
- Create: `src/main/java/areahint/train/logic/LinkEvaluator.java`
- Create: `src/main/java/areahint/train/logic/TractionController.java`
- Test: `src/test/java/areahint/train/logic/LinkEvaluatorTest.java`

- [ ] **Step 1: Write the failing evaluator test**

```java
package areahint.train.logic;

import areahint.train.model.LinkEvaluation;
import areahint.train.model.LinkStatus;
import areahint.train.model.RailPathResult;
import areahint.train.model.RailPathSegment;
import areahint.train.model.TrainLinkRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LinkEvaluatorTest {
    private final TrainLinkRules rules = new TrainLinkRules(3.0D, 0.02D, 16);

    @Test
    void marksDisconnectedPathAsTopologyBreak() {
        LinkEvaluator evaluator = new LinkEvaluator();

        LinkEvaluation evaluation = evaluator.evaluate(
            rules,
            RailPathResult.brokenByTopology(),
            Optional.empty(),
            Vec3d.ZERO,
            new Vec3d(1.0D, 0.0D, 0.0D)
        );

        assertEquals(LinkStatus.BROKEN_BY_TOPOLOGY, evaluation.status());
        assertFalse(evaluation.applyTraction());
    }

    @Test
    void marksLongConnectedPathAsLengthBreak() {
        LinkEvaluator evaluator = new LinkEvaluator();
        RailPathSegment segment = new RailPathSegment(new BlockPos(0, 64, 0), Direction.WEST, Direction.EAST, 4.0D, 1L);

        LinkEvaluation evaluation = evaluator.evaluate(
            rules,
            RailPathResult.connected(List.of(segment), 4.0D, new Vec3d(1.0D, 0.0D, 0.0D)),
            Optional.empty(),
            Vec3d.ZERO,
            new Vec3d(1.0D, 0.0D, 0.0D)
        );

        assertEquals(LinkStatus.BROKEN_BY_LENGTH, evaluation.status());
    }

    @Test
    void entersRebindingWhenLeaderHistoryDisagreesWithCurrentTrack() {
        LinkEvaluator evaluator = new LinkEvaluator();
        RailPathSegment segment = new RailPathSegment(new BlockPos(0, 64, 0), Direction.WEST, Direction.SOUTH, 1.0D, 1L);

        LinkEvaluation evaluation = evaluator.evaluate(
            rules,
            RailPathResult.connected(List.of(segment), 1.0D, new Vec3d(0.0D, 0.0D, 1.0D)),
            Optional.of(new Vec3d(1.0D, 0.0D, 0.0D)),
            Vec3d.ZERO,
            new Vec3d(0.0D, 0.0D, 1.0D)
        );

        assertEquals(LinkStatus.REBINDING, evaluation.status());
        assertTrue(evaluation.applyTraction());
    }

    @Test
    void entersReverseCoastWhenVelocityPointsAwayFromFurnace() {
        LinkEvaluator evaluator = new LinkEvaluator();
        RailPathSegment segment = new RailPathSegment(new BlockPos(0, 64, 0), Direction.WEST, Direction.EAST, 1.0D, 1L);

        LinkEvaluation evaluation = evaluator.evaluate(
            rules,
            RailPathResult.connected(List.of(segment), 1.0D, new Vec3d(1.0D, 0.0D, 0.0D)),
            Optional.empty(),
            new Vec3d(-0.20D, 0.0D, 0.0D),
            new Vec3d(1.0D, 0.0D, 0.0D)
        );

        assertEquals(LinkStatus.REVERSE_COAST, evaluation.status());
        assertFalse(evaluation.applyTraction());
    }

    @Test
    void tractionControllerReturnsZeroForReverseCoast() {
        TractionController controller = new TractionController();
        LinkEvaluation evaluation = new LinkEvaluation(
            LinkStatus.REVERSE_COAST,
            1.0D,
            new Vec3d(1.0D, 0.0D, 0.0D),
            new Vec3d(1.0D, 0.0D, 0.0D),
            false
        );

        assertEquals(Vec3d.ZERO, controller.computeAcceleration(evaluation, 0.08D));
    }
}
```

- [ ] **Step 2: Run the evaluator test to verify it fails**

Run: `./gradlew test --tests "areahint.train.logic.LinkEvaluatorTest"`
Expected: FAIL with `cannot find symbol LinkEvaluator`, `TractionController`, and `LinkEvaluation`.

- [ ] **Step 3: Implement evaluation and traction**

`src/main/java/areahint/train/model/LinkEvaluation.java`

```java
package areahint.train.model;

import net.minecraft.util.math.Vec3d;

public record LinkEvaluation(
    LinkStatus status,
    double pathLength,
    Vec3d followDirection,
    Vec3d furnaceDirection,
    boolean applyTraction
) {
}
```

`src/main/java/areahint/train/logic/LinkEvaluator.java`

```java
package areahint.train.logic;

import areahint.train.model.LinkEvaluation;
import areahint.train.model.LinkStatus;
import areahint.train.model.RailPathResult;
import areahint.train.model.TrainLinkRules;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class LinkEvaluator {
    public LinkEvaluation evaluate(
        TrainLinkRules rules,
        RailPathResult currentPath,
        Optional<Vec3d> leaderHistoryDirection,
        Vec3d velocity,
        Vec3d furnaceDirection
    ) {
        if (!currentPath.connected()) {
            return new LinkEvaluation(LinkStatus.BROKEN_BY_TOPOLOGY, 0.0D, Vec3d.ZERO, furnaceDirection, false);
        }

        if (currentPath.length() > rules.maxPathLength()) {
            return new LinkEvaluation(LinkStatus.BROKEN_BY_LENGTH, currentPath.length(), currentPath.directionTowardTarget(), furnaceDirection, false);
        }

        Vec3d currentDirection = normalizeOrZero(currentPath.directionTowardTarget());
        Vec3d normalizedFurnaceDirection = normalizeOrZero(furnaceDirection);

        if (velocity.dotProduct(normalizedFurnaceDirection) < -rules.reverseEpsilon()) {
            return new LinkEvaluation(LinkStatus.REVERSE_COAST, currentPath.length(), currentDirection, normalizedFurnaceDirection, false);
        }

        if (leaderHistoryDirection.isPresent()) {
            Vec3d historyDirection = normalizeOrZero(leaderHistoryDirection.get());
            if (historyDirection.dotProduct(currentDirection) < 0.95D) {
                return new LinkEvaluation(LinkStatus.REBINDING, currentPath.length(), currentDirection, normalizedFurnaceDirection, true);
            }
        }

        return new LinkEvaluation(LinkStatus.FOLLOWING, currentPath.length(), currentDirection, normalizedFurnaceDirection, true);
    }

    private Vec3d normalizeOrZero(Vec3d input) {
        return input.lengthSquared() == 0.0D ? Vec3d.ZERO : input.normalize();
    }
}
```

`src/main/java/areahint/train/logic/TractionController.java`

```java
package areahint.train.logic;

import areahint.train.model.LinkEvaluation;
import net.minecraft.util.math.Vec3d;

public final class TractionController {
    public Vec3d computeAcceleration(LinkEvaluation evaluation, double tractionStrength) {
        if (!evaluation.applyTraction() || evaluation.followDirection().lengthSquared() == 0.0D) {
            return Vec3d.ZERO;
        }
        return evaluation.followDirection().normalize().multiply(tractionStrength);
    }
}
```

- [ ] **Step 4: Run the evaluator test again**

Run: `./gradlew test --tests "areahint.train.logic.LinkEvaluatorTest"`
Expected: PASS.

- [ ] **Step 5: Commit the evaluator work**

```bash
git add src/main/java/areahint/train/model/LinkEvaluation.java \
  src/main/java/areahint/train/logic/LinkEvaluator.java \
  src/main/java/areahint/train/logic/TractionController.java \
  src/test/java/areahint/train/logic/LinkEvaluatorTest.java

git commit -m "test: add train link evaluator and traction controller"
```

### Task 5: Add the runtime manager and link registry

**Files:**
- Create: `src/main/java/areahint/train/TrainLinkService.java`
- Create: `src/main/java/areahint/train/runtime/MinecartRuntimeView.java`
- Create: `src/main/java/areahint/train/runtime/LinkedCartState.java`
- Create: `src/main/java/areahint/train/runtime/LinkRegistry.java`
- Create: `src/main/java/areahint/train/runtime/RailPathService.java`
- Create: `src/main/java/areahint/train/runtime/TrainLinkManager.java`
- Test: `src/test/java/areahint/train/runtime/TrainLinkManagerTest.java`

- [ ] **Step 1: Write the failing runtime-manager test**

```java
package areahint.train.runtime;

import areahint.train.model.RailPathResult;
import areahint.train.model.TrainLinkRules;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrainLinkManagerTest {
    private static final UUID FOLLOWER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LEADER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FURNACE = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void topologyBreakUnlinksFollowerImmediately() {
        FakeRailPathService service = new FakeRailPathService(RailPathResult.brokenByTopology(), Optional.empty(), new Vec3d(1.0D, 0.0D, 0.0D));
        TrainLinkManager manager = new TrainLinkManager(TrainLinkRules.defaults(), service, 0.08D);
        FakeMinecartRuntimeView follower = new FakeMinecartRuntimeView(FOLLOWER, new BlockPos(0, 64, 0), Vec3d.ZERO);

        manager.link(FOLLOWER, LEADER, FURNACE);
        manager.tick(follower);

        assertTrue(manager.linkedState(FOLLOWER).isEmpty());
        assertEquals(Vec3d.ZERO, follower.appliedDelta());
    }

    @Test
    void reverseCoastKeepsLinkButDoesNotApplyAcceleration() {
        FakeRailPathService service = new FakeRailPathService(
            RailPathResult.connected(java.util.List.of(), 1.0D, new Vec3d(1.0D, 0.0D, 0.0D)),
            Optional.empty(),
            new Vec3d(1.0D, 0.0D, 0.0D)
        );
        TrainLinkManager manager = new TrainLinkManager(TrainLinkRules.defaults(), service, 0.08D);
        FakeMinecartRuntimeView follower = new FakeMinecartRuntimeView(FOLLOWER, new BlockPos(0, 64, 0), new Vec3d(-0.10D, 0.0D, 0.0D));

        manager.link(FOLLOWER, LEADER, FURNACE);
        manager.tick(follower);

        assertTrue(manager.linkedState(FOLLOWER).isPresent());
        assertEquals(Vec3d.ZERO, follower.appliedDelta());
    }

    private static final class FakeMinecartRuntimeView implements MinecartRuntimeView {
        private final UUID id;
        private final BlockPos railPos;
        private final Vec3d velocity;
        private Vec3d appliedDelta = Vec3d.ZERO;

        private FakeMinecartRuntimeView(UUID id, BlockPos railPos, Vec3d velocity) {
            this.id = id;
            this.railPos = railPos;
            this.velocity = velocity;
        }

        @Override public UUID id() { return id; }
        @Override public ServerWorld world() { return null; }
        @Override public BlockPos railPos() { return railPos; }
        @Override public Vec3d position() { return Vec3d.ofBottomCenter(railPos); }
        @Override public Vec3d velocity() { return velocity; }
        @Override public void addVelocity(Vec3d delta) { this.appliedDelta = this.appliedDelta.add(delta); }
        private Vec3d appliedDelta() { return appliedDelta; }
    }

    private static final class FakeRailPathService implements RailPathService {
        private final RailPathResult pathResult;
        private final Optional<Vec3d> historyDirection;
        private final Vec3d furnaceDirection;

        private FakeRailPathService(RailPathResult pathResult, Optional<Vec3d> historyDirection, Vec3d furnaceDirection) {
            this.pathResult = pathResult;
            this.historyDirection = historyDirection;
            this.furnaceDirection = furnaceDirection;
        }

        @Override public void recordHistory(MinecartRuntimeView minecart) { }
        @Override public RailPathResult resolvePathToLeader(MinecartRuntimeView follower, UUID leaderId) { return pathResult; }
        @Override public Optional<Vec3d> resolveLeaderHistoryDirection(UUID leaderId, BlockPos followerRailPos) { return historyDirection; }
        @Override public Vec3d resolveDirectionToFurnace(MinecartRuntimeView follower, UUID furnaceId) { return furnaceDirection; }
    }
}
```

- [ ] **Step 2: Run the runtime-manager test to verify it fails**

Run: `./gradlew test --tests "areahint.train.runtime.TrainLinkManagerTest"`
Expected: FAIL with `cannot find symbol TrainLinkManager`, `MinecartRuntimeView`, and `RailPathService`.

- [ ] **Step 3: Implement the runtime API, registry, and manager**

`src/main/java/areahint/train/TrainLinkService.java`

```java
package areahint.train;

import areahint.train.runtime.MinecartRuntimeView;

import java.util.UUID;

public interface TrainLinkService {
    void link(UUID followerId, UUID leaderId, UUID furnaceId);
    void unlink(UUID followerId);
    void clear();
    void tick(MinecartRuntimeView minecart);
}
```

`src/main/java/areahint/train/runtime/MinecartRuntimeView.java`

```java
package areahint.train.runtime;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public interface MinecartRuntimeView {
    UUID id();
    ServerWorld world();
    BlockPos railPos();
    Vec3d position();
    Vec3d velocity();
    void addVelocity(Vec3d delta);
}
```

`src/main/java/areahint/train/runtime/LinkedCartState.java`

```java
package areahint.train.runtime;

import java.util.UUID;

public record LinkedCartState(UUID followerId, UUID leaderId, UUID furnaceId) {
}
```

`src/main/java/areahint/train/runtime/LinkRegistry.java`

```java
package areahint.train.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LinkRegistry {
    private final Map<UUID, LinkedCartState> byFollower = new HashMap<>();

    public void put(UUID followerId, UUID leaderId, UUID furnaceId) {
        byFollower.put(followerId, new LinkedCartState(followerId, leaderId, furnaceId));
    }

    public Optional<LinkedCartState> get(UUID followerId) {
        return Optional.ofNullable(byFollower.get(followerId));
    }

    public void remove(UUID followerId) {
        byFollower.remove(followerId);
    }

    public void clear() {
        byFollower.clear();
    }
}
```

`src/main/java/areahint/train/runtime/RailPathService.java`

```java
package areahint.train.runtime;

import areahint.train.model.RailPathResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.UUID;

public interface RailPathService {
    void recordHistory(MinecartRuntimeView minecart);
    RailPathResult resolvePathToLeader(MinecartRuntimeView follower, UUID leaderId);
    Optional<Vec3d> resolveLeaderHistoryDirection(UUID leaderId, BlockPos followerRailPos);
    Vec3d resolveDirectionToFurnace(MinecartRuntimeView follower, UUID furnaceId);
}
```

`src/main/java/areahint/train/runtime/TrainLinkManager.java`

```java
package areahint.train.runtime;

import areahint.train.TrainLinkService;
import areahint.train.logic.LinkEvaluator;
import areahint.train.logic.TractionController;
import areahint.train.model.LinkEvaluation;
import areahint.train.model.LinkStatus;
import areahint.train.model.TrainLinkRules;

import java.util.Optional;
import java.util.UUID;

public final class TrainLinkManager implements TrainLinkService {
    private final TrainLinkRules rules;
    private final RailPathService railPathService;
    private final double tractionStrength;
    private final LinkRegistry linkRegistry = new LinkRegistry();
    private final LinkEvaluator linkEvaluator = new LinkEvaluator();
    private final TractionController tractionController = new TractionController();

    public TrainLinkManager(TrainLinkRules rules, RailPathService railPathService, double tractionStrength) {
        this.rules = rules;
        this.railPathService = railPathService;
        this.tractionStrength = tractionStrength;
    }

    @Override
    public void link(UUID followerId, UUID leaderId, UUID furnaceId) {
        linkRegistry.put(followerId, leaderId, furnaceId);
    }

    @Override
    public void unlink(UUID followerId) {
        linkRegistry.remove(followerId);
    }

    @Override
    public void clear() {
        linkRegistry.clear();
    }

    public Optional<LinkedCartState> linkedState(UUID followerId) {
        return linkRegistry.get(followerId);
    }

    @Override
    public void tick(MinecartRuntimeView minecart) {
        railPathService.recordHistory(minecart);

        Optional<LinkedCartState> state = linkRegistry.get(minecart.id());
        if (state.isEmpty()) {
            return;
        }

        LinkedCartState link = state.get();
        LinkEvaluation evaluation = linkEvaluator.evaluate(
            rules,
            railPathService.resolvePathToLeader(minecart, link.leaderId()),
            railPathService.resolveLeaderHistoryDirection(link.leaderId(), minecart.railPos()),
            minecart.velocity(),
            railPathService.resolveDirectionToFurnace(minecart, link.furnaceId())
        );

        if (evaluation.status() == LinkStatus.BROKEN_BY_TOPOLOGY || evaluation.status() == LinkStatus.BROKEN_BY_LENGTH) {
            unlink(minecart.id());
            return;
        }

        minecart.addVelocity(tractionController.computeAcceleration(evaluation, tractionStrength));
    }
}
```

- [ ] **Step 4: Run the runtime-manager test again**

Run: `./gradlew test --tests "areahint.train.runtime.TrainLinkManagerTest"`
Expected: PASS.

- [ ] **Step 5: Commit the runtime manager**

```bash
git add src/main/java/areahint/train/TrainLinkService.java \
  src/main/java/areahint/train/runtime/MinecartRuntimeView.java \
  src/main/java/areahint/train/runtime/LinkedCartState.java \
  src/main/java/areahint/train/runtime/LinkRegistry.java \
  src/main/java/areahint/train/runtime/RailPathService.java \
  src/main/java/areahint/train/runtime/TrainLinkManager.java \
  src/test/java/areahint/train/runtime/TrainLinkManagerTest.java

git commit -m "test: add train link runtime manager"
```

### Task 6: Implement the live world rail-path service

**Files:**
- Create: `src/main/java/areahint/train/runtime/MinecraftMinecartView.java`
- Create: `src/main/java/areahint/train/runtime/WorldRailPathService.java`
- Test: `src/test/java/areahint/train/runtime/TrainLinkManagerTest.java`

- [ ] **Step 1: Extend the existing runtime-manager test with a failing world-service smoke test**

Add this test method to `src/test/java/areahint/train/runtime/TrainLinkManagerTest.java`:

```java
    @Test
    void worldRailPathServiceCanBeConstructed() {
        assertNotNull(new WorldRailPathService(TrainLinkRules.defaults()));
    }
```

- [ ] **Step 2: Run the runtime-manager test to verify it now fails on missing world classes**

Run: `./gradlew test --tests "areahint.train.runtime.TrainLinkManagerTest"`
Expected: FAIL with `cannot find symbol WorldRailPathService`.

- [ ] **Step 3: Implement the Minecraft adapters and live rail traversal**

`src/main/java/areahint/train/runtime/MinecraftMinecartView.java`

```java
package areahint.train.runtime;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public final class MinecraftMinecartView implements MinecartRuntimeView {
    private final AbstractMinecartEntity minecart;

    public MinecraftMinecartView(AbstractMinecartEntity minecart) {
        this.minecart = minecart;
    }

    public AbstractMinecartEntity minecart() {
        return minecart;
    }

    @Override
    public UUID id() {
        return minecart.getUuid();
    }

    @Override
    public ServerWorld world() {
        return (ServerWorld) minecart.getWorld();
    }

    @Override
    public BlockPos railPos() {
        BlockPos pos = minecart.getBlockPos();
        return world().getBlockState(pos).getBlock() instanceof net.minecraft.block.AbstractRailBlock ? pos : pos.down();
    }

    @Override
    public Vec3d position() {
        return minecart.getPos();
    }

    @Override
    public Vec3d velocity() {
        return minecart.getVelocity();
    }

    @Override
    public void addVelocity(Vec3d delta) {
        minecart.addVelocity(delta.x, delta.y, delta.z);
    }
}
```

`src/main/java/areahint/train/runtime/WorldRailPathService.java`

```java
package areahint.train.runtime;

import areahint.train.logic.HistoryPathBuffer;
import areahint.train.logic.RailPathWalker;
import areahint.train.model.RailAnchor;
import areahint.train.model.RailContinuation;
import areahint.train.model.RailPathResult;
import areahint.train.model.TrainLinkRules;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class WorldRailPathService implements RailPathService {
    private static final Set<RailShape> ASCENDING = EnumSet.of(
        RailShape.ASCENDING_EAST,
        RailShape.ASCENDING_WEST,
        RailShape.ASCENDING_NORTH,
        RailShape.ASCENDING_SOUTH
    );

    private final TrainLinkRules rules;
    private final RailPathWalker railPathWalker = new RailPathWalker();
    private final Map<UUID, HistoryPathBuffer> historyByCart = new HashMap<>();
    private final Map<UUID, Snapshot> snapshotByCart = new HashMap<>();

    public WorldRailPathService(TrainLinkRules rules) {
        this.rules = rules;
    }

    @Override
    public void recordHistory(MinecartRuntimeView minecart) {
        Snapshot snapshot = snapshotOf(minecart);
        if (snapshot == null) {
            return;
        }

        snapshotByCart.put(minecart.id(), snapshot);
        historyByCart.computeIfAbsent(minecart.id(), ignored -> new HistoryPathBuffer())
            .append(snapshot.segment(), rules.historyWindowSize());
    }

    @Override
    public RailPathResult resolvePathToLeader(MinecartRuntimeView follower, UUID leaderId) {
        Snapshot followerSnapshot = snapshotOf(follower);
        Snapshot leaderSnapshot = snapshotByCart.get(leaderId);
        if (followerSnapshot == null || leaderSnapshot == null || followerSnapshot.world() != leaderSnapshot.world()) {
            return RailPathResult.brokenByTopology();
        }

        return railPathWalker.walk(followerSnapshot.anchor(), new WorldLookup(followerSnapshot.world(), leaderSnapshot.railPos()), rules.historyWindowSize(), followerSnapshot.tick());
    }

    @Override
    public Optional<Vec3d> resolveLeaderHistoryDirection(UUID leaderId, BlockPos followerRailPos) {
        HistoryPathBuffer history = historyByCart.get(leaderId);
        return history == null ? Optional.empty() : history.directionAt(followerRailPos);
    }

    @Override
    public Vec3d resolveDirectionToFurnace(MinecartRuntimeView follower, UUID furnaceId) {
        Snapshot followerSnapshot = snapshotOf(follower);
        Snapshot furnaceSnapshot = snapshotByCart.get(furnaceId);
        if (followerSnapshot == null || furnaceSnapshot == null || followerSnapshot.world() != furnaceSnapshot.world()) {
            return Vec3d.ZERO;
        }
        return railPathWalker.walk(
            followerSnapshot.anchor(),
            new WorldLookup(followerSnapshot.world(), furnaceSnapshot.railPos()),
            rules.historyWindowSize(),
            followerSnapshot.tick()
        ).directionTowardTarget();
    }

    private Snapshot snapshotOf(MinecartRuntimeView minecart) {
        if (minecart.world() == null) {
            return null;
        }

        BlockPos railPos = minecart.railPos();
        BlockState railState = minecart.world().getBlockState(railPos);
        if (!(railState.getBlock() instanceof AbstractRailBlock)) {
            return null;
        }

        Direction horizontalMotion = horizontalFromVelocity(minecart.velocity());
        RailAnchor anchor = new RailAnchor(railPos, horizontalMotion.getOpposite());
        return new Snapshot(minecart.world(), railPos, anchor, new areahint.train.model.RailPathSegment(railPos, anchor.entryDirection(), horizontalMotion, segmentLength(railState), minecart.world().getServer().getTicks()));
    }

    private Direction horizontalFromVelocity(Vec3d velocity) {
        if (Math.abs(velocity.x) >= Math.abs(velocity.z)) {
            return velocity.x >= 0.0D ? Direction.EAST : Direction.WEST;
        }
        return velocity.z >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private double segmentLength(BlockState state) {
        RailShape shape = state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty());
        return ASCENDING.contains(shape) ? Math.sqrt(2.0D) : 1.0D;
    }

    private final class WorldLookup implements RailPathWalker.RailLookup {
        private final ServerWorld world;
        private final BlockPos target;

        private WorldLookup(ServerWorld world, BlockPos target) {
            this.world = world;
            this.target = target;
        }

        @Override
        public Optional<RailContinuation> next(RailAnchor anchor) {
            BlockState state = world.getBlockState(anchor.railPos());
            if (!(state.getBlock() instanceof AbstractRailBlock railBlock)) {
                return Optional.empty();
            }

            RailShape shape = state.get(railBlock.getShapeProperty());
            Direction exit = resolveExitDirection(shape, anchor.entryDirection());
            if (exit == null) {
                return Optional.empty();
            }

            BlockPos nextPos = resolveNextRailPos(anchor.railPos(), shape, exit);
            BlockState nextState = world.getBlockState(nextPos);
            if (!(nextState.getBlock() instanceof AbstractRailBlock)) {
                return Optional.empty();
            }

            return Optional.of(new RailContinuation(new RailAnchor(nextPos, exit.getOpposite()), exit, ASCENDING.contains(shape) ? Math.sqrt(2.0D) : 1.0D));
        }

        @Override
        public boolean isTarget(RailAnchor anchor) {
            return anchor.railPos().equals(target);
        }

        private Direction resolveExitDirection(RailShape shape, Direction entry) {
            return switch (shape) {
                case NORTH_SOUTH -> entry == Direction.NORTH ? Direction.SOUTH : entry == Direction.SOUTH ? Direction.NORTH : null;
                case EAST_WEST -> entry == Direction.WEST ? Direction.EAST : entry == Direction.EAST ? Direction.WEST : null;
                case ASCENDING_EAST -> entry == Direction.WEST ? Direction.EAST : entry == Direction.EAST ? Direction.WEST : null;
                case ASCENDING_WEST -> entry == Direction.EAST ? Direction.WEST : entry == Direction.WEST ? Direction.EAST : null;
                case ASCENDING_NORTH -> entry == Direction.SOUTH ? Direction.NORTH : entry == Direction.NORTH ? Direction.SOUTH : null;
                case ASCENDING_SOUTH -> entry == Direction.NORTH ? Direction.SOUTH : entry == Direction.SOUTH ? Direction.NORTH : null;
                case SOUTH_EAST -> entry == Direction.SOUTH ? Direction.EAST : entry == Direction.EAST ? Direction.SOUTH : null;
                case SOUTH_WEST -> entry == Direction.SOUTH ? Direction.WEST : entry == Direction.WEST ? Direction.SOUTH : null;
                case NORTH_WEST -> entry == Direction.NORTH ? Direction.WEST : entry == Direction.WEST ? Direction.NORTH : null;
                case NORTH_EAST -> entry == Direction.NORTH ? Direction.EAST : entry == Direction.EAST ? Direction.NORTH : null;
            };
        }

        private BlockPos resolveNextRailPos(BlockPos current, RailShape shape, Direction exit) {
            return switch (shape) {
                case ASCENDING_EAST -> exit == Direction.EAST ? current.east().up() : current.west();
                case ASCENDING_WEST -> exit == Direction.WEST ? current.west().up() : current.east();
                case ASCENDING_NORTH -> exit == Direction.NORTH ? current.north().up() : current.south();
                case ASCENDING_SOUTH -> exit == Direction.SOUTH ? current.south().up() : current.north();
                default -> current.offset(exit);
            };
        }
    }

    private record Snapshot(ServerWorld world, BlockPos railPos, RailAnchor anchor, areahint.train.model.RailPathSegment segment) {
        private long tick() {
            return world.getServer().getTicks();
        }
    }
}
```

- [ ] **Step 4: Keep the smoke test and rerun the runtime-manager test**

Run: `./gradlew test --tests "areahint.train.runtime.TrainLinkManagerTest"`
Expected: PASS, including the new `worldRailPathServiceCanBeConstructed` test.

- [ ] **Step 5: Commit the world-service adapter**

```bash
git add src/main/java/areahint/train/runtime/MinecraftMinecartView.java \
  src/main/java/areahint/train/runtime/WorldRailPathService.java

git commit -m "feat: add world rail path service for train links"
```

### Task 7: Wire bootstrap and the server-side minecart mixin

**Files:**
- Create: `src/main/java/areahint/train/TrainLinkBootstrap.java`
- Create: `src/main/java/areahint/mixin/AbstractMinecartEntityMixin.java`
- Modify: `src/main/java/areahint/Areashint.java:45-136, 176-183`
- Modify: `src/main/resources/areas-hint.mixins.json:5-7`
- Test: `src/test/java/areahint/train/TrainLinkBootstrapTest.java`

- [ ] **Step 1: Write the failing bootstrap test**

```java
package areahint.train;

import areahint.train.model.TrainLinkRules;
import areahint.train.runtime.MinecartRuntimeView;
import areahint.train.runtime.RailPathService;
import areahint.train.runtime.TrainLinkManager;
import areahint.train.model.RailPathResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrainLinkBootstrapTest {
    @AfterEach
    void tearDown() {
        TrainLinkBootstrap.shutdown();
    }

    @Test
    void initReturnsSingletonAndShutdownClearsIt() {
        RailPathService service = new RailPathService() {
            @Override public void recordHistory(MinecartRuntimeView minecart) { }
            @Override public RailPathResult resolvePathToLeader(MinecartRuntimeView follower, UUID leaderId) { return RailPathResult.brokenByTopology(); }
            @Override public Optional<Vec3d> resolveLeaderHistoryDirection(UUID leaderId, BlockPos followerRailPos) { return Optional.empty(); }
            @Override public Vec3d resolveDirectionToFurnace(MinecartRuntimeView follower, UUID furnaceId) { return Vec3d.ZERO; }
        };

        TrainLinkService first = TrainLinkBootstrap.init(new TrainLinkManager(TrainLinkRules.defaults(), service, 0.08D));
        TrainLinkService second = TrainLinkBootstrap.get();

        assertSame(first, second);

        TrainLinkBootstrap.shutdown();

        assertThrows(IllegalStateException.class, TrainLinkBootstrap::get);
    }
}
```

- [ ] **Step 2: Run the bootstrap test to verify it fails**

Run: `./gradlew test --tests "areahint.train.TrainLinkBootstrapTest"`
Expected: FAIL with `cannot find symbol TrainLinkBootstrap`.

- [ ] **Step 3: Implement bootstrap, mixin registration, and server startup wiring**

`src/main/java/areahint/train/TrainLinkBootstrap.java`

```java
package areahint.train;

import areahint.train.model.TrainLinkRules;
import areahint.train.runtime.TrainLinkManager;
import areahint.train.runtime.WorldRailPathService;

public final class TrainLinkBootstrap {
    private static TrainLinkService INSTANCE;

    private TrainLinkBootstrap() {
    }

    public static synchronized TrainLinkService init() {
        if (INSTANCE == null) {
            TrainLinkRules rules = TrainLinkRules.defaults();
            INSTANCE = new TrainLinkManager(rules, new WorldRailPathService(rules), 0.08D);
        }
        return INSTANCE;
    }

    public static synchronized TrainLinkService init(TrainLinkManager manager) {
        INSTANCE = manager;
        return INSTANCE;
    }

    public static synchronized TrainLinkService get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("TrainLinkBootstrap has not been initialized");
        }
        return INSTANCE;
    }

    public static synchronized void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.clear();
            INSTANCE = null;
        }
    }
}
```

`src/main/java/areahint/mixin/AbstractMinecartEntityMixin.java`

```java
package areahint.mixin;

import areahint.train.TrainLinkBootstrap;
import areahint.train.runtime.MinecraftMinecartView;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void areahint$trainLinkTick(CallbackInfo ci) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
        if (!minecart.getWorld().isClient) {
            TrainLinkBootstrap.get().tick(new MinecraftMinecartView(minecart));
        }
    }
}
```

`src/main/resources/areas-hint.mixins.json`

```json
{
	"required": true,
	"package": "areahint.mixin",
	"compatibilityLevel": "JAVA_17",
	"mixins": [
		"AbstractMinecartEntityMixin",
		"ExampleMixin"
	],
	"injectors": {
		"defaultRequire": 1
	}
}
```

`src/main/java/areahint/Areashint.java`

Add this import near the top:

```java
import areahint.train.TrainLinkBootstrap;
```

Inside `onInitialize()`, before the final `LOGGER.info("区域提示模组服务端初始化完成!");`, add:

```java
		TrainLinkBootstrap.init();
```

Inside `onServerStopped(MinecraftServer minecraftServer)`, before the final log-manager shutdown call, add:

```java
		TrainLinkBootstrap.shutdown();
```

- [ ] **Step 4: Run the bootstrap test, then the full test suite, then a build**

Run: `./gradlew test --tests "areahint.train.TrainLinkBootstrapTest"`
Expected: PASS.

Run: `./gradlew test`
Expected: PASS with all six new test classes green.

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit the bootstrap and mixin wiring**

```bash
git add src/main/java/areahint/train/TrainLinkBootstrap.java \
  src/main/java/areahint/mixin/AbstractMinecartEntityMixin.java \
  src/main/java/areahint/Areashint.java \
  src/main/resources/areas-hint.mixins.json \
  src/test/java/areahint/train/TrainLinkBootstrapTest.java

git commit -m "feat: wire server-side minecart train links"
```

---

## Verification checklist

After Task 7, confirm the implementation covers each spec requirement:

- **轨道路径断链** — `WorldRailPathService.resolvePathToLeader(...)` + `RailPathWalker.walk(...)` return `BROKEN_BY_LENGTH` based on path length instead of straight-line distance.
- **红石/道岔拓扑断链** — `WorldRailPathService.WorldLookup.next(...)` returns empty when the live rail graph no longer connects, which the evaluator converts to `BROKEN_BY_TOPOLOGY`.
- **前车历史路径参考** — `HistoryPathBuffer.directionAt(...)` feeds `LinkEvaluator.evaluate(...)` so history guides following when it does not conflict.
- **当前轨道优先重绑定** — `LinkEvaluator.evaluate(...)` emits `REBINDING` when the live path direction disagrees with leader history.
- **反向速度不加速** — `LinkEvaluator.evaluate(...)` checks `velocity.dotProduct(furnaceDirection) < -reverseEpsilon` and `TractionController.computeAcceleration(...)` returns zero.

## Risks and mitigations

1. **Current repository mismatch**
   - Risk: this repo is an area-name mod, not the real train mod.
   - Mitigation: keep the subsystem isolated under `areahint.train`; do not modify unrelated area logic.

2. **Rail-shape edge cases**
   - Risk: rails on unusual vertical transitions or modded rail blocks may not follow the simple `resolveNextRailPos(...)` rules.
   - Mitigation: keep the traversal logic inside `WorldRailPathService` only; if a modded rail needs special handling later, patch one file instead of the evaluator.

3. **No user-facing link creation in scope**
   - Risk: after implementation, only code can create links.
   - Mitigation: treat `TrainLinkService.link(...)` as the integration point for the future coupling mechanic.

4. **Default threshold literal**
   - Risk: `8.0D` may not match the real project’s previous straight-line threshold.
   - Mitigation: if porting into the actual train mod, replace only the literal in `TrainLinkRules.defaults()` and leave path logic untouched.
