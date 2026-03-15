# Area Hints 域名模组

这是一个Minecraft Fabric 1.20.4版本的区域提示模组。模组可以在玩家进入特定区域时显示由玩家自定义的区域名称。它通过智能区域检测和优雅的视觉提示，让玩家沉浸在精心构建的游戏世界中，提供类RPG的区域提示体验。

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
│   │   │       ├── addhint/         # AddHint服务端支持
│   │   │       │   └── AddHintServerNetworking.java # 服务端网络处理器
│   │   │       ├── command/         # 命令处理
│   │   │       │   ├── CheckCommand.java   # 联合域名查看指令处理器
│   │   │       │   ├── DebugCommand.java   # 调试命令处理器
│   │   │       │   ├── DimensionalNameCommands.java # 维度域名命令处理器
│   │   │       │   ├── RecolorCommand.java # 域名重新着色指令处理器
│   │   │       │   ├── RenameAreaCommand.java # 域名重命名指令处理器
│   │   │       │   ├── ServerCommands.java # 统一命令处理器（服务端+客户端命令）
│   │   │       │   └── SetHighCommand.java # 域名高度设置指令处理器
│   │   │       ├── data/            # 数据模型
│   │   │       │   ├── AreaData.java       # 区域数据模型（含altitude高度字段和color颜色字段）
│   │   │       │   ├── ConfigData.java     # 配置数据模型
│   │   │       │   └── DimensionalNameData.java # 维度域名数据模型
│   │   │       ├── debug/           # 调试功能
│   │   │       │   └── DebugManager.java   # 服务端调试管理器
│   │   │       ├── deletehint/      # DeleteHint服务端支持
│   │   │       │   └── DeleteHintServerNetworking.java # 服务端网络处理器
│   │   │       ├── dimensional/     # 维度域名管理
│   │   │       │   └── DimensionalNameManager.java # 服务端维度域名管理器
│   │   │       ├── dividearea/      # DivideArea域名分割支持
│   │   │       │   └── DivideAreaServerNetworking.java # 服务端网络处理器
│   │   │       ├── easyadd/         # EasyAdd服务端支持
│   │   │       │   └── EasyAddServerNetworking.java # 服务端网络处理器
│   │   │       ├── expandarea/      # ExpandArea域名扩展支持
│   │   │       │   └── ExpandAreaServerNetworking.java # 服务端网络处理器
│   │   │       ├── file/            # 文件操作
│   │   │       │   ├── FileManager.java    # 文件管理器（读写配置和区域数据）
│   │   │       │   └── JsonHelper.java     # JSON序列化工具（支持altitude）
│   │   │       ├── i18n/            # 国际化支持
│   │   │       │   └── ServerI18nManager.java # 服务端国际化管理器
│   │   │       ├── log/             # 日志系统
│   │   │       │   ├── AsyncLogManager.java # 异步日志管理器
│   │   │       │   ├── ServerLogManager.java # 服务端日志管理器
│   │   │       │   └── ServerLogNetworking.java # 服务端日志网络处理
│   │   │       ├── mixin/           # Mixin注入
│   │   │       │   └── ExampleMixin.java   # 服务端Mixin示例
│   │   │       ├── network/         # 网络通信
│   │   │       │   ├── DimensionalNameNetworking.java # 维度域名网络传输
│   │   │       │   ├── Packets.java        # 网络包定义和通道标识符
│   │   │       │   ├── ServerNetworking.java # 服务端网络处理
│   │   │       │   ├── ServerWorldNetworking.java # 服务端世界网络处理
│   │   │       │   └── TranslatableMessage.java # 可翻译消息类
│   │   │       ├── render/          # 渲染工具
│   │   │       │   └── DomainRenderer.java # 域名渲染工具类（处理颜色显示和层级关系）
│   │   │       ├── shrinkarea/      # ShrinkArea域名收缩支持
│   │   │       │   └── ShrinkAreaServerNetworking.java # 服务端网络处理器
│   │   │       ├── util/            # 工具类
│   │   │       │   ├── AreaDataConverter.java # AreaData与JsonObject转换工具
│   │   │       │   ├── ColorUtil.java      # 颜色工具类（颜色验证、转换和预定义颜色）
│   │   │       │   └── SurfaceNameHandler.java # 联合域名处理器（显示逻辑和重复检查）
│   │   │       └── world/           # 世界文件夹管理
│   │   │           └── WorldFolderManager.java # 服务端世界文件夹管理器
│   │   └── resources/               # 服务端资源
│   │       ├── areas-hint.mixins.json # 服务端Mixin配置
│   │       ├── assets/
│   │       │   └── areas-hint/
│   │       │       ├── icon.png     # 模组图标
│   │       │       └── lang/        # 语言文件
│   │       │           ├── de_de.json      # 德语翻译
│   │       │           ├── en_pt.json      # 海盗英语翻译
│   │       │           ├── en_us.json      # 英语翻译
│   │       │           ├── es_es.json      # 西班牙语翻译
│   │       │           ├── fr_fr.json      # 法语翻译
│   │       │           ├── ja_jp.json      # 日语翻译
│   │       │           ├── ko_kr.json      # 韩语翻译
│   │       │           ├── ru_ru.json      # 俄语翻译
│   │       │           ├── zh_cn.json      # 简体中文翻译
│   │       │           ├── zh_cn_neko.json # 简体中文猫娘翻译
│   │       │           └── zh_tw.json      # 繁体中文翻译
│   │       └── fabric.mod.json      # Fabric模组配置文件
│   └── client/                      # 客户端代码
│       ├── java/
│       │   └── areahint/
│       │       ├── AreashintClient.java     # 客户端主类
│       │       ├── AreashintDataGenerator.java # 数据生成器
│       │       ├── addhint/         # AddHint客户端支持
│       │       │   ├── AddHintClientNetworking.java # 客户端网络处理器
│       │       │   └── AddHintManager.java # AddHint管理器
│       │       ├── boundviz/        # 边界可视化系统
│       │       │   ├── BoundVizManager.java # 边界可视化管理器
│       │       │   └── BoundVizRenderer.java # 边界可视化渲染器
│       │       ├── command/         # 客户端命令
│       │       │   ├── ClientCommands.java # 客户端命令处理（已弃用）
│       │       │   ├── ModToggleCommand.java # 模组开关命令处理器
│       │       │   └── SetHighClientCommand.java # 域名高度设置客户端命令
│       │       ├── config/          # 配置管理
│       │       │   └── ClientConfig.java   # 客户端配置处理
│       │       ├── debug/           # 调试功能
│       │       │   ├── ClientDebugManager.java # 客户端调试管理器
│       │       │   └── DebugManager.java        # 调试管理器（空文件）
│       │       ├── delete/          # 域名删除系统
│       │       │   ├── DeleteManager.java  # 删除管理器
│       │       │   ├── DeleteNetworking.java # 删除网络处理
│       │       │   └── DeleteUI.java       # 删除用户界面
│       │       ├── deletehint/      # DeleteHint客户端支持
│       │       │   ├── DeleteHintClientNetworking.java # 客户端网络处理器
│       │       │   └── DeleteHintManager.java # DeleteHint管理器
│       │       ├── detection/       # 区域检测（核心功能）
│       │       │   ├── AltitudeFilter.java # 高度预筛选器
│       │       │   ├── AreaDetector.java   # 区域检测器（集成高度预筛选）
│       │       │   ├── AsyncAreaDetector.java # 异步区域检测器
│       │       │   └── RayCasting.java     # 射线检测和AABB算法
│       │       ├── dimensional/     # 维度域名管理
│       │       │   ├── ClientDimensionalNameManager.java # 客户端维度域名管理器
│       │       │   ├── DimensionalNameUI.java # 维度域名用户界面
│       │       │   └── DimensionalNameUIManager.java # 维度域名UI管理器
│       │       ├── dividearea/      # DivideArea域名分割系统
│       │       │   ├── DivideAreaClientNetworking.java # 客户端网络处理
│       │       │   ├── DivideAreaManager.java # 域名分割管理器
│       │       │   └── DivideAreaUI.java   # 域名分割用户界面
│       │       ├── easyadd/         # EasyAdd交互式域名添加系统
│       │       │   ├── EasyAddAltitudeManager.java # 高度设置管理器
│       │       │   ├── EasyAddConfig.java     # 配置管理器
│       │       │   ├── EasyAddGeometry.java   # 几何计算工具
│       │       │   ├── EasyAddKeyHandler.java # 按键监听处理器
│       │       │   ├── EasyAddManager.java    # EasyAdd核心管理器
│       │       │   ├── EasyAddNetworking.java # 客户端网络通信
│       │       │   └── EasyAddUI.java         # 用户界面系统
│       │       ├── expandarea/      # ExpandArea域名扩展系统
│       │       │   ├── ExpandAreaClientNetworking.java # 客户端网络通信
│       │       │   ├── ExpandAreaKeyHandler.java # 按键监听处理器（X键记录、Enter确认）
│       │       │   ├── ExpandAreaManager.java   # 域名扩展核心管理器
│       │       │   ├── ExpandAreaUI.java        # 用户界面系统（域名选择、记录提示）
│       │       │   └── GeometryCalculator.java  # 复杂几何计算工具（边界点、交叉点算法）
│       │       ├── i18n/            # 国际化支持
│       │       │   └── I18nManager.java    # 国际化管理器
│       │       ├── keyhandler/      # 按键处理
│       │       │   └── UnifiedKeyHandler.java # 统一按键处理器
│       │       ├── language/        # 语言管理
│       │       │   ├── LanguageManager.java # 语言管理器
│       │       │   └── LanguageUI.java      # 语言选择用户界面
│       │       ├── log/             # 日志系统
│       │       │   ├── AreaChangeTracker.java # 区域变更追踪器
│       │       │   ├── ClientLogManager.java # 客户端日志管理器
│       │       │   └── ClientLogNetworking.java # 客户端日志网络处理
│       │       ├── mixin/           # 客户端Mixin
│       │       │   └── client/
│       │       │       ├── ExampleClientMixin.java # 客户端Mixin示例
│       │       │       ├── TranslationStorageMixin.java # 翻译存储Mixin
│       │       │       └── WorldRendererMixin.java # 世界渲染器Mixin
│       │       ├── network/         # 网络通信
│       │       │   ├── ClientDimensionalNameNetworking.java # 客户端维度域名网络处理
│       │       │   ├── ClientDimNameNetworking.java # 客户端维度名称网络处理
│       │       │   ├── ClientNetworking.java # 客户端网络处理
│       │       │   └── ClientWorldNetworking.java # 客户端世界网络处理
│       │       ├── recolor/         # 域名重新着色系统
│       │       │   ├── RecolorClientCommand.java # 重新着色客户端命令
│       │       │   ├── RecolorManager.java # 重新着色管理器
│       │       │   └── RecolorUI.java      # 重新着色用户界面
│       │       ├── rename/          # 域名重命名系统
│       │       │   ├── RenameManager.java  # 重命名管理器
│       │       │   ├── RenameNetworking.java # 重命名网络处理
│       │       │   └── RenameUI.java       # 重命名用户界面
│       │       ├── render/          # 渲染系统
│       │       │   ├── CPURender.java      # CPU渲染实现
│       │       │   ├── FlashColorHelper.java # 闪烁颜色辅助类
│       │       │   ├── GLRender.java       # OpenGL渲染实现
│       │       │   ├── RenderManager.java  # 渲染管理器
│       │       │   └── VulkanRender.java   # Vulkan渲染实现（模拟）
│       │       ├── replacebutton/   # 替换按钮系统
│       │       │   ├── ReplaceButtonKeyListener.java # 替换按钮按键监听器
│       │       │   ├── ReplaceButtonManager.java # 替换按钮管理器
│       │       │   ├── ReplaceButtonNetworking.java # 替换按钮网络处理
│       │       │   └── ReplaceButtonUI.java # 替换按钮用户界面
│       │       ├── shrinkarea/      # ShrinkArea域名收缩系统
│       │       │   ├── ShrinkAreaClientNetworking.java # 客户端网络通信
│       │       │   ├── ShrinkAreaKeyHandler.java # 按键监听处理器（X键记录）
│       │       │   ├── ShrinkAreaManager.java   # 域名收缩核心管理器
│       │       │   ├── ShrinkAreaUI.java        # 用户界面系统（域名选择、记录提示）
│       │       │   └── ShrinkGeometryCalculator.java # 收缩几何计算工具（边界点、交叉点算法）
│       │       ├── subtitlesize/    # 字幕大小系统
│       │       │   ├── SubtitleSizeManager.java # 字幕大小管理器
│       │       │   └── SubtitleSizeUI.java # 字幕大小用户界面
│       │       ├── subtitlestyle/   # 字幕样式系统
│       │       │   ├── SubtitleStyleManager.java # 字幕样式管理器
│       │       │   └── SubtitleStyleUI.java # 字幕样式用户界面
│       │       └── world/           # 世界文件夹管理
│       │           └── ClientWorldFolderManager.java # 客户端世界文件夹管理器
│       └── resources/               # 客户端资源
│           └── areas-hint.client.mixins.json # 客户端Mixin配置
├── build.gradle                     # Gradle构建脚本
├── gradle.properties                # Gradle配置属性
├── gradlew                          # Unix/Linux Gradle包装器脚本
├── gradlew.bat                      # Windows Gradle包装器脚本
├── settings.gradle                  # Gradle设置文件
├── LICENSE                          # MIT许可证文件
├── README.md                        # 项目说明文档
├── .gitignore                       # Git忽略文件配置
├── .gitattributes                   # Git属性配置
├── .github/                         # GitHub工作流和配置文件目录
│   └── workflows/
│       └── build.yml                # GitHub Actions构建工作流
├── prompt.txt                       # 原始需求文档（包含模组基本功能需求）
├── prompt_implementation.txt        # 实现方案文档（详细的功能实现说明）
├── second-prompt.txt                # 第二阶段功能需求文档（高级功能需求）
├── thrid-promrt.txt                 # 第三阶段功能需求文档
├── CLAUDE.md                        # Claude AI开发指导文档（项目架构、构建命令、开发工作流说明）
├── COMMAND_USAGE.md                 # 命令使用文档
├── LOG_FEATURE.md                   # 日志功能文档
├── RECOLOR_REFACTOR_SUMMARY.md      # 重新着色重构总结
├── REPLACEBUTTON_EXPANDAREA_SHRINKAREA_INTEGRATION.md # 替换按钮、扩展区域、收缩区域集成文档
├── REPLACEBUTTON_FIX_SUMMARY.md     # 替换按钮修复总结
├── REPLACEBUTTON_IMPLEMENTATION_SUMMARY.md # 替换按钮实现总结
├── REPLACEBUTTON_TEST_GUIDE.md      # 替换按钮测试指南
├── SHRINKAREA_CHANGES.md            # 收缩区域变更文档
├── SHRINKAREA_REFACTOR.md           # 收缩区域重构文档
├── SUBTITLESIZE_GUIDE.md            # 字幕大小指南
├── SUBTITLESTYLE_INTERACTIVE_GUIDE.md # 字幕样式交互指南
├── TRANSLATION_GUIDE.md             # 翻译指南
├── TRANSLATION_REPORT.md            # 翻译报告
├── extraction_record.md             # 提取记录
├── 提取完成报告.md                  # 提取完成报告（中文）
├── overworld.json                   # 主世界区域数据示例
├── en_us.json                       # 英语翻译文件（根目录）
├── zh_cn.json                       # 简体中文翻译文件（根目录）
├── areas-hint-template-1.20.4.zip   # 模组压缩包模板
├── modicon.psd                      # 模组图标源文件（Photoshop格式）
├── extract_chinese.py               # 中文提取脚本
├── extract_chinese_v2.py            # 中文提取脚本v2
├── fix_spacing.py                   # 修复间距脚本
├── fix_translation.py               # 修复翻译脚本
├── generate_csv.py                  # 生成CSV脚本
├── translate_complete.py            # 完整翻译脚本
├── translate_full.py                # 完整翻译脚本
├── translate_lolcat.py              # Lolcat翻译脚本
├── translate_manual.py              # 手动翻译脚本
├── translate_neko.py                # 猫娘翻译脚本
├── translate_script.py              # 翻译脚本
├── translate_zh_tw.py               # 繁体中文翻译脚本
├── build_log.txt                    # 构建日志
├── chinese_strings_raw.txt          # 原始中文字符串
├── extraction_stats.txt             # 提取统计
├── FINAL_REPORT.txt                 # 最终报告
├── 提取记录.csv                     # 提取记录（CSV格式）
├── .idea/                           # IntelliJ IDEA项目配置目录
├── .vscode/                         # Visual Studio Code配置目录
├── build/                           # 构建输出目录（自动生成）
│   ├── libs/
│   │   ├── areas-hint-1.0.0.jar       # 主模组JAR文件
│   │   └── areas-hint-1.0.0-sources.jar # 源代码JAR文件
│   └── ...                             # 其他构建文件
├── bin/                             # 编译输出目录（自动生成）
├── run/                             # 运行时目录（自动生成）
│   ├── config/                      # 运行时配置
│   ├── logs/                        # 运行时日志
│   ├── mods/                        # 运行时模组
│   ├── options.txt                  # 游戏选项
│   └── ...                          # 其他运行时文件
└── .gradle/                         # Gradle缓存目录（自动生成)

