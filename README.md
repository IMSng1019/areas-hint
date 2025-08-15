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
- 交互式域名添加系统（EasyAdd），普通玩家也能轻松创建域名

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

### 7. 世界文件夹管理 (World Folder Management)

每个世界（服务器）都有独立的文件夹来存储域名和维度域名配置，实现多世界支持。

#### 特性
- **独立世界文件夹**：每个世界使用 `IP地址_世界名称` 格式的文件夹
- **自动创建**：连接世界时自动检测并创建对应文件夹
- **智能路径管理**：服务端和客户端自动使用正确的世界文件夹
- **配置分离**：全局配置在根目录，世界特定数据在世界文件夹

#### 文件夹结构示例

```
.minecraft/areas-hint/
├── config.json                    # 全局配置文件
├── 192.168.1.100_MyServer/        # 世界文件夹（多人游戏）
│   ├── overworld.json             # 主世界域名文件
│   ├── nether.json                # 地狱域名文件
│   ├── end.json                   # 末地域名文件
│   └── dimensional_names.json     # 维度域名文件
├── localhost_SinglePlayer/        # 单人游戏世界
│   ├── overworld.json
│   └── dimensional_names.json
└── localhost_Server/               # 本地服务器
    ├── overworld.json
    ├── nether.json
    ├── end.json
    └── dimensional_names.json
```

#### 工作原理
1. **连接检测**：每次加入世界时检测IP地址和世界名称
2. **文件夹创建**：如果对应文件夹不存在则自动创建
3. **文件初始化**：在新文件夹中创建默认的配置文件
4. **路径重定向**：所有域名文件操作重定向到当前世界文件夹
5. **网络同步**：服务端向客户端发送世界信息以确保一致性

### 8. 维度域名模块 (Dimensional Names)

- 管理维度（世界）的自定义名称
- 维度域名等级为0.5，介于普通区域之外
- 离开所有区域时显示维度域名
- 进入世界时如果不在任何区域内显示维度域名
- 支持动态配置和同步

### 9. 命令模块 (Commands)

- 实现各种命令功能
- 命令参数处理和验证
- 维度域名管理命令

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
│   │   │       │   ├── DebugCommand.java   # 调试命令处理器
│   │   │       │   ├── RecolorCommand.java # 域名重新着色指令处理器（新增）
│   │   │       │   ├── RenameAreaCommand.java # 域名重命名指令处理器（新增）
│   │   │       │   └── DimensionalNameCommands.java # 维度域名命令处理器
│   │   │       ├── data/            # 数据模型
│   │   │       │   ├── AreaData.java       # 区域数据模型（含altitude高度字段和color颜色字段）
│   │   │       │   ├── ConfigData.java     # 配置数据模型
│   │   │       │   └── DimensionalNameData.java # 维度域名数据模型
│   │   │       ├── debug/           # 调试功能
│   │   │       │   └── DebugManager.java   # 服务端调试管理器
│   │   │       ├── dimensional/     # 维度域名管理
│   │   │       │   └── DimensionalNameManager.java # 服务端维度域名管理器
│   │   │       ├── easyadd/         # EasyAdd服务端支持
│   │   │       │   └── EasyAddServerNetworking.java # 服务端网络处理器
│   │   │       ├── render/          # 渲染工具（新增）
│   │   │       │   └── DomainRenderer.java # 域名渲染工具类（处理颜色显示和层级关系）
│   │   │       ├── util/            # 工具类（新增）
│   │   │       │   ├── ColorUtil.java      # 颜色工具类（颜色验证、转换和预定义颜色）
│   │   │       │   ├── SurfaceNameHandler.java # 联合域名处理器（显示逻辑和重复检查）
│   │   │       │   └── AreaDataConverter.java # AreaData与JsonObject转换工具
│   │   │       ├── world/           # 世界文件夹管理（新增）
│   │   │       │   └── WorldFolderManager.java # 服务端世界文件夹管理器
│   │   │       ├── file/            # 文件操作
│   │   │       │   ├── FileManager.java    # 文件管理器（读写配置和区域数据）
│   │   │       │   └── JsonHelper.java     # JSON序列化工具（支持altitude）
│   │   │       ├── mixin/           # Mixin注入
│   │   │       │   └── ExampleMixin.java   # 服务端Mixin示例
│   │   │       └── network/         # 网络通信
│   │   │           ├── Packets.java        # 网络包定义和通道标识符
│   │   │           ├── ServerNetworking.java # 服务端网络处理
│   │   │           ├── DimensionalNameNetworking.java # 维度域名网络传输
│   │   │           └── ServerWorldNetworking.java # 服务端世界网络处理（新增）
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
│       │       ├── command/         # 客户端命令
│       │       │   ├── ClientCommands.java # 客户端命令处理（已弃用）
│       │       │   └── ModToggleCommand.java # 模组开关命令处理器（新增）
│       │       ├── config/          # 配置管理
│       │       │   └── ClientConfig.java   # 客户端配置处理
│       │       ├── debug/           # 调试功能
│       │       │   ├── ClientDebugManager.java # 客户端调试管理器
│       │       │   └── DebugManager.java        # 调试管理器（空文件）
│       │       ├── dimensional/     # 维度域名管理
│       │       │   └── ClientDimensionalNameManager.java # 客户端维度域名管理器
│       │       ├── detection/       # 区域检测（核心功能）
│       │       │   ├── AreaDetector.java   # 区域检测器（集成高度预筛选）
│       │       │   ├── AltitudeFilter.java # 高度预筛选器（新增功能）
│       │       │   └── RayCasting.java     # 射线检测和AABB算法
│       │       ├── easyadd/        # EasyAdd交互式域名添加系统
│       │       │   ├── EasyAddManager.java    # EasyAdd核心管理器
│       │       │   ├── EasyAddUI.java         # 用户界面系统
│       │       │   ├── EasyAddGeometry.java   # 几何计算工具
│       │       │   ├── EasyAddConfig.java     # 配置管理器
│       │       │   ├── EasyAddKeyHandler.java # 按键监听处理器
│       │       │   ├── EasyAddAltitudeManager.java # 高度设置管理器（新增）
│       │       │   └── EasyAddNetworking.java # 客户端网络通信
│       │       ├── mixin/           # 客户端Mixin
│       │       │   └── client/
│       │       │       └── ExampleClientMixin.java # 客户端Mixin示例
│       │       ├── network/         # 网络通信
│       │       │   ├── ClientNetworking.java # 客户端网络处理
│       │       │   ├── ClientDimensionalNameNetworking.java # 客户端维度域名网络处理
│       │       │   └── ClientWorldNetworking.java # 客户端世界网络处理（新增）
│       │       ├── world/           # 世界文件夹管理（新增）
│       │       │   └── ClientWorldFolderManager.java # 客户端世界文件夹管理器
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
├── dimensional_names.json          # 维度域名配置文件
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

