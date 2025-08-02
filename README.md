# Areas Hint 区域提示模组

这是一个Minecraft Fabric 1.20.4版本的区域提示模组，可以在玩家进入特定区域时显示区域名称。

## 功能概述

- 通过多边形区域定义不同的区域（称为"域名"）
- 支持多层级域名体系（顶级域名、二级域名、三级域名等）
- 使用射线法检测玩家是否在特定区域内
- 动态显示区域名称，带有平滑的动画效果
- 支持多种渲染方式（CPU、OpenGL、Vulkan）
- 提供多种字幕样式选项
- 可通过命令进行配置和区域管理
- 智能命令补全系统，支持自动补全可删除的域名

## 架构设计

模组分为客户端和服务端两部分：

### 客户端部分

- 负责读取配置文件和区域数据文件
- 使用射线法计算玩家是否在区域内
- 根据配置文件显示不同样式的区域提示
- 处理命令和配置更新

### 服务端部分

- 负责存储区域数据
- 将区域数据发送给客户端
- 处理管理员添加区域的命令

## 模块划分

### 1. 核心模块 (Core)

- 模组初始化和生命周期管理
- 注册命令和事件监听器

### 2. 数据模型模块 (Data Model)

- 区域（域名）数据结构
- 配置文件数据结构
- 数据验证和转换

### 3. 文件管理模块 (File Management)

- 检查和创建配置目录
- 读取和写入配置文件
- 读取和写入域名文件
- 生成默认配置文件

### 4. 区域检测模块 (Area Detection)

- 使用射线法检测点是否在多边形内
- 动态调整检测频率
- 区域层级和优先级计算

### 5. 渲染模块 (Rendering)

- 实现不同的渲染模式（CPU、OpenGL、Vulkan）
- 显示区域提示动画
- 管理显示样式

### 6. 网络模块 (Networking)

- 服务端向客户端发送区域数据
- 处理网络数据包和同步

### 7. 命令模块 (Commands)

- 实现各种命令功能
- 命令参数处理和验证

## 文件结构

```
areas-hint-mod/
├── gradle/                          # Gradle构建工具
│   └── wrapper/
│       ├── gradle-wrapper.jar       # Gradle包装器JAR文件
│       └── gradle-wrapper.properties # Gradle包装器配置
├── src/                             # 源代码目录
│   ├── main/                        # 服务端代码
│   │   ├── java/
│   │   │   └── areahint/
│   │   │       ├── Areashint.java   # 服务端主类（模组入口）
│   │   │       ├── command/         # 命令处理
│   │   │       │   ├── ServerCommands.java # 统一命令处理器（服务端+客户端命令）
│   │   │       │   └── DebugCommand.java   # 调试命令处理器
│   │   │       ├── data/            # 数据模型
│   │   │       │   ├── AreaData.java       # 区域数据模型（含altitude高度字段）
│   │   │       │   └── ConfigData.java     # 配置数据模型
│   │   │       ├── debug/           # 调试功能
│   │   │       │   └── DebugManager.java   # 服务端调试管理器
│   │   │       ├── file/            # 文件操作
│   │   │       │   ├── FileManager.java    # 文件管理器（读写配置和区域数据）
│   │   │       │   └── JsonHelper.java     # JSON序列化工具（支持altitude）
│   │   │       ├── mixin/           # Mixin注入
│   │   │       │   └── ExampleMixin.java   # 服务端Mixin示例
│   │   │       └── network/         # 网络通信
│   │   │           ├── Packets.java        # 网络包定义和通道标识符
│   │   │           └── ServerNetworking.java # 服务端网络处理
│   │   └── resources/               # 服务端资源
│   │       ├── fabric.mod.json      # Fabric模组配置文件
│   │       ├── areas-hint.mixins.json # 服务端Mixin配置
│   │       └── assets/
│   │           └── areas-hint/
│   │               └── icon.png     # 模组图标
│   └── client/                      # 客户端代码
│       ├── java/
│       │   └── areahint/
│       │       ├── AreashintClient.java     # 客户端主类
│       │       ├── AreashintDataGenerator.java # 数据生成器
│       │       ├── command/         # 客户端命令（已弃用）
│       │       │   └── ClientCommands.java # 客户端命令处理（已弃用）
│       │       ├── config/          # 配置管理
│       │       │   └── ClientConfig.java   # 客户端配置处理
│       │       ├── debug/           # 调试功能
│       │       │   ├── ClientDebugManager.java # 客户端调试管理器
│       │       │   └── DebugManager.java        # 调试管理器（空文件）
│       │       ├── detection/       # 区域检测（核心功能）
│       │       │   ├── AreaDetector.java   # 区域检测器（集成高度预筛选）
│       │       │   ├── AltitudeFilter.java # 高度预筛选器（新增功能）
│       │       │   └── RayCasting.java     # 射线检测和AABB算法
│       │       ├── mixin/           # 客户端Mixin
│       │       │   └── client/
│       │       │       └── ExampleClientMixin.java # 客户端Mixin示例
│       │       ├── network/         # 网络通信
│       │       │   └── ClientNetworking.java # 客户端网络处理
│       │       └── render/          # 渲染系统
│       │           ├── RenderManager.java  # 渲染管理器
│       │           ├── CPURender.java      # CPU渲染实现
│       │           ├── GLRender.java       # OpenGL渲染实现
│       │           └── VulkanRender.java   # Vulkan渲染实现（模拟）
│       └── resources/               # 客户端资源
│           └── areas-hint.client.mixins.json # 客户端Mixin配置
├── build.gradle                     # Gradle构建脚本
├── gradle.properties               # Gradle配置属性
├── gradlew                         # Unix/Linux Gradle包装器脚本
├── gradlew.bat                     # Windows Gradle包装器脚本
├── settings.gradle                 # Gradle设置文件
├── LICENSE                         # MIT许可证文件
├── README.md                       # 项目说明文档
├── .gitignore                      # Git忽略文件配置
├── .gitattributes                  # Git属性配置
├── prompt.txt                      # 原始需求文档
├── prompt_implementation.txt       # 实现方案文档
├── overworld.json                  # 主世界区域数据示例
└── areas-hint-template-1.20.4.zip # 模组压缩包模板

build/                              # 构建输出目录（自动生成）
├── libs/
│   ├── areas-hint-1.0.0.jar       # 主模组JAR文件
│   └── areas-hint-1.0.0-sources.jar # 源代码JAR文件
└── ...                             # 其他构建文件

.minecraft/areas-hint/              # 运行时配置和数据目录
├── config.json                     # 模组配置文件
├── overworld.json                  # 主世界区域数据
├── the_nether.json                 # 地狱区域数据
└── the_end.json                    # 末地区域数据
```

