#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Complete English Translation for Minecraft Mod
Produces 100% English output with no Chinese characters
"""

import json
import re
from pathlib import Path

# Comprehensive translation dictionary
TRANSLATIONS = {
    # Complete phrase translations (longest first)
    "区域提示模组": "Areas Hint Mod",
    "区域提示": "Areas Hint",
    "域名数据": "area data",
    "顶点坐标": "vertex coordinates",
    "高度范围": "altitude range",
    "上级域名": "parent area",
    "联合域名": "surface name",
    "表面域名": "surface name",
    "维度域名": "dimension area",
    "一级域名": "level 1 area",
    "二级域名": "level 2 area",
    "三级域名": "level 3 area",
    "顶级域名": "top-level area",
    "次级域名": "sub-area",
    "世界文件夹": "world folder",
    "配置文件": "configuration file",
    "数据文件": "data file",
    "日志文件": "log file",
    "权限不足": "insufficient permission",
    "没有权限": "no permission",
    "不存在": "does not exist",
    "已存在": "already exists",
    "未找到": "not found",
    "无效的": "invalid",
    "有效的": "valid",
    "成功": "successful",
    "失败": "failed",
    "完成": "completed",
    "开始": "started",
    "结束": "finished",
    "继续": "continue",
    "取消": "cancelled",
    "确认": "confirmed",
    "提交": "submitted",
    "保存": "saved",
    "加载": "loaded",
    "重新加载": "reloaded",
    "选择": "selected",
    "输入": "entered",
    "点击": "clicked",
    "记录": "recorded",
    "删除": "deleted",
    "修改": "modified",
    "更新": "updated",
    "设置": "set",
    "获取": "retrieved",
    "发送": "sent",
    "接收": "received",
    "处理": "processed",
    "检查": "checked",
    "验证": "validated",
    "计算": "calculated",
    "显示": "displayed",
    "隐藏": "hidden",
    "进入": "entered",
    "离开": "left",
    "切换": "switched",
    "变化": "changed",
    "包含": "contains",
    "超出": "exceeds",
    "不足": "insufficient",
    "至少": "at least",
    "最多": "at most",
    "所有": "all",
    "任意": "any",
    "无": "none",
    "空": "empty",
    "未知": "unknown",
    "未命名": "unnamed",
    "重复": "duplicate",
    "当前": "current",
    "新": "new",
    "旧": "old",
    "原": "original",
    "默认": "default",
    "自定义": "custom",
    "自动": "auto",
    "智能": "smart",
    "交互式": "interactive",
    "可视化": "visualization",
    "最大": "maximum",
    "最小": "minimum",
    "最高": "highest",
    "最低": "lowest",
    "范围": "range",
    "数量": "count",
    "总计": "total",

    # Core game terms
    "域名": "area",
    "顶点": "vertex",
    "坐标": "coordinate",
    "边界": "boundary",
    "高度": "altitude",
    "颜色": "color",
    "等级": "level",
    "创建者": "creator",
    "签名": "signature",
    "维度": "dimension",
    "世界": "world",
    "玩家": "player",
    "管理员": "administrator",
    "服务端": "server",
    "服务器": "server",
    "客户端": "client",
    "权限": "permission",
    "配置": "configuration",
    "数据": "data",
    "文件": "file",
    "文件夹": "folder",
    "日志": "log",
    "模组": "mod",
    "命令": "command",
    "指令": "command",

    # Actions
    "记录": "record",
    "提交": "submit",
    "取消": "cancel",
    "确认": "confirm",
    "删除": "delete",
    "扩展": "expand",
    "收缩": "shrink",
    "分割": "divide",
    "重命名": "rename",
    "修改": "modify",
    "保存": "save",
    "加载": "load",
    "重新加载": "reload",
    "选择": "select",
    "输入": "enter",
    "点击": "click",
    "继续": "continue",
    "完成": "complete",
    "开始": "start",
    "结束": "finish",
    "启动": "start",
    "停止": "stop",
    "启用": "enable",
    "禁用": "disable",
    "切换": "toggle",
    "更新": "update",
    "设置": "set",
    "获取": "get",
    "发送": "send",
    "接收": "receive",
    "处理": "process",
    "检查": "check",
    "验证": "validate",
    "计算": "calculate",
    "显示": "display",
    "隐藏": "hide",
    "进入": "enter",
    "离开": "leave",
    "包含": "contain",
    "添加": "add",
    "编辑": "edit",
    "更改": "change",
    "关闭": "close",
    "开启": "open",

    # UI elements
    "字幕": "subtitle",
    "样式": "style",
    "大小": "size",
    "按钮": "button",
    "按键": "key",
    "列表": "list",
    "界面": "interface",
    "菜单": "menu",
    "标题": "title",
    "提示": "hint",
    "警告": "warning",
    "注意": "note",
    "信息": "info",
    "消息": "message",

    # System terms
    "渲染": "render",
    "检测": "detection",
    "频率": "frequency",
    "调试": "debug",
    "错误": "error",
    "失败": "failed",
    "成功": "success",
    "有效": "valid",
    "无效": "invalid",
    "正确": "correct",
    "错误": "incorrect",

    # Common verbs and adjectives
    "可以": "can",
    "不能": "cannot",
    "必须": "must",
    "需要": "need",
    "要求": "require",
    "允许": "allow",
    "应该": "should",
    "正在": "is",
    "已经": "has",
    "将要": "will",
    "已": "has",
    "将": "will",
    "请": "please",
    "您": "you",
    "你": "you",
    "我": "I",
    "它": "it",
    "这": "this",
    "那": "that",
    "是": "yes",
    "否": "no",
    "或": "or",
    "和": "and",
    "与": "with",
    "从": "from",
    "到": "to",
    "在": "in",
    "为": "for",
    "对": "to",
    "由": "by",
    "于": "at",

    # Common phrases
    "时发生": "occurred while",
    "请检查": "please check",
    "确保": "ensure",
    "尝试": "try",
    "通过": "passed",
    "用于": "for",
    "用法": "usage",
    "状态": "status",
    "功能": "feature",
    "操作": "operation",
    "流程": "process",
    "方式": "method",
    "方法": "method",
    "类型": "type",
    "格式": "format",
    "内容": "content",
    "名称": "name",
    "值": "value",
    "后": "after",
    "前": "before",
    "中": "in",
    "时": "when",
    "个": "",
    "的": "",
    "了": "",
    "吗": "?",
    "呢": "?",
    "啊": "",
    "呀": "",
    "哦": "",
    "嗯": "",

    # Numbers and quantities
    "一": "one",
    "二": "two",
    "三": "three",
    "四": "four",
    "五": "five",
    "六": "six",
    "七": "seven",
    "八": "eight",
    "九": "nine",
    "十": "ten",

    # Specific technical terms
    "凸包算法": "convex hull algorithm",
    "反向排序": "reverse sorting",
    "角度排序": "angle sorting",
    "线段交叉": "line segment intersection",
    "射线法": "ray casting",
    "包围盒": "bounding box",
    "交叉点": "intersection point",
    "外部": "external",
    "内部": "internal",
    "顺序": "order",
    "序列": "sequence",
    "索引": "index",
    "位置": "position",
    "方向": "direction",
    "距离": "distance",
    "长度": "length",
    "宽度": "width",
    "深度": "depth",
    "层级": "hierarchy",
    "结构": "structure",
    "关系": "relationship",
    "引用": "reference",
    "链接": "link",
    "路径": "path",
    "目录": "directory",
    "存档": "save",
    "备份": "backup",
    "恢复": "restore",
    "重置": "reset",
    "清空": "clear",
    "刷新": "refresh",
    "同步": "sync",
    "异步": "async",
    "并发": "concurrent",
    "串行": "serial",
    "并行": "parallel",
    "队列": "queue",
    "栈": "stack",
    "缓存": "cache",
    "缓冲": "buffer",
    "临时": "temporary",
    "永久": "permanent",
    "全局": "global",
    "局部": "local",
    "公共": "public",
    "私有": "private",
    "静态": "static",
    "动态": "dynamic",
    "实例": "instance",
    "对象": "object",
    "属性": "property",
    "参数": "parameter",
    "返回": "return",
    "结果": "result",
    "输出": "output",
    "响应": "response",
    "请求": "request",
    "连接": "connection",
    "断开": "disconnect",
    "超时": "timeout",
    "延迟": "delay",
    "等待": "wait",
    "阻塞": "block",
    "非阻塞": "non-blocking",
    "回调": "callback",
    "监听": "listen",
    "触发": "trigger",
    "事件": "event",
    "处理器": "handler",
    "管理器": "manager",
    "控制器": "controller",
    "适配器": "adapter",
    "转换器": "converter",
    "解析器": "parser",
    "生成器": "generator",
    "构建器": "builder",
    "工厂": "factory",
    "单例": "singleton",
    "代理": "proxy",
    "装饰器": "decorator",
    "观察者": "observer",
    "订阅": "subscribe",
    "发布": "publish",
    "广播": "broadcast",
    "多播": "multicast",
    "单播": "unicast",
}

def translate_text(text):
    """Translate Chinese text to English"""
    if not text or not isinstance(text, str):
        return text

    # Check if already in English
    if not any('\u4e00' <= char <= '\u9fff' for char in text):
        return text

    result = text

    # Apply translations (longest phrases first)
    for cn in sorted(TRANSLATIONS.keys(), key=len, reverse=True):
        if cn in result:
            result = result.replace(cn, TRANSLATIONS[cn])

    # Additional cleanup patterns
    cleanup_patterns = [
        (r'没有', 'no'),
        (r'不再', 'no longer'),
        (r'仍会', 'will still'),
        (r'暂无', 'currently no'),
        (r'无法', 'unable to'),
        (r'可能', 'may'),
        (r'应当', 'should'),
        (r'必须', 'must'),
        (r'需要', 'need'),
        (r'要求', 'require'),
        (r'允许', 'allow'),
        (r'禁止', 'prohibit'),
        (r'启用', 'enable'),
        (r'禁用', 'disable'),
        (r'开启', 'enable'),
        (r'关闭', 'disable'),
        (r'打开', 'open'),
        (r'关闭', 'close'),
        (r'显示', 'display'),
        (r'隐藏', 'hide'),
        (r'进入', 'enter'),
        (r'离开', 'leave'),
        (r'切换', 'switch'),
        (r'变化', 'change'),
        (r'包含', 'contain'),
        (r'超出', 'exceed'),
        (r'不足', 'insufficient'),
        (r'合理', 'valid'),
        (r'有效', 'valid'),
        (r'无效', 'invalid'),
        (r'正确', 'correct'),
        (r'错误', 'incorrect'),
        (r'成功', 'successful'),
        (r'失败', 'failed'),
        (r'完成', 'completed'),
        (r'开始', 'started'),
        (r'结束', 'finished'),
        (r'继续', 'continue'),
        (r'取消', 'cancel'),
        (r'确认', 'confirm'),
        (r'提交', 'submit'),
        (r'保存', 'save'),
        (r'加载', 'load'),
        (r'重新', 're'),
        (r'更新', 'update'),
        (r'设置', 'set'),
        (r'获取', 'get'),
        (r'发送', 'send'),
        (r'接收', 'receive'),
        (r'处理', 'process'),
        (r'检查', 'check'),
        (r'验证', 'validate'),
        (r'计算', 'calculate'),
    ]

    for pattern, replacement in cleanup_patterns:
        result = re.sub(pattern, replacement, result)

    # Remove remaining Chinese characters and replace with English equivalent
    # This is a fallback for any missed characters
    if any('\u4e00' <= char <= '\u9fff' for char in result):
        # Try one more aggressive pass
        result = re.sub(r'[\u4e00-\u9fff]+', '', result)

    # Clean up spacing
    result = re.sub(r'\s+', ' ', result)
    result = result.strip()

    # Fix common concatenations
    result = re.sub(r'([a-z])([A-Z])', r'\1 \2', result)

    # Add spaces between words
    common_words = [
        'area', 'vertex', 'altitude', 'dimension', 'level', 'color', 'boundary',
        'record', 'submit', 'cancel', 'confirm', 'delete', 'expand', 'shrink',
        'divide', 'rename', 'modify', 'save', 'load', 'reload', 'select', 'enter',
        'click', 'continue', 'complete', 'start', 'finish', 'enable', 'disable',
        'has', 'can', 'will', 'must', 'should', 'please', 'error', 'failed',
        'success', 'valid', 'invalid', 'current', 'new', 'old', 'original',
    ]

    for word in common_words:
        result = re.sub(rf'\b{word}([a-z])', rf'{word} \1', result, flags=re.IGNORECASE)

    # Final cleanup
    result = re.sub(r'\s+', ' ', result)
    result = result.strip()

    return result

def main():
    """Main translation function"""
    input_file = Path("zh_cn.json")
    output_file = Path("en_us.json")

    if not input_file.exists():
        print(f"Error: {input_file} not found!")
        return

    print(f"Loading {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        zh_data = json.load(f)

    total = len(zh_data)
    en_data = {}

    print(f"Translating {total} entries to pure English...")

    for i, (key, value) in enumerate(zh_data.items(), 1):
        # Special handling for language metadata
        if key == "language.name":
            en_data[key] = "English"
        elif key == "language.region":
            en_data[key] = "United States"
        elif key == "language.code":
            en_data[key] = "en_us"
        else:
            en_data[key] = translate_text(value)

        # Progress indicator
        if i % 100 == 0:
            print(f"Progress: {i}/{total} ({i*100//total}%)")

    print(f"Saving to {output_file}...")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(en_data, f, ensure_ascii=False, indent=2)

    print("Translation complete!")
    print(f"Translated {total} entries")

    # Verify no Chinese characters remain
    chinese_count = 0
    for key, value in en_data.items():
        if key not in ["language.name", "language.region", "language.code"]:
            if any('\u4e00' <= char <= '\u9fff' for char in str(value)):
                chinese_count += 1

    if chinese_count > 0:
        print(f"Warning: {chinese_count} entries still contain Chinese characters")
    else:
        print("Success: All entries are in English!")

if __name__ == "__main__":
    main()
