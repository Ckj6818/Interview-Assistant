import os

fixes = {
    "MindSpark 面之?": "MindSpark 面之光",
    "退出登?": "退出登录",
    "系统总题?": "系统总题库",
    "仪表?": "仪表盘",
    "快速指?": "快速指南",
    "AI简历定?诊所": "AI简历定制/诊所",
    "开始全真多轮语音面?": "开始全真多轮语音面试",
    "鹅来面系统核心优?": "鹅来面系统核心优势",
    "AI简历定?& 深度精修": "AI简历定制 & 深度精修",
    "实时面试灵感?": "实时面试灵感库",
    "退出登?</button>": "退出登录</button>",
    "AI??": "AI简历诊断",
    "˳??": "退出登录",
    "AI??& ": "AI简历定制 & 精修",
    "??": "简历内容",
    "ĿְλJDλ??- ѡ": "目标职位JD - 选填",
    "?? ????? (????????)": "正常模式 (正常追问)",
    "?? ?????? (?????/????)": "算法模式 (考察算法/手撕代码)",
    "??? ??????? (???/????)": "系统设计模式 (考察架构/设计)",
    "?? ????? (???????/????)": "压力测试模式 (考察抗压/极限)",
    "ҳ": "首页",
    "ȫģ": "全真模拟面试",
    "ʵʱ": "实时提词助手",
    "AI/": "AI简历定制/诊所",
    "Լ¼": "面试记录"
}

files = ['index.html', 'questions.html', 'resume.html', 'records.html', 'copilot.html']
base_dir = r"H:\java spring\interviewai\src\main\resources\templates"

for f in files:
    path = os.path.join(base_dir, f"recovered_{f}")
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        for k, v in fixes.items():
            if k in content:
                content = content.replace(k, v)
        
        dest = os.path.join(base_dir, f)
        with open(dest, 'w', encoding='utf-8') as out:
            out.write(content)
        print(f"Fixed {f}")
