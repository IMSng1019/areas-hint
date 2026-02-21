#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Minecraft Mod Language File Translator
Translates zh_cn.json to en_us.json using translation services
"""

import json
import re
import time
from pathlib import Path

# Try to import translation libraries
try:
    from googletrans import Translator as GoogleTranslator
    GOOGLE_AVAILABLE = True
except ImportError:
    GOOGLE_AVAILABLE = False
    print("googletrans not available. Install with: pip install googletrans==4.0.0-rc1")

try:
    import deepl
    DEEPL_AVAILABLE = True
except ImportError:
    DEEPL_AVAILABLE = False
    print("deepl not available. Install with: pip install deepl")


class MinecraftTranslator:
    """Translator for Minecraft mod language files"""

    def __init__(self):
        self.translator = None
        self.init_translator()

        # Key terminology mapping for consistency
        self.terminology = {
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
            "维度": "dimension",
            "维度域名": "dimension area",
            "高度": "altitude",
            "坐标": "coordinate",
            "边界": "boundary",
            "创建者": "creator",
            "签名": "signature",
            "等级": "level",
            "颜色": "color",
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
            "字幕": "subtitle",
            "样式": "style",
            "大小": "size",
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
            "配置": "configuration",
            "数据": "data",
        }

    def init_translator(self):
        """Initialize translation service"""
        if GOOGLE_AVAILABLE:
            self.translator = GoogleTranslator()
            self.service = "google"
            print("Using Google Translate")
        else:
            print("No translation service available!")
            print("Please install: pip install googletrans==4.0.0-rc1")
            self.service = None

    def preserve_special_chars(self, text):
        """Extract and preserve special characters"""
        if not text or not isinstance(text, str):
            return text, []

        preserved = []

        # Preserve Minecraft color codes (§a, §c, etc.)
        color_pattern = r'§[0-9a-fk-or]'
        colors = re.findall(color_pattern, text)
        for i, color in enumerate(colors):
            placeholder = f"__COLOR{i}__"
            text = text.replace(color, placeholder, 1)
            preserved.append(('color', i, color))

        # Preserve placeholders
        placeholder_patterns = [
            (r'\{[0-9]+\}', 'brace'),  # {0}, {1}
            (r'%s', 'percent_s'),
            (r'%d', 'percent_d'),
            (r'%.1f', 'percent_f'),
            (r'\\n', 'newline'),
            (r'\\\\', 'backslash'),
        ]

        for pattern, ptype in placeholder_patterns:
            matches = re.findall(pattern, text)
            for i, match in enumerate(matches):
                placeholder = f"__{ptype.upper()}{i}__"
                text = text.replace(match, placeholder, 1)
                preserved.append((ptype, i, match))

        return text, preserved

    def restore_special_chars(self, text, preserved):
        """Restore preserved special characters"""
        if not preserved:
            return text

        for ptype, index, original in preserved:
            if ptype == 'color':
                placeholder = f"__COLOR{index}__"
            else:
                placeholder = f"__{ptype.upper()}{index}__"
            text = text.replace(placeholder, original)

        return text

    def apply_terminology(self, text):
        """Apply consistent terminology"""
        for cn, en in self.terminology.items():
            text = text.replace(cn, en)
        return text

    def translate_text(self, text):
        """Translate a single text string"""
        if not text or not isinstance(text, str):
            return text

        # Check if text is already in English or contains only special chars
        if not any('\u4e00' <= char <= '\u9fff' for char in text):
            return text

        # Preserve special characters
        processed_text, preserved = self.preserve_special_chars(text)

        # Apply terminology first
        processed_text = self.apply_terminology(processed_text)

        # Check again if translation is still needed
        if not any('\u4e00' <= char <= '\u9fff' for char in processed_text):
            return self.restore_special_chars(processed_text, preserved)

        # Translate
        try:
            if self.service == "google" and self.translator:
                result = self.translator.translate(processed_text, src='zh-cn', dest='en')
                translated = result.text
            else:
                # Fallback: return original with terminology applied
                translated = processed_text

            # Restore special characters
            translated = self.restore_special_chars(translated, preserved)

            return translated

        except Exception as e:
            print(f"Translation error for '{text[:50]}...': {e}")
            return self.restore_special_chars(processed_text, preserved)

    def translate_file(self, input_file, output_file):
        """Translate entire JSON file"""
        print(f"Loading {input_file}...")

        with open(input_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        total = len(data)
        translated_data = {}

        print(f"Translating {total} entries...")

        for i, (key, value) in enumerate(data.items(), 1):
            # Special handling for language metadata
            if key == "language.name":
                translated_data[key] = "English"
            elif key == "language.region":
                translated_data[key] = "United States"
            elif key == "language.code":
                translated_data[key] = "en_us"
            else:
                translated_data[key] = self.translate_text(value)

            # Progress indicator
            if i % 50 == 0:
                print(f"Progress: {i}/{total} ({i*100//total}%)")
                time.sleep(0.5)  # Rate limiting

        print(f"Saving to {output_file}...")
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(translated_data, f, ensure_ascii=False, indent=2)

        print(f"✓ Translation complete! Saved to {output_file}")


def main():
    """Main function"""
    translator = MinecraftTranslator()

    if translator.service is None:
        print("\n" + "="*60)
        print("ERROR: No translation service available!")
        print("="*60)
        print("\nPlease install one of the following:")
        print("  1. Google Translate (Free):")
        print("     pip install googletrans==4.0.0-rc1")
        print("\n  2. DeepL (Requires API key):")
        print("     pip install deepl")
        print("="*60)
        return

    input_file = Path("zh_cn.json")
    output_file = Path("en_us.json")

    if not input_file.exists():
        print(f"Error: {input_file} not found!")
        return

    translator.translate_file(input_file, output_file)


if __name__ == "__main__":
    main()