### 维度域名配置格式 (dimensional_names.json)

```json
[
  {
    "dimensionId": "minecraft:overworld",
    "displayName": "蛮荒大陆"
  },
  {
    "dimensionId": "minecraft:the_nether", 
    "displayName": "恶堕之域"
  },
  {
    "dimensionId": "minecraft:the_end",
    "displayName": "终末之地"
  }
]
```

**字段说明**：
- `dimensionId`: 维度标识符（如 minecraft:overworld）
- `displayName`: 显示名称（玩家看到的维度域名）

**特殊说明**：
- 维度域名等级为0.5，优先级低于所有普通区域
- 离开所有区域时自动显示维度域名
- 进入世界时如果不在任何区域内显示维度域名

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
- `/areahint easyadd` - 启动交互式域名添加（普通玩家可用）
- `/areahint recolor` - 列出当前维度可编辑的域名
- `/areahint recolor <域名> <颜色>` - 修改指定域名的颜色（支持颜色名称或十六进制代码）
- `/areahint debug` - 切换调试模式（需要管理员权限）
- `/areahint debug on|off|status` - 启用/禁用/查看调试模式状态（需要管理员权限）
- `/areahint dimensionalityname` - 列出所有维度及其域名配置（需要管理员权限）
- `/areahint dimensionalityname <维度>` - 显示指定维度的当前域名
- `/areahint dimensionalityname <维度> <新名称>` - 设置维度的域名

## EasyAdd 交互式域名添加

### 功能概述

EasyAdd是专为普通玩家设计的交互式域名添加系统，无需手动编写复杂的JSON格式，通过简单的聊天界面和按键操作即可创建域名。

### 主要特点

- 🎮 **用户友好**：完全图形化界面，无需学习JSON语法
- 🔐 **权限开放**：普通玩家即可使用，无需管理员权限
- 🎯 **精确定位**：使用可自定义的按键（默认T键）记录坐标点
- ✅ **智能验证**：自动验证域名层级关系和边界包含
- 🔄 **实时反馈**：每一步都有清晰的提示和确认
- 📊 **自动计算**：自动生成AABB包围盒和高度范围

### 使用流程

1. **启动EasyAdd**
   ```
   /areahint easyadd
   ```

2. **输入域名名称**
   - 在聊天框中直接输入域名名称
   - 例如：商业区、住宅区、工业区等

