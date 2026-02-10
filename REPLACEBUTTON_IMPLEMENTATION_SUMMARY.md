# ReplaceButton 功能实现总结

## 功能描述
实现了 `/areahint replacebutton` 指令，允许玩家自定义用于记录域名顶点的按键。该功能提供了完整的交互式界面，支持按键选择、确认和取消操作。

## 实现的文件

### 1. 数据模型层
- **ConfigData.java** (修改)
  - 添加 `recordKey` 字段（int 类型，存储 GLFW 键码）
  - 默认值：88（X 键）
  - 在所有构造方法中初始化该字段

- **ClientConfig.java** (修改)
  - 添加 `getRecordKey()` 方法
  - 添加 `setRecordKey(int)` 方法
  - 自动保存配置到文件

### 2. 客户端核心功能
- **ReplaceButtonManager.java** (新建)
  - 单例模式管理器
  - 状态机：IDLE → WAITING_FOR_KEY → CONFIRMING → IDLE
  - 处理按键选择和确认逻辑
  - 验证按键有效性（排除特殊键）
  - 保存配置并通知 UnifiedKeyHandler 重新注册

- **ReplaceButtonUI.java** (新建)
  - 显示等待按键界面
  - 显示确认界面（带按键名称）
  - 提供可点击的确认/取消按钮
  - 显示错误、成功和信息消息

- **ReplaceButtonKeyListener.java** (新建)
  - 注册客户端 tick 事件监听器
  - 在 WAITING_FOR_KEY 状态下监听按键
  - 获取按键名称（支持中文显示）
  - 将按键信息传递给 Manager

- **ReplaceButtonNetworking.java** (新建)
  - 注册客户端网络接收器
  - 处理服务器发送的 start/confirm/cancel 消息
  - 发送请求到服务器（预留功能）

### 3. 网络层
- **Packets.java** (修改)
  - 添加 `REPLACEBUTTON_START` 数据包标识符
  - 添加 `REPLACEBUTTON_CANCEL` 数据包标识符
  - 添加 `REPLACEBUTTON_CONFIRM` 数据包标识符

- **ClientNetworking.java** (修改)
  - 在 `handleClientCommand()` 中添加 replacebutton 命令处理
  - 添加 `handleReplaceButtonCommand()` 方法
  - 支持 start/confirm/cancel 三种操作

### 4. 命令层
- **ServerCommands.java** (修改)
  - 注册 `/areahint replacebutton` 命令
  - 添加 `replacebutton confirm` 子命令
  - 添加 `replacebutton cancel` 子命令
  - 添加 `executeReplaceButtonStart()` 方法
  - 添加 `executeReplaceButtonConfirm()` 方法
  - 添加 `executeReplaceButtonCancel()` 方法
  - 在帮助命令中添加说明

### 5. 按键处理层
- **UnifiedKeyHandler.java** (修改)
  - 从硬编码的 X 键改为使用 `ClientConfig.getRecordKey()`
  - 添加 `reregisterKey()` 方法支持动态更换按键
  - 重命名方法：`getXKeyDisplayName()` → `getRecordKeyDisplayName()`
  - 添加 `tickEventRegistered` 标志避免重复注册

- **EasyAddKeyHandler.java** (修改)
  - 更新方法调用：使用 `getRecordKeyDisplayName()`

### 6. 初始化层
- **AreashintClient.java** (修改)
  - 添加 `initReplaceButton()` 方法
  - 注册 ReplaceButtonKeyListener
  - 注册 ReplaceButtonNetworking
  - 在客户端初始化时调用

## 功能特性

### 1. 交互式界面
- 清晰的步骤提示
- 可点击的按钮（确认/取消）
- 实时显示选择的按键名称
- 友好的错误提示

### 2. 按键验证
不允许使用的按键：
- ESC、Enter、Tab
- Shift、Ctrl、Alt（左右）
- 未知键

### 3. 配置持久化
- 自动保存到 `config.json`
- 游戏重启后保持设置
- 默认值：X 键（键码 88）

### 4. 动态更新
- 按键更改后立即生效
- 自动重新注册按键绑定
- 所有相关功能（EasyAdd、ExpandArea、ShrinkArea）自动使用新按键

### 5. 中文支持
- 按键名称支持中文显示
- 特殊键有中文名称（如"空格"、"回车"等）
- 所有提示信息使用中文

## 使用流程

```
用户输入: /areahint replacebutton
    ↓
显示等待界面，提示按下新按键
    ↓
用户按下按键（例如 C 键）
    ↓
显示确认界面："您选择的按键是：C"
    ↓
用户点击 [确认] 或输入 /areahint replacebutton confirm
    ↓
保存配置，重新注册按键
    ↓
显示成功消息："记录按键已更改为：C"
```

## 技术亮点

1. **单例模式**：ReplaceButtonManager 使用单例模式，确保全局只有一个实例
2. **状态机设计**：清晰的状态转换逻辑，避免状态混乱
3. **按键监听优化**：只在需要时监听按键，避免性能开销
4. **配置管理**：统一的配置读写接口，自动保存
5. **网络架构**：遵循现有的客户端-服务器通信模式
6. **代码复用**：复用 EasyAdd 的 UI 设计模式

## 测试建议

1. **基本功能测试**
   - 测试默认按键（X 键）
   - 更改为其他按键（如 C、V、Z 等）
   - 验证新按键在 EasyAdd/ExpandArea/ShrinkArea 中工作

2. **边界条件测试**
   - 尝试使用禁用的按键（ESC、Enter 等）
   - 测试取消功能
   - 测试配置持久化（重启游戏）

3. **兼容性测试**
   - 与其他模组的按键冲突测试
   - 不同键盘布局测试
   - 多人游戏环境测试

## 权限等级
- **权限等级**：0（所有玩家可用）
- **目标选择器**：仅作用于客户端

## 构建状态
✅ 编译成功
✅ 所有依赖正确
✅ 无编译错误或警告

## 文档
- 创建了详细的测试指南：`REPLACEBUTTON_TEST_GUIDE.md`
- 包含使用方法、技术细节、测试步骤和故障排除

## 总结
成功实现了完整的 ReplaceButton 功能，提供了用户友好的交互式界面，支持动态更换记录按键。代码结构清晰，遵循项目现有的架构模式，易于维护和扩展。
