# 功能截图

本目录存放 README 使用的项目真实截图，均来自本地运行的 Interview-Assistant（MindSpark 面之光）。

| 文件 | 页面 | 路径 |
|------|------|------|
| `01-home.png` | 系统首页 | `/` |
| `02-questions.png` | 题库大厅 | `/questions` |
| `03-mock-interview.png` | 全真模拟面试 | `/mock-interview` |

## 重新生成

1. 启动应用（默认账号 `user` / `123456`）：
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
2. 执行截图脚本（需 Python 3 + Selenium + Edge）：
   ```bash
   py scripts/capture_screenshots.py
   ```

请勿使用外部网站的截图，以免与 GitHub 仓库展示不一致。
