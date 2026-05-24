import json
import os

log_path = r'C:\Users\asus\.gemini\antigravity-ide\brain\3408e3a4-da0f-4715-8c46-82cd4f37059f\.system_generated\logs\transcript.jsonl'
output_dir = r'H:\java spring\interviewai\src\main\resources\templates'

files_to_recover = {
    'index.html': None,
    'questions.html': None,
    'records.html': None,
    'resume.html': None,
    'copilot.html': None
}

# Read from bottom to top to get the LATEST uncorrupted version (which is any version printed or written)
lines = []
with open(log_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for line in reversed(lines):
    try:
        data = json.loads(line)
        # Check tool calls: write_to_file, replace_file_content
        # Actually, replace_file_content only contains diffs. 
        # But write_to_file contains the FULL file!
        if data.get('type') == 'PLANNER_RESPONSE' and 'tool_calls' in data:
            for tc in data['tool_calls']:
                if tc['name'] == 'write_to_file':
                    args = tc.get('args', {})
                    target = args.get('TargetFile', '')
                    content = args.get('CodeContent', '')
                    for fname in files_to_recover:
                        if fname in target and files_to_recover[fname] is None:
                            # We found the latest full write_to_file for this file!
                            files_to_recover[fname] = content
                            print(f"Recovered {fname} from write_to_file")
    except Exception as e:
        pass

# Wait, I might not have used write_to_file recently for all of them. 
# Some files were never modified by me today, so I need to find the earliest view_file or read them from scratch/questions_output.html?
