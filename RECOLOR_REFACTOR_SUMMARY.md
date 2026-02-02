# Recolor 指令重构总结

## 概述
已成功重构 `recolor` 指令，完全参考 `easyadd` 指令的实现方式和流程，提供了交互式的域名颜色修改功能。

## 新增文件

### 1. 客户端管理器
**文件**: `src/client/java/areahint/recolor/RecolorManager.java`
- 单例模式管理 Recolor 流程
- 状态机管理：IDLE → AREA_SELECTION → COLOR_SELECTION → CONFIRM_CHANGE
- 处理域名选择、颜色选择和确认修改
- 通过网络发送请求到服务端

### 2. 客户端UI
**文件**: `src/client/java/areahint/recolor/RecolorUI.java`
- 显示域名选择界面（可点击按钮）
- 显示颜色选择界面（16种预设颜色按钮）
- 显示二级确认对话框（原颜色 vs 新颜色）
- 所有按钮都使用 ClickEvent 实现交互

### 3. 客户端命令处理器
**文件**: `src/client/java/areahint/recolor/RecolorClientCommand.java`
- 处理客户端命令：select、color、confirm、cancel
- 与 RecolorManager 交互

## 修改的文件

### 1. ServerCommands.java
**修改内容**:
```java
// 原来的简单命令
.then(literal("recolor")
    .executes(RecolorCommand::executeRecolor)
    .then(argument("areaName", StringArgumentType.word())
        .then(argument("color", StringArgumentType.greedyString())
            .executes(...))))

// 新的交互式命令
.then(literal("recolor")
    .executes(RecolorCommand::executeRecolor)
    .then(literal("select")
        .then(argument("selectAreaName", StringArgumentType.greedyString())
            .executes(context -> executeRecolorSelect(...))))
    .then(literal("color")
        .then(argument("colorValue", StringArgumentType.greedyString())
            .executes(context -> executeRecolorColor(...))))
    .then(literal("confirm")
        .executes(ServerCommands::executeRecolorConfirm))
    .then(literal("cancel")
        .executes(ServerCommands::executeRecolorCancel)))
```

**新增方法**:
- `executeRecolorSelect()` - 处理域名选择
- `executeRecolorColor()` - 处理颜色选择
- `executeRecolorConfirm()` - 处理确认
- `executeRecolorCancel()` - 处理取消

### 2. ClientNetworking.java
**修改内容**:
- 在 `handleClientCommand()` 中添加 Recolor 命令处理分支
- 修改 `handleRecolorResponse()` 中的 `recolor_interactive` 处理逻辑
  - 从网络包中读取域名列表
  - 创建 AreaData 对象
  - 调用 `RecolorManager.getInstance().startRecolor()`

**新增方法**:
- `handleRecolorCommand()` - 处理 recolor 相关的客户端命令

### 3. RecolorCommand.java
**保持不变**:
- `executeRecolor()` - 主入口，发送可编辑域名列表到客户端
- `handleRecolorRequest()` - 处理客户端发送的颜色修改请求
- 权限检查逻辑保持不变

## 功能流程

### 完整交互流程
1. **玩家执行**: `/areahint recolor`
2. **服务端**:
   - 获取玩家当前维度
   - 查找玩家可编辑的域名（创建者或管理员）
   - 发送 `recolor_interactive` 包到客户端
3. **客户端**:
   - 接收域名列表
   - 启动 `RecolorManager`
   - 显示域名选择界面（带可点击按钮）
4. **玩家点击域名按钮**: 执行 `/areahint recolor select "域名"`
5. **客户端**:
   - 显示当前颜色
   - 显示颜色选择界面（16种颜色按钮）
6. **玩家点击颜色按钮**: 执行 `/areahint recolor color #RRGGBB`
7. **客户端**:
   - 显示二级确认对话框
   - 显示原颜色和新颜色对比
   - 提供 [是] 和 [否] 按钮
8. **玩家点击确认**: 执行 `/areahint recolor confirm`
9. **客户端**:
   - 发送 `C2S_RECOLOR_REQUEST` 包到服务端
10. **服务端**:
    - 验证权限
    - 修改域名颜色
    - 保存文件
    - 执行 reload
    - 发送成功/失败响应

### 取消流程
- 任何阶段都可以点击 [取消] 按钮或执行 `/areahint recolor cancel`
- 客户端重置状态，返回 IDLE

## 权限控制
- **权限等级**: 0（不需要管理员）
- **可编辑域名**:
  - 玩家是域名的创建者（signature 字段匹配）
  - 或玩家是管理员（权限等级 ≥ 2）

## 颜色支持
### 预设颜色（16种）
- 白色 (#FFFFFF)、灰色 (#808080)、深灰色 (#555555)、黑色 (#000000)
- 深红色 (#AA0000)、红色 (#FF5555)、粉红色 (#FF55FF)、橙色 (#FFAA00)
- 黄色 (#FFFF55)、绿色 (#55FF55)、深绿色 (#00AA00)、天蓝色 (#55FFFF)
- 湖蓝色 (#00AAAA)、蓝色 (#5555FF)、深蓝色 (#0000AA)、紫色 (#AA00AA)

### 自定义颜色
- 支持十六进制格式：`#RRGGBB`
- 通过 `/areahint recolor color #FF0000` 输入

## 二级确认对话框
```
§6=== 确认颜色修改 ===
§f您确认要修改域名颜色吗？

§a域名：§6商业区
§a原颜色：§6#FFFFFF
§a新颜色：§6#FF0000

§a[是]  §c[否]
§7请确认以上信息无误后点击按钮
```

## 网络通信
### 服务端 → 客户端
- `S2C_RECOLOR_RESPONSE` (recolor_interactive)
  - 发送可编辑域名列表
  - 包含域名名称、当前颜色、等级、上级域名

### 客户端 → 服务端
- `C2S_RECOLOR_REQUEST`
  - 发送域名名称、新颜色、维度信息

## 与 EasyAdd 的相似之处
1. **状态机管理**: 使用枚举管理流程状态
2. **单例模式**: Manager 类使用单例
3. **UI 系统**: 使用可点击的聊天消息组件
4. **网络通信**: 客户端-服务端双向通信
5. **命令结构**: 使用子命令（select、color、confirm、cancel）
6. **二级确认**: 在执行关键操作前显示确认对话框

## 测试建议
1. 测试权限控制：
   - 普通玩家只能修改自己创建的域名
   - 管理员可以修改所有域名
2. 测试颜色选择：
   - 预设颜色按钮
   - 自定义十六进制颜色
3. 测试取消流程：
   - 在各个阶段取消
4. 测试二级确认：
   - 确认修改
   - 取消修改
5. 测试多维度：
   - 在不同维度执行命令
   - 验证域名列表正确

## 构建状态
✅ 构建成功 (BUILD SUCCESSFUL)
