#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""将 zh_cn.json 转换为猫娘语 zh_cn_neko.json"""
import json, re, random

random.seed(42)  # 保证可重复

src = r"src/main/resources/assets/areas-hint/lang/zh_cn.json"
dst = r"src/main/resources/assets/areas-hint/lang/zh_cn_neko.json"

with open(src, "r", encoding="utf-8") as f:
    data = json.load(f)

# 特殊键直接覆盖
OVERRIDES = {
    "language.code": "zh_cn_neko",
    "language.name": "猫娘语",
    "language.region": "喵星球",
}

# 末尾语气词池
TAILS_SUCCESS = ["喵~", "喵！", "喵喵~", "nya~"]
TAILS_ERROR = ["喵呜...", "呜呜...", "呜~", "喵呜~"]
TAILS_NORMAL = ["喵~", "喵", "~", "nya~", "喵喵"]
TAILS_PROMPT = ["喵~", "喵：", "喵喵~"]

# 词汇替换表 (原文, 替换) - 不在替换词中加语气词，语气词统一在末尾添加
REPLACEMENTS = [
    ("操作已取消", "操作取消了"),
    ("已取消", "取消了"),
    ("请选择", "请选选"),
    ("成功！", "成功了！"),
    ("成功!", "成功了!"),
    ("已启用", "开启啦"),
    ("已禁用", "关掉啦"),
    ("已保存", "保存好啦"),
    ("已加载", "加载好啦"),
    ("已创建", "创建好啦"),
    ("已设置", "设置好啦"),
    ("已更新", "更新好啦"),
    ("已接收", "收到啦"),
    ("已发送", "发送啦"),
    ("已记录", "记下啦"),
    ("已选择", "选好啦"),
    ("已重置", "重置啦"),
    ("不存在", "找不到呢"),
    ("无法确定", "搞不清"),
    ("无法识别", "认不出"),
    ("无法获取", "拿不到"),
    ("无法", "没办法"),
    ("未找到", "找不到"),
    ("未知的", "不认识的"),
    ("未知", "不认识的"),
    ("无效的", "不对的"),
    ("无效", "不对的"),
    ("警告：", "注意喵："),
    ("警告:", "注意喵:"),
    ("确认要", "真的要"),
    ("初始化完成", "准备好啦"),
    ("初始化", "准备"),
    ("完成", "搞定"),
]

def is_debug_or_technical(key, val):
    """判断是否为DEBUG/技术性消息"""
    if key.startswith("message.message.general_") or key.startswith("message.message.finish"):
        return True
    if "DEBUG" in val or "debug" in val.lower():
        return True
    if any(x in val for x in ["CPURender", "GLRender", "VulkanRender", "AABB", "JsonHelper"]):
        return True
    return False

def transform_neko(key, val):
    """将一个值转换为猫娘语"""
    if key in OVERRIDES:
        return OVERRIDES[key]

    # 空值或纯格式/占位符
    if not val or len(val.strip()) <= 2:
        return val

    original = val

    # 对DEBUG/技术消息只加轻微猫娘化
    if is_debug_or_technical(key, val):
        return val  # 技术消息保持原样

    # 应用词汇替换
    for old, new in REPLACEMENTS:
        if old in val:
            val = val.replace(old, new, 1)
            break  # 只替换第一个匹配，避免过度替换

    # 判断消息类型来选择尾巴
    stripped = val.rstrip()

    # 不给以下情况加尾巴：
    # - 以占位符/标点/颜色代码结尾
    # - 很短的片段（<4字符）
    # - 以冒号、逗号、空格结尾（通常是拼接用的片段）
    skip_tail = False
    if len(stripped) < 4:
        skip_tail = True
    if stripped and stripped[-1] in ":：,，、 \t\\{}()（）[]§\"'0123456789":
        skip_tail = True
    if stripped.endswith("\\n"):
        skip_tail = True
    if re.search(r'[%{]\S*$', stripped):
        skip_tail = True
    # 已经有语气词结尾
    if any(stripped.endswith(x) for x in ["喵", "nya", "~", "呜", "啦", "呢", "吧", "哦", "嘛"]):
        skip_tail = True
    # 以 ] 或 === 结尾的标题/按钮不加尾巴
    if stripped.endswith("]") or stripped.endswith("==="):
        skip_tail = True

    if not skip_tail:
        # 根据键名/内容选择语气
        if ".error." in key or "§c" in val:
            tail = random.choice(TAILS_ERROR)
        elif ".success." in key or "§a" in val and ("成功" in val or "已" in val):
            tail = random.choice(TAILS_SUCCESS)
        elif ".prompt." in key:
            tail = random.choice(TAILS_PROMPT)
        else:
            tail = random.choice(TAILS_NORMAL)
        val = stripped + tail

    return val

result = {}
for key, val in data.items():
    result[key] = transform_neko(key, val)

with open(dst, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"猫娘语翻译完成！共 {len(result)} 个条目 -> {dst}")
