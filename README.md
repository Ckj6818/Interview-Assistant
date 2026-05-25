# MindSpark (InterviewAI) - 智能模拟面试与全真技术对线平台

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg" alt="Spring Boot 3.x">
  <img src="https://img.shields.io/badge/Spring%20Security-6.x-blue.svg" alt="Spring Security 6">
  <img src="https://img.shields.io/badge/UI-Dark%20Theme-black.svg" alt="Dark Theme">
</p>

---

## 💡 项目背景与核心痛点 (Background & Pain Points)

在当前竞争激烈的招聘环境下，求职者面临**“缺乏真实高压面试环境”**与**“传统八股文背诵失效”**的痛点。
**MindSpark (InterviewAI)** 是一款基于大语言模型（LLM）驱动的沉浸式全真模拟面试与智能评测平台。项目采用全面适配开发者的**全暗黑（Dark Theme）低能耗视觉设计**，后端基于 **Spring Boot 3.x** 工业级架构，实现了“智能题库检索 -> 简历解析诊断 -> AI 流式对线追问 -> 多维复盘报告”的完整全栈业务闭环。

---

## ✨ 核心特性 (Key Features)

### 1. 🎭 动态面试官人设与流式追问引擎
* **多身份上下文注入：** 系统支持动态配置面试官人设（如大厂严厉架构师、资深 HR 专家等），后端 Service 自动拼装系统级提示词（System Prompt）。
* **技术深度追问：** 拒绝简单的“一问一答”模式，AI 会根据用户前一句回答中的技术漏洞发起**连续施压追问**，高保真还原大厂真实的技术对线场景。

### 2. 📊 智能复盘与结构化多维诊断报告
* **结构化 JSON 解析：** 后端服务对 AI 反馈的长文本和评分进行深度解析，动态提取并发处理、源码理解、系统架构等核心指标。
* **能力可视化：** 将清洗后的评分数据落库，为前端生成多维能力雷达图提供精准的数据支撑，量化技术盲区并给出大厂标准修复建议。

### 3. 🛡️ 工业级容灾防御与文本清洗链（项目亮点）
* **流式文本容错拦截：** 针对大模型流式传输或多字符集转换中极易出现的 `FFFD` 替换字符及 `BOM` 编码格式问题，内置自动化清洗组件，确保前端数据百分之百渲染稳定。
* **数据恢复与容灾机制：** 自研完备的离线日志状态恢复工具（Log-to-HTML Recovery），在系统突发崩溃的极端场景下，能够根据运行日志实现用户历史面试会话的无损还原。

---

## 📸 系统预览 (System Preview)

### 1. 🎛️ 控制面板与数据看板 (Dashboard)
项目主控台，直观追踪模拟面试场次、累计时长及核心技术栈熟练度。
![Dashboard](images/dashboard.png)

### 2. ⚔️ 沉浸式 AI 面试对线界面 (AI Interview Interface)
深度还原高压面试。AI 面试官正在根据求职者的回答进行连环技术施压追问。
![Interview](images/interview.png)

### 3. 📝 智能题目库与解析矩阵 (Question Matrix)
基于关系型数据库建立的多维技术题库，支持按技术栈标签与难度梯度精细化检索。
![Questions](images/questions.png)

---

## 🛠️ 技术架构与选型 (Tech Stack & Architecture)

### 后端核心技术 (Backend Stack)
* **核心框架：** `Java 17` + `Spring Boot 3.x`（享受新版 GraalVM 原生镜像生态与极致启动速度）
* **安全鉴权：** `Spring Security 6.x`（基于 RBAC 权限模型，实现用户、面试官数据全方位隔离）
* **网络通信：** `Spring WebClient`（全面弃用传统的同步阻塞式 `RestTemplate`，面对大模型长文本响应采用**异步非阻塞通信**，避免高并发下对 Tomcat 线程池的死锁式消耗）
* **持久层：** `Spring Data JPA` + `MySQL 8.0`（利用实体映射机制，配合 `@Transactional` 保证多表联查与数据写入的一致性）

### 鲁棒性与工程工具集 (System Tools)
为保障生产环境的健壮性，项目中开发并集成了多套自研底层脚本：
* `SearchTranscript` / `CheckNul`：自适应文本流高并发字符清洗过滤检测器。
* `RemoveBOM` / `FixHtmlSyntax`：跨平台字符集格式不一致修正引擎，阻断前端 HTML/JS 渲染瑕疵。
* `RecoverFromLog` / `ExtractHistory`：会话级容灾恢复工具，支持基于历史日志无损重建用户面试上下文。

---

## 🚀 快速开始 (Quick Start)

### 1. 环境依赖
* Java 17+
* Maven 3.8+
* MySQL 8.0+

### 2. 配置初始化
修改 `src/main/resources/application.yml` 中的数据库配置与大模型 API Key：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/interviewai?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password

llm:
  api-key: your_llm_api_key_here