.minecraft/areas-hint/              # 运行时配置和数据目录
├── config.json                     # 模组配置文件
├── dimensional_names.json          # 维度域名配置文件
├── overworld.json                  # 主世界区域数据
├── the_nether.json                 # 地狱区域数据
└── the_end.json                    # 末地区域数据
```

## 新增文件功能说明

### 开发指导文档

#### `CLAUDE.md`
- **功能**：为Claude AI提供项目开发指导
- **内容**：包含项目概述、构建系统命令、架构说明、关键系统介绍、开发工作流等
- **用途**：帮助开发者快速理解项目结构和实现细节

#### `prompt.txt`
- **功能**：原始需求文档
- **内容**：包含模组基本功能需求，如区域检测、渲染系统、命令系统等
- **用途**：记录项目初始需求，作为开发基础

#### `prompt_implementation.txt`
- **功能**：实现方案文档
- **内容**：详细的功能实现说明，包括代码结构、文件用途、实现原理等
- **用途**：指导具体代码实现，确保功能完整性

#### `second-prompt.txt`
- **功能**：第二阶段功能需求文档
- **内容**：包含高级功能需求，如高度系统、域名签名、EasyAdd交互系统、维度域名等
- **用途**：指导后续功能开发和扩展

### 项目配置文件

#### `modicon.psd`
- **功能**：模组图标源文件
- **格式**：Photoshop PSD格式
- **用途**：模组图标的原始设计文件，可导出为不同格式

#### `.idea/` 和 `.vscode/`
- **功能**：IDE配置文件目录
- **内容**：IntelliJ IDEA和Visual Studio Code的项目配置
- **用途**：确保开发环境的一致性和项目配置的完整性

#### `.github/`
- **功能**：GitHub工作流和配置文件目录
- **内容**：CI/CD配置、Issue模板、Pull Request模板等
- **用途**：自动化构建、测试和部署流程

### 构建和运行时目录

#### `build/`
- **功能**：Gradle构建输出目录
- **内容**：编译后的JAR文件、类文件、资源文件等
- **用途**：存放最终的可执行模组文件

#### `bin/`
- **功能**：编译输出目录
- **内容**：Java编译后的.class文件
- **用途**：开发过程中的临时编译结果

#### `run/`
- **功能**：运行时目录
- **内容**：Minecraft运行时的临时文件和配置
- **用途**：开发和测试时的运行环境

#### `.gradle/`
- **功能**：Gradle缓存目录
- **内容**：依赖下载、构建缓存等
- **用途**：加速构建过程，避免重复下载依赖

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

### 核心算法： 射线法判断点是否在多边形内

射线法(Ray Casting)通过从一个点向右发射一条射线，计算与多边形边界的交点数：
- 奇数个交点：点在多边形内
- 偶数个交点：点在多边形外

### 渲染模式

1. CPU渲染：所有渲染操作使用软件渲染，不使用GPU
2. OpenGL渲染：使用OpenGL API进行渲染（默认）
3. Vulkan渲染：使用Vulkan API进行渲染（并没有实现）

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

### 基础命令

- `/areahint help` - 显示所有命令及其用法
- `/areahint reload` - 重新加载配置和域名文件

### 域名查看与管理

- `/areahint check` - 显示所有联合域名列表（权限等级：0，普通玩家可用）
- `/areahint delete` - 交互式域名删除（列出可删除的域名）

### 域名创建与编辑

- `/areahint add <JSON>` - 添加新的域名（需要管理员权限，已经被easyadd指令代替）
- `/areahint easyadd` - 启动交互式域名添加（普通玩家可用）
- `/areahint rename` - 启动交互式域名重命名

### 域名几何编辑

- `/areahint expandarea` - 启动交互式域名扩展
- `/areahint shrinkarea` - 启动交互式域名收缩
- `/areahint dividearea` - 启动交互式域名分割

### 域名顶点编辑

- `/areahint addhint` - 启动交互式顶点添加
- `/areahint deletehint` - 启动交互式顶点删除

### 域名样式与显示

- `/areahint recolor` - 重新为域名着色
- `/areahint sethigh` - 重新为域名设置高度 

### 配置与显示设置

- `/areahint frequency` - 显示当前检测频率
- `/areahint frequency <值>` - 设置检测频率（1-60）
- `/areahint subtitlerender` - 显示当前渲染模式
- `/areahint subtitlerender <cpu|opengl|vulkan>` - 设置渲染模式
- `/areahint subtitlestyle` - 启动交互式字幕样式选
- `/areahint subtitlesize` - 启动交互式字幕大小选择

### 维度域名管理

- `/areahint dimensionalityname` - 启动交互式维度域名管理
- `/areahint dimensionalitycolor` - 启动交互式维度域名颜色管理
- `/areahint firstdimname <名称>` - 首次进入维度时设置维度域名
- `/areahint firstdimname_skip` - 跳过首次维度域名设置

### 其他功能

- `/areahint replacebutton` - 启动交互式按键替换
- `/areahint replacebutton confirm` - 确认按键替换
- `/areahint replacebutton cancel` - 取消按键替换6
- `/areahint boundviz` - 切换边界可视化显示
- `/areahint language` - 启动交互式语言选择
- `/areahint debug` - 切换调试模式（需要管理员权限）
- `/areahint debug on|off|status` - 启用/禁用/查看调试模式状态（需要管理员权限）
- `/areahint serverlanguage <语言代码>` - 设置服务端日志语言（权限等级4，仅控制台可用）

## 模组兼容度

| 模组名称 | 兼容性 |
|---------|--------|
| Starlight | ✓ |
| Stutterfix | ✓ |
| Syncmatica | ✓ |
| ThreadTweak | ✓ |
| Travelers Titles | ✓ |
| TweakMore | ✓ |
| Tweakeroo | ✓ |
| ViaFabricPlus | ✓ |
| Xaeros Minimap | ✓ |
| Xaeros World Map | ✓ |
| Yungs Api | ✓ |
| Gugle的Carpet附加包 | ✓ |
| Fabric Carpet | ✓ |
| Replay Mod | ✓ |
| MiniHUD | ✓ |
| MagicLib | ✓ |
| Sodium | ✓ |
| AppleSkin | ✓ |
| Litematica | ✓ |
| Jade | ✓ |
| AutoModpack | ✓ |
| Baritone | ✓ |
| Bobby | ✓ |
| Cloth Config | ✓ |
| Continuity | ✓ |
| Cull Leaves | ✓ |
| Enhanced Block Entities | ✓ |
| Entity Model Features | ✓ |
| Entity Texture Features | ✓ |
| Entity Culling | ✓ |
| Exordium | ✓ |
| Fabric API | ✓ |
| Fast Chest | ✓ |
| FPS Reducer | ✓ |
| ImmediatelyFast | ✓ |
| Indium | ✓ |
| Iris | ✓ |
| JEI | ✓ |
| Krypton | ✓ |
| LAN Server Properties | ✓ |
| LazyDFU | ✓ |
| MaliLib | ✓ |
| Masa Gadget | ✓ |
| ModernFix | ✓ |
| Mod Menu | ✓ |
| More Culling | ✓ |
| Noisium | ✓ |
| Nvidium | ✓ |
| Sodium Extra | ✓ |

####area hints 重新定义了Minecraft区域提示体验，将技术性能与视觉美学完美融合，为玩家和服务器主提供前所未有的区域管理解决方案。无论是大型RPG服务器还是单人冒险世界，都能通过这款模组提升沉浸感和游戏体验。