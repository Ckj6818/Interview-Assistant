import json
import os

log_path = r'C:\Users\asus\.gemini\antigravity-ide\brain\e019ca05-a167-4b4a-b0df-2623390a3909\.system_generated\logs\transcript.jsonl'
target_file = 'mock_interview.html'
output_file = r'h:\java spring\interviewai\recovered_mock_interview.html'

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            step = json.loads(line)
            if step.get('type') == 'TOOL_RESPONSE' and step.get('status') == 'DONE':
                content = step.get('content', '')
                if 'mock_interview.html' in content and 'The above content shows the entire, complete file contents' in content:
                    # extract
                    lines = content.split('\n')
                    parsed_lines = []
                    recording = False
                    for l in lines:
                        if 'The following code has been modified' in l:
                            recording = True
                            continue
                        if 'The above content shows the entire' in l:
                            recording = False
                            break
                        if recording:
                            if ':' in l:
                                parsed_lines.append(l.split(':', 1)[1][1:])
                    with open(output_file, 'w', encoding='utf-8') as out:
                        out.write('\n'.join(parsed_lines))
        except Exception:
            pass
