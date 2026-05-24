import os
import re

directory = r'H:\java spring\interviewai\src\main\resources\templates'

files_to_fix = ['copilot.html', 'mock_interview.html', 'resume.html', 'records.html']

for filename in files_to_fix:
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace specific bad contrast classes on form labels
    content = re.sub(r'class="([^"]*)text-muted([^"]*)"', r'class="\1text-white-50\2"', content)
    content = re.sub(r'class="([^"]*)text-secondary([^"]*)"', r'class="\1text-white-50\2"', content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

print("Colors updated successfully.")
