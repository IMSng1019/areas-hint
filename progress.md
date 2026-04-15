# Progress

- 已创建磁盘规划文件。
- 已通过全文检索确认 `hasPermissionLevel(...)` 的主要分布位置。
- 下一步：读取关键源码文件，设计统一权限兼容层并开始替换。
- 已确认命令层、Rename/Recolor/SetHigh、Delete/Expand/Shrink/Divide/AddHint/DeleteHint、维度命名网络入口的具体替换点。
- 正在确认 Fabric 服务端环境下 LuckPerms 对 `ServerPlayerEntity` 的最佳查询方式。
- 已完成 LuckPerms 软依赖接入：新增统一权限兼容层，命令层与网络层均接入统一权限服务。
- 已更新构建与 fabric 元数据，并通过 `./gradlew build` 验证成功。
- 已开始 README 发布页风格美化任务，并确认尽量减少原文本内容修改。
- 已读取现有 README、版本信息与可复用图标路径，准备进行结构性优化。
- 已完成首屏重排：新增居中标题、图标、环境信息、快速导航与亮点速览。
- 已确认需要继续优化长章节展示：完整子命令、文件结构、兼容性列表适合折叠处理。
- 已完成长章节收纳：完整子命令、文件结构、兼容性列表均改为折叠展示，首页滚动压力明显下降。
- 已通过 Markdown 诊断检查，README 当前无语法诊断问题。
