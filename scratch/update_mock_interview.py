import os

file_path = r"h:\java spring\interviewai\src\main\resources\templates\mock_interview.html"

with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
    content = f.read()

# 1. Font Contrast
content = content.replace("--text-white-50: #94a3b8;", "--text-white-50: #cbd5e1;")

# 2. Page Title
content = content.replace("<title>全真模拟面试 - MindSpark 面之</title>", "<title>全真模拟面试 - MindSpark 面之光</title>")

# 3. Sidebar Logo
content = content.replace('style="font-size: 1.1rem; background: linear-gradient(135deg, #3b82f6, #10b981); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">MindSpark 面之</h4>', 'style="font-size: 1.1rem; background: linear-gradient(135deg, #3b82f6, #10b981); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">MindSpark 面之光</h4>')

# 4. Logout Button
content = content.replace("退出登?            </button>", "退出登录\n            </button>")

# 5. Exit Interview Link
content = content.replace("退出模拟面?            </a>", "退出模拟面试\n            </a>")

# 6. Live Status text
content = content.replace('<span class="text-white-50 small me-2">正在面试</span>', '<span class="text-white-50 small me-2">正在面试中</span>')

# 7. Prep Overlay Text
content = content.replace("我们将模拟真实电?语音面试。AI 面试官将主动提问并进行针对性追问", "我们将模拟真实电话/语音面试。AI 面试官将主动提问并进行针对性追问。")

# 8. setPanelState
old_panel_state = """    // Audio status states: 'thinking' (thinking & waiting api), 'speaking' (AI outputting audio), 'listening' (recording user speech)
    function setPanelState(state) {
        mainVoicePanel.classList.remove('status-speaking', 'status-listening', 'status-thinking');
        
        if (state === 'speaking') {
            mainVoicePanel.classList.add('status-speaking');
            statusTitle.innerText = "面试官正在提?..";
            statusTitle.style.color = "var(--primary-neon)";
            visualizerWave.classList.add('active');
        } else if (state === 'listening') {
            mainVoicePanel.classList.add('status-listening');
            statusTitle.innerText = "面试官倾听?(请按住说?";
            statusTitle.style.color = "var(--danger-red)";
            visualizerWave.classList.add('active');
        } else {
            mainVoicePanel.classList.add('status-thinking');
            statusTitle.innerText = "面试官思考中...";
            statusTitle.style.color = "var(--secondary-neon)";
            visualizerWave.classList.remove('active');
        }
    }"""

new_panel_state = """    // Audio status states: 'thinking' (thinking & waiting api), 'speaking' (AI outputting audio), 'listening' (recording user speech)
    function setPanelState(state) {
        mainVoicePanel.classList.remove('status-speaking', 'status-listening', 'status-thinking');
        
        if (state === 'speaking') {
            mainVoicePanel.classList.add('status-speaking');
            statusTitle.innerText = "面试官正在提问...";
            statusTitle.style.color = "var(--primary-neon)";
            visualizerWave.classList.add('active');
        } else if (state === 'listening') {
            mainVoicePanel.classList.add('status-listening');
            statusTitle.innerText = "麦克风正在收音中...";
            statusTitle.style.color = "var(--danger-red)";
            visualizerWave.classList.add('active');
        } else {
            mainVoicePanel.classList.add('status-thinking');
            statusTitle.innerText = "面试官思考中...";
            statusTitle.style.color = "var(--secondary-neon)";
            visualizerWave.classList.remove('active');
        }
    }"""

content = content.replace(old_panel_state, new_panel_state)

