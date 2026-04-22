# Areas Hint 技术解析：一个 Minecraft 区域提示模组是如何把“世界观提示”做成工程系统的

> 参考资料：`README.md`、`CLAUDE.md`、`build.gradle`、`gradle.properties` 以及项目核心源码。

## 前言

在很多 Minecraft 服务器与大型单人存档里，区域名称并不只是一个 HUD 文案。
它往往承担着空间导览、世界观表达、玩法边界、权限控制，甚至剧情节奏提示等多重职责。

**Areas Hint** 这个模组表面上做的是“玩家进入区域时，在屏幕上方显示区域名称”，但从代码实现来看，它实际上解决的是一整套更完整的工程问题：

- 如何用数据描述一个可编辑、可嵌套的区域系统
- 如何在客户端高频检测玩家位置，又尽量不影响性能
- 如何让服务端统一管理数据、权限与同步
- 如何把多世界、多服务器的数据彻底隔离
- 如何在不强绑生态的前提下接入 LuckPerms、BlueMap 这类外部系统

如果把它放回 Minecraft 模组开发的语境里看，这更像是一个**“区域感知 + 轻交互编辑 + 服务端协同”**的小型平台，而不只是一个标题显示功能。

---

## 1. 先定义问题：这里的“域名”到底是什么？

这个项目里的“域名”不是互联网域名，而是**一块游戏空间的命名单位**。
例如：王国、主城、商业区、王座厅、地下矿井，都可以被建模为一个“域名”。

从项目文档与数据结构看，作者希望这个“域名”系统至少满足几件事：

1. **区域必须是多边形，而不是固定矩形**
2. **区域可以有层级关系**，例如“王国 → 主城 → 王座厅”
3. **区域可以带高度范围**，实现 2.5D 的空间过滤
4. **区域有显示名、颜色、创建者等元信息**
5. **区域数据应该能脱离代码存在于 JSON 中**
6. **不同服务器 / 世界的数据不能混在一起**

这也是为什么项目核心数据对象 `AreaData` 不是只保存一个名字和坐标，而是保存了一整套几何、层级、显示和权限相关字段，见 `src/main/java/areahint/data/AreaData.java:11`。

---

## 2. 总体架构：客户端负责“感知与显示”，服务端负责“存储与裁决”

这个模组最值得先看的，不是某个算法，而是**职责分层**。

服务端入口集中在 `src/main/java/areahint/Areashint.java:47`，客户端入口集中在 `src/client/java/areahint/AreashintClient.java:55`。
两边都采用“集中初始化”的方式，但承担的职责明显不同。

### 服务端做什么？

服务端负责：

- 初始化文件目录与世界文件夹
- 注册命令
- 注册网络处理器
- 统一处理权限判断
- 存储区域 JSON 与维度域名 JSON
- 在玩家加入或数据变化时同步给客户端
- 对接 LuckPerms、BlueMap 等可选生态

例如：

- LuckPerms 兼容层初始化：`src/main/java/areahint/Areashint.java:57`
- BlueMap 兼容层初始化：`src/main/java/areahint/Areashint.java:60`
- 世界文件夹初始化：`src/main/java/areahint/Areashint.java:160`
- 网络层初始化：`src/main/java/areahint/Areashint.java:87`
- 命令注册：`src/main/java/areahint/Areashint.java:134`

### 客户端做什么？

客户端负责：

- 读取配置
- 管理区域检测器与异步检测器
- 管理渲染器
- 接收服务端同步数据
- 执行 tick 级别的位置检测
- 处理 EasyAdd / ExpandArea / ShrinkArea / DeleteHint 等交互式编辑流程

例如：

- 检测器初始化：`src/client/java/areahint/AreashintClient.java:67`
- 渲染器初始化：`src/client/java/areahint/AreashintClient.java:71`
- 网络初始化：`src/client/java/areahint/AreashintClient.java:77`
- 统一按键处理器注册：`src/client/java/areahint/AreashintClient.java:119`
- 客户端 tick 主循环：`src/client/java/areahint/AreashintClient.java:342`

### 这套分层为什么合理？

