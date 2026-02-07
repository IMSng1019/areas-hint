# ReplaceButton 功能测试指南

## 功能概述
ReplaceButton 功能允许玩家自定义用于记录域名顶点的按键。默认情况下，记录按键是 X 键，但玩家可以通过此功能将其更改为任何其他按键。

## 使用方法

### 1. 启动按键更换流程
在游戏中输入命令：
```
/areahint replacebutton
```

系统会显示：
```
=== 更改记录按键 ===
请按下您想要使用的新按键...
提示：按下任意键后将询问您是否确认
注意：某些特殊键（如ESC、Enter、Shift等）不能使用
[取消]
```

### 2. 按下新按键
在等待状态下，按下您想要使用的新按键（例如按下 C 键）。

系统会显示确认界面：
```
=== 确认新按键 ===
您选择的按键是：C
确认后，该按键将用于记录域名顶点
[确认]  [取消]
```

### 3. 确认或取消
- 点击 **[确认]** 或输入 `/areahint replacebutton confirm` 来确认使用新按键
- 点击 **[取消]** 或输入 `/areahint replacebutton cancel` 来取消更改

### 4. 完成
确认后，系统会显示：
```
记录按键已更改为：C
```

从此以后，在使用 EasyAdd、ExpandArea、ShrinkArea 等功能时，按下 C 键（而不是 X 键）来记录坐标点。

## 技术细节

### 配置保存
- 新的按键配置会自动保存到 `config.json` 文件中
- 配置字段：`recordKey`（存储 GLFW 键码）
- 默认值：88（X 键的 GLFW 键码）

### 不允许使用的按键
以下按键不能用作记录键：
- ESC 键
- Enter（回车）键
- Tab 键
- Shift 键（左/右）
- Ctrl 键（左/右）
- Alt 键（左/右）
- 未知键

### 按键绑定更新
- 按键更改后，UnifiedKeyHandler 会自动重新注册新的按键绑定
- 所有使用记录键的功能（EasyAdd、ExpandArea、ShrinkArea）都会自动使用新按键

## 文件结构

### 客户端文件
- `ReplaceButtonManager.java` - 管理按键更换流程的状态和逻辑
- `ReplaceButtonUI.java` - 提供交互式用户界面
- `ReplaceButtonKeyListener.java` - 监听玩家按键输入
- `ReplaceButtonNetworking.java` - 处理客户端网络通信

### 服务端文件
- `ServerCommands.java` - 注册 replacebutton 命令

### 配置文件
- `ConfigData.java` - 添加 recordKey 字段
- `ClientConfig.java` - 提供 getRecordKey() 和 setRecordKey() 方法

### 按键处理
- `UnifiedKeyHandler.java` - 修改为支持动态按键，添加 reregisterKey() 方法

## 测试步骤

1. **启动游戏并进入世界**

2. **测试默认按键（X 键）**
   - 输入 `/areahint easyadd`
   - 按 X 键记录坐标点
   - 确认 X 键正常工作

3. **更改按键为 C 键**
   - 输入 `/areahint replacebutton`
   - 按下 C 键
   - 点击 [确认]

4. **测试新按键（C 键）**
   - 输入 `/areahint easyadd`
   - 按 C 键记录坐标点
   - 确认 C 键正常工作
   - 确认 X 键不再工作

5. **测试配置持久化**
   - 退出游戏
   - 重新启动游戏
   - 输入 `/areahint easyadd`
   - 确认 C 键仍然有效

6. **测试取消功能**
   - 输入 `/areahint replacebutton`
   - 按下 Z 键
   - 点击 [取消]
   - 确认按键没有改变（仍然是 C 键）

7. **测试无效按键**
   - 输入 `/areahint replacebutton`
   - 按下 ESC 键
   - 确认显示错误消息："该按键不能用作记录键！"

## 已知限制

1. **按键冲突**：如果选择的按键与 Minecraft 或其他模组的按键冲突，可能会导致意外行为。

2. **特殊键限制**：某些特殊键（如功能键、修饰键）不能用作记录键。

3. **实时更新**：按键更改后，正在进行的 EasyAdd/ExpandArea/ShrinkArea 流程会立即使用新按键。

## 故障排除

### 问题：按键更改后不生效
**解决方案**：
1. 检查 `config.json` 文件中的 `recordKey` 字段是否已更新
2. 尝试重新启动游戏
3. 检查日志文件中是否有错误消息

### 问题：无法按下某个按键
**解决方案**：
1. 确认该按键不在禁用列表中
2. 尝试在聊天框外按下按键（不要在 GUI 界面中按键）
3. 检查该按键是否被其他程序占用

### 问题：配置文件损坏
**解决方案**：
1. 删除 `.minecraft/areas-hint/config.json` 文件
2. 重新启动游戏，配置文件会自动重新创建
3. 默认按键会恢复为 X 键
