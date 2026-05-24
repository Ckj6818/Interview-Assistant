import os

directory = r'H:\java spring\interviewai\src\main\resources\templates'

for filename in os.listdir(directory):
    if filename.endswith('.html'):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'rb') as f:
            content = f.read()
        if content.startswith(b'\xef\xbb\xbf'):
            print(f"Removing BOM from {filename}")
            content = content[3:]
            with open(filepath, 'wb') as f:
                f.write(content)
print("BOM removal complete.")
