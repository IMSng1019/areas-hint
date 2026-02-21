#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Final cleanup: Add proper spacing between English words
"""

import json
import re
from pathlib import Path

def add_proper_spacing(text):
    """Add spaces between concatenated English words"""
    if not text or not isinstance(text, str):
        return text

    # List of common English words that should be separated
    words = [
        'record', 'complete', 'after', 'click', 'button', 'area', 'modify', 'data',
        'invalid', 'cancel', 'dimension', 'process', 'failed', 'server', 'incorrect',
        'save', 'file', 'vertex', 'occurred', 'while', 'error', 'has', 'add', 'need',
        'at', 'least', 'submit', 'message', 'permission', 'you', 'no', 'not', 'found',
        'creator', 'current', 'continue', 'new', 'update', 'count', 'insufficient',
        'please', 'select', 'success', 'title', 'load', 'boundary', 'visualization',
        'for', 'close', 'open', 'none', 'file', 'custom', 'altitude', 'lowest',
        'exceeds', 'valid', 'range', 'highest', 'display', 'and', 'detection',
        'feature', 'mod', 'status', 'usage', 'maximum', 'minimum', 'set', 'start',
        'interactive', 'expand', 'shrink', 'divide', 'list', 'editable', 'parent',
        'surface', 'name', 'color', 'format', 'confirm', 'delete', 'rename', 'size',
        'style', 'command', 'execution', 'request', 'response', 'client', 'render',
        'method', 'frequency', 'language', 'selection', 'input', 'number', 'debug',
        'mode', 'enabled', 'disabled', 'receive', 'real', 'time', 'information',
        'already', 'marked', 'remaining', 'deletion', 'index', 'keep', 'required',
        'level', 'finish', 'recording', 'line', 'intersection', 'external', 'order',
        'reverse', 'sorting', 'angle', 'convex', 'hull', 'algorithm', 'successful',
        'calculate', 'ensure', 'segment', 'final', 'coordinate', 'validate', 'passed',
        'original', 'shrink', 'permission', 'correct', 'try', 'reverse', 'order',
        'cross', 'detect', 'attempt', 'final', 'ensure', 'segment', 'intersection',
    ]

    result = text

    # Add space before each word if it's preceded by a lowercase letter
    for word in sorted(set(words), key=len, reverse=True):
        # Match word preceded by lowercase letter (not already spaced)
        pattern = rf'([a-z])({word})'
        result = re.sub(pattern, r'\1 \2', result, flags=re.IGNORECASE)

    # Fix specific common patterns
    fixes = {
        'recordcomplete': 'record complete',
        'afterclick': 'after click',
        'nomodify': 'no modify',
        'datainvalid': 'data invalid',
        'processfailed': 'process failed',
        'serverprocess': 'server process',
        'savefile': 'save file',
        'processvertex': 'process vertex',
        'occurredwhile': 'occurred while',
        'hascancel': 'has cancelled',
        'addvertex': 'add vertex',
        'atleast': 'at least',
        'needrecord': 'need record',
        'hassubmit': 'has submitted',
        'nopermission': 'no permission',
        'modifyarea': 'modify area',
        'notfound': 'not found',
        'currenthas': 'current has',
        'hasrecord': 'has recorded',
        'continuerecord': 'continue record',
        'newvertex': 'new vertex',
        'recordnew': 'record new',
        'vertexupdate': 'vertex update',
        'vertexcount': 'vertex count',
        'hasselectarea': 'has selected area',
        'pleaseselectadd': 'please select add',
        'vertexarea': 'vertex area',
        'selectarea': 'select area',
        'vertexupdatesuccess': 'vertex update success',
        'addvertex': 'add vertex',
        'selectarea': 'select area',
        'loadarea': 'load area',
        'boundarydata': 'boundary data',
        'hasload': 'has loaded',
        'areaboundary': 'area boundary',
        'forvisualization': 'for visualization',
        'boundaryvisualization': 'boundary visualization',
        'hasclose': 'has closed',
        'hasopen': 'has opened',
        'nonedimension': 'none dimension',
        'dimensionfile': 'dimension file',
        'customaltitude': 'custom altitude',
        'lowestaltitude': 'lowest altitude',
        'exceedsvalid': 'exceeds valid',
        'validrange': 'valid range',
        'highestaltitude': 'highest altitude',
        'hasenable': 'has enabled',
        'areadisplay': 'area display',
        'anddetection': 'and detection',
        'detectionfeature': 'detection feature',
        'modcurrent': 'mod current',
        'currentstatus': 'current status',
        'maximumaltitude': 'maximum altitude',
        'hasdisable': 'has disabled',
        'altitudeset': 'altitude set',
        'setprocess': 'set process',
        'startinteractive': 'start interactive',
        'interactivealtitude': 'interactive altitude',
        'selectauto': 'select auto',
        'autoaltitude': 'auto altitude',
        'selectcustom': 'select custom',
        'altitudevalue': 'altitude value',
        'minimumaltitude': 'minimum altitude',
        'processaltitude': 'process altitude',
        'setrequest': 'set request',
        'areawill': 'area will',
        'nolonger': 'no longer',
        'willstill': 'will still',
        'receiveserver': 'receive server',
        'serverdata': 'server data',
        'addarea': 'add area',
        'pleasecheck': 'please check',
        'checklog': 'check log',
        'currentdimension': 'current dimension',
        'youcan': 'you can',
        'modifyaltitude': 'modify altitude',
        'altitudearea': 'altitude area',
        'getlist': 'get list',
        'occurredwhile': 'occurred while',
        'continuearea': 'continue area',
        'areaexpand': 'area expand',
        'areashrink': 'area shrink',
        'deletearea': 'delete area',
        'arealist': 'area list',
        'dimensionsave': 'dimension save',
        'areaconfiguration': 'area configuration',
        'configurationfailed': 'configuration failed',
        'currentlynoarea': 'currently no area',
        'areadata': 'area data',
        'canedit': 'can edit',
        'editarea': 'edit area',
        'startarea': 'start area',
        'areadivide': 'area divide',
        'dividefailed': 'divide failed',
        'expandarea': 'expand area',
        'expandlist': 'expand list',
        'expandfailed': 'expand failed',
        'arealevel': 'area level',
        'ensureat': 'ensure at',
        'atleast': 'at least',
        'andcorrect': 'and correct',
        'correctarea': 'correct area',
        'setarea': 'set area',
        'sendarea': 'send area',
        'arealisttoclient': 'area list to client',
        'selectparent': 'select parent',
        'parentarea': 'parent area',
        'savearea': 'save area',
        'saveexpand': 'save expand',
        'saveshrink': 'save shrink',
        'shrinkarea': 'shrink area',
        'shrinkfailed': 'shrink failed',
        'surfacename': 'surface name',
        'selectfailed': 'select failed',
        'readarea': 'read area',
        'switchboundary': 'switch boundary',
        'canceloperation': 'cancel operation',
        'canceldelete': 'cancel delete',
        'cancelexpand': 'cancel expand',
        'cancellanguage': 'cancel language',
        'languageselect': 'language select',
        'cancelrename': 'cancel rename',
        'cancelshrink': 'cancel shrink',
        'cancelsize': 'cancel size',
        'sizeselect': 'size select',
        'cancelfailed': 'cancel failed',
        'cancelstyle': 'cancel style',
        'styleselect': 'style select',
        'invalidcolor': 'invalid color',
        'colorformat': 'color format',
        'confirmdelete': 'confirm delete',
        'confirmrename': 'confirm rename',
        'unableto': 'unable to',
        'determinecurrent': 'determine current',
        'pleasespecify': 'please specify',
        'setdimension': 'set dimension',
        'dimensionname': 'dimension name',
        'unabletorecognize': 'unable to recognize',
        'recognizecurrent': 'recognize current',
        'alreadybeen': 'already been',
        'beennamed': 'been named',
        'validoptions': 'valid options',
        'thiscommand': 'this command',
        'commandcan': 'command can',
        'canonly': 'can only',
        'onlybe': 'only be',
        'beexecuted': 'be executed',
        'executedby': 'executed by',
        'byplayers': 'by players',
        'inputformat': 'input format',
        'formatincorrect': 'format incorrect',
        'entervalid': 'enter valid',
        'validnumber': 'valid number',
        'sendinginteractive': 'sending interactive',
        'interactiverecolor': 'interactive recolor',
        'recolorinterface': 'recolor interface',
        'interfaceto': 'interface to',
        'sendingresponse': 'sending response',
        'sendingclient': 'sending client',
        'clientcommand': 'client command',
        'sendingrecolor': 'sending recolor',
        'recolorresponse': 'recolor response',
        'processingrequest': 'processing request',
        'commandexecution': 'command execution',
        'executionfailed': 'execution failed',
        'executingcommand': 'executing command',
        'commanderror': 'command error',
        'tryagain': 'try again',
        'againlater': 'again later',
        'submissionfailed': 'submission failed',
        'invalidjson': 'invalid JSON',
        'jsondata': 'JSON data',
        'checkformat': 'check format',
        'invalidrender': 'invalid render',
        'rendermethod': 'render method',
    }

    for wrong, correct in fixes.items():
        result = result.replace(wrong, correct)

    # Clean up multiple spaces
    result = re.sub(r'\s+', ' ', result)
    result = result.strip()

    return result

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
    print(f"Adding proper spacing to {total} entries...")

    for key in data:
        if key not in ["language.name", "language.region", "language.code"]:
            data[key] = add_proper_spacing(data[key])

    print(f"Saving improved translation...")
    with open(input_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print("Spacing correction complete!")

if __name__ == "__main__":
    main()
