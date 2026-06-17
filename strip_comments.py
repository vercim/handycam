"""Strip all Java comments from .java files in the current directory tree."""
import os, re, sys

def strip_java_comments(src: str) -> str:
    result = []
    i = 0
    n = len(src)
    while i < n:
        # String literal
        if src[i] == '"':
            j = i + 1
            while j < n:
                if src[j] == '\\':
                    j += 2
                elif src[j] == '"':
                    j += 1
                    break
                else:
                    j += 1
            result.append(src[i:j])
            i = j
        # Char literal
        elif src[i] == "'":
            j = i + 1
            while j < n:
                if src[j] == '\\':
                    j += 2
                elif src[j] == "'":
                    j += 1
                    break
                else:
                    j += 1
            result.append(src[i:j])
            i = j
        # Block comment
        elif src[i:i+2] in ('/*', '/**'):
            end = src.find('*/', i + 2)
            if end == -1:
                i = n
            else:
                i = end + 2
        # Line comment
        elif src[i:i+2] == '//':
            end = src.find('\n', i)
            if end == -1:
                i = n
            else:
                i = end  # keep the newline
        else:
            result.append(src[i])
            i += 1
    return ''.join(result)

def clean_blank_lines(src: str) -> str:
    """Collapse 3+ consecutive blank lines to 2."""
    return re.sub(r'\n{3,}', '\n\n', src)

def process_file(path: str) -> bool:
    with open(path, 'r', encoding='utf-8') as f:
        original = f.read()
    stripped = strip_java_comments(original)
    stripped = clean_blank_lines(stripped)
    if stripped != original:
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(stripped)
        return True
    return False

root = sys.argv[1] if len(sys.argv) > 1 else '.'
changed = 0
for dirpath, _, files in os.walk(root):
    for name in files:
        if name.endswith('.java'):
            full = os.path.join(dirpath, name)
            if process_file(full):
                print(f'  stripped: {os.path.relpath(full, root)}')
                changed += 1
print(f'\nDone. {changed} file(s) modified.')