# 9. SpeechRecognition Continuous Setup and buttons
old_speech_rec = """    // webkitSpeechRecognition setups
    let recognition = null;
    let isRecording = false;

    if ('webkitSpeechRecognition' in window) {
        recognition = new webkitSpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = true;
        recognition.lang = 'zh-CN';

        recognition.onstart = function() {
            setPanelState('listening');
            previewText.innerHTML = "正在聆听，请自由回答...";
            currentSpeechText = '';
        };

        let speechTimeout = null;
        recognition.onresult = function(event) {
            let interimTranscript = '';
            let finalTranscript = '';
            for (let i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    finalTranscript += event.results[i][0].transcript;
                } else {
                    interimTranscript += event.results[i][0].transcript;
                }
            }
            let liveText = finalTranscript || interimTranscript;
            if (liveText) {
                currentSpeechText = liveText;
                previewText.innerText = liveText;
                
                // 实时收音：说话停?秒自动发?
                if (speechTimeout) clearTimeout(speechTimeout);
                speechTimeout = setTimeout(() => {
                    if (isRecording && currentSpeechText.trim().length > 0) {
                        stopSpeechInput();
                    }
                }, 3000);
            }
        };

        recognition.onerror = function(event) {
            console.error("Speech Recognition Error: ", event.error);
            stopSpeechInput();
        };

        recognition.onend = function() {
            if (isRecording) {
                stopSpeechInput();
            }
        };
    } else {
        voiceMicBtn.classList.add('disabled');
        micInstruction.innerText = "当前浏览器不支持语音输入，请使用键盘输入";
        toggleKeyboardDrawer(true);
    }

    // Hold to speak triggers
    function startSpeechInput() {
        if (isRequesting || isRecording) return;
        if (!recognition) return;
        
        window.speechSynthesis.cancel();
        
        isRecording = true;
        voiceMicBtn.classList.add('active');
        previewText.innerText = "正在开启麦克风...";
        
        try {
            recognition.start();
        } catch(e) {
            console.error(e);
        }
    }

    function stopSpeechInput() {
        if (!isRecording) return;
        isRecording = false;
        voiceMicBtn.classList.remove('active');
        
        if (recognition) {
            try {
                recognition.stop();
            } catch(e) {
                console.error(e);
            }
        }
        
        setPanelState('thinking');
        
        if (typeof speechTimeout !== 'undefined' && speechTimeout) {
            clearTimeout(speechTimeout);
        }
        setTimeout(() => {
            const answer = currentSpeechText.trim();
            if (answer) {
                submitAnswer(answer);
            } else {
                previewText.innerText = "未听清您的回答，请点击麦克风重新说话?;
                setPanelState('thinking');
            }
        }, 300);
    }

    if (recognition) {
        voiceMicBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (isRecording) {
                stopSpeechInput();
            } else {
                startSpeechInput();
            }
        });
    }"""

new_speech_rec = """    // webkitSpeechRecognition setups
    let recognition = null;
    let isInterviewActive = false;
    let isMuted = false;

    if ('webkitSpeechRecognition' in window) {
        recognition = new webkitSpeechRecognition();
        recognition.continuous = true;
        recognition.interimResults = true;
        recognition.lang = 'zh-CN';

        recognition.onstart = function() {
            if (mainVoicePanel.classList.contains('status-speaking')) {
                // Keep speaking
            } else if (mainVoicePanel.classList.contains('status-thinking')) {
                // Keep thinking
            } else {
                setPanelState('listening');
                previewText.innerHTML = "正在聆听，请自由回答...";
            }
            currentSpeechText = '';
        };

        let speechTimeout = null;
        recognition.onresult = function(event) {
            if (mainVoicePanel.classList.contains('status-thinking') || isMuted) {
                return;
            }

            let interimTranscript = '';
            let finalTranscript = '';
            for (let i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    finalTranscript += event.results[i][0].transcript;
                } else {
                    interimTranscript += event.results[i][0].transcript;
                }
            }
            
            let liveText = finalTranscript || interimTranscript;
            if (liveText.trim().length > 0) {
                // Interrupt AI speech
                if (mainVoicePanel.classList.contains('status-speaking') || (window.speechSynthesis && window.speechSynthesis.speaking)) {
                    if (window.speechSynthesis) {
                        window.speechSynthesis.cancel();
                    }
                    setPanelState('listening');
                    previewText.innerHTML = "正在打断，请继续说话...";
                }

                currentSpeechText = liveText;
                previewText.innerText = liveText;
                
                // VAD: 2.5 seconds of silence
                if (speechTimeout) clearTimeout(speechTimeout);
                speechTimeout = setTimeout(() => {
                    if (isInterviewActive && !isMuted) {
                        const answer = currentSpeechText.trim();
                        if (answer) {
                            submitAnswer(answer);
                            currentSpeechText = '';
                        }
                    }
                }, 2500);
            }
        };

        recognition.onerror = function(event) {
            console.error("Speech Recognition Error: ", event.error);
            if (event.error === 'not-allowed') {
                isInterviewActive = false;
                previewText.innerText = "麦克风权限被拒绝，请检查权限设置。";
                setPanelState('thinking');
            }
        };

        recognition.onend = function() {
            if (isInterviewActive && !isMuted) {
                try {
                    recognition.start();
                } catch(e) {
                    console.error("Auto-restart recognition failed:", e);
                }
            }
        };
    } else {
        voiceMicBtn.classList.add('disabled');
        micInstruction.innerText = "当前浏览器不支持语音输入，请使用键盘输入";
        toggleKeyboardDrawer(true);
    }

    if (recognition) {
        voiceMicBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (!isInterviewActive) return;
            
            isMuted = !isMuted;
            voiceMicBtn.classList.toggle('active', !isMuted);
            
            if (isMuted) {
                micInstruction.innerText = "已静音，点击恢复收音";
                previewText.innerText = "【麦克风已静音，面试官听不到您的声音】";
                try {
                    recognition.stop();
                } catch(err) {}
            } else {
                micInstruction.innerText = "实时收音中，点击静音";
                previewText.innerText = "正在聆听，请自由回答...";
                try {
                    recognition.start();
                } catch(err) {}
            }
        });
    }"""

content = content.replace(old_speech_rec, new_speech_rec)

