#!/usr/bin/env python3
import re

file_path = "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomial.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Fix bad replacements from sed
content = content.replace('init(map._size)', 'init(map.size)')
content = content.replace('for (i in 0 until size)', 'for (i in 0 until _size)')
content = content.replace('override fun head(): Term? = if (size > 0) content[size - 1] else null',
                         'override fun head(): Term? = if (_size > 0) content[_size - 1] else null')
content = content.replace('override fun tail(): Term? = if (size > 0) content[0] else null',
                         'override fun tail(): Term? = if (_size > 0) content[0] else null')
content = content.replace('System.arraycopy(q.content, 0, content, 0, size)',
                         'System.arraycopy(q.content, 0, content, 0, _size)')
content = content.replace('degree = _degree', '_degree = q._degree')
content = content.replace('val p = newinstance(size)', 'val p = newinstance(_size)')
content = content.replace('p._degree = degree', 'p._degree = _degree')

# Fix remaining size references within the class
lines = content.split('\n')
fixed_lines = []
in_function = False
for i, line in enumerate(lines):
    # Don't replace size in function parameters or init(size)
    if 'fun init(size:' in line or 'fun resize(size:' in line:
        fixed_lines.append(line)
        continue
    if 'fun indexOf' in line or 'constructor(size:' in line:
        fixed_lines.append(line)
        continue

    # Replace standalone size/degree references to _size/_degree
    line = re.sub(r'\bsize\b(?!\s*:)', '_size', line)
    line = re.sub(r'\bdegree\b(?!\s*:)(?!\s*\()', '_degree', line)

    fixed_lines.append(line)

content = '\n'.join(fixed_lines)

# Fix ordering.compare ambiguity - use explicit syntax
content = content.replace('-ordering.compare(', '-ordering.compare(')  # Keep the same for now, will fix later

# Remove override from gcd methods
content = content.replace('override fun gcd(polynomial:', 'fun gcd(polynomial:')
content = content.replace('override fun gcd():', 'fun gcd():')
content = content.replace('override fun monomialGcd():', 'fun monomialGcd():')

# Fix newinstance to newInstance
content = content.replace('override fun newinstance(', 'fun newInstance(')
content = content.replace('newinstance(', 'newInstance(')

# Fix genericValue, elements, compareTo
content = content.replace('override fun genericValue():', 'fun genericValue():')
content = content.replace('override fun elements():', 'fun elements():')
content = content.replace('override fun compareTo(polynomial:', 'fun compareTo(polynomial:')

with open(file_path, 'w') as f:
    f.write(content)

print("Fixed ArrayPolynomial.kt")
