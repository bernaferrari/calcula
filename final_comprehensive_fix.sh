#!/bin/bash

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

    # Fix map._size to map.size
    sed -i '' 's/map\._size/map.size/g' "$file"

    # Fix TreeMap._size to TreeMap.size
    sed -i '' 's/TreeMap\._size/TreeMap.size/g' "$file"

    # Remove override from gcd, monomialGcd, genericValue, elements, compareTo, coefficient
    sed -i '' 's/override fun gcd(polynomial:/fun gcd(polynomial:/g' "$file"
    sed -i '' 's/override fun gcd():/fun gcd():/g' "$file"
    sed -i '' 's/override fun monomialGcd():/fun monomialGcd():/g' "$file"
    sed -i '' 's/override fun genericValue():/fun genericValue():/g' "$file"
    sed -i '' 's/override fun elements():/fun elements():/g' "$file"
    sed -i '' 's/override fun compareTo(polynomial:/fun compareTo(polynomial:/g' "$file"
    sed -i '' 's/override fun coefficient(generic:/fun coefficient(generic:/g' "$file"

    # Fix _degree = degree(p) to p._degree = degree(p)
    sed -i '' 's/_degree = degree(p)/p._degree = degree(p)/g' "$file"

    # Make newInstance open in Generic and override in subclasses
    if [[ "$file" == *"Generic.kt"* ]]; then
        sed -i '' 's/fun newInstance(/open fun newInstance(/g' "$file"
    fi

    if [[ "$file" == *"Boolean"* ]] || [[ "$file" == *"Integer"* ]] || [[ "$file" == *"Modular"* ]] || [[ "$file" == *"Rational"* ]]; then
        sed -i '' 's/override fun newInstance(/override fun newInstance(/g' "$file"
        # Add size() and degree() overrides if missing
        if ! grep -q "override fun size():" "$file"; then
            # Find line with "constructor(monomialFactory" and add after the closing }
            # This is complex, will handle it separately
            echo "Note: $file may need size() and degree() overrides added manually"
        fi
    fi

    # Fix coefFactory!! to coefFactory
    sed -i '' 's/coefFactory!!/coefFactory!!/g' "$file"

    echo "Fixed $file"
done

echo "All files fixed! Check for any remaining manual fixes needed."