因为“区域检测”是一个**高频、和玩家位置强绑定**的动作，天然更适合在客户端完成；
而“区域存储、权限控制、最终写入与广播同步”则更适合放在服务端统一处理。

换句话说，这个项目选择的是：

```text
客户端：高频感知 + 本地显示
服务端：真实数据源 + 权限裁决 + 同步广播
```

这个边界划分很清晰，也直接决定了后续所有模块的设计风格。

---

## 3. 数据模型：一个区域对象，如何同时服务“检测”“显示”“编辑”和“同步”？

`AreaData` 是整个项目最核心的数据结构之一，定义位于 `src/main/java/areahint/data/AreaData.java:11`。
它包含的字段非常完整：

- `name`：区域真实名称
- `vertices`：一级顶点，多边形本体
- `second-vertices`：二级顶点，用于 AABB 快速预筛选
- `altitude`：高度范围
- `level`：层级等级
- `base-name`：上级域名
- `signature`：创建者
- `color`：颜色
- `surfacename`：联合 / 表面显示名

### 一个很好的设计细节：显示名和真实名分离

显示时并不总是直接使用 `name`。
`SurfaceNameHandler` 会优先读取 `surfacename`，如果没有，再回退到 `name`，见 `src/main/java/areahint/util/SurfaceNameHandler.java:18`。

而 `AreaDataConverter.getDisplayName()` 又把这个逻辑封装成统一入口，见 `src/main/java/areahint/util/AreaDataConverter.java:158`。

这意味着：

- 数据层仍保留稳定的真实标识 `name`
- 展示层可以使用更友好的 `surfacename`
- 命令、显示、JSON 转换之间不会互相缠死

这是一个很典型的“**标识字段**”与“**展示字段**”解耦设计。

### 另一个关键细节：缓存直接放进数据对象

`AreaData` 不只是一个 DTO。
它内部还缓存了：

- AABB 的 `minX / maxX / minZ / maxZ`
- 多边形中心点

相关逻辑在 `src/main/java/areahint/data/AreaData.java:149`。

加载区域后，客户端会立刻调用 `computeCache()`，见 `src/client/java/areahint/detection/AreaDetector.java:41`。
这让后续检测时不需要每次重新遍历顶点求包围盒，直接把“预处理结果”跟着数据对象走。

这类“把热点计算前移到加载阶段”的做法，非常适合需要频繁判断的几何系统。

---

## 4. 文件系统设计：为什么它不是只写一个 `overworld.json`？

很多模组的数据管理容易做成“单目录堆文件”，但这个项目显然不是这个思路。

### 基础数据目录

模组会在游戏目录下创建 `areas-hint` 文件夹，见 `src/main/java/areahint/file/FileManager.java:35`。

### 按世界 / 服务器隔离

真正有辨识度的设计，在 `WorldFolderManager` 和 `ClientWorldFolderManager`：

- 服务端：`src/main/java/areahint/world/WorldFolderManager.java:31`
- 客户端：`src/client/java/areahint/world/ClientWorldFolderManager.java:41`

服务端会根据 `serverAddress + worldName` 生成独立目录，见 `src/main/java/areahint/world/WorldFolderManager.java:71`。
如果目录不存在，就自动创建：

- `overworld.json`
- `the_nether.json`
- `the_end.json`
- `dimensional_names.json`

对应实现见 `src/main/java/areahint/world/WorldFolderManager.java:109`。

这意味着：

```text
.minecraft/areas-hint/
  ├── localhost_SinglePlayer/
  ├── 192.168.1.100_MyServer/
  └── localhost_Server/
```

每个世界、每台服务器的数据彼此独立，不会互相污染。

### 客户端还有一个很细的握手流程

客户端连接服务器时，最开始只知道服务器地址，不知道服务端世界名。
所以它先临时初始化一个目录，再向服务端请求世界信息，等收到响应后再完成最终目录初始化：

