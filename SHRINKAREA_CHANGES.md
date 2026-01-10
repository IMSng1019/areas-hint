# ShrinkArea 修改记录

## 问题描述
用户报告在使用 shrinkarea 命令时出现"当前状态不允许修改域名"的错误提示。

## 根本原因
shrinkarea 的状态管理过于严格，与 expandarea 不一致：
- `selectArea()` 方法检查 `currentState == SELECTING_AREA` 才允许选择域名
- `selectAreaByName()` 直接调用 `selectArea()`，但没有先设置状态
- expandarea 使用 `handleAreaSelection()` 方法，不检查状态，直接设置

## 修改内容

### 1. 移除不必要的方法
- **移除 `stopRecording()` 方法** - expandarea 没有此方法
- **移除 `selectArea()` 方法** - expandarea 没有此方法，只有 `handleAreaSelection()`

### 2. 添加缺失的方法
- **添加 `handleAreaSelection()` 方法**
  ```java
  public void handleAreaSelection(AreaData selectedArea) {
      this.selectedArea = selectedArea;
      sendMessage("§a已选择域名: " + AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
      sendMessage("§e请按 §6X §e键开始记录收缩区域的顶点位置", Formatting.YELLOW);
      sendMessage("§7记录完成后点击 §6[保存域名] §7按钮完成收缩", Formatting.GRAY);
      startRecording();
  }
  ```

### 3. 更新现有方法

#### `selectAreaByName()`
- 改为调用 `handleAreaSelection()` 而不是 `selectArea()`

#### `reset()`
- 改为 public 方法
- 添加 `isActive` 的重置

#### `finishAndSave()`
- 移除 `currentState = ShrinkState.CALCULATING` 的设置
- 移除多余的消息提示
- 使用 try-catch 结构包装处理逻辑

#### `continueRecording()`
- 改为检查 `client.player == null` 而不是 `!isActive`

#### `handleXKeyPress()`
- 简化为直接调用 `recordCurrentPosition()`

### 4. 状态管理简化
- 移除了严格的状态检查
- 允许直接选择域名而不需要先进入特定状态
- 与 expandarea 的状态管理保持一致

## 测试建议

1. **基本功能测试**
   ```
   /areahint shrinkarea 38324
   ```
   应该能够成功选择域名并开始记录模式

2. **权限测试**
   - 测试管理员权限
   - 测试域名创建者权限
   - 测试 basename 引用权限

3. **顶点记录测试**
   - 按 X 键记录至少 3 个顶点
   - 点击"保存域名"按钮完成收缩

4. **跨维度测试**
   - 在主世界、下界、末地分别测试

## 构建状态
✅ 构建成功 (BUILD SUCCESSFUL)

## 注意事项
- 核心几何算法逻辑保持不变（删除外部顶点，保留内部顶点）
- 只修改了状态管理和方法调用流程
- 完全按照 expandarea 的模式重构
