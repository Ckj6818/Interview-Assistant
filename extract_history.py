import json
import os
import dateutil.parser

log_path = r'C:\Users\asus\.gemini\antigravity-ide\brain\e019ca05-a167-4b4a-b0df-2623390a3909\.system_generated\logs\transcript.jsonl'
target_file = 'mock_interview.html'

print("Parsing log file...")
with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            step = json.loads(line)
            created_at = step.get('created_at', '')
            if not created_at: continue
            
            # Look for write_to_file or replace_file_content or view_file
            if step.get('type') == 'PLANNER_RESPONSE':
                for call in step.get('tool_calls', []):
                    args = call.get('args', {})
                    if 'mock_interview.html' in str(args) or 'copilot.html' in str(args) or 'interview.html' in str(args):
                        print(f"[{created_at}] Tool call: {call.get('name')} - {str(args)[:100]}")
                        
            if step.get('type') == 'TOOL_RESPONSE' and step.get('status') == 'DONE':
                content = step.get('content', '')
                if 'mock_interview.html' in content and 'The above content shows the entire, complete file contents' in content:
                    print(f"[{created_at}] Found full content in tool response")
        except Exception as e:
            pass