- 请求世界信息：`src/client/java/areahint/network/ClientWorldNetworking.java:39`
- 服务端响应：`src/main/java/areahint/network/ServerWorldNetworking.java:24`
- 客户端完成目录初始化：`src/client/java/areahint/world/ClientWorldFolderManager.java:71`
- 初始化完后强制重检当前区域：`src/client/java/areahint/network/ClientWorldNetworking.java:95`

这说明作者不是简单假设“连接时所有上下文都已就绪”，而是专门为**连接早期状态不完整**设计了一个补全流程。

这是非常工程化的思路。

---

## 5. 检测链路：从“玩家坐标”到“当前域名”的完整流程

整个项目最有技术含量的部分，是区域检测流程。
核心类是 `src/client/java/areahint/detection/AreaDetector.java:21`。

### 5.1 加载阶段：先把区域按等级分组

加载区域数据时，`AreaDetector.loadAreaData()` 会做三件事：

1. 从当前世界目录读取维度 JSON
2. 对每个区域调用 `computeCache()` 预计算缓存
3. 按 `level` 建立分组映射

对应代码见 `src/client/java/areahint/detection/AreaDetector.java:35`。

这意味着运行时不需要每次从零整理结构，检测阶段可以直接使用缓存和分组结果。

### 5.2 检测阶段的核心顺序

真正的检测逻辑在 `src/client/java/areahint/detection/AreaDetector.java:152`，其链路非常清晰：

```text
玩家坐标
  ↓
高度预筛选
  ↓
按 level 分组
  ↓
先找命中的一级域名
  ↓
沿 base-name 继续向下找子域名
  ↓
返回命中的最深层区域
  ↓
按显示样式格式化文本
```

#### 第一步：高度预筛选

高度筛选器在 `src/client/java/areahint/detection/AltitudeFilter.java:24`。
它先根据玩家的 Y 坐标，把不满足高度范围的区域全部剔除。

这是非常划算的一步，因为高度判断几乎没有成本，却能显著减少后续几何运算的候选集。

#### 第二步：AABB 快速排除

对候选区域，不会直接上射线法，而是先检查 `second-vertices` 对应的包围盒。
相关调用见：

- 一级区域检查：`src/client/java/areahint/detection/AreaDetector.java:186`
- 子区域检查：`src/client/java/areahint/detection/AreaDetector.java:235`

AABB 判断实现在 `src/client/java/areahint/detection/RayCasting.java:97`。

这一步的意义很直接：
如果点连包围盒都不在，就完全没必要进入更昂贵的多边形判断。

#### 第三步：射线法精确判定

多边形判定使用射线法，实现在 `src/client/java/areahint/detection/RayCasting.java:26`。

算法思想很经典：

- 从点向右发射一条水平射线
- 统计射线与多边形边的交点数
- 交点为奇数则在内部，偶数则在外部

这个项目里，射线法不是单独存在的，而是被安排在高度过滤和 AABB 之后，成为最后的精确判定器。
换句话说，它不是“唯一算法”，而是整个多层优化链路中的**最后一步**。

### 5.3 层级区域不是“取第一个命中”，而是“逐层下钻”

很多区域系统只会返回“第一个命中的区域”，但这个项目不是。

`AreaDetector.findArea()` 会先找到命中的一级域名，然后基于 `base-name` 继续检查其子域，再继续往下，直到找不到更深层命中为止，见 `src/client/java/areahint/detection/AreaDetector.java:210`。

这意味着最终结果不是“随便一个命中的多边形”，而是**命中的最深层上下文区域**。

对于像“王国 → 主城 → 商业区 → 商会大厅”这种世界结构来说，这个设计非常关键。

---

## 6. 显示逻辑：区域名不是简单字符串，而是可组合的层级文本

检测得到的不是最终展示内容。
展示前还会经过样式格式化。

对应逻辑在 `src/client/java/areahint/detection/AreaDetector.java:320`，支持三种模式：

- `full`
- `simple`
- `mixed`

### `DomainRenderer` 的作用

真正负责把层级区域拼成显示文本的是 `DomainRenderer`：

- 构建完整层级文本：`src/main/java/areahint/render/DomainRenderer.java:24`
- 构建域名链：`src/main/java/areahint/render/DomainRenderer.java:42`
- 简单模式：`src/main/java/areahint/render/DomainRenderer.java:107`