# 10. startInterviewSession
old_start_session = """    function startInterviewSession() {
        targetCompany = document.getElementById('targetCompanyInput').value.trim();
        targetJob = document.getElementById('targetJobInput').value.trim();
        resumeText = document.getElementById('resumeInput').value.trim();

        if (targetJob) {
            document.getElementById('interviewTopicTitle').innerText = targetJob;
        }

        prepOverlay.style.opacity = '0';
        setTimeout(() => {
            prepOverlay.style.display = 'none';
        }, 500);

        startTimer();
        getNextAiTurn();
    }"""

new_start_session = """    function startInterviewSession() {
        targetCompany = document.getElementById('targetCompanyInput').value.trim();
        targetJob = document.getElementById('targetJobInput').value.trim();
        resumeText = document.getElementById('resumeInput').value.trim();

        if (targetJob) {
            document.getElementById('interviewTopicTitle').innerText = targetJob;
        }

        prepOverlay.style.opacity = '0';
        setTimeout(() => {
            prepOverlay.style.display = 'none';
        }, 500);

        startTimer();

        // Continuous VAD Voice Capturing Activation
        isInterviewActive = true;
        isMuted = false;
        if (recognition) {
            voiceMicBtn.classList.add('active');
            micInstruction.innerText = "实时收音中，点击静音";
            try {
                recognition.start();
            } catch(e) {
                console.error("Failed to start voice capturing on session start:", e);
            }
        }

        getNextAiTurn();
    }"""

content = content.replace(old_start_session, new_start_session)

# 11. submitAnswer thinking text
content = content.replace('previewText.innerText = "面试官正在思考与准备下一轮问?..";', 'previewText.innerText = "面试官正在思考与准备下一轮问题...";')

# 12. speakBuffer sentence split
content = content.replace("if (/[.?!。？??；]$/.test(text.trim()) || sentenceBuffer.length > 40) {", "if (/[.?!。？；]$/.test(text.trim()) || sentenceBuffer.length > 40) {")

# 13. network error text
content = content.replace('previewText.innerText = "网络异常，无法连接到面试服务器?;', 'previewText.innerText = "网络异常，无法连接到面试服务器。";')

# 14. onAiTurnComplete MAX_TURNS and listening texts
content = content.replace('previewText.innerText = "【模拟面试已达最大轮次，正在生成评估报告...?;', 'previewText.innerText = "【模拟面试已达最大轮次，正在生成评估报告...】";')
content = content.replace('previewText.innerText = "【您可以点击麦克风进行回答，或点击右下角结束面试?;', 'previewText.innerText = "【您可以说话开始回答，或点击右下角结束面试】";')

# 15. bubble label text
content = content.replace("label.innerText = role === 'ai' ? 'AI 面试? : '?;", "label.innerText = role === 'ai' ? 'AI 面试官' : '我';")

# 16. executeFinish stop mic & loading text
old_execute_finish = """    async function executeFinish() {
        confirmFinishModal.hide();
        window.speechSynthesis.cancel();
        
        prepOverlay.style.display = 'flex';
        prepOverlay.style.opacity = '1';
        prepOverlay.innerHTML = `
            <div class="glass-card-prep">
                <div class="spinner-border text-primary mb-3" style="width: 3rem; height: 3rem;" role="status"></div>
                <h3 class="fw-bold text-white mb-2">正在评估全场面试表现...</h3>
                <p class="text-white-50">AI 正在深度审查您的多轮对答记录，打分并编排雷达图各项能力评测。请稍等几秒钟</p>
            </div>
        `;"""

new_execute_finish = """    async function executeFinish() {
        confirmFinishModal.hide();
        window.speechSynthesis.cancel();
        
        // Stop voice recognition
        isInterviewActive = false;
        if (recognition) {
            try {
                recognition.stop();
            } catch(e) {}
        }
        
        prepOverlay.style.display = 'flex';
        prepOverlay.style.opacity = '1';
        prepOverlay.innerHTML = `
            <div class="glass-card-prep">
                <div class="spinner-border text-primary mb-3" style="width: 3rem; height: 3rem;" role="status"></div>
                <h3 class="fw-bold text-white mb-2">正在评估全场面试表现...</h3>
                <p class="text-white-50">AI 正在深度审查您的多轮对答记录，打分并编排雷达图各项能力评测。请稍等几秒钟...</p>
            </div>
        `;"""

content = content.replace(old_execute_finish, new_execute_finish)

# 17. fullLog mapping
content = content.replace("fullLog += `${msg.role === 'user' ? '候选人' : '面试?}: ${msg.content}\\n\\n`;", "fullLog += `${msg.role === 'user' ? '候选人' : '面试官'}: ${msg.content}\\n\\n`;")

# 18. alert error text
content = content.replace('alert("评分生成失败，请重试?);', 'alert("评分生成失败，请重试");')

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Mock Interview HTML Updated successfully!")
