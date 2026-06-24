# 第二阶段独立指令图形化清单

## 复核依据

- 服务端主命令注册：`src/main/java/areahint/command/ServerCommands.java`
- 独立注册命令：`src/main/java/areahint/command/CheckCommand.java`、`src/main/java/areahint/command/DebugCommand.java`
- 当前图形化入口：`src/client/java/areahint/commandui/CommandVisualRegistry.java`
- 帮助文本命令列表：`src/main/resources/assets/areas-hint/lang/zh_cn.json` 中的 `help.command.*` 和 `command.usage.*`

## 统一规则

- 一轮对话只处理一个顶层指令；同名子指令属于同一个顶层指令。
- 不处理 `easyadd` 指令，也不单独处理 `addarea`，因为 `addarea` 是 `easyadd` 的完整别名。
- 图形化只新增或完善客户端 Screen/VisualController，把输入转换成现有 `/areahint ...` 子命令或现有 Manager/Networking 方法。
- 不重写命令业务逻辑；几何计算、JSON 保存、网络同步、权限校验继续复用现有实现。
- 执行结果可以继续使用聊天消息反馈，不强制做结果弹窗。
- 用户已要求不要写测试；每轮只做代码检查和构建验证。

## 按首字母顺序执行

9. `debug`
10. `delete`
11. `deletedescription`
12. `deletedimensionalitydescription`
13. `deletehint`
14. `deletesignature`
15. `deletesubtitle`
16. `dimensionalitycolor`
17. `dimensionalityname`
18. `dividearea`
19. `expandarea`
20. `firstdimname`
21. `firstdimname_skip`
22. `frequency`
23. `help`
24. `hintrender`
25. `language`
26. `off`
27. `on`
28. `recolor`
29. `reload`
30. `rename`
31. `replacebutton`
32. `replacedescription`
33. `replacedimensionalitydescription`
34. `replacesubtitle`
35. `replacesubtitlecolor`
36. `replacesubtitlesize`
37. `serverlanguage`
38. `sethigh`
39. `settp`
40. `shrinkarea`
41. `tcp`
42. `titlesize`
43. `titlestyle`
44. `udp`

## 已完成

1. `add`： 已完成
2. `adddescription`：已完成
3. `adddimensionalitydescription`：已完成
4. `addhint`：已完成
5. `addsignature`：已完成
6. `addsubtitle`：已完成
7. `boundviz`：已完成
8. `check`：已完成

## 已排除

- `easyadd`：已完成高质量图形化流程，后续作为参考实现。
- `addarea`：`easyadd` 的完整别名，逻辑与权限都复用 `easyadd`。