## 实现细节

### 域名数据格式 (JSON) (新的)

```json
{
  "name": "区域名称",
  "vertices": [
    {"x": 0, "z": 10},
    {"x": 10, "z": 0},
    {"x": 0, "z": -10},
    {"x": -10, "z": 0}
  ],
  "second-vertices": [
    {"x": -10, "z": 10},
    {"x": 10, "z": 10},
    {"x": 10, "z": -10},
    {"x": -10, "z": -10}
  ],
  "altitude": {
    "max": 100,
    "min": 0
  },
  "level": 1,
  "base-name": null
}
```

**新增字段说明：**
- `altitude`: 高度范围设置（可选）
  - `max`: 最大高度，null表示无上限
  - `min`: 最小高度，null表示无下限
  - 玩家只有在指定高度范围内才会检测到该区域

### 配置文件格式 (JSON)

```json
{
  "Frequency": 1,
  "SubtitleRender": "OpenGL",
  "SubtitleStyle": "mixed"
}
```

### 射线法判断点是否在多边形内

射线法(Ray Casting)通过从一个点向右发射一条射线，计算与多边形边界的交点数：
- 奇数个交点：点在多边形内
- 偶数个交点：点在多边形外

### 渲染模式

1. CPU渲染：所有渲染操作使用软件渲染，不使用GPU
2. OpenGL渲染：使用OpenGL API进行渲染（默认）
3. Vulkan渲染：使用Vulkan API进行渲染

### 字幕样式

1. full：显示完整域名路径（如"顶级域名·二级域名·三级域名"）
2. simple：仅显示当前所在级别的域名
3. mixed：根据层级显示适当的域名组合（默认）

## 命令系统

- `/areahint help` - 显示所有命令及其用法
- `/areahint reload` - 重新加载配置和域名文件
- `/areahint delete` - 列出所有可删除的域名
- `/areahint delete <域名>` - 删除指定域名（需要创建者权限或管理员权限）
  - 💡 **智能补全**：输入 `/areahint delete ` 后按Tab键，自动显示当前玩家可删除的域名列表
- `/areahint frequency <值>` - 设置检测频率
- `/areahint subtitlerender <cpu|opengl|vulkan>` - 设置渲染模式
- `/areahint subtitlestyle <full|simple|mixed>` - 设置字幕样式
- `/areahint add <域名JSON>` - 添加新的域名（需要管理员权限）
- `/areahint debug` - 切换调试模式（需要管理员权限）
- `/areahint debug on|off|status` - 启用/禁用/查看调试模式状态（需要管理员权限）

## 域名删除功能

### 域名签名系统

从该版本开始，所有新创建的域名都会自动包含创建者的签名(`signature`字段)，用于权限管理：

