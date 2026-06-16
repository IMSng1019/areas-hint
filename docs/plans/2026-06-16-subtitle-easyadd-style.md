# Subtitle EasyAdd-Style Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make addsubtitle, deletesubtitle, replacesubtitlecolor, and replacesubtitlesize behave like EasyAdd-style interactive commands without extra manual subcommand entry.

**Architecture:** Keep the server command tree as the entry point and let the client subtitle manager own the interaction state. Add one path for subtitle text input, one path for subtitle color, and one path for subtitle size, all driven by the same chat-button pattern EasyAdd uses.

**Tech Stack:** Java 17, Fabric command/networking APIs, existing subtitle manager/UI classes.

---

### Task 1: Align command routing

**Files:**
- Modify: `src/main/java/areahint/command/ServerCommands.java`
- Modify: `src/client/java/areahint/network/ClientNetworking.java`

**Step 1: Inspect the existing subtitle branches**
- Confirm `addsubtitle`, `deletesubtitle`, `replacesubtitlecolor`, and `replacesubtitlesize` all route to the subtitle manager.

**Step 2: Remove the extra command-style branch for subtitle text entry**
- `addsubtitle` must enter the text-input flow directly after area selection.
- No extra `text` subcommand should be required from the player.

**Step 3: Keep the client dispatcher consistent**
- Ensure the client still handles the interactive actions and no stale path points to manual command entry.

### Task 2: Rework subtitle manager flow

**Files:**
- Modify: `src/client/java/areahint/subtitle/SubtitleManager.java`

**Step 1: Mirror EasyAdd state progression**
- Keep a single state machine for add/delete/color/size.
- `addsubtitle` should move from area selection straight to chat text input.

**Step 2: Keep chat capture only for the add text stage**
- Consume chat only while entering subtitle text.
- Preserve cancel handling and prefix stripping like EasyAdd.

**Step 3: Add subtitle-size auto linkage**
- Support `auto` in subtitle size selection.
- Keep auto behavior tied to the title size logic, with subtitle one size smaller when auto is active.

### Task 3: Refresh subtitle UI text and controls

**Files:**
- Modify: `src/client/java/areahint/subtitle/SubtitleUI.java`
- Modify: `src/main/resources/assets/areas-hint/lang/zh_cn.json`
- Modify: `src/main/resources/assets/areas-hint/lang/en_us.json`

**Step 1: Make prompts match the new flow**
- Remove wording that implies extra manual command input.
- Keep the button-driven layout and confirm/cancel actions.

**Step 2: Add subtitle size auto option wording**
- Update the subtitle size screen to include `auto` clearly.

**Step 3: Add or adjust missing translation keys**
- Keep Chinese terminology for 域名 and existing subtitle fields.

### Task 4: Verify behavior by inspection and build

**Files:**
- None

**Step 1: Review the diff**
- Confirm no unrelated modules were changed.

**Step 2: Run the project build**
- Verify the subtitle command paths still compile after the flow change.
