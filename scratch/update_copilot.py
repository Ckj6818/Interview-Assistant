import re

file_path = "h:/java spring/interviewai/src/main/resources/templates/copilot.html"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Add Libraries to <head>
head_addition = """
    <!-- Markdown & Mermaid & KaTeX -->
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
"""
content = content.replace("</head>", head_addition + "\n</head>")

# 2. Add New Modes
new_modes = """
                    <option value="detailed">🔍 详细模式 (源码级/排坑)</option>
                    <option value="conversational">🗣️ 口语化表达 (自然连贯)</option>
                    <option value="phone_interview">📞 电话面试 (纯口述逻辑)</option>
"""
content = re.sub(r'<option value="detailed">.*?</option>', new_modes, content)

# 3. Add Resume & JD Context to Left Panel
resume_jd_ui = """
            <div class="p-3 border-bottom border-secondary" style="background: rgba(0,0,0,0.2);">
                <div class="mb-2">
                    <label class="form-label small text-secondary mb-1">候选人简历上下文</label>
                    <textarea class="form-control bg-dark text-white border-secondary small" id="resumeContext" rows="2" placeholder="粘贴简历重点内容..."></textarea>
                </div>
                <div>
                    <label class="form-label small text-secondary mb-1">目标岗位要求 (JD)</label>
                    <textarea class="form-control bg-dark text-white border-secondary small" id="jdContext" rows="2" placeholder="粘贴职位要求..."></textarea>
                </div>
            </div>
            <div class="card-body-scroll" id="analysisBox">
"""
content = content.replace('<div class="card-body-scroll" id="analysisBox">', resume_jd_ui)

# 4. Add Speaker Toggle to Right Panel
speaker_toggle_ui = """
            <div class="px-3 pt-3">
                <div class="btn-group w-100" role="group">
                    <input type="radio" class="btn-check" name="speakerRadio" id="speakerInterviewer" value="interviewer" checked>
                    <label class="btn btn-outline-primary btn-sm" for="speakerInterviewer">面试官声音</label>
                    <input type="radio" class="btn-check" name="speakerRadio" id="speakerMe" value="me">
                    <label class="btn btn-outline-success btn-sm" for="speakerMe">我的声音</label>
                </div>
            </div>
            <div class="p-3 border-top border-secondary bg-dark bg-opacity-25 d-flex gap-2">
"""
content = content.replace('<div class="p-3 border-top border-secondary bg-dark bg-opacity-25 d-flex gap-2">', speaker_toggle_ui)

# 5. Update getAiSuggestion Payload & Rendering
fetch_update = """
        const resumeText = document.getElementById('resumeContext') ? document.getElementById('resumeContext').value : "";
        const targetJd = document.getElementById('jdContext') ? document.getElementById('jdContext').value : "";
        const isUserSpeaking = document.getElementById('speakerMe') && document.getElementById('speakerMe').checked;

        try {
            const response = await fetch('/api/copilot/suggest', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    text: text, 
                    mode: selectedMode,
                    resumeText: resumeText,
                    targetJd: targetJd,
                    isUserSpeaking: isUserSpeaking
                })
            });
"""
content = re.sub(r'try\s*\{\s*const response = await fetch\(\'/api/copilot/suggest\', \{(?:[^{}]*|\{[^{}]*\})*\}\);', fetch_update, content)

render_update = """
                // 使用 marked.js 渲染 Markdown
                let formattedAnalysis = data.analysis || '';
                let formattedAnswer = data.answer || '';
                
                if (typeof marked !== 'undefined') {
                    formattedAnalysis = marked.parse(formattedAnalysis);
                    formattedAnswer = marked.parse(formattedAnswer);
                } else {
                    formattedAnalysis = formattedAnalysis.replace(/\\n/g, '<br>');
                    formattedAnswer = formattedAnswer.replace(/\\n/g, '<br>');
                }

                cardAnalysis.innerHTML = `
                    <div class="text-primary small mb-2 fw-bold"><i class="bi bi-search"></i> 深度分析与意图</div>
                    <div class="text-white markdown-body">${formattedAnalysis}</div>
                `;
                
                cardAnswer.innerHTML = `
                    <div class="text-warning small mb-2 fw-bold"><i class="bi bi-lightbulb-fill"></i> 回答策略 / 实时纠正</div>
                    <div class="lh-lg text-white markdown-body" style="font-size: 1.05rem;">${formattedAnswer}</div>
                `;

                // 渲染数学公式和流程图
                setTimeout(() => {
                    if (window.renderMathInElement) {
                        renderMathInElement(cardAnswer, {
                            delimiters: [
                                {left: "$$", right: "$$", display: true},
                                {left: "$", right: "$", display: false}
                            ]
                        });
                    }
                    if (typeof mermaid !== 'undefined') {
                        mermaid.init(undefined, cardAnswer.querySelectorAll('.language-mermaid'));
                    }
                }, 100);
"""
content = re.sub(r'const formattedAnalysis = \(data\.analysis \|\| \'\'\)\.replace\(/\\n/g, \'<br>\'\);\s*const formattedAnswer = \(data\.answer \|\| \'\'\)\.replace\(/\\n/g, \'<br>\'\);.*?(?=} else \{)', render_update, content, flags=re.DOTALL)


# Write back
with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Updated copilot.html")
