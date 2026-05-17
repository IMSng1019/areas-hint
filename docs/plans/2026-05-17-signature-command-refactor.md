# Signature Command Refactor Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebuild `/areahint addsignature` and `/areahint deletesignature` as full interactive commands modeled after EasyAdd.

**Architecture:** Keep the existing `areahint.signature` package, but replace the client flow with a clearer state machine and narrower server request contract. The client lists modifiable current-dimension domains, collects the target player, confirms the change, and sends only the operation, area name, dimension, and target player to the server; the server reloads the stored area, checks permission, mutates signatures, saves, and redistributes area data.

**Tech Stack:** Java 17, Fabric networking, Brigadier command forwarding, existing `AreaData`, `FileManager`, `AreaPermissionUtil`, and chat `Text` click events.

---

### Task 1: Rebuild Client Signature Flow

**Files:**
- Modify: `src/client/java/areahint/signature/SignatureManager.java`
- Modify: `src/client/java/areahint/signature/SignatureUI.java`
- Modify: `src/client/java/areahint/network/ClientNetworking.java`

**Steps:**
- Replace the compact flow with explicit states: `IDLE`, `SELECT_AREA`, `INPUT_PLAYER_NAME`, `CONFIRM_ADD`, `CONFIRM_DELETE`, `FINAL_DELETE_CONFIRM`.
- Keep click buttons compatible with `/areahint addsignature ...` and `/areahint deletesignature ...`.
- Allow player-name input both by chat capture and `/areahint <command> name <player>`.
- For delete, only show removable extended signatures, then require a second confirmation before sending.
- Preserve Chinese comments and existing package layout.

### Task 2: Narrow Server Mutation Contract

**Files:**
- Modify: `src/client/java/areahint/signature/SignatureClientNetworking.java`
- Modify: `src/main/java/areahint/signature/SignatureServerNetworking.java`

**Steps:**
- Send `operation`, `areaName`, `dimension`, and `targetPlayerName` instead of full mutable area JSON.
- On the server, always reload the current stored area from the world dimension file before mutation.
- Keep permission check server-side: admin level 2 or basename-referenced player through `AreaPermissionUtil.canModifyArea`.
- Add duplicate checks for add and extension-only checks for delete.
- Save the target dimension file and call `ServerNetworking.sendAllAreaDataToAll()`.

### Task 3: Command Wiring

**Files:**
- Modify: `src/main/java/areahint/command/ServerCommands.java`
- Modify: `src/client/java/areahint/network/ClientNetworking.java`

**Steps:**
- Add `/areahint deletesignature confirm2`.
- Forward `confirm2` to the client as `areahint:deletesignature_confirm2`.
- Keep permission level 0 on both commands.

### Task 4: Verification

**Files:**
- No test files, per user instruction.

**Steps:**
- Run `.\gradlew.bat build`.
- If build fails from compilation, fix and rerun.
- Report build result and changed files.
