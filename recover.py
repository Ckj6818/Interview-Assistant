import json
import os

log_path = r'C:\Users\asus\.gemini\antigravity-ide\brain\e019ca05-a167-4b4a-b0df-2623390a3909\.system_generated\logs\transcript.jsonl'
target_files = ['copilot.html', 'mock_interview.html', 'resume.html', 'records.html']
contents = {k: None for k in target_files}

print("Parsing log file...")
with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            step = json.loads(line)
            if step.get('type') == 'TOOL_RESPONSE' and step.get('status') == 'DONE':
                content = step.get('content', '')
                if content:
                    for filename in target_files:
                        if filename in content and 'The above content shows the entire, complete file contents' in content:
                            # Try to extract the file content from a full view_file output
                            if 'The following code has been modified to include a line number' in content:
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
                                        # format is line_number: content
                                        if ':' in l:
                                            parsed_lines.append(l.split(':', 1)[1][1:])
                                contents[filename] = '\n'.join(parsed_lines)
        except Exception as e:
            pass

for k, v in contents.items():
    if v:
        print(f"Found full content for {k} (length: {len(v)})")
    else:
        print(f"Did not find full content for {k}")