3. **选择域名等级**
   - 点击按钮选择：1级（顶级域名）、2级（二级域名）、3级（三级域名）
   - 1级域名无需上级域名
   - 2/3级域名需要选择上级域名

4. **选择上级域名**（仅次级域名需要）
   - 系统自动列出可选的上级域名
   - 点击选择合适的上级域名

5. **记录坐标点**
   - 走到区域边界，按 **T键**（可自定义）记录坐标点
   - 至少需要记录3个点形成有效区域
   - 每次记录后可选择继续记录或完成记录

6. **设置高度范围**
   - **确认域名信息**：基于记录点的Y坐标自动扩展高度范围（推荐）
   - **自定义高度**：手动输入最低和最高Y坐标值
   - **不限制高度**：域名在所有Y坐标生效，无高度限制
   - 自动计算会在记录点基础上向下扩展5格，向上扩展10格
   - 自定义高度可以精确控制域名的垂直边界
   - 不限制高度适用于跨越多个高度层的大型区域

7. **确认保存**
   - 查看域名信息摘要（包含高度范围）
   - 确认无误后点击保存

### 按键设置

- **默认按键**：T键
- **自定义方式**：在游戏设置 > 控制 > 按键绑定 > Areas Hint 类别中修改
- **按键功能**：记录当前玩家位置作为域名顶点

### 智能验证机制

EasyAdd会自动验证：

1. **坐标有效性**：确保记录足够的坐标点（至少3个）
2. **层级关系**：验证域名等级的正确性
3. **边界包含**：确保子域名完全位于父域名内
4. **高度范围**：验证子域名的高度在父域名范围内（不限制高度除外）
5. **高度合理性**：确保自定义高度值在合理范围内（-64到320）
6. **重名检查**：防止创建重名的域名

### 网络同步

EasyAdd创建的域名会：
- 自动保存到服务端文件
- 同步到所有在线玩家
- 立即生效，无需重启

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

## 域名颜色系统

模组支持为每个域名设置自定义颜色，实现个性化的域名显示效果。

### 颜色功能特点

- **层级着色**：不同等级的域名可以设置不同颜色
- **智能显示**：域名链显示时，各级域名使用各自的颜色，分隔符为白色
- **预定义颜色**：提供14种常用颜色供快速选择
- **自定义颜色**：支持十六进制颜色代码自定义
- **兼容性**：未设置颜色的域名默认显示为白色

### 颜色管理命令

#### 查看可编辑域名
```bash
/areahint recolor
```
列出当前维度中您有权限编辑的域名及其当前颜色。

#### 修改域名颜色
```bash
/areahint recolor <域名> <颜色>
```

**颜色参数支持**：
- **颜色名称**：白色、红色、粉红色、橙色、黄色、棕色、浅绿色、深绿色、浅蓝色、深蓝色、浅紫色、紫色、灰色、黑色
- **十六进制代码**：如 `#FF0000`（红色）、`#00FF00`（绿色）

### 使用示例

```bash
# 设置商业区为金色
/areahint recolor 商业区 黄色

# 使用十六进制设置自定义颜色
/areahint recolor 居住区 #87CEEB

# 查看当前可编辑的域名
/areahint recolor
```

### 权限说明

- **普通玩家**：只能修改自己创建的域名颜色
- **管理员**：可以修改所有域名的颜色

### 域名显示效果

当域名存在层级关系时，显示格式为：
```
顶级域名（金色）·二级域名（绿色）·三级域名（蓝色）
```

其中"·"符号始终为白色，起分隔作用。

### JSON格式扩展

新的域名JSON格式包含`color`字段：

```json
{
  "name": "商业区",
  "vertices": [...],
  "second-vertices": [...],
  "altitude": {"max": 100, "min": 0},
  "level": 1,
  "base-name": null,
  "signature": "player_name",
  "color": "#FFD700"
}
```

**字段说明**：
- `color`: 域名颜色，十六进制格式（如#FFFFFF），可以为null（默认白色）

## 域名重命名系统

模组支持安全的域名重命名功能，允许玩家修改已创建的域名。

### 重命名功能特点

- **权限控制**：普通玩家只能重命名自己创建的域名，管理员可以重命名任何域名
- **二级确认**：重命名操作需要玩家明确确认，避免误操作
- **重复检查**：自动检查新域名是否已存在，确保唯一性
- **自动同步**：重命名成功后自动reload并重新分发给所有玩家
- **层级保持**：重命名不影响域名的等级和父子关系

### 重命名管理命令