它会：

1. 从当前区域向上回溯 `base-name`
2. 构建完整链路
3. 对每个节点应用颜色
4. 用 `·` 作为层级分隔符

再叠加上 `SurfaceNameHandler` 的优先显示逻辑后，最终玩家看到的就不再只是一个机械的 ID，而是一个更符合世界观表达的**层级标题**。

---

## 7. 性能优化：把昂贵计算放到更少的时候做

如果这个模组只有几十个区域，也许怎么写都能跑。
但一旦区域数量变多、检测频率提高，性能问题就会很快出现。

这个项目的优化思路非常明确：

### 7.1 先过滤，再精算

检测链本身就是优化：

```text
高度过滤 → AABB → 射线法
```

这让“真正进入射线法”的区域数量尽量少。

### 7.2 使用缓存而不是重复计算

`AreaData` 在加载后预计算 AABB 与中心点，见 `src/main/java/areahint/data/AreaData.java:149`。
检测时直接使用 `isPointInCachedAABB()` 与 `distanceSqToCenter()`，见：

- `src/main/java/areahint/data/AreaData.java:172`
- `src/main/java/areahint/data/AreaData.java:176`

### 7.3 候选区域按距离排序

在同一层级内，`AreaDetector` 会先按区域中心与玩家的距离排序，再进行几何判断，见 `src/client/java/areahint/detection/AreaDetector.java:272`。

这虽然不是严格意义上的空间索引，但在典型地图布局下，往往能更快命中最可能的区域。

### 7.4 检测频率可配置

`shouldDetect()` 会根据配置控制检测间隔，见 `src/client/java/areahint/detection/AreaDetector.java:120`。
这给了使用者一个在“实时性”和“性能”之间自己平衡的旋钮。

### 7.5 异步检测：主线程只消费结果

更进一步，项目还做了 `AsyncAreaDetector`，位于 `src/client/java/areahint/detection/AsyncAreaDetector.java:14`。
它的策略非常实用：

- 使用单线程执行器，避免并发乱序：`src/client/java/areahint/detection/AsyncAreaDetector.java:28`
- 玩家移动距离小于阈值时直接跳过：`src/client/java/areahint/detection/AsyncAreaDetector.java:39`
- 用 `detecting` 防止重复提交：`src/client/java/areahint/detection/AsyncAreaDetector.java:47`
- 主线程通过 `pollResult()` 消费最新结果：`src/client/java/areahint/detection/AsyncAreaDetector.java:70`

而客户端主循环正是这样配合它的：

- 提交检测任务：`src/client/java/areahint/AreashintClient.java:471`
- 主线程消费结果：`src/client/java/areahint/AreashintClient.java:477`

这就把“高频检测”从“主线程持续重算”变成了“后台算、前台收结果”。
对 Fabric 客户端模组来说，这是很典型也很正确的优化方向。

---

## 8. 渲染系统：统一入口，多后端实现

显示标题的入口被统一封装在 `RenderManager`：`src/client/java/areahint/render/RenderManager.java:11`。

它内部管理三种渲染实现：

- CPU
- OpenGL
- Vulkan

并通过统一接口 `IRender` 暴露给上层，见 `src/client/java/areahint/render/RenderManager.java:84`。

这带来的好处很直接：

- 上层逻辑完全不用关心底层怎么画
- 只需要调用 `showAreaTitle()` 即可，见 `src/client/java/areahint/render/RenderManager.java:74`
- 配置切换渲染模式时，直接替换当前实现即可，见 `src/client/java/areahint/render/RenderManager.java:39`

### 以 `CPURender` 为例，它不是“直接 drawText 就完了”

`CPURender` 注册了：

- 客户端 tick 事件：`src/client/java/areahint/render/CPURender.java:75`
- HUD 渲染事件：`src/client/java/areahint/render/CPURender.java:79`

并使用 `IN / STAY / OUT` 三段动画状态机管理标题显示，见：

