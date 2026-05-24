import json
import os
import re

log_path = r'C:\Users\asus\.gemini\antigravity-ide\brain\3408e3a4-da0f-4715-8c46-82cd4f37059f\.system_generated\logs\transcript.jsonl'
output_dir = r'H:\java spring\interviewai\src\main\resources\templates'

files_to_recover = {
    'index.html': False,
    'questions.html': False,
    'records.html': False,
    'resume.html': False,
    'copilot.html': False
}

def unescape_string(s):
    # If the string starts and ends with double quotes, it might be JSON encoded.
    if s.startswith('"') and s.endswith('"'):
        try:
            return json.loads(s)
        except:
            pass
    return s

lines = []
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Read {len(lines)} lines from transcript")

# We iterate backwards
for i in range(len(lines) - 1, -1, -1):
    line = lines[i]
    if 'write_to_file' not in line and 'replace_file_content' not in line:
        continue
    
    try:
        data = json.loads(line)
        if data.get('type') == 'PLANNER_RESPONSE' and 'tool_calls' in data:
            for tc in data['tool_calls']:
                if tc['name'] == 'write_to_file':
                    args = tc.get('args', {})
                    target = args.get('TargetFile', '')
                    content = args.get('CodeContent', '')
                    for fname in files_to_recover:
                        if not files_to_recover[fname] and fname in target:
                            # Recover it!
                            decoded_content = unescape_string(content)
                            out_path = os.path.join(output_dir, fname)
                            with open(out_path, 'w', encoding='utf-8') as out_f:
                                out_f.write(decoded_content)
                            print(f"Recovered {fname} from write_to_file at line {i+1}")
                            files_to_recover[fname] = True
    except Exception as e:
        pass

# Wait, I didn't use write_to_file for ALL of them recently. 
# For copilot.html, records.html, resume.html, index.html, questions.html:
# Let's check scratch files too!
scratch_dir = r'C:\Users\asus\.gemini\antigravity-ide\brain\3408e3a4-da0f-4715-8c46-82cd4f37059f\scratch'
if not files_to_recover['questions.html']:
    src = os.path.join(scratch_dir, 'questions_output.html')
    if os.path.exists(src):
        with open(src, 'r', encoding='utf-8') as f:
            content = f.read()
        with open(os.path.join(output_dir, 'questions.html'), 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Recovered questions.html from scratch/questions_output.html")
        files_to_recover['questions.html'] = True

