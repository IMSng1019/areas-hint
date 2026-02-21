#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
优化版:提取Java代码中的硬编码中文字符串
生成规范的zh_cn.json和详细记录表
"""

import re
import json
import os
from collections import defaultdict
from pathlib import Path

# 存储提取的字符串
extracted_strings = []

def extract_chinese_from_file(file_path):
    """从单个文件中提取中文字符串"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            lines = content.split('\n')

        for line_num, line in enumerate(lines, 1):
            # 跳过注释行
            if line.strip().startswith('//') or line.strip().startswith('*'):
                continue

            # 匹配字符串字面量中的中文
            pattern = r'"([^"]*[\u4e00-\u9fa5][^"]*)"'
            matches = re.finditer(pattern, line)

            for match in matches:
                chinese_text = match.group(1)

                # 跳过纯代码判断的字符串
                if any(keyword in line for keyword in ['contains(', 'startswith(', 'endsWith(', 'equals(', 'getPath()']):
                    # 但如果包含Minecraft颜色代码或中文标点,则保留
                    if not ('§' in chinese_text or any(c in chinese_text for c in '【】《》""''：，。！？')):
                        continue

                # 跳过文件路径
                if '.json' in chinese_text or '.txt' in chinese_text or '/' in chinese_text:
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

def generate_key_name(text, file_path, context):
    """生成规范的翻译键名"""
    # 移除颜色代码
    clean_text = re.sub(r'§[0-9a-fk-or]', '', text).strip()

    # 第一级:模块分类
    if 'command' in file_path.lower():
        module = 'command'
    elif any(x in file_path.lower() for x in ['easyadd', 'expandarea', 'shrinkarea', 'dividearea', 'addhint', 'deletehint']):
        # 提取具体功能名
        for func in ['easyadd', 'expandarea', 'shrinkarea', 'dividearea', 'addhint', 'deletehint', 'recolor', 'rename', 'delete']:
            if func in file_path.lower():
                module = func
                break
    elif 'ui' in file_path.lower() or 'UI' in file_path:
        module = 'gui'
    elif 'debug' in file_path.lower():
        module = 'debug'
    elif 'language' in file_path.lower():
        module = 'language'
    elif 'boundviz' in file_path.lower():
        module = 'boundviz'
    elif 'replacebutton' in file_path.lower():
        module = 'replacebutton'
    else:
        module = 'message'

    # 第二级:消息类型
    if '===' in text or '帮助' in clean_text:
        msg_type = 'title'
    elif '错误' in clean_text or '失败' in clean_text or '无效' in clean_text or text.startswith('§c'):
        msg_type = 'error'
    elif '成功' in clean_text or '已' in clean_text and '完成' in clean_text:
        msg_type = 'success'
    elif '请' in clean_text or '输入' in clean_text or '选择' in clean_text:
        msg_type = 'prompt'
    elif '[' in text and ']' in text:
        msg_type = 'button'
    elif '提示' in clean_text or '说明' in clean_text:
        msg_type = 'hint'
    else:
        msg_type = 'message'

    # 第三级:具体内容关键词
    keywords = []
    keyword_map = {
        '域名': 'area',
        '顶点': 'vertex',
        '高度': 'altitude',
        '颜色': 'color',
        '维度': 'dimension',
        '坐标': 'coordinate',
        '记录': 'record',
        '保存': 'save',
        '取消': 'cancel',
        '确认': 'confirm',
        '继续': 'continue',
        '完成': 'finish',
        '删除': 'delete',
        '添加': 'add',
        '修改': 'modify',
        '扩展': 'expand',
        '收缩': 'shrink',
        '分割': 'divide',
        '重命名': 'rename',
        '联合': 'surface',
        '等级': 'level',
        '上级': 'parent',
        '权限': 'permission',
        '名称': 'name',
        '列表': 'list',
        '启动': 'start',
        '边界': 'boundary',
        '可视化': 'visualization',
        '按键': 'key',
        '语言': 'language',
    }

    for cn_word, en_word in keyword_map.items():
        if cn_word in clean_text:
            keywords.append(en_word)

    # 组合键名
    if keywords:
        # 去重并限制关键词数量
        keywords = list(dict.fromkeys(keywords))[:3]
        key = f"{module}.{msg_type}.{'.'.join(keywords)}"
    else:
        key = f"{module}.{msg_type}.general"

    return key

