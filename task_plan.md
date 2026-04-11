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
