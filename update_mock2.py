import re

file_path = r'H:\java spring\interviewai\src\main\resources\templates\mock_interview.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove toggleSolverBtn HTML
content = re.sub(r'<button class="btn-circle" id="toggleSolverBtn".*?</button>', '', content, flags=re.DOTALL)

# Remove toggleHistoryBtn HTML
content = re.sub(r'<button class="btn-circle" id="toggleHistoryBtn".*?</button>', '', content, flags=re.DOTALL)

# Remove toggleHistoryBtn declaration
content = re.sub(r'const toggleHistoryBtn = document\.getElementById\(\'toggleHistoryBtn\'\);\s*', '', content)

# Remove toggleHistoryBtn and toggleSolverBtn listeners
content = re.sub(r'// Toggle Sidebar Smartly\s*toggleHistoryBtn\.addEventListener\(\'click\', \(\) => \{.*?\}\);\s*toggleSolverBtn\.addEventListener\(\'click\', \(\) => \{.*?\}\);', '', content, flags=re.DOTALL)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Buttons removed successfully.")
