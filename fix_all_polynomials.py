#!/usr/bin/env python3
import re
import os

files = [
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialGeneric.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialInteger.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialBoolean.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialModular.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialRational.kt",
]

for file_path in files:
    if not os.path.exists(file_path):
        print(f"Skipping {file_path} - not found")
        continue

    with open(file_path, 'r') as f:
        content = f.read()

    # Replace override var size/degree with private vars and functions
    content = re.sub(r'override var size: Int', 'private var _size: Int', content)
    content = re.sub(r'override var degree: Int', 'private var _degree: Int', content)

    # Add size() and degree() functions after the first constructor
    if 'override fun size()' not in content:
        # Find the first constructor body end
        lines = content.split('\n')
        new_lines = []
        added = False
        for i, line in enumerate(lines):
            new_lines.append(line)
            # Add after the variable declarations
            if not added and 'private var _degree: Int' in line:
                # Look ahead to find the first constructor
                for j in range(i+1, min(i+10, len(lines))):
                    if lines[j].strip().startswith('constructor(') and '_size' in lines[j:j+5]:
                        # Add the functions after the constructor
                        for k in range(j+1, min(j+10, len(lines))):
                            if lines[k].strip() == '}':
                                new_lines.append('')
                                new_lines.append('    override fun size(): Int = _size')
                                new_lines.append('')
                                new_lines.append('    override fun degree(): Int = _degree')
                                added = True
                                break
                        break
        if added:
            content = '\n'.join(new_lines)

    # Fix this.size = to this._size =
    content = re.sub(r'this\.size\s*=', 'this._size =', content)
    content = re.sub(r'this\.degree\s*=', 'this._degree =', content)

    # Fix p.size, q.size, p.degree, q.degree
    content = re.sub(r'\bp\.size\b', 'p._size', content)
    content = re.sub(r'\bq\.size\b', 'q._size', content)
    content = re.sub(r'\bp\.degree\b', 'p._degree', content)
    content = re.sub(r'\bq\.degree\b', 'q._degree', content)

    # Fix standalone size/degree (but not in function params)
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        # Don't replace in function definitions or parameters
        if ('fun init(size:' in line or 'fun resize(size:' in line or
            'constructor(size:' in line or 'fun indexOf' in line):
            new_lines.append(line)
            continue

        # Replace size/degree but not function calls
        line = re.sub(r'\bsize\b(?!\s*:)(?!\()', '_size', line)
        line = re.sub(r'\bdegree\b(?!\s*:)(?!\s*\()', '_degree', line)
        new_lines.append(line)

    content = '\n'.join(new_lines)

    # Fix ordering.compare ambiguity
    content = content.replace('-ordering.compare(m1, m2)', '-(ordering as Comparator<Monomial>).compare(m1, m2)')

    # Remove override from gcd/monomialGcd/genericValue/elements/compareTo
    content = re.sub(r'override fun gcd\(polynomial:', 'fun gcd(polynomial:', content)
    content = re.sub(r'override fun gcd\(\):', 'fun gcd():', content)
    content = re.sub(r'override fun monomialGcd\(\):', 'fun monomialGcd():', content)
    content = re.sub(r'override fun genericValue\(\):', 'fun genericValue():', content)
    content = re.sub(r'override fun elements\(\):', 'fun elements():', content)
    content = re.sub(r'override fun compareTo\(polynomial:', 'fun compareTo(polynomial:', content)

    # Fix newinstance to newInstance
    content = re.sub(r'override fun newinstance\(', 'fun newInstance(', content)
    content = re.sub(r'\bnewinstance\(', 'newInstance(', content)

    # Fix coefFactory null issue
    content = content.replace('return ArrayPolynomialGeneric(n, monomialFactory, coefFactory)',
                             'return ArrayPolynomialGeneric(n, monomialFactory, coefFactory!!)')
    content = content.replace('return ArrayPolynomialInteger(n, monomialFactory)',
                             'return ArrayPolynomialInteger(n, monomialFactory)')
    content = content.replace('return ArrayPolynomialBoolean(n, monomialFactory)',
                             'return ArrayPolynomialBoolean(n, monomialFactory)')
    content = content.replace('return ArrayPolynomialModular(n, monomialFactory, coefFactory)',
                             'return ArrayPolynomialModular(n, monomialFactory, coefFactory!!)')
    content = content.replace('return ArrayPolynomialRational(n, monomialFactory)',
                             'return ArrayPolynomialRational(n, monomialFactory)')

    with open(file_path, 'w') as f:
        f.write(content)

    print(f"Fixed {file_path}")

print("Done!")
