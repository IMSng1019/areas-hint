# ReplaceButton 功能集成到 ExpandArea 和 ShrinkArea

## 概述

本次修改使 `replacebutton` 指令的按键更改功能完全应用到 `expandarea` 和 `shrinkarea` 指令中。

## 修改内容

### 1. ExpandAreaManager.java

**修改位置：**
- 第 198 行：域名选择后的提示消息
- 第 656 行：继续记录顶点的提示消息

**修改前：**
```java
sendMessage("§e请按 §6X §e键开始记录新区域的顶点位置", Formatting.YELLOW);
sendMessage("§a继续记录更多顶点，按 §6X §a记录当前位置", Formatting.GREEN);
```

**修改后：**
```java
sendMessage("§e请按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §e键开始记录新区域的顶点位置", Formatting.YELLOW);
sendMessage("§a继续记录更多顶点，按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §a记录当前位置", Formatting.GREEN);
```

### 2. ExpandAreaUI.java

**修改位置：**
- 第 84 行：记录界面的提示消息

**修改前：**
```java
client.player.sendMessage(Text.of("§e按 §6X §e键记录当前位置"), false);
```

**修改后：**
```java
client.player.sendMessage(Text.of("§e按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §e键记录当前位置"), false);
```

### 3. ShrinkAreaManager.java

**修改位置：**
- 第 44 行：类注释中的工作流程说明
- 第 183 行：域名选择后的提示消息
- 第 413 行：继续记录顶点的提示消息

**修改前：**
```java
// 注释：4. 记录收缩区域的顶点（按 X 键）
sendMessage("§e请按 §6X §e键开始记录收缩区域的顶点位置", Formatting.YELLOW);
sendMessage("§a继续记录更多顶点，按 §6X §a记录当前位置", Formatting.GREEN);
```

**修改后：**
```java
// 注释：4. 记录收缩区域的顶点（按记录键）
sendMessage("§e请按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §e键开始记录收缩区域的顶点位置", Formatting.YELLOW);
sendMessage("§a继续记录更多顶点，按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §a记录当前位置", Formatting.GREEN);
```

**注意：** `ShrinkAreaUI.java` 不需要修改，因为它没有 `showRecordingInterface()` 方法。所有按键提示都在 `ShrinkAreaManager.java` 中处理。

## 技术实现

### 统一按键管理

所有三个功能（easyadd、expandarea、shrinkarea）现在都通过 `UnifiedKeyHandler` 统一管理记录键：

1. **按键注册**：`UnifiedKeyHandler.register()` 在客户端初始化时注册记录键
2. **按键分发**：`UnifiedKeyHandler.handleRecordKeyPress()` 根据当前活跃的模块分发按键事件
3. **按键更新**：`UnifiedKeyHandler.reregisterKey()` 在按键配置改变时更新按键绑定
4. **按键显示**：`UnifiedKeyHandler.getRecordKeyDisplayName()` 获取当前按键的显示名称

### 配置存储

按键配置存储在 `ClientConfig` 中：
- `ClientConfig.getRecordKey()` - 获取按键代码（GLFW键码）
- `ClientConfig.setRecordKey(int)` - 设置按键代码并保存到配置文件

### ReplaceButton 工作流程

1. 玩家执行 `/areahint replacebutton`
2. 系统提示玩家按下新按键
3. `ReplaceButtonKeyListener` 监听玩家按键
4. 玩家按下按键后，系统显示确认界面
5. 玩家确认后：
   - 保存新按键到 `ClientConfig`
   - 调用 `UnifiedKeyHandler.reregisterKey()` 更新按键绑定
   - 所有模块（easyadd、expandarea、shrinkarea）自动使用新按键

## 测试建议

1. **基本功能测试**：
   - 执行 `/areahint replacebutton` 更改按键为其他键（如 'R'）
   - 测试 `/areahint easyadd` 是否使用新按键记录顶点
   - 测试 `/areahint expandarea` 是否使用新按键记录顶点
   - 测试 `/areahint shrinkarea` 是否使用新按键记录顶点

2. **提示消息测试**：
   - 验证所有提示消息中显示的按键名称是否正确更新
   - 验证按键名称是否与实际按键一致

3. **配置持久化测试**：
   - 更改按键后退出游戏
   - 重新进入游戏，验证按键配置是否保存
   - 验证所有模块是否使用保存的按键

4. **边界情况测试**：
   - 测试特殊键（如空格、方向键等）
   - 测试无效键（如ESC、Enter等）是否被正确拒绝

## 相关文件

### 核心文件
- `UnifiedKeyHandler.java` - 统一按键处理器
- `ClientConfig.java` - 客户端配置管理
- `ConfigData.java` - 配置数据模型

### ReplaceButton 功能
- `ReplaceButtonManager.java` - 按键更换管理器
- `ReplaceButtonKeyListener.java` - 按键监听器
- `ReplaceButtonUI.java` - 用户界面
- `ReplaceButtonNetworking.java` - 网络通信

### 已修改文件
- `ExpandAreaManager.java` - 域名扩展管理器
- `ExpandAreaUI.java` - 域名扩展界面
- `ShrinkAreaManager.java` - 域名收缩管理器

### 命令注册
- `ServerCommands.java` - 服务端命令注册（包含 replacebutton 命令）

## 总结

通过本次修改，`replacebutton` 指令的按键更改功能已完全集成到 `expandarea` 和 `shrinkarea` 指令中。所有提示消息都会动态显示当前配置的按键名称，用户体验更加一致和友好。
