#!/usr/bin/env python3
import re

files = [
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialGeneric.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialInteger.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialBoolean.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialModular.kt",
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialRational.kt",
]

for file_path in files:
    try:
        with open(file_path, 'r') as f:
            content = f.read()
    except:
        print(f"Skipping {file_path}")
        continue

    # Fix incorrect replacements from previous script
    content = content.replace('init(_size)', 'init(size)')
    content = content.replace('resize(_size)', 'resize(size)')
    content = content.replace('fun init(size: Int) {\n        monomial = arrayOfNulls(_size)',
                             'fun init(size: Int) {\n        monomial = arrayOfNulls(size)')
    content = content.replace('fun init(size: Int) {\n        monomial = arrayOfNulls(_size)',
                             'fun init(size: Int) {\n        monomial = arrayOfNulls(size)')
    content = content.replace('intCoef = IntArray(_size)', 'intCoef = IntArray(size)')
    content = content.replace('coef = arrayOfNulls(_size)', 'coef = arrayOfNulls(size)')
    content = content.replace('this._size = _size', 'this._size = size')
    content = content.replace('monomial._size', 'monomial.size')
    content = content.replace('if (_size < length)', 'if (size < length)')
    content = content.replace('val newMonomial = arrayOfNulls<Monomial>(_size)',
                             'val newMonomial = arrayOfNulls<Monomial>(size)')
    content = content.replace('val newCoef = arrayOfNulls<Generic>(_size)',
                             'val newCoef = arrayOfNulls<Generic>(size)')
    content = content.replace('val newCoef = arrayOfNulls<BigInteger>(_size)',
                             'val newCoef = arrayOfNulls<BigInteger>(size)')
    content = content.replace('val newCoef = IntArray(_size)',
                             'val newCoef = IntArray(size)')
    content = content.replace('System.arraycopy(monomial, length - _size, newMonomial, 0, _size)',
                             'System.arraycopy(monomial, length - size, newMonomial, 0, size)')
    content = content.replace('System.arraycopy(coef, length - _size, newCoef, 0, _size)',
                             'System.arraycopy(coef, length - size, newCoef, 0, size)')
    content = content.replace('System.arraycopy(intCoef, length - _size, newCoef, 0, _size)',
                             'System.arraycopy(intCoef, length - size, newCoef, 0, size)')

    with open(file_path, 'w') as f:
        f.write(content)

    print(f"Fixed {file_path}")

print("Done!")