def main():
    # 扫描所有Java文件
    src_dir = Path('src')
    java_files = list(src_dir.rglob('*.java'))

    print(f"找到 {len(java_files)} 个Java文件")

    for java_file in java_files:
        extract_chinese_from_file(java_file)

    print(f"提取到 {len(extracted_strings)} 个中文字符串")

    # 去重(保留第一次出现的位置)
    unique_strings = {}
    for item in extracted_strings:
        text = item['text']
        if text not in unique_strings:
            unique_strings[text] = item

    print(f"去重后剩余 {len(unique_strings)} 个唯一字符串")

    # 生成翻译键值对
    translations = {}
    translations['language.name'] = '简体中文'
    translations['language.region'] = '中国'
    translations['language.code'] = 'zh_cn'

    # 按键名分组,避免重复
    key_counter = defaultdict(int)
    key_mapping = {}  # 原文 -> 键名

    for text, item in sorted(unique_strings.items()):
        base_key = generate_key_name(text, item['file'], item['context'])

        # 如果键名已存在,添加序号
        key_counter[base_key] += 1
        if key_counter[base_key] > 1:
            final_key = f"{base_key}_{key_counter[base_key]}"
        else:
            final_key = base_key

        translations[final_key] = text
        key_mapping[text] = final_key

    # 保存为JSON(按键名排序)
    sorted_translations = dict(sorted(translations.items()))
    with open('zh_cn.json', 'w', encoding='utf-8') as f:
        json.dump(sorted_translations, f, ensure_ascii=False, indent=2)

    print(f"已生成 zh_cn.json，包含 {len(translations)} 个条目")

    # 生成详细记录(Excel格式的Markdown表格)
    with open('extraction_record.md', 'w', encoding='utf-8') as f:
        f.write('# 中文字符串提取记录\n\n')
        f.write(f'- 总计提取: {len(extracted_strings)} 个字符串\n')
        f.write(f'- 去重后: {len(unique_strings)} 个唯一字符串\n')
        f.write(f'- 生成键值对: {len(translations)} 个\n\n')

        f.write('## 完整提取列表\n\n')
        f.write('| 序号 | 翻译键名 | 原始文本 | 文件路径 | 行号 | 用途说明 | 占位符 |\n')
        f.write('|------|---------|---------|---------|------|---------|--------|\n')

        for idx, (text, item) in enumerate(sorted(unique_strings.items(), key=lambda x: key_mapping[x[0]]), 1):
            key = key_mapping[text]
            file_short = item['file'].replace('src\\', '').replace('src/', '')

            # 检测占位符
            has_placeholder = 'Yes' if any(p in text for p in ['%s', '%d', '%f', '{0}', '{1}']) else 'No'

            # 用途说明
            if 'command' in file_short.lower():
                usage = '命令系统'
            elif 'ui' in file_short.lower() or 'UI' in file_short:
                usage = '用户界面'
            elif 'easyadd' in file_short.lower():
                usage = 'EasyAdd功能'
            elif 'debug' in file_short.lower():
                usage = '调试信息'
            else:
                usage = '一般消息'

            # 截断过长的文本
            display_text = text[:60] + '...' if len(text) > 60 else text
            f.write(f"| {idx} | `{key}` | {display_text} | {file_short} | {item['line']} | {usage} | {has_placeholder} |\n")

    print("已生成 extraction_record.md 详细记录")

    # 生成统计信息
    with open('extraction_stats.txt', 'w', encoding='utf-8') as f:
        f.write('=== 提取统计 ===\n\n')
        f.write(f'总字符串数: {len(extracted_strings)}\n')
        f.write(f'唯一字符串数: {len(unique_strings)}\n')
        f.write(f'生成键值对数: {len(translations)}\n\n')

        # 按模块统计
        module_stats = defaultdict(int)
        for key in translations.keys():
            if key.startswith('language.'):
                continue
            module = key.split('.')[0]
            module_stats[module] += 1

        f.write('按模块统计:\n')
        for module, count in sorted(module_stats.items(), key=lambda x: -x[1]):
            f.write(f'  {module}: {count}\n')

    print("已生成 extraction_stats.txt 统计信息")

if __name__ == '__main__':
    main()
