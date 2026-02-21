#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Improved Minecraft Mod Language File Translator
Uses batch translation with better error handling
"""

import json
import re
import time
from pathlib import Path

# Manual translation dictionary for common terms
MANUAL_TRANSLATIONS = {
    # Core game terms
    "域名": "area",
    "顶点": "vertex",
    "顶级域名": "level 1 area",
    "一级域名": "level 1 area",
    "二级域名": "level 2 area",
    "三级域名": "level 3 area",
    "次级域名": "sub-area",
    "上级域名": "parent area",
    "联合域名": "surface name",
    "表面域名": "surface name",
    "维度域名": "dimension area",
    "维度": "dimension",
    "高度": "altitude",
    "坐标": "coordinate",
    "边界": "boundary",
    "创建者": "creator",
    "签名": "signature",
    "等级": "level",
    "颜色": "color",

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

    # UI elements
    "字幕": "subtitle",
    "样式": "style",
    "大小": "size",
    "按钮": "button",
    "按键": "key",
    "列表": "list",
    "界面": "interface",
    "菜单": "menu",

    # System terms
    "渲染": "render",
    "检测": "detection",
    "频率": "frequency",
    "调试": "debug",
    "权限": "permission",
    "管理员": "administrator",
    "玩家": "player",
    "服务端": "server",
    "客户端": "client",
    "世界": "world",
    "文件夹": "folder",
    "文件": "file",
    "配置": "configuration",
    "数据": "data",
    "日志": "log",
    "模组": "mod",
    "命令": "command",
    "指令": "command",

    # Status terms
    "错误": "error",
    "失败": "failed",
    "成功": "success",
    "有效": "valid",
    "无效": "invalid",
    "正确": "correct",
    "错误": "incorrect",

    # Descriptors
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

    # Quantities
    "最大": "maximum",
    "最小": "minimum",
    "最高": "highest",
    "最低": "lowest",
    "范围": "range",
    "数量": "count",
    "总计": "total",
    "至少": "at least",
    "最多": "at most",
    "所有": "all",
    "任意": "any",
    "无": "none",
    "空": "empty",

    # Common phrases
    "未知": "unknown",
    "未命名": "unnamed",
    "未找到": "not found",
    "不存在": "does not exist",
    "已存在": "already exists",
    "重复": "duplicate",
    "不能": "cannot",
    "必须": "must",
    "需要": "need",
    "要求": "require",
    "允许": "allow",
    "可以": "can",
    "应该": "should",
    "正在": "is",
    "已": "has",
    "将": "will",

    # Connectors
    "或": "or",
    "和": "and",
    "与": "with",
    "从": "from",
    "到": "to",
    "在": "in",
    "为": "for",
    "对": "to",
    "由": "by",

    # Questions/Responses
    "是": "yes",
    "否": "no",
    "您": "you",
    "你": "you",
    "请": "please",

    # Particles (often removed in English)
    "的": "",
    "了": "",
    "个": "",
    "吗": "?",
    "呢": "?",
}

# Phrase translations (longer phrases take precedence)
PHRASE_TRANSLATIONS = {
    "区域提示": "Areas Hint",
    "区域提示模组": "Areas Hint Mod",
    "域名数据": "area data",
    "顶点坐标": "vertex coordinates",
    "高度范围": "altitude range",
    "上级域名": "parent area",
    "联合域名": "surface name",
    "维度域名": "dimension area",
    "创建者": "creator",
    "等级": "level",
    "颜色": "color",
    "世界文件夹": "world folder",
    "配置文件": "configuration file",
    "数据文件": "data file",
    "权限不足": "insufficient permission",
    "没有权限": "no permission",
    "不存在": "does not exist",
    "已存在": "already exists",
    "未找到": "not found",
    "无效的": "invalid",
    "有效的": "valid",
    "成功": "successful",
    "失败": "failed",
    "错误": "error",
    "警告": "warning",
    "提示": "hint",
    "注意": "note",
    "信息": "info",
    "消息": "message",
    "请求": "request",
    "响应": "response",
    "处理": "process",
    "发送": "send",
    "接收": "receive",
    "更新": "update",
    "设置": "set",
    "获取": "get",
    "查找": "find",
    "检查": "check",
    "验证": "validate",
    "计算": "calculate",
    "显示": "display",
    "隐藏": "hide",
    "进入": "enter",
    "离开": "leave",
    "切换": "switch",
    "变化": "change",
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
}


def translate_text(text):
    """Translate Chinese text to English using manual dictionary"""
    if not text or not isinstance(text, str):
        return text

    # Check if already in English
    if not any('\u4e00' <= char <= '\u9fff' for char in text):
        return text

    result = text

    # Apply phrase translations first (longer matches first)
    for cn_phrase in sorted(PHRASE_TRANSLATIONS.keys(), key=len, reverse=True):
        if cn_phrase in result:
            result = result.replace(cn_phrase, PHRASE_TRANSLATIONS[cn_phrase])

    # Apply word translations
    for cn_word in sorted(MANUAL_TRANSLATIONS.keys(), key=len, reverse=True):
        if cn_word in result:
            result = result.replace(cn_word, MANUAL_TRANSLATIONS[cn_word])

    # Clean up extra spaces
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

    print(f"Translating {total} entries...")

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

    print(f"✓ Translation complete! Saved to {output_file}")
    print(f"✓ Translated {total} entries")


if __name__ == "__main__":
    main()
