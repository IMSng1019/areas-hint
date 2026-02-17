# SubtitleSize 功能说明

## 功能概述
`subtitlesize` 是一个交互式命令，用于调整模组进入域名时显示的字幕大小。

## 命令使用

### 基本命令
```
/areahint subtitlesize
```

### 功能特点
1. **交互式界面**：类似于 `easyadd` 命令，使用可点击按钮进行选择
2. **七种大小选项**：
   - 极大 (extra_large)
   - 大 (large)
   - 较大 (medium_large)
   - 中 (medium) - 默认值
   - 较小 (medium_small)
   - 小 (small)
   - 极小 (extra_small)

3. **自动保存**：选择后自动保存到配置文件并重新加载模组
4. **显示当前设置**：启动命令时会显示当前的字幕大小

## 使用流程

1. 输入命令 `/areahint subtitlesize`
2. 系统显示当前字幕大小和可选项
3. 点击想要的大小按钮（例如：[极大] [大] [较大] [中] [较小] [小] [极小]）
4. 系统自动保存配置并重新加载
5. 如需取消，点击 [取消] 按钮

## 权限要求
- **权限等级**：0（所有玩家都可以使用）
- **作用范围**：仅影响玩家自己的本地配置文件

## 配置文件
字幕大小设置保存在 `.minecraft/areas-hint/config.json` 中：
```json
{
  "Frequency": 1,
  "SubtitleRender": "OpenGL",
  "SubtitleStyle": "mixed",
  "SubtitleSize": "medium"
}
```

## 技术实现

### 新增文件
1. `SubtitleSizeManager.java` - 管理字幕大小选择流程
2. `SubtitleSizeUI.java` - 处理用户界面显示

### 修改文件
1. `ConfigData.java` - 添加 `subtitleSize` 字段和相关方法
2. `ClientConfig.java` - 添加 getter/setter 方法
3. `ServerCommands.java` - 注册新命令
4. `ClientNetworking.java` - 处理客户端命令

### 默认值
- 首次初始化时，字幕大小默认为 "medium"（中）

## 示例

### 启动命令
```
/areahint subtitlesize
```

### 输出示例
```
=== 字幕大小设置 ===
当前大小: 中
请选择新的字幕大小：

[极大]  [大]  [较大]  [中]
[较小]  [小]  [极小]

[取消]
提示：选择大小后将自动重新加载配置
```

### 选择后输出
```
字幕大小已设置为: 大
配置已重新加载
```

## 注意事项
1. 此命令只能由玩家执行，不能在控制台执行
2. 修改后会立即执行 reload 操作，重新加载所有配置和域名文件
3. 每个玩家的设置独立保存在各自的客户端配置文件中
