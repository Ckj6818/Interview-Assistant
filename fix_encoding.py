import os

files = ['copilot.html', 'mock_interview.html', 'resume.html', 'records.html']
base = r'H:\java spring\interviewai\src\main\resources\templates'

for f in files:
    path = os.path.join(base, f)
    with open(path, 'rb') as file:
        raw = file.read()
    
    # Try utf-8
    try:
        text = raw.decode('utf-8')
        print(f + ' is utf-8')
    except UnicodeDecodeError:
        try:
            text = raw.decode('gbk')
            print(f + ' is gbk')
            with open(path, 'w', encoding='utf-8') as out:
                out.write(text)
        except Exception as e:
            print(f + ' is unknown: ' + str(e))
