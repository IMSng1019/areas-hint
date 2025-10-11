# AreaHint 命令使用说明

## ExpandArea 和 ShrinkArea 命令更新

### 新功能：直接指定域名

现在 `expandarea` 和 `shrinkarea` 命令支持直接在命令中指定域名，无需先执行命令再从列表中选择。

## 使用方式

### ExpandArea（域名扩展）

#### 方式一：直接指定域名（推荐）
```
/areahint expandarea <域名>
```
- 支持 Tab 键自动补全
- 只显示你有权限扩展的域名
- 直接进入记录顶点流程

**示例**：
```
/areahint expandarea MyArea
```

#### 方式二：从列表选择
```
/areahint expandarea
```
- 显示可点击的域名列表
- 点击金色按钮选择域名

### ShrinkArea（域名收缩）

#### 方式一：直接指定域名（推荐）
```
/areahint shrinkarea <域名>
```
- 支持 Tab 键自动补全
- 只显示你有权限收缩的域名
- 直接进入记录顶点流程

**示例**：
```
/areahint shrinkarea MyArea
```

#### 方式二：从列表选择
```
/areahint shrinkarea
```
- 显示可点击的域名列表
- 显示权限提示
- 点击金色按钮选择域名

## 权限规则

你可以扩展/收缩以下域名：
- ✅ 你创建的域名（basename 引用为你的名字）
- ✅ 如果你是管理员（权限等级 2）：所有域名

## Tab 补全功能

输入以下命令后按 Tab 键：
- `/areahint expandarea ` → 显示可扩展的域名列表
- `/areahint shrinkarea ` → 显示可收缩的域名列表

补全列表会根据你的权限自动过滤。

## 完整流程示例

### 扩展域名示例

```bash
# 1. 输入命令（使用Tab补全）
/areahint expandarea MyArea

# 2. 系统提示
§a成功选择域名: MyArea
§e按 X 键记录扩展区域的新顶点位置

# 3. 按 X 键记录各个顶点

# 4. 记录≥3个顶点后，点击 [保存域名] 按钮

# 5. 完成！
```

### 收缩域名示例

```bash
# 1. 输入命令（使用Tab补全）
/areahint shrinkarea MyArea

# 2. 系统显示权限提示和操作说明
§e按 X 键记录收缩区域的顶点位置

# 3. 按 X 键记录各个顶点

# 4. 记录≥3个顶点后，点击 [保存域名] 按钮

# 5. 完成！
```

## 其他子命令

### ExpandArea 子命令
- `/areahint expandarea continue` - 继续记录顶点
- `/areahint expandarea save` - 保存扩展后的域名
- `/areahint expandarea cancel` - 取消扩展操作

### ShrinkArea 子命令
- `/areahint shrinkarea continue` - 继续记录顶点
- `/areahint shrinkarea save` - 保存收缩后的域名
- `/areahint shrinkarea cancel` - 取消收缩操作

## 优势对比

### 旧方式（仅列表选择）
1. 执行 `/areahint expandarea`
2. 等待列表加载
3. 找到域名并点击按钮

### 新方式（直接指定）
1. 执行 `/areahint expandarea MyArea` （使用Tab补全）
2. ✅ 完成！直接进入记录流程

**节省时间，提高效率！**

## 技术实现

- 命令系统：Brigadier 命令框架
- 补全提供器：自动根据玩家权限过滤域名
- 向后兼容：保留原有的 `select` 子命令
- 交互方式：聊天消息 + 可点击按钮

## 更新日期

2025-10-06 - 添加直接指定域名功能和 Tab 补全支持