#### 查看可重命名域名
```bash
/areahint renamearea
```
列出当前维度中您有权限重命名的域名及其创建者信息。

#### 重命名域名
```bash
/areahint renamearea <域名> <新名称>
```

**使用流程**：
1. 输入重命名命令
2. 系统显示确认对话框，包含原域名和新域名
3. 使用确认命令完成重命名

#### 确认重命名
```bash
/areahint renamearea confirm
```
确认执行待处理的重命名操作。

#### 取消重命名
```bash
/areahint renamearea cancel
```
取消待处理的重命名操作。

### 使用示例

```bash
# 查看可重命名的域名
/areahint renamearea

# 重命名域名
/areahint renamearea 旧商业区 新商业中心

# 在确认对话框出现后，确认重命名
/areahint renamearea confirm

# 或者取消重命名
/areahint renamearea cancel
```

### 权限说明

- **普通玩家**：只能重命名自己创建的域名（signature字段匹配）
- **管理员**（权限等级2）：可以重命名所有域名

### 安全机制

- **唯一性检查**：新域名不能与现有域名重复
- **权限验证**：严格检查玩家对域名的编辑权限
- **事务性操作**：确保重命名操作的原子性
- **自动备份**：重命名前自动保存域名数据

### 确认对话框示例

当玩家输入重命名命令后，系统会显示：

```
===== 域名重命名确认 =====
您确认将域名重命名吗？
原域名: 旧商业区
新域名: 新商业中心

点击确认: [是] 或输入 /areahint renamearea confirm
点击取消: [否] 或输入 /areahint renamearea cancel
```

### 成功提示

重命名成功后，系统会显示：

```
域名重命名成功！
原域名: 旧商业区
新域名: 新商业中心
```

## 模组开关系统

模组提供了便捷的开关功能，允许玩家根据需要随时启用或禁用模组的显示功能。

### 功能特点

- **客户端本地控制**：开关命令完全在客户端执行，无需服务器管理员权限
- **配置持久化**：开关状态会自动保存到配置文件中，下次进入世界时保持设置
- **智能数据管理**：关闭时不显示域名和不计算，但仍会接收服务器发送的域名数据
- **即时生效**：命令执行后立即生效，无需重启游戏

### 开关命令

#### 启用模组
```bash
/areahint on
```
启用模组的所有功能，包括域名显示和区域检测。

#### 禁用模组  
```bash
/areahint off
```
禁用模组的显示和检测功能，但仍会接收来自服务器的域名数据更新。

### 使用示例

```bash
# 启用模组
/areahint on
> §a[AreaHint] 模组已启用！域名显示和检测功能已开启。

# 禁用模组
/areahint off  
> §c[AreaHint] 模组已禁用！域名将不再显示，但仍会接收服务器数据。
```

### 工作原理

#### 启用状态（默认）
- ✅ 实时检测玩家位置
- ✅ 显示区域域名和维度域名
- ✅ 接收服务器域名数据
- ✅ 执行所有模组功能

#### 禁用状态
- ❌ 不检测玩家位置
- ❌ 不显示任何域名
- ✅ 继续接收服务器域名数据（保持数据同步）
- ❌ 暂停所有显示相关功能

### 配置存储

开关状态存储在客户端配置文件 `config.json` 中：

```json
{
  "frequency": 1,
  "subtitleRender": "OpenGL", 
  "subtitleStyle": "mixed",
  "enabled": true
}
```

**字段说明**：
- `enabled`: 模组启用状态，true为开启，false为关闭

## 联合域名系统

### 功能概述

联合域名（Surface Name）系统允许不同的实际域名显示相同的名称，解决了多个区域需要显示相同标识的问题。通过保持域名（name）的唯一性作为ID，同时支持联合域名（surfacename）作为显示名称，确保了系统的稳定性和用户体验。

### 核心特性

- **域名唯一性保证**：实际域名（name字段）保持唯一，作为系统内部ID
- **显示名称共享**：多个不同域名可以使用相同的联合域名进行显示
- **智能显示逻辑**：优先显示联合域名，如无设置则回退到实际域名
- **EasyAdd集成**：在域名创建流程中自动询问联合域名设置
- **重复检查机制**：确保实际域名不会重复，避免引用错误

### 数据格式

完整的域名JSON格式现在包含 `surfacename` 字段：

```json
{
  "name": "实际域名（唯一ID）",
  "surfacename": "联合域名（显示名称）",
  "vertices": [...],
  "second-vertices": [...],
  "altitude": {...},
  "level": 1,
  "base-name": null,
  "signature": "创建者",
  "color": "#FFFFFF"
}
```

### 使用示例

