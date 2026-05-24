const fs = require('fs');
const path = require('path');

const htmlDir = 'src/main/resources/templates';

const sidebarHtml = `<!-- 侧边栏 -->
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
                    <a class="nav-link" href="/"><i class="bi bi-grid-fill me-2"></i> 系统首页</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/questions"><i class="bi bi-journal-text me-2"></i> 题库大厅</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/mock-interview"><i class="bi bi-mic me-2"></i> 模拟面试</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/copilot"><i class="bi bi-rocket-takeoff me-2"></i> 面试助手</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/resume"><i class="bi bi-file-earmark-person me-2"></i> 简历优化</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/records"><i class="bi bi-clock-history me-2"></i> 面试记录</a>
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
                <form action="/logout" method="post" style="margin: 0;">
                    <button type="submit" class="nav-link py-2 px-1 text-start" style="font-size: 0.88rem; gap: 8px; border: none; background: transparent; width: 100%;">
                       <i class="bi bi-box-arrow-right me-2"></i> 退出登录
                    </button>
                </form>
            </li>
        </ul>
    </div>
</div>
`;

function processFile(filepath) {
    if (!fs.existsSync(filepath)) return;
    let content = fs.readFileSync(filepath, 'utf8');

    // 1. replace sidebar based on different patterns
    if (content.includes('class="sidebar"')) {
        // Fallback for when there's an enclosing div with class="sidebar"
        // But let's just do a broader replace
    }
    
    // Replace questions.html sidebar:
    content = content.replace(/<!--  -->[\s\S]*?(?=<div class="main-content">)/, sidebarHtml + '\n');
    content = content.replace(/<div class="sidebar d-flex[\s\S]*?(?=<div class="main-content">)/, sidebarHtml + '\n');
    content = content.replace(/<div class="sidebar"[\s\S]*?(?=<div class="main-content">)/, sidebarHtml + '\n');
    
    // Replace index.html sidebar:
    content = content.replace(/<div class="col-md-2 sidebar[\s\S]*?(?=<!-- 右侧主体 -->)/, sidebarHtml + '\n');

    // Apply active class
    const filename = path.basename(filepath);
    const replaceNav = (href) => {
        const regex = new RegExp(`<a class="nav-link" href="${href}">`, 'g');
        content = content.replace(regex, `<a class="nav-link active" href="${href}">`);
    };
    
    if (filename === 'index.html') replaceNav('/');
    else if (filename === 'questions.html') replaceNav('/questions');
    else if (filename === 'mock_interview.html') replaceNav('/mock-interview');
    else if (filename === 'copilot.html') replaceNav('/copilot');
    else if (filename === 'resume.html') replaceNav('/resume');
    else if (filename === 'records.html') replaceNav('/records');

    fs.writeFileSync(filepath, content, 'utf8');
}

const files = fs.readdirSync(htmlDir);
for (const file of files) {
    if (file.endsWith('.html')) {
        processFile(path.join(htmlDir, file));
    }
}

console.log('Sidebar Fixed');
