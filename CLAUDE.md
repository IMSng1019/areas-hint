# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **Areas Hint (区域提示模组)**, a Minecraft Fabric 1.20.4 mod that displays area names when players enter specific regions. The mod supports multi-level domain hierarchies, altitude filtering, and provides both server and client functionality with multiple rendering modes.

## Build System & Commands

### Essential Development Commands

```bash
# Build the mod
./gradlew build

# Clean build artifacts
./gradlew clean

# Build and publish to local Maven repository
./gradlew publishToMavenLocal

# Run data generation
./gradlew runDatagen

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer
```

### Project Configuration

- **Minecraft Version**: 1.20.4
- **Fabric Loader**: 0.16.14
- **Java Version**: 17
- **Yarn Mappings**: 1.20.4+build.3
- **Fabric API**: 0.97.2+1.20.4

## Architecture

The mod follows a **client-server split architecture**:

### Server Side (`src/main/java/areahint/`)
- **Entry Point**: `Areashint.java` - Main mod initializer
- **Command System**: `command/` - Handles all mod commands including area management and debugging
- **Data Models**: `data/` - Area data, config data, and dimensional name structures
- **File Management**: `file/` - JSON serialization, file I/O, and config management
- **Network Layer**: `network/` - Server-side networking for data synchronization
- **World Management**: `world/` - Per-world folder management for multi-server support

### Client Side (`src/client/java/areahint/`)
- **Entry Point**: `AreashintClient.java` - Client mod initializer with tick-based position detection
- **Area Detection**: `detection/` - Ray casting algorithm, AABB collision, and altitude filtering
- **Rendering System**: `render/` - Multiple rendering backends (CPU, OpenGL, Vulkan)
- **EasyAdd System**: `easyadd/` - Interactive area creation with GUI and key bindings
- **Configuration**: `config/` - Client-side config management

## Key Systems

### Area Detection Algorithm
- **Primary Method**: Ray casting (射线法) for polygon containment
- **Optimization**: Altitude pre-filtering reduces computation overhead
- **AABB Pre-check**: Bounding box filtering before ray casting
- **Detection Frequency**: Configurable tick-based checking

### World Folder Management
Each server/world gets its own folder structure:
```
.minecraft/areas-hint/
├── config.json (global)
├── 192.168.1.100_MyServer/
│   ├── overworld.json
│   ├── the_nether.json
│   └── dimensional_names.json
└── localhost_SinglePlayer/
    └── overworld.json
```

### EasyAdd Interactive System
- **Key Binding**: Default 'T' key for coordinate recording
- **UI Flow**: Name → Level → Parent Selection → Coordinate Recording → Height Settings
- **Network Sync**: Real-time synchronization with server
- **Validation**: Automatic parent-child containment validation

### Command System
Core commands (all prefixed with `/areahint`):
- `reload` - Reload all configuration and area data
- `add <JSON>` - Add new area (admin only)
- `easyadd` - Start interactive area creation
- `delete [name]` - List or delete areas with permission checking
- `debug [on|off|status]` - Toggle debugging with colored output
- `dimensionalityname` - Manage dimension display names

## File Formats

### Area Data JSON Structure
```json
{
  "name": "区域名称",
  "vertices": [{"x": 0, "z": 10}, {"x": 10, "z": 0}],
  "second-vertices": [{"x": -10, "z": 10}, {"x": 10, "z": 10}],
  "altitude": {"max": 100, "min": 0},
  "level": 1,
  "base-name": null,
  "signature": "player_name"
}
```

### Configuration Structure
```json
{
  "Frequency": 1,
  "SubtitleRender": "OpenGL",
  "SubtitleStyle": "mixed"
}
```

## Development Workflow

### Adding New Features
1. Determine if feature is client-side, server-side, or both
2. For area detection: modify `detection/` package
3. For rendering: extend `render/RenderManager`
4. For commands: add to `command/ServerCommands`
5. For networking: update both client and server networking classes

### Debugging
- Use `/areahint debug on` for real-time debugging
- Debug categories: area detection (cyan), player position (green), config (yellow), network (purple), rendering (blue)
- Check logs in both client and server contexts

### Testing Areas
1. Use `/areahint easyadd` for quick area creation
2. Test with different altitude ranges
3. Verify parent-child relationships work correctly
4. Test across dimension changes and server reconnections

## Important Implementation Details

### Networking
- **Bidirectional**: Client receives area data, sends EasyAdd creations
- **World-Aware**: Automatic synchronization based on current world context
- **Packets**: Defined in `network/Packets.java` with proper channel identifiers

### Performance Considerations
- **Altitude Pre-filtering**: Dramatically reduces ray casting operations
- **Configurable Frequency**: Detection rate adjustable based on server load
- **AABB Optimization**: Quick rejection of obviously outside areas

### Multi-World Support
- **Automatic Folder Creation**: World folders created on first connection
- **Server Detection**: IP-based identification for proper folder selection
- **State Management**: Clean state transitions between worlds/servers

This mod emphasizes user-friendly area creation while maintaining high performance through optimized detection algorithms and proper client-server architecture.
- 我的话会涉及到一些概念主要是在域名文件json格式当中：完整格式为{"name": "这里是区域名称（我将其定义为域名 这个定义上下文通用）", "vertices": [这是多边形的一个点（一级顶点）,{"x":横坐标,"z":纵坐标},{"x":横坐标,"z":纵坐标},{"x":横坐标,"z":纵坐标}],"second-vertices":[{"x":横向最小坐标值,"y":纵向最大坐标值},{"x":横向最大坐标值,"y":纵向最大坐标值},{"x":横向最大坐标值,"y":纵向最小坐标值},{"x":横向最小坐标值,"y":纵向最小坐标值}],"altitude": {"max":最大的高度,"min":最小的高度"},"level": 指域名等级（ 必须为整数 数字越大域名等级越小 1为顶级域名 2为二级域名 3为三级域名 2和3为次级域名 注意 1对于域名等级并不是最大值 每个维度有维度域名 维度域名等级为0.5 ） ,"base-name":"这里是该域名所指向的是上一级的域名（指向的域名的域名等级必须等于该域名等级-1,null就是无上级域名）,"signature":null(的是该域名的创建者 null指无创建者) ,"color":"这里是域名颜色用的是十六进制表示法","surfacename":"这里指联合域名（或者叫表面域名）"} 每个世界都有一个世界文件夹 second-vertices为二级顶点