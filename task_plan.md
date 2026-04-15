# LuckPerms 接入任务计划

## 目标
为 Areas Hint 服务端增加可选 LuckPerms 联动：
- 安装 LuckPerms 时，命令与对应网络操作先按 LuckPerms 节点判定
- LuckPerms 节点结果为 TRUE/FALSE 时直接采用
- 结果为 UNDEFINED 或 LuckPerms 不可用时，回退原有权限规则与业务规则
- 未安装 LuckPerms 时模组保持现状、正常构建与运行

## 阶段
- [complete] 阶段 1：确认现有权限判断与初始化入口
- [complete] 阶段 2：新增 permission 兼容层与节点常量
- [complete] 阶段 3：替换命令层权限检查
- [complete] 阶段 4：替换网络层权限检查
- [complete] 阶段 5：更新构建与 mod 元数据
- [complete] 阶段 6：构建验证并整理结果

## 关键决策
- 使用 compileOnly 引入 LuckPerms API，避免打包进产物
- 使用 fabric.mod.json 的 suggests 声明软依赖
- 除兼容层外，不在其他类直接调用 LuckPerms API
- 命令层与网络层共用统一 PermissionService，避免权限结果不一致

## 错误记录
- 暂无

---

# README 发布页美化任务计划

## 目标
在尽量减少原 README 文本内容修改的前提下，将首页调整为更适合 GitHub 展示的发布页风格：
- 首屏更醒目
- 一眼能看懂模组定位与环境信息
- 长内容更易折叠浏览
- 保留原有说明主体与大部分原文

## 阶段
- [in_progress] 阶段 1：确认可复用内容与首屏素材
- [pending] 阶段 2：设计首屏、快速导航与关键信息块
- [pending] 阶段 3：对长章节做折叠与结构优化
- [pending] 阶段 4：复核 Markdown 展示效果与版本信息

## 关键决策
- 不大幅重写原有正文，优先通过新增首屏与结构包装来美化
- 使用仓库内现有 `icon.png` 作为首页视觉元素
- 使用 `<details>` 折叠超长章节，减少首页滚动压力
- 使用手动锚点保证 GitHub 内部导航稳定

## 错误记录
- 暂无
