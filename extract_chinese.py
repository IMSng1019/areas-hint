#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
提取Java代码中的硬编码中文字符串
生成zh_cn.json和详细记录表
"""

import re
import json
import os
from collections import defaultdict
from pathlib import Path

# 存储提取的字符串
extracted_strings = []
translation_dict = {}

def extract_chinese_from_file(file_path):
    """从单个文件中提取中文字符串"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        for line_num, line in enumerate(lines, 1):
            # 匹配字符串字面量中的中文
            # 支持: "...", Text.of("..."), Text.literal("..."), sendMessage(Text.of("..."))
            patterns = [
                r'"([^"]*[\u4e00-\u9fa5][^"]*)"',  # 基本字符串
                r'Text\.of\("([^"]*[\u4e00-\u9fa5][^"]*)"',  # Text.of
                r'Text\.literal\("([^"]*[\u4e00-\u9fa5][^"]*)"',  # Text.literal
            ]

            for pattern in patterns:
                matches = re.finditer(pattern, line)
                for match in matches:
                    chinese_text = match.group(1) if '(' in pattern else match.group(1)

                    # 跳过纯代码的行(如contains, startsWith等)
                    if any(keyword in line for keyword in ['contains(', 'startsWith(', 'endsWith(', 'equals(']):
                        if not any(c in chinese_text for c in '§【】《》""'''):
                            continue

                    # 记录提取信息
                    extracted_strings.append({
                        'file': str(file_path),
                        'line': line_num,
                        'text': chinese_text,
                        'context': line.strip()
                    })

    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")

def categorize_and_generate_keys(strings):
    """分类字符串并生成键名"""
    categories = defaultdict(list)

    for item in strings:
        text = item['text']
        file_path = item['file']

        # 根据文件路径和内容分类
        if 'command' in file_path.lower():
            if '===' in text or '帮助' in text:
                category = 'command.help'
            elif '成功' in text or '已' in text:
                category = 'command.success'
            elif '错误' in text or '失败' in text or '无效' in text:
                category = 'command.error'
            else:
                category = 'command.message'
        elif 'UI' in file_path or 'ui' in file_path.lower():
            if '[' in text and ']' in text:
                category = 'gui.button'
            elif '请' in text or '选择' in text:
                category = 'gui.prompt'
            else:
                category = 'gui.message'
        elif 'easyadd' in file_path.lower():
            if '请' in text:
                category = 'easyadd.prompt'
            elif '已' in text or '成功' in text:
                category = 'easyadd.success'
            elif '错误' in text or '失败' in text:
                category = 'easyadd.error'
            else:
                category = 'easyadd.message'
        elif 'error' in text.lower() or '错误' in text or '失败' in text:
            category = 'error'
        elif 'debug' in file_path.lower() or 'LOGGER' in item['context']:
            category = 'debug'
        else:
            category = 'message'

        categories[category].append(item)

    return categories

def generate_translation_key(text, category, index):
    """生成翻译键名"""
    # 移除颜色代码
    clean_text = re.sub(r'§[0-9a-fk-or]', '', text)
    clean_text = clean_text.strip()

    # 提取关键词
    keywords = []
    if '域名' in clean_text:
        keywords.append('area')
    if '顶点' in clean_text:
        keywords.append('vertex')
    if '高度' in clean_text:
        keywords.append('altitude')
    if '颜色' in clean_text:
        keywords.append('color')
    if '成功' in clean_text:
        keywords.append('success')
    if '失败' in clean_text or '错误' in clean_text:
        keywords.append('error')
    if '请' in clean_text or '选择' in clean_text:
        keywords.append('prompt')
    if '取消' in clean_text:
        keywords.append('cancel')
    if '确认' in clean_text or '保存' in clean_text:
        keywords.append('confirm')

    # 生成键名
    if keywords:
        key = f"{category}.{'.'.join(keywords)}_{index}"
    else:
        key = f"{category}.message_{index}"

    return key.replace('..', '.')

def main():
    # 扫描所有Java文件
    src_dir = Path('src')
    java_files = list(src_dir.rglob('*.java'))

    print(f"找到 {len(java_files)} 个Java文件")

    for java_file in java_files:
        extract_chinese_from_file(java_file)

    print(f"提取到 {len(extracted_strings)} 个中文字符串")

    # 去重
    unique_strings = {}
    for item in extracted_strings:
        text = item['text']
        if text not in unique_strings:
            unique_strings[text] = item

    print(f"去重后剩余 {len(unique_strings)} 个唯一字符串")

    # 分类
    categories = categorize_and_generate_keys(list(unique_strings.values()))

    # 生成翻译文件
    translations = {}
    translations['language.name'] = '简体中文'
    translations['language.region'] = '中国'
    translations['language.code'] = 'zh_cn'

    # 按类别生成键值对
    for category, items in sorted(categories.items()):
        for index, item in enumerate(items, 1):
            key = generate_translation_key(item['text'], category, index)
            translations[key] = item['text']

    # 保存为JSON
    with open('zh_cn.json', 'w', encoding='utf-8') as f:
        json.dump(translations, f, ensure_ascii=False, indent=2)

    print(f"已生成 zh_cn.json，包含 {len(translations)} 个条目")

    # 生成详细记录
    with open('extraction_record.md', 'w', encoding='utf-8') as f:
        f.write('# 中文字符串提取记录\n\n')
        f.write(f'总计提取: {len(extracted_strings)} 个字符串\n')
        f.write(f'去重后: {len(unique_strings)} 个唯一字符串\n\n')

        for category, items in sorted(categories.items()):
            f.write(f'## {category}\n\n')
            f.write('| 原始文本 | 文件 | 行号 | 建议键名 |\n')
            f.write('|---------|------|------|----------|\n')

            for index, item in enumerate(items, 1):
                key = generate_translation_key(item['text'], category, index)
                file_short = item['file'].replace('src\\', '').replace('src/', '')
                f.write(f"| {item['text'][:50]} | {file_short} | {item['line']} | {key} |\n")

            f.write('\n')

    print("已生成 extraction_record.md 详细记录")

if __name__ == '__main__':
    main()