- 使用 `/areahint add` 命令创建域名时，会自动设置`signature`为命令执行者的用户名
- 没有签名的域名（旧版本创建）只能被管理员删除，普通玩家无法删除
- 有签名的域名可以被创建者或管理员(Op等级2)删除

### 删除权限检查

删除域名时会进行以下检查：

1. **签名验证**：
   - 有签名的域名：检查执行者是否为域名创建者或管理员
   - 无签名的旧域名：只有管理员可以删除
2. **依赖检查**：检查是否有其他域名通过`base-name`字段引用该域名
3. **级联保护**：如果存在次级域名，则无法删除上级域名

### 删除命令使用示例

```bash
# 列出所有可删除的域名
/areahint delete

# 删除指定域名
/areahint delete "商业区"

# 智能自动补全使用方法：
# 1. 输入: /areahint delete 
# 2. 按Tab键
# 3. 系统自动显示当前玩家可删除的域名列表
# 4. 选择或继续输入域名
```

### 智能自动补全系统

删除命令支持智能自动补全功能，具有以下特点：

- **权限感知**：只显示当前玩家有权限删除的域名
- **实时更新**：根据当前维度和玩家权限动态生成建议列表
- **智能过滤**：
  - 普通玩家：只显示自己创建的域名
  - 管理员：显示所有可删除的域名（包括旧版本无签名域名）
- **模糊匹配**：支持输入域名前缀进行过滤

### JSON格式更新

新的域名JSON格式现在包含`signature`字段：

```json
{
  "name": "域名名称",
  "vertices": [{"x": 0, "z": 0}, {"x": 10, "z": 0}, {"x": 10, "z": 10}, {"x": 0, "z": 10}],
  "second-vertices": [{"x": 0, "z": 10}, {"x": 10, "z": 10}, {"x": 10, "z": 0}, {"x": 0, "z": 0}],
  "altitude": {"max": 100, "min": 50},
  "level": 1,
  "base-name": null,
  "signature": "player_name"
}
```

**字段说明**：
- `signature`: 域名创建者的用户名，自动设置，可以为null（适配旧版本）

## 调试功能

模组提供了强大的调试功能，可以帮助开发者和服务器管理员诊断问题：

### 调试命令

使用 `/areahint debug` 命令可以切换调试模式。启用调试模式后，模组会向玩家实时显示以下信息：

- 区域检测过程和结果
- 玩家位置信息
- 配置加载和应用情况
- 渲染状态和过程
- 网络通信情况

调试信息按类别使用不同颜色显示，便于区分：

- 区域检测（青色）
- 玩家位置（绿色）
- 配置（黄色）
- 网络（紫色）
- 渲染（蓝色）
- 命令（白色）
- 通用（灰色）

### 调试文件

调试相关的代码文件：

- `src/main/java/areahint/debug/DebugManager.java` - 服务端调试管理器
- `src/client/java/areahint/debug/ClientDebugManager.java` - 客户端调试管理器
- `src/main/java/areahint/command/DebugCommand.java` - 调试命令处理器

调试功能设计为仅在需要时消耗资源，不使用调试命令时不会影响游戏性能。

## 高度预筛选机制

### 概述

高度预筛选是一个新增的性能优化功能，通过在射线检测和AABB检测之前预先筛选符合高度条件的区域，提高区域检测效率。

### 工作原理

1. **3D扩展原理**：将原本的2D多边形区域扩展为3D柱体
2. **横截面不变**：在玩家所在高度的横截面仍然是原来的2D多边形
3. **向量兼容**：多边形的顶点坐标在XZ平面上保持不变，完全兼容现有算法

### 处理流程

```
玩家位置检测
     ↓
获取玩家Y坐标（高度）
     ↓
高度预筛选 (AltitudeFilter)
     ↓
筛选后的区域列表
     ↓
原有检测机制（AABB + 射线法）
     ↓
最终结果
```

### 高度数据格式

```json
"altitude": {
    "max": 100,    // 最大高度，null表示无上限
    "min": 0       // 最小高度，null表示无下限
}
```

### 实现文件

- `src/client/java/areahint/detection/AltitudeFilter.java` - 高度预筛选器
  - `filterByAltitude()` - 根据玩家高度筛选区域
  - `validateAltitude()` - 验证高度数据有效性
  - 集成调试信息输出

### 性能优势

- **减少计算量**：预筛选可显著减少需要进行射线检测的区域数量
- **向后兼容**：altitude字段为可选，现有数据无需修改
- **模块化设计**：独立的预筛选模块，易于维护和扩展

### 高度验证

系统会验证高度数据的合理性：
- 最大高度不能小于最小高度
- 高度值建议在Minecraft合理范围内（-64到320）
- 在`/areahint add`命令中自动验证 