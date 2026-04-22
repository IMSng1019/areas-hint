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

---

## Vulkan 渲染接入需求（2026-04-22）
- 在原有 `VulkanRender` 位置补全 Vulkan 渲染实现。
- 目标效果与逻辑应与 `GLRender` 一致，而不是保留当前的“兼容模式模拟”。
- 启动时检查是否正在使用 Vulkan 模组：
  - 若是，则把配置改为 `Vulkan`
  - 若不是且当前配置为 `Vulkan`，则改为 `OpenGL`
  - 若不是且当前配置为 `OpenGL` 或 `CPU`，则不修改
- Vulkan 模组是可选依赖，未安装不能影响模组运行。
- 命令切换渲染方式时，禁止 `Vulkan` 与 `OpenGL/CPU` 双向互切。

## Vulkan 渲染接入发现
- `GLRender.java` 已完整实现 HUD 文本动画、透明度/位移插值、尺寸缩放、闪烁色支持与调试日志，是 Vulkan 路径最直接的行为参考。
- `VulkanRender.java` 当前并未接入 Vulkan API，而是复刻了一部分 `GLRender` 逻辑，并额外画了蓝色背景，效果已与 OpenGL 不一致。
- `RenderManager.java` 在启动时无能力判断 Vulkan 模组状态，只是按 `ClientConfig.getSubtitleRender()` 直接选渲染器。
- `AreashintClient.java` 当前初始化顺序是 `ClientConfig.init()` 后立即 `new RenderManager()`，若要做启动期自动修正配置，可在两者之间插入 Vulkan 兼容探测/配置修正。
- `/areahint subtitlerender` 的服务端入口只做字符串规范化并透传给客户端：`ServerCommands.java:625`。
- 实际切换与保存配置发生在客户端网络处理：`ClientNetworking.java:210-219`，这里也是限制非法跨后端切换的关键入口。
- `ClientConfig.setSubtitleRender(...)` 当前只做模式规范化并保存，没有任何与 Vulkan 可用性相关的约束。
- `FileManager.readConfigData(...)` 会把非法渲染模式补回默认 `OpenGL`，合法值 `Vulkan` 会被保留。
- 项目里已有软依赖兼容模式：
  - `BlueMapCompat.java`：先用 `FabricLoader.isModLoaded(...)` 判断，再反射加载集成类。
  - `LuckPermsCompat.java`：先判断模组是否存在，再延迟解析 API。
- 当前仓库还没有任何 `vulkanmod` 或 Vulkan API 依赖，也没有对应 mod metadata 声明。
- 用户已确认目标模组就是 `VulkanMod`，并且“是否使用 Vulkan”按 `vulkanmod` 已加载来判断。
- Context7 文档显示 VulkanMod 会在客户端初始化时注册自己的 renderer；这支持我们用“软依赖 + 已加载检测”的方式做兼容，而不必强依赖更深的运行时状态 API。

## Vulkan 技术决策（暂定）
| Decision | Rationale |
|----------|-----------|
| Vulkan 接入应采用软依赖兼容层 | 避免未安装 Vulkan 模组时触发类加载错误 |
| `VulkanRender` 结构应尽量与 `GLRender` 一一对应 | 这是用户新增要求，便于保持阅读方式、调试路径和行为对齐 |
| 共享逻辑只做最小限度抽取或完全不抽 | 优先满足“结构相同”，避免为了复用而打散现有 OpenGL 结构 |
| 切换限制重点放在客户端实际执行处 | 服务端命令当前只是转发，真正修改配置的是客户端 |
| Vulkan 使用判定按 `vulkanmod` 已加载处理 | 这是用户明确选择的标准，实现稳定且不依赖额外运行时状态 |
