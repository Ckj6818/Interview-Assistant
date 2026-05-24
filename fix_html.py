import os
import re

files = ['copilot.html', 'mock_interview.html', 'resume.html', 'records.html']
directory = r'H:\java spring\interviewai\src\main\resources\templates'

for filename in files:
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    # Repair broken tags (e.g. ??/span> or ?/title> -> </span> or </title>)
    repaired = re.sub(r'\?+?/([a-zA-Z0-9]+)>', r'</\1>', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(repaired)

print("Repair completed.")
