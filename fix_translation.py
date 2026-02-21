#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Post-process translation to fix spacing and improve quality
"""

import json
import re
from pathlib import Path


def fix_spacing(text):
    """Fix spacing issues in translated text"""
    if not text or not isinstance(text, str):
        return text

    # Add spaces between concatenated English words
    # Match lowercase followed by uppercase
    text = re.sub(r'([a-z])([A-Z])', r'\1 \2', text)

    # Add space after common words that are stuck together
    common_words = [
        'area', 'vertex', 'altitude', 'dimension', 'level', 'color', 'boundary',
        'record', 'submit', 'cancel', 'confirm', 'delete', 'expand', 'shrink',
        'divide', 'rename', 'modify', 'save', 'load', 'reload', 'select', 'enter',
        'click', 'continue', 'complete', 'start', 'finish', 'enable', 'disable',
        'toggle', 'update', 'set', 'get', 'send', 'receive', 'process', 'check',
        'validate', 'calculate', 'display', 'hide', 'contain', 'subtitle', 'style',
        'size', 'button', 'key', 'list', 'interface', 'menu', 'render', 'detection',
        'frequency', 'debug', 'permission', 'administrator', 'player', 'server',
        'client', 'world', 'folder', 'file', 'configuration', 'data', 'log', 'mod',
        'command', 'error', 'failed', 'success', 'valid', 'invalid', 'correct',
        'incorrect', 'current', 'new', 'old', 'original', 'default', 'custom',
        'auto', 'smart', 'interactive', 'visualization', 'maximum', 'minimum',
        'highest', 'lowest', 'range', 'count', 'total', 'unknown', 'unnamed',
        'duplicate', 'cannot', 'must', 'need', 'require', 'allow', 'should',
        'has', 'will', 'please', 'none', 'empty', 'exceeds', 'insufficient',
    ]

    for word in common_words:
        # Add space after word if followed by another word
        text = re.sub(rf'\b{word}([a-z])', rf'{word} \1', text, flags=re.IGNORECASE)

    # Fix common patterns
    replacements = {
        'hascancel': 'has cancelled',
        'hassubmit': 'has submitted',
        'hasrecord': 'has recorded',
        'hasselect': 'has selected',
        'hasenable': 'has been enabled',
        'hasdisable': 'has been disabled',
        'hasload': 'has loaded',
        'hassave': 'has saved',
        'hasupdate': 'has updated',
        'hasset': 'has set',
        'hasget': 'has retrieved',
        'hassend': 'has sent',
        'hasreceive': 'has received',
        'hasprocess': 'has processed',
        'hascheck': 'has checked',
        'hasvalidate': 'has validated',
        'hascalculate': 'has calculated',
        'hasdisplay': 'has displayed',
        'hashide': 'has hidden',
        'hascontain': 'contains',
        'hasstart': 'has started',
        'hasfinish': 'has finished',
        'hascomplete': 'has completed',
        'hasmodify': 'has modified',
        'hasdelete': 'has deleted',
        'hasrename': 'has renamed',
        'hasexpand': 'has expanded',
        'hasshrink': 'has shrunk',
        'hasdivide': 'has divided',

        'canmodify': 'can modify',
        'candelete': 'can delete',
        'canexpand': 'can expand',
        'canshrink': 'can shrink',
        'candivide': 'can divide',
        'canrename': 'can rename',
        'canedit': 'can edit',
        'canset': 'can set',

        'willnot': 'will not',
        'cannot': 'cannot',
        'mustnot': 'must not',
        'shouldnot': 'should not',

        'nopermission': 'no permission',
        'nodata': 'no data',
        'nofile': 'no file',
        'noarea': 'no area',
        'novertex': 'no vertex',

        'at least': 'at least',
        'at most': 'at most',

        'none效': 'invalid',
        '没有': 'no',
        '不再': 'no longer',
        '仍会': 'will still',
        '时发生': 'occurred while',
        '请check': 'please check',
        '暂none': 'currently no',
        '可以': 'can',
        '不能': 'cannot',
        '必须': 'must',
        '需要': 'need',
        '要求': 'require',
        '允许': 'allow',
        '应该': 'should',
        '正在': 'is',
        '已经': 'has',
        '将要': 'will',
        '确保': 'ensure',
        '尝试': 'try',
        '通过': 'passed',
        '失败': 'failed',
        '成功': 'successful',
        '完成': 'completed',
        '开始': 'started',
        '结束': 'finished',
        '继续': 'continue',
        '取消': 'cancelled',
        '确认': 'confirmed',
        '提交': 'submitted',
        '保存': 'saved',
        '加载': 'loaded',
        '重新': 're',
        '更新': 'updated',
        '设置': 'set',
        '获取': 'retrieved',
        '发送': 'sent',
        '接收': 'received',
        '处理': 'processed',
        '检查': 'checked',
        '验证': 'validated',
        '计算': 'calculated',
        '显示': 'displayed',
        '隐藏': 'hidden',
        '进入': 'entered',
        '离开': 'left',
        '切换': 'switched',
        '变化': 'changed',
        '包含': 'contains',
        '超出': 'exceeds',
        '不足': 'insufficient',
        '所有': 'all',
        '任意': 'any',
        '无': 'none',
        '空': 'empty',
        '未知': 'unknown',
        '未命名': 'unnamed',
        '未找到': 'not found',
        '不存在': 'does not exist',
        '已存在': 'already exists',
        '重复': 'duplicate',
        '当前': 'current',
        '新': 'new',
        '旧': 'old',
        '原': 'original',
        '默认': 'default',
        '自定义': 'custom',
        '自动': 'auto',
        '智能': 'smart',
        '交互式': 'interactive',
        '可视化': 'visualization',
        '最大': 'maximum',
        '最小': 'minimum',
        '最高': 'highest',
        '最低': 'lowest',
        '范围': 'range',
        '数量': 'count',
        '总计': 'total',
        '至少': 'at least',
        '最多': 'at most',
        '或': 'or',
        '和': 'and',
        '与': 'with',
        '从': 'from',
        '到': 'to',
        '在': 'in',
        '为': 'for',
        '对': 'to',
        '由': 'by',
        '是': 'yes',
        '否': 'no',
        '您': 'you',
        '你': 'you',
        '请': 'please',
        '的': '',
        '了': '',
        '个': '',
        '吗': '?',
        '呢': '?',
        '用法': 'usage',
        '状态': 'status',
        '功能': 'feature',
        '操作': 'operation',
        '流程': 'process',
        '方式': 'method',
        '方法': 'method',
        '类型': 'type',
        '格式': 'format',
        '内容': 'content',
        '信息': 'information',
        '消息': 'message',
        '提示': 'hint',
        '警告': 'warning',
        '注意': 'note',
        '错误': 'error',
        '失败': 'failed',
        '成功': 'success',
        '完成': 'complete',
        '开始': 'start',
        '结束': 'finish',
        '继续': 'continue',
        '取消': 'cancel',
        '确认': 'confirm',
        '提交': 'submit',
        '保存': 'save',
        '加载': 'load',
        '重新加载': 'reload',
        '选择': 'select',
        '输入': 'enter',
        '点击': 'click',
        '按': 'press',
        '键': 'key',
        '按钮': 'button',
        '列表': 'list',
        '界面': 'interface',
        '菜单': 'menu',
        '配置': 'configuration',
        '数据': 'data',
        '文件': 'file',
        '文件夹': 'folder',
        '日志': 'log',
        '模组': 'mod',
        '命令': 'command',
        '指令': 'command',
        '权限': 'permission',
        '管理员': 'administrator',
        '玩家': 'player',
        '服务器': 'server',
        '服务端': 'server',
        '客户端': 'client',
        '世界': 'world',
        '维度': 'dimension',
        '区域': 'area',
        '域名': 'area',
        '顶点': 'vertex',
        '坐标': 'coordinate',
        '边界': 'boundary',
        '高度': 'altitude',
        '颜色': 'color',
        '等级': 'level',
        '创建者': 'creator',
        '签名': 'signature',
        '记录': 'record',
        '渲染': 'render',
        '检测': 'detection',
        '频率': 'frequency',
        '调试': 'debug',
        '字幕': 'subtitle',
        '样式': 'style',
        '大小': 'size',
        '扩展': 'expand',
        '收缩': 'shrink',
        '分割': 'divide',
        '重命名': 'rename',
        '修改': 'modify',
        '删除': 'delete',
        '添加': 'add',
        '编辑': 'edit',
        '更改': 'change',
        '切换': 'toggle',
        '启用': 'enable',
        '禁用': 'disable',
        '关闭': 'close',
        '开启': 'open',
        '显示': 'display',
        '隐藏': 'hide',
        '进入': 'enter',
        '离开': 'leave',
        '包含': 'contain',
        '超出': 'exceed',
        '合理': 'valid',
        '有效': 'valid',
        '无效': 'invalid',
        '正确': 'correct',
        '错误': 'incorrect',
    }

    for cn, en in replacements.items():
        text = text.replace(cn, en)

    # Clean up multiple spaces
    text = re.sub(r'\s+', ' ', text)
    text = text.strip()

    return text


def main():
    """Main function"""
    input_file = Path("en_us.json")

    if not input_file.exists():
        print(f"Error: {input_file} not found!")
        return

    print(f"Loading {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    total = len(data)
    print(f"Post-processing {total} entries...")

    for key in data:
        if key not in ["language.name", "language.region", "language.code"]:
            data[key] = fix_spacing(data[key])

    print(f"Saving improved translation...")
    with open(input_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print("Post-processing complete!")


if __name__ == "__main__":
    main()
