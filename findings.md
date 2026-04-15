# Findings

## 已确认的权限检查分布
- 命令注册中的 `.requires(source -> source.hasPermissionLevel(...))` 位于：
  - `src/main/java/areahint/command/ServerCommands.java`
  - `src/main/java/areahint/command/DebugCommand.java`
  - `src/main/java/areahint/command/CheckCommand.java`
- 运行期 `hasPermissionLevel(...)` 位于：
  - `src/main/java/areahint/command/RecolorCommand.java`
  - `src/main/java/areahint/command/RenameAreaCommand.java`
  - `src/main/java/areahint/command/SetHighCommand.java`
  - `src/main/java/areahint/network/ServerNetworking.java`
  - `src/main/java/areahint/addhint/AddHintServerNetworking.java`
  - `src/main/java/areahint/deletehint/DeleteHintServerNetworking.java`
  - `src/main/java/areahint/dividearea/DivideAreaServerNetworking.java`
  - `src/main/java/areahint/expandarea/ExpandAreaServerNetworking.java`
  - `src/main/java/areahint/shrinkarea/ShrinkAreaServerNetworking.java`
  - `src/main/java/areahint/Areashint.java`

## 当前实现边界
- 仅服务端需要接入 LuckPerms
- 需要保证命令可见性、命令执行、GUI/网络提交的权限一致
- 不能把 LuckPerms 的 `UNDEFINED` 当作拒绝，必须回退原规则

## 进一步确认
- `EasyAddServerNetworking.java` 当前没有 `hasPermissionLevel(...)` 判断，不在本次计划的强制替换名单内。
- `ServerCommands.java` 中真正使用 `.requires(...)` 的只有 `dimensionalityname`、`dimensionalitycolor`、`add`、`serverlanguage`，其余管理命令主要靠运行期业务判断决定可操作对象范围。
- `rename` 仅允许管理员或创建者操作；`sethigh` 允许管理员、创建者，或“引用当前域名为 baseName 的子域名创建者”操作。
- `expandarea` / `dividearea`：管理员、创建者、上级域名创建者可操作；`shrinkarea`：管理员或上级域名创建者可操作。
- `delete`：管理员或创建者可操作，但仍受“不能删除有子域名的域名”业务限制。
- LuckPerms 官方文档确认可使用 `compileOnly 'net.luckperms:api:5.4'`，并通过 `LuckPermsProvider.get()` + `CachedPermissionData.checkPermission(node)` 获取 `Tristate` 结果。

---

## README 发布页美化发现
- 当前 `README.md` 内容完整，但首页缺少发布页式首屏与快速导航。
- 适合保留原有正文，优先通过新增视觉头部、环境信息和导航来提升展示效果。
- `完整子命令速查`、`ExpandArea 与 ShrinkArea 补充说明`、`文件结构（完整保留）`、`模组兼容性` 都属于较长章节，适合用 `<details>` 折叠。
- 仓库内已有可复用图标：`src/main/resources/assets/areas-hint/icon.png`。
- `gradle.properties` 中 `mod_version=4.1.0`，与 README 当前版本一致，可直接复用。
- 当前首页已改成发布页式头部，但正文主体仍保持原文为主，符合“尽量减少原文本内容修改”的要求。
- 后续最值得继续折叠的长段落是：完整子命令速查、完整文件结构、兼容性长列表。
- 已实际完成上述三处折叠，其中“文件结构”改为更适合 README 首页阅读的精简版核心结构，仍保留原章节主题。
- VS Code Markdown 诊断结果为空，当前 README 结构有效。
