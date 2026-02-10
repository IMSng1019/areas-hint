# ReplaceButton 指令修复总结

## 问题描述
执行 `/areahint replacebutton` 指令后，按下确认按钮没有反应，新按键无法生效。

## 根本原因
在 `UnifiedKeyHandler.reregisterKey()` 方法中，代码尝试使用 `KeyBindingHelper.registerKeyBinding()` 来重新注册按键。但是：

1. **Minecraft/Fabric 的按键注册机制限制**：`KeyBindingHelper.registerKeyBinding()` 只能在客户端初始化阶段调用
2. **运行时无法重新注册**：在游戏运行过程中调用此方法不会生效
3. **正确的做法**：应该使用 `KeyBinding.setBoundKey()` 方法来更新现有按键绑定的键码

## 修复内容

### 修改文件
`src/client/java/areahint/keyhandler/UnifiedKeyHandler.java`

### 修改前（第58-72行）
```java
public static void reregisterKey() {
    // 获取新的按键代码
    int keyCode = ClientConfig.getRecordKey();

    // 由于Fabric API没有unregisterKeyBinding方法，我们直接创建新的按键绑定
    // 旧的按键绑定会被自动覆盖
    recordKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.areahint.unified.record", // 翻译键
        InputUtil.Type.KEYSYM,
        keyCode, // 使用新的按键
        "category.areahint.general" // 通用类别
    ));

    System.out.println("DEBUG: 记录键已重新注册为键码 " + keyCode);
}
```

### 修改后
```java
public static void reregisterKey() {
    if (recordKeyBinding == null) {
        System.out.println("DEBUG: recordKeyBinding 为 null，无法重新注册");
        return;
    }

    // 获取新的按键代码
    int keyCode = ClientConfig.getRecordKey();

    // 使用 setBoundKey 方法更新按键绑定
    recordKeyBinding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));

    // 保存按键绑定设置到 Minecraft 的配置文件
    net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
    if (client != null && client.options != null) {
        client.options.write();
    }

    System.out.println("DEBUG: 记录键已更新为键码 " + keyCode);
}
```

## 修复说明

1. **添加空值检查**：确保 `recordKeyBinding` 不为 null
2. **使用 setBoundKey()**：直接更新现有按键绑定的键码，而不是尝试重新注册
3. **保存配置**：调用 `client.options.write()` 将按键绑定保存到 Minecraft 的配置文件
4. **改进调试信息**：更新调试输出信息

## 测试步骤

1. **重新编译模组**：
   ```bash
   ./gradlew build
   ```

2. **启动游戏并测试**：
   ```
   /areahint replacebutton
   ```

3. **按下新按键**（例如按 'Y' 键）

4. **点击确认按钮**

5. **验证**：
   - 应该看到消息："§a记录按键已更改为：Y"
   - 控制台应该输出："DEBUG: 记录键已更新为键码 [键码]"
   - 新按键应该立即生效

6. **测试新按键**：
   - 执行 `/areahint easyadd` 开始创建域名
   - 按下新设置的按键（例如 'Y'）
   - 应该能够正常记录坐标点

## 技术细节

### KeyBinding API 的正确使用方式

- **初始化时注册**：使用 `KeyBindingHelper.registerKeyBinding()`
- **运行时更新**：使用 `keyBinding.setBoundKey()`
- **保存配置**：使用 `client.options.write()`

### 为什么之前的方法不工作

Minecraft 的按键绑定系统在客户端初始化时构建按键映射表。运行时调用 `registerKeyBinding()` 不会更新这个映射表，因此新按键不会被识别。

### 正确的流程

1. 用户按下新按键 → `ReplaceButtonKeyListener` 捕获
2. 用户点击确认 → `ReplaceButtonManager.confirmNewKey()`
3. 保存到配置 → `ClientConfig.setRecordKey()`
4. 更新按键绑定 → `UnifiedKeyHandler.reregisterKey()`
5. 使用 `setBoundKey()` 更新现有绑定
6. 保存到 Minecraft 配置文件
7. 新按键立即生效 ✓

## 相关文件

- `src/client/java/areahint/keyhandler/UnifiedKeyHandler.java` - 修复的主要文件
- `src/client/java/areahint/replacebutton/ReplaceButtonManager.java` - 调用 reregisterKey()
- `src/client/java/areahint/config/ClientConfig.java` - 保存按键配置
- `src/client/java/areahint/replacebutton/ReplaceButtonKeyListener.java` - 捕获新按键

## 预期结果

修复后，`/areahint replacebutton` 指令应该能够：
1. ✓ 正确显示等待按键界面
2. ✓ 捕获玩家按下的新按键
3. ✓ 显示确认界面
4. ✓ 点击确认后立即生效
5. ✓ 新按键能够正常用于记录域名顶点
6. ✓ 配置被正确保存到文件

## 注意事项

- 修改后需要重新编译模组
- 建议清理旧的构建文件：`./gradlew clean build`
- 测试时注意查看控制台的调试输出
- 如果仍有问题，检查 `config.json` 文件中的 `RecordKey` 字段是否正确更新
