#!/bin/bash

# Fix Array Polynomial files
files=(
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomial.kt"
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialGeneric.kt"
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialInteger.kt"
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialBoolean.kt"
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialModular.kt"
    "jscl/src/main/kotlin/jscl/math/polynomial/ArrayPolynomialRational.kt"
)

for file in "${files[@]}"; do
    if [ ! -f "$file" ]; then
        continue
    fi

    # Fix function name
    sed -i '' 's/override fun _size()/override fun size()/g' "$file"

    # Fix _size and _degree to protected
    sed -i '' 's/private var _size:/protected var _size:/g' "$file"
    sed -i '' 's/private var _degree:/protected var _degree:/g' "$file"

    # Fix gcd, monomialGcd, genericValue, elements, compareTo to have override
    sed -i '' 's/^    fun gcd(polynomial:/    override fun gcd(polynomial:/g' "$file"
    sed -i '' 's/^    fun gcd():/    override fun gcd():/g' "$file"
    sed -i '' 's/^    fun monomialGcd():/    override fun monomialGcd():/g' "$file"
    sed -i '' 's/^    fun genericValue():/    override fun genericValue():/g' "$file"
    sed -i '' 's/^    fun elements():/    override fun elements():/g' "$file"
    sed -i '' 's/^    fun compareTo(polynomial:/    override fun compareTo(polynomial:/g' "$file"

    # Fix newInstance to have override in subclasses
    if [[ "$file" == *"Boolean"* ]] || [[ "$file" == *"Integer"* ]] || [[ "$file" == *"Modular"* ]] || [[ "$file" == *"Rational"* ]]; then
        sed -i '' 's/^    fun newInstance(/    override fun newInstance(/g' "$file"
    fi

    echo "Fixed $file"
done

echo "All files fixed!"
