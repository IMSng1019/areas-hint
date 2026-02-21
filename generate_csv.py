#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成CSV格式的提取记录
"""

import json
import csv
import re
from pathlib import Path

def main():
    # 读取zh_cn.json
    with open('zh_cn.json', 'r', encoding='utf-8') as f:
        translations = json.load(f)

    # 读取extraction_record.md获取详细信息
    with open('extraction_record.md', 'r', encoding='utf-8') as f:
        content = f.read()

    # 创建CSV文件
    with open('提取记录.csv', 'w', newline='', encoding='utf-8-sig') as f:
        writer = csv.writer(f)

        # 写入表头
        writer.writerow([
            '序号',
            '翻译键名',
            '原始文本(中文)',
            '文件路径',
            '行号',
            '用途说明',
            '是否含占位符',
            '是否含颜色代码',
            '模块分类'
        ])

        # 写入数据
        idx = 1
        for key, value in sorted(translations.items()):
            # 跳过language开头的元数据
            if key.startswith('language.'):
                continue

            # 检测占位符
            has_placeholder = 'Yes' if any(p in value for p in ['%s', '%d', '%f', '{0}', '{1}', '{2}']) else 'No'

            # 检测颜色代码
            has_color = 'Yes' if '§' in value else 'No'

            # 提取模块
            module = key.split('.')[0]

            # 提取消息类型
            parts = key.split('.')
            msg_type = parts[1] if len(parts) > 1 else 'general'

            # 用途说明
            usage_map = {
                'command': '命令系统',
                'easyadd': 'EasyAdd交互式添加',
                'expandarea': 'ExpandArea域名扩展',
                'shrinkarea': 'ShrinkArea域名收缩',
                'dividearea': 'DivideArea域名分割',
                'addhint': 'AddHint添加顶点',
                'deletehint': 'DeleteHint删除顶点',
                'recolor': 'Recolor重新着色',
                'rename': 'Rename重命名',
                'delete': 'Delete删除',
                'gui': '用户界面',
                'debug': '调试信息',
                'boundviz': '边界可视化',
                'replacebutton': '按键替换',
                'language': '语言设置',
                'message': '一般消息'
            }
            usage = usage_map.get(module, '其他')

            writer.writerow([
                idx,
                key,
                value,
                '',  # 文件路径(从md中提取比较复杂,暂时留空)
                '',  # 行号
                usage,
                has_placeholder,
                has_color,
                module
            ])

            idx += 1

    print(f"已生成 提取记录.csv，包含 {idx-1} 条记录")

if __name__ == '__main__':
    main()