- HUD 渲染逻辑：`src/client/java/areahint/render/CPURender.java:87`
- 动画状态更新：`src/client/java/areahint/render/CPURender.java:204`

也就是说，标题显示不是一次性的“出现就结束”，而是：

1. 渐入
2. 停留
3. 渐出

再叠加颜色文本与闪烁色支持，整体体验会比单纯的聊天栏提示更接近真正的“区域标题系统”。

---

## 9. 交互式编辑：不用传统 GUI，也能做出完整流程

项目里另一个很有意思的部分，是编辑功能的实现方式。

它没有强依赖复杂 GUI 窗口，而是把很多流程做成了：

- 聊天输入
- 可点击文本按钮
- 命令回写
- 统一记录键
- 少量辅助可视化

### EasyAdd 是一个标准状态机

`EasyAddManager` 几乎就是一个完整的流程引擎：

- 状态定义：`src/client/java/areahint/easyadd/EasyAddManager.java:26`
- 启动流程：`src/client/java/areahint/easyadd/EasyAddManager.java:75`
- 聊天输入处理：`src/client/java/areahint/easyadd/EasyAddManager.java:112`
- 高度与颜色继续流程：`src/client/java/areahint/easyadd/EasyAddManager.java:331`
- 最终确认保存：`src/client/java/areahint/easyadd/EasyAddManager.java:494`

其状态流大致如下：

```text
输入名称
  → 输入联合域名
  → 选择等级
  → 选择上级域名
  → 记录顶点
  → 选择高度
  → 选择颜色
  → 确认保存
```

这其实就是把“交互式区域创建”做成了一个轻量状态机，而不是把所有逻辑硬塞进某个命令处理函数里。

### 统一按键分发避免冲突

项目里存在 EasyAdd、ExpandArea、ShrinkArea、DivideArea、AddHint 等多个需要记录坐标的功能。
如果每个功能自己注册一个相同按键，很容易冲突。

所以作者做了一个 `UnifiedKeyHandler`：

- 注册统一记录键：`src/client/java/areahint/keyhandler/UnifiedKeyHandler.java:31`
- 按当前活跃模块分发按键事件：`src/client/java/areahint/keyhandler/UnifiedKeyHandler.java:87`

这实际上是在做一个“小型输入路由器”。

### 临时顶点可视化提升编辑体验

`BoundVizManager` 除了显示现有区域边界，还支持临时顶点显示：

- 临时顶点字段：`src/client/java/areahint/boundviz/BoundVizManager.java:25`
- 设置临时顶点：`src/client/java/areahint/boundviz/BoundVizManager.java:137`
- 清理临时顶点：`src/client/java/areahint/boundviz/BoundVizManager.java:146`

这让“记录点位”不再只是盲操作，而是能结合视觉反馈完成编辑。

---

## 10. 网络同步：服务端不是只发 JSON，还在驱动客户端交互

这个项目的网络层不只是“把数据发过去”。
它其实承担了两类不同职责：

1. **数据同步**
2. **交互编排**

### 数据同步

服务端网络入口在 `src/main/java/areahint/network/ServerNetworking.java:35`。

玩家加入时，服务端会：

- 发送所有维度的区域数据：`src/main/java/areahint/network/ServerNetworking.java:43`
- 发送维度域名配置：`src/main/java/areahint/network/ServerNetworking.java:51`

区域数据变化后，还会进行全服同步：

- 向所有客户端发送所有维度数据：`src/main/java/areahint/network/ServerNetworking.java:135`

这意味着客户端虽然负责检测，但它依赖的区域数据仍然由服务端统一下发。

### 交互编排

更有意思的是 `S2C_CLIENT_COMMAND` 这条通道。
服务端会发送字符串命令给客户端，见 `src/main/java/areahint/network/ServerNetworking.java:159`；
客户端收到后解析 `type` 和 `action`，再路由到 EasyAdd、ExpandArea、Rename、Delete 等不同模块，见 `src/client/java/areahint/network/ClientNetworking.java:166`。

这相当于实现了一个轻量的“**客户端命令总线**”。

它的好处是：

