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
