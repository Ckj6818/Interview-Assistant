import re

file_path = r'H:\java spring\interviewai\src\main\resources\templates\mock_interview.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Replace the entire sidebarHistory HTML
sidebar_regex = re.compile(r'<!-- 右侧：面试对话历史实录 -->.*?<div class="chat-sidebar" id="sidebarHistory">.*?<div class="sidebar-header.*?</div>\s*</div>\s*</div>\s*</div>\s*</div>', re.DOTALL)

new_sidebar = """<!-- 右侧：面试对话历史实录 -->
        <div class="chat-sidebar" id="sidebarHistory">
            <div class="sidebar-header d-flex align-items-center justify-content-between py-2 px-3">
                <span class="text-white small fw-bold">
                    <i class="bi bi-chat-dots-fill text-info me-1"></i> 对话实录
                </span>
                <button class="btn btn-link text-muted p-0 border-0" id="closeHistoryBtn"><i class="bi bi-x-lg"></i></button>
            </div>
            <div class="tab-content flex-1 overflow-hidden" style="height: calc(100% - 57px); display: flex; flex-direction: column;">
                <div class="tab-pane fade show active chat-history-flow" id="historyContainer" role="tabpanel" style="flex: 1; overflow-y: auto;">
                    <!-- 聊天泡泡在此追加 -->
                </div>
            </div>
        </div>"""

content = sidebar_regex.sub(new_sidebar, content)

# 2. Remove AI 笔试秒解 JS Logic
js_regex = re.compile(r'// AI 笔试秒解 Logic.*?startSolveBtn\.addEventListener\(\'click\', async \(\) => \{.*?\n    \}\);\n', re.DOTALL)
content = js_regex.sub('', content)

# 3. Update the speakBuffer function to match copilot.html
old_speak_buffer = r"""const speakBuffer = \(textBuffer\) => \{.*?window\.speechSynthesis\.speak\(utterance\);\n                \}\n            \};"""

new_speak_buffer = """const speakBuffer = (textBuffer) => {
                if (ttsEnabled && 'speechSynthesis' in window && textBuffer.trim().length > 0) {
                    const cleanText = textBuffer.replace(/[*#`]/g, '').replace(/([。！？；])/g, '$1 ').trim();
                    if (!cleanText) return;
                    
                    const utterance = new SpeechSynthesisUtterance(cleanText);
                    utterance.lang = 'zh-CN';
                    utterance.rate = 1.0;
                    utterance.pitch = 1.0;
                    
                    if (availableVoices.length === 0) {
                        availableVoices = window.speechSynthesis.getVoices();
                    }
                    
                    let bestVoice = null;
                    const preferredKeywords = ['Xiaoxiao', 'Natural', 'Online', 'Ting-Ting', 'Google'];
                    for (let keyword of preferredKeywords) {
                        bestVoice = availableVoices.find(v => v.lang.includes('zh') && v.name.includes(keyword));
                        if (bestVoice) break;
                    }
                    if (!bestVoice) {
                        bestVoice = availableVoices.find(v => v.lang.includes('zh-CN')) || availableVoices.find(v => v.lang.includes('zh'));
                    }
                    if (bestVoice) {
                        utterance.voice = bestVoice;
                    }
                    
                    window.speechSynthesis.speak(utterance);
                }
            };"""

content = re.sub(old_speak_buffer, new_speak_buffer, content, flags=re.DOTALL)

# 4. Remove the waiting for speech synthesis to finish before onAiTurnComplete
old_wait_code = r"""if \(ttsEnabled && 'speechSynthesis' in window && window\.speechSynthesis\.speaking\) \{
                const checkSpeaking = setInterval\(\(\) => \{
                    if \(!window\.speechSynthesis\.speaking\) \{
                        clearInterval\(checkSpeaking\);
                        onAiTurnComplete\(\);
                    \}
                \}, 500\);
            \} else \{
                onAiTurnComplete\(\);
            \}"""

new_wait_code = """onAiTurnComplete();"""

content = re.sub(old_wait_code, new_wait_code, content, flags=re.DOTALL)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Update completed.")
