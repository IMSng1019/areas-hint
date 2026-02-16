# SubtitleStyle 交互式指令使用说明

## 概述
`/areahint subtitlestyle` 指令已改造为交互式指令，参考了 `easyadd` 指令的实现方式。

## 使用方法

### 1. 启动交互式流程
```
/areahint subtitlestyle
```

执行此命令后，系统会显示：
- 当前使用的字幕样式
- 三个可点击的样式选项按钮
- 取消按钮

### 2. 样式选项

#### 完整样式 (Full)
- 显示完整的域名层级信息
- 例如：一级域名 > 二级域名 > 三级域名
- 点击 `[完整样式]` 按钮选择

#### 简洁样式 (Simple)
- 只显示当前所在的最低级域名
- 例如：三级域名
- 点击 `[简洁样式]` 按钮选择

#### 混合样式 (Mixed)
- 智能显示域名信息
- 根据层级自动调整显示方式
- 点击 `[混合样式]` 按钮选择

### 3. 确认和取消

- **选择样式**：点击任意样式按钮后，系统会：
  1. 更新配置文件中的 `SubtitleStyle` 选项
  2. 显示设置成功的消息
  3. 自动执行 `/areahint reload` 重新加载配置
  4. 退出交互流程

- **取消操作**：点击 `[取消]` 按钮可以退出交互流程而不做任何更改

## 技术实现

### 架构
- **服务端命令注册**：`ServerCommands.java`
  - `/areahint subtitlestyle` - 启动交互流程
  - `/areahint subtitlestyle select <style>` - 选择样式
  - `/areahint subtitlestyle cancel` - 取消操作

- **客户端管理器**：`SubtitleStyleManager.java`
  - 管理交互流程状态
  - 处理样式选择逻辑
  - 更新配置并重新加载

- **客户端UI**：`SubtitleStyleUI.java`
  - 显示交互式界面
  - 创建可点击的按钮
  - 提供悬停提示

- **网络通信**：`ClientNetworking.java`
  - 处理服务端发送的命令
  - 调用客户端管理器执行相应操作

### 命令流程
1. 玩家执行 `/areahint subtitlestyle`
2. 服务端通过网络发送 `areahint:subtitlestyle_start` 到客户端
3. 客户端 `SubtitleStyleManager` 启动交互流程
4. `SubtitleStyleUI` 显示样式选择界面
5. 玩家点击样式按钮，执行 `/areahint subtitlestyle select <style>`
6. 服务端通过网络发送 `areahint:subtitlestyle_select:<style>` 到客户端
7. 客户端更新配置，执行 reload，显示成功消息

## 权限等级
- **权限等级**：0（所有玩家可用）
- **目标选择器**：作用于玩家自己的本地配置文件

## 与旧版本的区别

### 旧版本（非交互式）
```
/areahint subtitlestyle full
/areahint subtitlestyle simple
/areahint subtitlestyle mixed
```
需要记住具体的样式名称，直接在命令中指定。

### 新版本（交互式）
```
/areahint subtitlestyle
```
通过可点击的按钮选择，更加直观和用户友好。

## 文件结构
```
src/client/java/areahint/subtitlestyle/
├── SubtitleStyleManager.java  # 交互流程管理器
└── SubtitleStyleUI.java        # 用户界面系统

src/main/java/areahint/command/
└── ServerCommands.java         # 命令注册（已修改）

src/client/java/areahint/network/
└── ClientNetworking.java       # 网络处理（已修改）
```

## 注意事项
1. 此命令只能由玩家执行，不能在控制台执行
2. 选择样式后会自动重新加载配置，可能会有短暂的延迟
3. 配置文件保存在 `.minecraft/areas-hint/config.json`
4. 修改会立即生效，无需重启游戏