- 服务端只需要决定“接下来客户端该进入哪个交互动作”
- 客户端只需要根据字符串路由到对应 manager
- 不必为每一个微交互都单独设计一套复杂包结构

对于命令驱动型 Minecraft 模组来说，这是一种很实用的折中方案。

---

## 11. 权限系统：把 LuckPerms 结果和原有规则统一起来

权限处理是这个项目近期比较成熟的一块设计。

### 命令注册层直接走统一权限入口

`ServerCommands` 中大量子命令都通过 `.requires(...)` 接入 `PermissionService`：

- `src/main/java/areahint/command/ServerCommands.java:70`
- `src/main/java/areahint/command/ServerCommands.java:75`
- `src/main/java/areahint/command/ServerCommands.java:122`
- `src/main/java/areahint/command/ServerCommands.java:177`
- `src/main/java/areahint/command/ServerCommands.java:315`

这说明权限判断不是散在各处，而是已经有意识地往统一服务收口。

### `PermissionService` 的关键思想：优先节点结果，未定义时回退原规则

`PermissionService` 位于 `src/main/java/areahint/permission/PermissionService.java:11`。

它核心做的事情非常清楚：

1. 先调用 `LuckPermsCompat.checkPermission()`
2. 如果得到 `TRUE`，直接放行
3. 如果得到 `FALSE`，直接拒绝
4. 如果得到 `UNDEFINED`，回退到原有规则（例如 OP、创建者等）

对应代码在：

- 命令权限入口：`src/main/java/areahint/permission/PermissionService.java:15`
- 节点或业务规则二选一：`src/main/java/areahint/permission/PermissionService.java:29`
- 最终解析逻辑：`src/main/java/areahint/permission/PermissionService.java:43`

这套设计的好处很大：

- 装了 LuckPerms，可以吃节点系统
- 没装 LuckPerms，不影响原行为
- 节点没定义，也不会误伤已有权限逻辑

### `LuckPermsCompat` 本身也是软依赖写法

`LuckPermsCompat` 位于 `src/main/java/areahint/permission/LuckPermsCompat.java:13`。

它通过以下策略避免强绑定：

- 先判断模组是否安装：`src/main/java/areahint/permission/LuckPermsCompat.java:80`
- 使用反射获取 API：`src/main/java/areahint/permission/LuckPermsCompat.java:89`
- 检查权限时返回 `TRUE / FALSE / UNDEFINED` 三态：`src/main/java/areahint/permission/LuckPermsCompat.java:38`

再结合 `build.gradle` 中的 `compileOnly` 依赖配置（`build.gradle:36-45`），整个权限兼容层就成了一个很稳的软依赖模块。

---

## 12. 可选生态扩展：BlueMap 的接入方式同样很“克制”

BlueMap 集成也没有直接把 API 写死在入口里。

### 构建层面：只做可选编译依赖

`build.gradle:36-45` 中，LuckPerms 和 BlueMap 都是 `compileOnly`。
这意味着它们不会被强行打进最终产物。

### 运行时：通过桥接接口 + 反射装配

BlueMap 兼容入口在 `src/main/java/areahint/map/BlueMapCompat.java:11`。
其做法是：

1. 先判断 BlueMap 是否安装：`src/main/java/areahint/map/BlueMapCompat.java:29`
2. 通过反射加载实现类：`src/main/java/areahint/map/BlueMapCompat.java:35`
3. 只和 `BlueMapBridge` 接口交互：`src/main/java/areahint/map/BlueMapBridge.java:8`

当区域数据发生变化时，还会触发：

- 维度级同步：`src/main/java/areahint/map/BlueMapCompat.java:96`
- 全量同步：`src/main/java/areahint/map/BlueMapCompat.java:108`

这种写法的价值在于：

- 未安装 BlueMap 时不会触发类加载错误
- 集成逻辑与主流程隔离得很干净
- 模组本体能保持独立运行

这是典型的“**软依赖优先**”工程思路，非常适合 Minecraft 模组生态。

---

## 13. 一些很值得借鉴的工程点

如果把这份代码当作一个案例来看，我觉得最值得借鉴的不是某个单独类，而是以下几条整体策略。

