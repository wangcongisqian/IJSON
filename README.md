# IJSON - IntelliJ IDEA JSON 操作插件

工程名称：**IJSON**

一个功能完整的 IntelliJ IDEA 插件，专注于 JSON 的校验、美化与交互式编辑、新建/生成。

## 功能特性

### 1. 校验 JSON
- 验证选中文本或当前文件内容是否为合法 JSON
- 支持宽松解析（注释、单引号等）
- 快捷键：`Ctrl + Alt + J` 然后 `V`
- 菜单位置：Tools → IJSON → 校验 JSON，或编辑器右键菜单

### 2. 美化 JSON（树形交互编辑）
- 格式化（Pretty Print）JSON
- **交互式树视图**：
  - 节点可**收缩 / 展开**
  - 支持**增加节点**（Object / Array / String / Number / Boolean / Null）
  - 支持**删除节点**
  - 双击叶子节点可直接编辑值
  - 右键菜单快捷操作
- 美化后可写回编辑器，同时支持打开右侧 IJSON 工具窗口继续编辑
- 快捷键：`Ctrl + Alt + J` 然后 `B`

### 3. 新建 / 生成 JSON
- **任意生成**：示例 JSON、空 Object
- **根据输入内容生成**：
  - 支持 `key=value` 或 `key:value` 形式（每行或逗号分隔）
  - 自动识别字符串、数字、布尔、null
  - 普通描述文本会生成带示例结构的 JSON
- 生成后可插入当前编辑器、打开为新文件或复制到剪贴板
- 快捷键：`Ctrl + Alt + J` 然后 `N`

### 4. IJSON 工具窗口
- 右侧 Tool Window「IJSON」
- 输入区 + 可交互树编辑 + 一键校验/美化/导出/复制/生成

## 项目结构

```
IJSON/
├── build.gradle.kts          # Gradle 构建脚本（IntelliJ Platform Plugin 2.x）
├── settings.gradle.kts
├── gradle.properties
├── src/main/
│   ├── java/com/ijson/plugin/
│   │   ├── actions/          # 菜单/快捷键动作
│   │   │   ├── ValidateJsonAction.java
│   │   │   ├── BeautifyJsonAction.java
│   │   │   ├── NewJsonAction.java
│   │   │   └── OpenJsonToolWindowAction.java
│   │   ├── model/
│   │   │   └── JsonTreeNode.java   # 可编辑树节点模型
│   │   ├── ui/
│   │   │   ├── JsonTreePanel.java          # 核心交互树面板
│   │   │   ├── JsonToolWindowFactory.java
│   │   │   ├── JsonToolWindowPanel.java
│   │   │   └── NewJsonDialog.java
│   │   └── utils/
│   │       └── JsonUtils.java              # Jackson 封装
│   └── resources/META-INF/
│       └── plugin.xml
└── README.md
```

## 环境要求

- JDK 17+
- IntelliJ IDEA 2024.3 及以上（since-build 243）
- Gradle 8.2+（推荐使用 Wrapper）

## 构建与运行

### 1. 导入项目
用 IntelliJ IDEA 打开 `IJSON` 目录，选择以 Gradle 项目导入。

### 2. 运行插件（开发调试）
在 Gradle 工具窗口中找到并执行：

```
intellijPlatform → runIde
```

或直接运行预配置的 **Run Plugin** 配置。这会启动一个带有本插件的 IDE 沙箱实例。

### 3. 构建插件包
```bash
./gradlew buildPlugin
```
生成的 zip 位于 `build/distributions/IJSON-1.0.0.zip`，可在 IDE 中通过 **Settings → Plugins → 齿轮 → Install Plugin from Disk** 安装。

### 4. 验证
```bash
./gradlew verifyPlugin
```

## 依赖

- Jackson Databind（JSON 解析与序列化）
- IntelliJ Platform SDK

## 使用示例

1. 在编辑器中粘贴一段 JSON，选中后右键 → **IJSON → 美化 JSON（树形编辑）**
2. 在弹出的树对话框中展开/收缩节点，右键增加或删除字段，确认后写回编辑器
3. Tools → IJSON → 新建 / 生成 JSON，输入 `name=张三,age=25,active=true` 即可生成对应 JSON
4. 打开右侧 **IJSON** 工具窗口，进行持续编辑与校验

## 许可证

MIT（可根据需要修改）

---

**IJSON Team**  
享受更高效的 JSON 工作流！
