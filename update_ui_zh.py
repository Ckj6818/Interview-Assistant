import os
import re

html_dir = 'src/main/resources/templates'

sidebar_html = """<!-- 侧边栏 -->
<div class="sidebar d-flex flex-column justify-content-between py-4">
    <div>
        <div class="logo-container mb-4 px-3">
            <div class="logo-icon me-2">
                <i class="bi bi-robot text-white fs-5"></i>
            </div>
            <div>
                <h4 class="logo-text m-0 fw-bold text-white" style="font-size: 1.1rem; background: linear-gradient(135deg, #3b82f6, #10b981); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">MindSpark 面之光</h4>
                <div style="color: #64748b; font-size: 0.75rem; font-weight: 500;">智能面试辅助平台</div>
            </div>
        </div>
        <div class="px-3">
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/}"><i class="bi bi-grid-fill me-2"></i> 系统首页</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/questions}"><i class="bi bi-journal-text me-2"></i> 题库大厅</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/mock-interview}"><i class="bi bi-mic me-2"></i> 模拟面试</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/copilot}"><i class="bi bi-rocket-takeoff me-2"></i> 面试助手</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/resume}"><i class="bi bi-file-earmark-person me-2"></i> 简历优化</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/records}"><i class="bi bi-clock-history me-2"></i> 面试记录</a>
                </li>
            </ul>
        </div>
    </div>
    
    <div class="px-3">
        <button class="btn w-100 mb-3 d-flex align-items-center justify-content-center gap-2" 
                style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.08); color: #f1f5f9; font-size: 0.85rem; padding: 10px; border-radius: 8px; font-weight: 500;"
                onclick="window.location.href='/mock-interview'">
            <i class="bi bi-plus-lg"></i> 开始新面试
        </button>
        <ul class="nav flex-column border-top pt-3" style="border-color: rgba(255,255,255,0.05) !important;">
            <li class="nav-item">
                <a class="nav-link py-2 px-1" href="#" style="font-size: 0.88rem; gap: 8px;"><i class="bi bi-gear me-2"></i> 设置</a>
            </li>
            <li class="nav-item">
                <form th:action="@{/logout}" method="post" style="margin: 0;">
                    <button type="submit" class="nav-link py-2 px-1 text-start" style="font-size: 0.88rem; gap: 8px; border: none; background: transparent; width: 100%;">
                       <i class="bi bi-box-arrow-right me-2"></i> 退出登录
                    </button>
                </form>
            </li>
        </ul>
    </div>
</div>
"""

questions_translations = {
    'Dashboard': '系统首页',
    'Question Bank': '题库大厅',
    'Mock Interview': '模拟面试',
    'Live Prompts': '面试助手',
    'AI Analysis': '简历优化',
    'History': '面试记录',
    'Start New Session': '开始新面试',
    'Settings': '设置',
    'Support': '支持',
    'Search prompts...': '搜索题目...',
    'Go Premium': '升级高级版',
    'Interview Library': '题库大厅',
    'Curated prompt sets and technical scenarios designed to test specific competencies. Select a category to begin practice.': '海量大厂经典算法与底层架构真题，通过多维 AI 评估您的全景专业实力。请选择分类开始练习。',
    'All Sets': '全部题库',
    'Technical': '技术问题',
    'Behavioral': '行为问题',
    'Leadership': '领导力',
    'Back to Interview Library': '返回题库大厅',
    'Software Engineering': '软件工程',
    'Algorithms, data structures, debugging, and core language concepts.': '算法、数据结构、调试与核心语言概念。',
    'Completed': '已完成',
    'Search questions in this set...': '在当前分类中搜索题目...',
    'All Difficulties': '所有难度',
    'Easy': '简单',
    'Medium': '中等',
    'Hard': '困难',
    'All Types': '所有类型',
    'Coding': '编程题',
    'Conceptual': '概念题',
    'All Status': '所有状态',
    'Solved': '已解答',
    'Unsolved': '未解答',
    'Reset': '重置',
    'Outline View': '大纲视图',
    'Question List': '题目列表',
    'Expand All': '展开全部',
    'Collapse All': '折叠全部',
    'Status': '状态',
    'Title': '题目',
    'Category': '分类',
    'Difficulty': '难度',
    'Type': '类型',
    'Action': '操作',
    'No questions found. Try adjusting your search filters.': '未找到题目，请尝试调整筛选条件。'
}