### 13.1 高频逻辑放客户端，裁决逻辑放服务端

这是整个项目最稳的前提。
它避免了“服务端持续帮每个玩家算位置”的高负载，也避免了“客户端私自改真数据”的一致性问题。

### 13.2 几何检测不是靠一个算法取胜，而是靠链路设计取胜

真正高效的不是射线法本身，而是：

```text
高度过滤 → AABB → 射线法 → 层级下钻
```

每一步都在减少下一步的工作量。

### 13.3 数据对象不只是存数据，也要承载热点缓存

把 AABB 和中心点缓存在 `AreaData` 内，是非常务实的做法。
这种优化既不复杂，也能立即减少重复计算。

### 13.4 交互流程用状态机，比堆命令分支更清晰

EasyAdd 这类功能如果全塞进命令处理器，很快会变得难维护。
单独做成状态机后，流程就有了清晰的阶段边界。

### 13.5 软依赖一定要有“未安装也能正常跑”的回退路径

LuckPerms 和 BlueMap 的兼容写法都说明了一点：
对于模组生态，**兼容能力不是“把 API 接进来”这么简单，而是“在缺失时也保持稳定”**。

---

## 结语

Areas Hint 最有意思的地方，在于它把一个看起来很轻的功能，做成了一套结构完整、边界清晰、具备扩展能力的系统。

它不是单纯地“进入区域就显示文字”，而是围绕这个目标完成了：

- 区域数据建模
- 层级域名组织
- 高性能空间检测
- 多后端渲染
- 交互式编辑
- 服务端同步与权限裁决
- 多世界数据隔离
- 可选生态兼容

从模组工程的角度看，这种实现方式非常值得借鉴：
**把用户感受到的“简单体验”，建立在内部清晰的职责拆分和可维护结构之上。**

如果之后继续扩展，这套架构也已经为更多能力留出了空间——例如更强的地图集成、更完整的编辑可视化、更精细的权限节点，以及更进一步的区域索引优化。

但就当前代码而言，它已经很好地展示了一个优秀 Minecraft 功能模组该有的样子：
**目标明确，数据外置，性能有意识，扩展有边界。**

---

## 参考材料与源码定位

### 项目文档

- `README.md`
- `CLAUDE.md`
- `COMMAND_USAGE.md`

### 构建与环境

- `build.gradle:36`
- `gradle.properties:7`

### 核心入口

- `src/main/java/areahint/Areashint.java:47`
- `src/client/java/areahint/AreashintClient.java:55`

### 数据与文件系统

- `src/main/java/areahint/data/AreaData.java:11`
- `src/main/java/areahint/file/FileManager.java:35`
- `src/main/java/areahint/world/WorldFolderManager.java:31`
- `src/client/java/areahint/world/ClientWorldFolderManager.java:41`

### 检测与渲染

- `src/client/java/areahint/detection/AreaDetector.java:35`
- `src/client/java/areahint/detection/AltitudeFilter.java:24`
- `src/client/java/areahint/detection/RayCasting.java:26`
- `src/client/java/areahint/detection/AsyncAreaDetector.java:14`
- `src/client/java/areahint/render/RenderManager.java:11`
- `src/client/java/areahint/render/CPURender.java:28`
- `src/main/java/areahint/render/DomainRenderer.java:24`

### 交互、网络与权限

- `src/client/java/areahint/easyadd/EasyAddManager.java:21`
- `src/client/java/areahint/keyhandler/UnifiedKeyHandler.java:20`
- `src/client/java/areahint/network/ClientNetworking.java:166`
- `src/main/java/areahint/network/ServerNetworking.java:35`
- `src/main/java/areahint/network/ServerWorldNetworking.java:24`
- `src/client/java/areahint/network/ClientWorldNetworking.java:17`
- `src/main/java/areahint/permission/PermissionService.java:11`
- `src/main/java/areahint/permission/LuckPermsCompat.java:13`
- `src/main/java/areahint/map/BlueMapCompat.java:11`
- `src/main/java/areahint/map/BlueMapBridge.java:8`