```bash
# 创建两个不同域名但显示相同名称
/areahint easyadd
# 域名名称: shop_area_1
# 联合域名: 商店区
# 后续设置...

/areahint easyadd  
# 域名名称: shop_area_2
# 联合域名: 商店区  
# 后续设置...
```

两个域名在系统中都显示为"商店区"，但实际引用的是不同的唯一ID。

### EasyAdd流程更新

EasyAdd指令现在包含联合域名询问步骤：

1. **输入域名名称**：输入唯一的实际域名标识
2. **输入联合域名**：输入显示名称（可与其他域名相同，可留空）
3. **选择域名等级**：选择1级、2级或3级域名
4. **其他设置**：按原流程继续设置
5. **重复检查**：系统自动检查实际域名是否重复

## 域名高度设置系统

模组提供了强大的域名高度管理功能，允许玩家设置域名在Y轴的覆盖范围，实现精确的三维区域控制。

### 功能特点

- **权限控制**：普通玩家可以修改自己创建的域名或被自己basename引用的域名，管理员可以修改所有域名
- **交互式界面**：提供直观的域名选择和高度设置界面
- **灵活配置**：支持自定义高度范围或无限制高度
- **即时生效**：设置完成后立即重新分发数据给所有玩家
- **安全验证**：包含高度范围合理性检查和权限验证

### 高度设置命令

#### 启动高度设置
```bash
/areahint sethigh
```
列出当前维度中所有您有权限修改高度的域名。

### 权限逻辑

#### 管理员权限（权限等级2）
- ✅ 可以修改当前维度中的所有域名高度
- ✅ 无任何限制

#### 普通玩家权限（权限等级0）
- ✅ 可以修改自己创建的域名（signature字段匹配玩家名称）
- ✅ 可以修改被自己basename引用的域名
- ❌ 不能修改其他玩家创建的域名

### 使用流程

#### 第一步：执行命令
```bash
/areahint sethigh
> §a已向您发送可修改高度的域名列表，请在客户端选择要设置高度的域名
```

#### 第二步：选择域名
客户端会显示可编辑的域名列表，包含：
- 域名名称
- 当前高度设置情况

#### 第三步：选择高度类型
- **不限制高度**：域名在所有Y轴高度都有效
- **自定义高度**：设置具体的最高和最低高度

#### 第四步：设置具体高度（如选择自定义）
- 输入最高高度（可为空表示无上限）
- 输入最低高度（可为空表示无下限）

#### 第五步：确认设置
系统会验证高度数据并保存，然后自动重新分发给所有玩家。

### 高度范围说明

#### 高度值限制
- **合理范围**：-64 到 320（Minecraft世界标准高度）
- **最大高度**：可设置为null表示无上限
- **最小高度**：可设置为null表示无下限
- **逻辑验证**：最大高度必须大于等于最小高度

#### 高度效果
```json
// 示例：商业区域高度限制在地面到天空
{
  "name": "商业区",
  "altitude": {
    "max": 256.0,
    "min": 64.0
  }
}

// 示例：地下矿区高度限制
{
  "name": "矿区",
  "altitude": {
    "max": 0.0,
    "min": -64.0
  }
}

// 示例：无高度限制
{
  "name": "全高度区域",
  "altitude": null
}
```

### 使用示例

```bash
# 普通玩家修改自己创建的域名
/areahint sethigh
# 选择域名："我的商店"，当前高度：无限制
# 选择：自定义高度
# 最高高度：100
# 最低高度：64
> §a域名 "我的商店" 高度设置成功！
> §7原高度: 无限制
> §7新高度: 最高:100.0, 最低:64.0

# 管理员修改任意域名
/areahint sethigh
# 选择域名："公共广场"，当前高度：最高:200.0, 最低:60.0
# 选择：不限制高度
> §a域名 "公共广场" 高度设置成功！
> §7原高度: 最高:200.0, 最低:60.0
> §7新高度: 无限制
```

### 工作原理

#### 权限检查机制
1. **身份验证**：检查玩家是否为管理员或域名创建者
2. **关联检查**：检查域名是否被玩家创建的其他域名通过basename引用
3. **权限授予**：满足条件的域名加入可编辑列表

#### 数据修改流程
1. **客户端选择**：玩家在客户端界面选择域名和高度设置
2. **服务器验证**：服务器再次验证权限和数据合理性
3. **文件更新**：修改对应维度的域名文件
4. **数据分发**：自动执行reload将更新发送给所有在线玩家

#### 三维区域控制
- 高度设置将平面域名扩展为立体区域
- 玩家只有在指定高度范围内才会触发域名检测
- 实现了精确的三维空间管理

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