js_translations = {
    '"Java Foundation"': '"Java基础"',
    '"Core Java syntax, Object-Oriented programming, Collections framework, and JVM internals."': '"Java核心语法、面向对象编程、集合框架及JVM原理。"',
    '"Concurrent Programming"': '"并发编程"',
    '"Multithreading, JMM memory model, thread pools, locks, and synchronizers."': '"多线程、JMM内存模型、线程池、锁及同步器。"',
    '"Database Systems"': '"数据库"',
    '"MySQL transactional isolation, InnoDB indexing, locks, and optimization techniques."': '"MySQL事务隔离、InnoDB索引、锁及优化技巧。"',
    '"Spring Ecosystem"': '"Spring生态"',
    '"Spring Boot auto-configuration, IoC, AOP, transaction, and cloud integrations."': '"Spring Boot自动配置、IoC、AOP、事务及云原生集成。"',
    '"System Design"': '"系统设计"',
    '"Scalability, high availability design patterns, cache strategies, and distributed storage."': '"高可用设计模式、缓存策略及分布式存储。"',
    '"Algorithms & Coding"': '"算法与编程"',
    '"LeetCode coding problems, complexity analysis, data structures, and division strategy."': '"LeetCode真题、复杂度分析及数据结构。"',
    '"Data Structures"': '"数据结构"',
    '"Linked lists, stacks, queues, trees, heaps, hashes, and graph operations."': '"链表、栈、队列、树、堆、哈希表及图。"',
    '"Emerging AI Tech"': '"前沿AI技术"',
    '"DeepSeek architecture, Large Language Model optimization, and Agent patterns."': '"大语言模型优化及Agent智能体设计模式。"',
    '"Natural Language Processing"': '"自然语言处理"',
    '"Transformer self-attention layers, embedding, tokenization, and LLM fine-tuning."': '"Transformer架构、词嵌入、分词及微调技术。"'
}

def process_file(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. replace brutalist with modern
    content = content.replace('brutalist-theme.css', 'modern-theme.css')
    
    # 2. replace sidebar
    sidebar_pattern = re.compile(r'<!-- 侧边栏 -->.*?<div class="main-content">', re.DOTALL)
    if sidebar_pattern.search(content):
        content = sidebar_pattern.sub(sidebar_html + '\n<div class="main-content">', content)
    else:
        # Fallback for index.html which has different structure
        sidebar_pattern2 = re.compile(r'<div class="col-md-2 sidebar.*?>.*?</div>.*?<!-- 右侧主体 -->', re.DOTALL)
        if sidebar_pattern2.search(content):
            pass # index.html needs careful replacement or just leave it for now, let's fix it manually or via regex
            
    # For index.html specifically:
    if 'index.html' in filepath:
        content = re.sub(r'<div class="col-md-2 sidebar.*?>.*?<!-- 右侧主体 -->', 
                         sidebar_html + '\n<!-- 右侧主体 -->', content, flags=re.DOTALL)

    # 3. For questions.html, replace texts
    if 'questions.html' in filepath:
        for en, zh in questions_translations.items():
            content = content.replace(f'>{en}<', f'>{zh}<')
            content = content.replace(f'placeholder="{en}"', f'placeholder="{zh}"')
        
        for en, zh in js_translations.items():
            content = content.replace(en, zh)

    # Apply active class correctly
    filename = os.path.basename(filepath)
    if filename == 'index.html':
        content = content.replace('th:href="@{/}"><i class="bi bi-grid-fill me-2"></i> 系统首页</a>', 'class="nav-link active" th:href="@{/}"><i class="bi bi-grid-fill me-2"></i> 系统首页</a>')
    elif filename == 'questions.html':
        content = content.replace('th:href="@{/questions}"><i class="bi bi-journal-text me-2"></i> 题库大厅</a>', 'class="nav-link active" th:href="@{/questions}"><i class="bi bi-journal-text me-2"></i> 题库大厅</a>')
    elif filename == 'mock_interview.html':
        content = content.replace('th:href="@{/mock-interview}"><i class="bi bi-mic me-2"></i> 模拟面试</a>', 'class="nav-link active" th:href="@{/mock-interview}"><i class="bi bi-mic me-2"></i> 模拟面试</a>')
    elif filename == 'copilot.html':
        content = content.replace('th:href="@{/copilot}"><i class="bi bi-rocket-takeoff me-2"></i> 面试助手</a>', 'class="nav-link active" th:href="@{/copilot}"><i class="bi bi-rocket-takeoff me-2"></i> 面试助手</a>')
    elif filename == 'resume.html':
        content = content.replace('th:href="@{/resume}"><i class="bi bi-file-earmark-person me-2"></i> 简历优化</a>', 'class="nav-link active" th:href="@{/resume}"><i class="bi bi-file-earmark-person me-2"></i> 简历优化</a>')
    elif filename == 'records.html':
        content = content.replace('th:href="@{/records}"><i class="bi bi-clock-history me-2"></i> 面试记录</a>', 'class="nav-link active" th:href="@{/records}"><i class="bi bi-clock-history me-2"></i> 面试记录</a>')


    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in os.listdir(html_dir):
    if filename.endswith('.html'):
        process_file(os.path.join(html_dir, filename))

print("UI Update Complete")
