import traceback

try:
    file_path = r"h:\java spring\interviewai\src\main\resources\templates\copilot.html"
    
    with open(file_path, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    # 1. Head
    content = content.replace("    <style>", '''    <!-- Markdown & Mermaid & KaTeX -->
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
    <style>''')

    # 2. Modes (Using string replacement that avoids Chinese)
    content = content.replace('<option value="detailed">', '''<option value="detailed">详细模式 (源码级/排坑)</option>
                    <option value="conversational">🗣️ 口语化表达 (自然连贯)</option>
                    <option value="phone_interview">📞 电话面试 (纯口述逻辑)</option>
                    <!-- fallback for --> <option value="detailed_old" style="display:none;">''')

    # 3. Context fields
    content = content.replace('<div class="card-body-scroll" id="analysisBox">', '''            <div class="p-3 border-bottom border-secondary" style="background: rgba(0,0,0,0.2);">
                <div class="mb-2">
                    <label class="form-label small text-secondary mb-1">候选人简历上下文</label>
                    <textarea class="form-control bg-dark text-white border-secondary small" id="resumeContext" rows="2" placeholder="粘贴简历重点内容..."></textarea>
                </div>
                <div>
                    <label class="form-label small text-secondary mb-1">目标岗位要求 (JD)</label>
                    <textarea class="form-control bg-dark text-white border-secondary small" id="jdContext" rows="2" placeholder="粘贴职位要求..."></textarea>
                </div>
            </div>
            <div class="card-body-scroll" id="analysisBox">''')

    # 4. Speaker Toggle
    content = content.replace('<div class="p-3 border-top border-secondary bg-dark bg-opacity-25 d-flex gap-2">', '''            <div class="px-3 pt-3">
                <div class="btn-group w-100" role="group">
                    <input type="radio" class="btn-check" name="speakerRadio" id="speakerInterviewer" value="interviewer" checked>
                    <label class="btn btn-outline-primary btn-sm" for="speakerInterviewer">面试官声音</label>
                    <input type="radio" class="btn-check" name="speakerRadio" id="speakerMe" value="me">
                    <label class="btn btn-outline-success btn-sm" for="speakerMe">我的声音</label>
                </div>
            </div>
            <div class="p-3 border-top border-secondary bg-dark bg-opacity-25 d-flex gap-2">''')

    # 5. Fetch body
    content = content.replace('body: JSON.stringify({ text: text, mode: selectedMode })', '''body: JSON.stringify({ 
                    text: text, 
                    mode: selectedMode,
                    resumeText: document.getElementById('resumeContext') ? document.getElementById('resumeContext').value : "",
                    targetJd: document.getElementById('jdContext') ? document.getElementById('jdContext').value : "",
                    isUserSpeaking: document.getElementById('speakerMe') && document.getElementById('speakerMe').checked
                })''')

    # 6. Render Logic
    content = content.replace("const formattedAnalysis = (data.analysis || '').replace(/\\n/g, '<br>');", "")
    content = content.replace("const formattedAnswer = (data.answer || '').replace(/\\n/g, '<br>');", "")

    content = content.replace("cardAnalysis.innerHTML = `", '''
                let formattedAnalysis = data.analysis || '';
                let formattedAnswer = data.answer || '';
                
                if (typeof marked !== 'undefined') {
                    formattedAnalysis = marked.parse(formattedAnalysis);
                    formattedAnswer = marked.parse(formattedAnswer);
                } else {
                    formattedAnalysis = formattedAnalysis.replace(/\\n/g, '<br>');
                    formattedAnswer = formattedAnswer.replace(/\\n/g, '<br>');
                }
                
                cardAnalysis.innerHTML = `''')

    content = content.replace("cardAnswer.innerHTML = `", '''
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
                
                cardAnswer.innerHTML = `''')

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

    print("Success")
except Exception as e:
    with open(r"h:\java spring\interviewai\scratch\error.log", "w") as f:
        f.write(traceback.format_exc())
    print("Error occurred")
