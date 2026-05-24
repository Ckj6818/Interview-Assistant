import os
import re

template_dir = r"h:\java spring\interviewai\src\main\resources\templates"

script_to_add = """
<script>
    document.addEventListener("DOMContentLoaded", function() {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar && !document.querySelector('.sidebar-toggle')) {
            const toggleBtn = document.createElement('div');
            toggleBtn.className = 'sidebar-toggle';
            toggleBtn.innerHTML = '<i class="bi bi-chevron-left"></i>';
            toggleBtn.onclick = function() {
                sidebar.classList.toggle('collapsed');
                const mainContent = document.querySelector('.main-content');
                if (mainContent) mainContent.classList.toggle('expanded');
                // Save state
                localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
            };
            sidebar.appendChild(toggleBtn);
            
            // Wrap text nodes in spans for easy hiding
            document.querySelectorAll('.sidebar .nav-link, .sidebar .btn').forEach(el => {
                Array.from(el.childNodes).forEach(node => {
                    if (node.nodeType === Node.TEXT_NODE && node.textContent.trim().length > 0) {
                        const span = document.createElement('span');
                        span.className = 'nav-text';
                        span.textContent = node.textContent;
                        el.replaceChild(span, node);
                    }
                });
            });

            // Restore state
            if (localStorage.getItem('sidebarCollapsed') === 'true') {
                sidebar.classList.add('collapsed');
                const mainContent = document.querySelector('.main-content');
                if (mainContent) mainContent.classList.add('expanded');
            }
        }
    });
</script>
</body>
"""

for filename in os.listdir(template_dir):
    if filename.endswith(".html"):
        filepath = os.path.join(template_dir, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
            
        if "sidebar-toggle" not in content and "toggleSidebar" not in content:
            # Replace the closing </body> tag
            new_content = content.replace("</body>", script_to_add)
            
            if new_content != content:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print(f"Updated {filename}")
