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
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── areahint/
│   │   │       ├── Areashint.java  # 服务端主类
│   │   │       ├── data/
│   │   │       │   ├── AreaData.java  # 区域数据模型
│   │   │       │   └── ConfigData.java  # 配置数据模型
│   │   │       ├── file/
│   │   │       │   ├── FileManager.java  # 文件管理工具
│   │   │       │   └── JsonHelper.java  # JSON处理工具
│   │   │       ├── network/
│   │   │       │   ├── ServerNetworking.java  # 服务端网络处理
│   │   │       │   └── Packets.java  # 网络数据包定义
│   │   │       └── command/
│   │   │           └── ServerCommands.java  # 服务端命令
│   │   └── resources/
│   │       ├── fabric.mod.json  # 模组元数据
│   │       └── assets/
│   │           └── areas-hint/
│   │               └── lang/
│   │                   └── zh_cn.json  # 中文语言文件
│   └── client/
│       ├── java/
│       │   └── areahint/
│       │       ├── AreashintClient.java  # 客户端主类
│       │       ├── detection/
│       │       │   ├── AreaDetector.java  # 区域检测逻辑
│       │       │   └── RayCasting.java  # 射线法实现
│       │       ├── render/
│       │       │   ├── RenderManager.java  # 渲染管理
│       │       │   ├── CPURender.java  # CPU渲染实现
│       │       │   ├── GLRender.java  # OpenGL渲染实现
│       │       │   └── VulkanRender.java  # Vulkan渲染实现
│       │       ├── config/
│       │       │   └── ClientConfig.java  # 客户端配置处理
│       │       ├── network/
│       │       │   └── ClientNetworking.java  # 客户端网络处理
│       │       └── command/
│       │           └── ClientCommands.java  # 客户端命令
│       └── resources/
│           └── areas-hint.client.mixins.json  # 客户端Mixin配置
└── ../areas-hint/  # 外部配置和数据目录（不在mod JAR中）
    ├── config.json  # 配置文件
    ├── overworld.json  # 主世界域名文件
    ├── the_nether.json  # 地狱域名文件
    └── the_end.json  # 末地域名文件
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
  "level": 1,
  "base-name": null
}
```

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
- `/areahint frequency <值>` - 设置检测频率
- `/areahint subtitlerender <cpu|opengl|vulkan>` - 设置渲染模式
- `/areahint subtitlestyle <full|simple|mixed>` - 设置字幕样式
- `/areahint add <域名JSON>` - 添加新的域名（需要管理员权限） 