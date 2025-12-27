package jscl.math.function

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotIntegrableException
import jscl.math.Variable
import jscl.mathml.MathML
import jscl.util.ArrayComparator

class ImplicitFunction(
    name: String,
    parameter: Array<Generic>?,
    private val derivations: IntArray,
    private val subscripts: Array<Generic>
) : Function(name, parameter) {

    override fun antiDerivative(n: Int): Generic {
        val c = IntArray(derivations.size)
        for (i in c.indices) {
            if (i == n) {
                if (derivations[i] > 0) c[i] = derivations[i] - 1
                else throw NotIntegrableException(this)
            } else c[i] = derivations[i]
        }
        return ImplicitFunction(name, parameters, c, subscripts).selfExpand()
    }

    override fun derivative(n: Int): Generic {
        val c = IntArray(derivations.size)
        for (i in c.indices) {
            if (i == n) c[i] = derivations[i] + 1
            else c[i] = derivations[i]
        }
        return ImplicitFunction(name, parameters, c, subscripts).selfExpand()
    }

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return expressionValue()
    }

    override fun selfSimplify(): Generic {
        return expressionValue()
    }

    /*override fun numeric(): Generic {
        return evaluateNumerically()
    }*/

    override fun selfNumeric(): Generic {
        throw ArithmeticException()
        /*Function function = FunctionsRegistry.getInstance().get(this.name);

          Generic result;
          if ( function == null ) {
              throw new ArithmeticException();
          } else {
              function.setParameters(this.parameters);

              result = function;
              for (int derivation : derivations) {
                  for ( int i = 0; i < derivation; i++ ) {
                      result = result.derivative(derivation);
                  }
              }

              result = result.numeric();
          }

          return result;*/
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0
        var c = comparator.compare(this, variable)
        if (c < 0) return -1
        else if (c > 0) return 1
        else {
            val v = variable as ImplicitFunction
            c = name.compareTo(v.name)
            if (c < 0) return -1
            else if (c > 0) return 1
            else {
                @Suppress("UNCHECKED_CAST")
                c = ArrayComparator.comparator.compare(subscripts as Array<Comparable<*>?>, v.subscripts as Array<Comparable<*>?>)
                if (c < 0) return -1
                else if (c > 0) return 1
                else {
                    c = compareDerivation(derivations, v.derivations)
                    if (c < 0) return -1
                    else if (c > 0) return 1
                    else {
                        @Suppress("UNCHECKED_CAST")
                        return ArrayComparator.comparator.compare(parameters as Array<Comparable<*>?>, v.parameters as Array<Comparable<*>?>)
                    }
                }
            }
        }
    }

    override fun toString(): String {
        val result = StringBuilder()

        var n = 0
        for (derivation in derivations) {
            n += derivation
        }

        result.append(name)

        for (aSubscript in subscripts) {
            result.append("[").append(aSubscript).append("]")
        }

        if (n == 0) {
            // do nothing
        } else if (parameters!!.size == 1 && n <= Constant.PRIME_CHARS) {
            result.append(Constant.primeChars(n))
        } else {
            result.append(derivationToString())
        }

        result.append("(")

        for (i in parameters!!.indices) {
            result.append(parameters!![i]).append(if (i < parameters!!.size - 1) ", " else "")
        }

        result.append(")")

        return result.toString()
    }

    private fun derivationToString(): String {
        val buffer = StringBuilder()
        buffer.append("{")
        for (i in derivations.indices) {
            buffer.append(derivations[i]).append(if (i < derivations.size - 1) ", " else "")
        }
        buffer.append("}")
        return buffer.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()

        var n = 0
        for (derivation in derivations) {
            n += derivation
        }

        result.append(name)
        if (n == 0) {
            // do nothing
        } else if (parameters!!.size == 1 && n <= Constant.PRIME_CHARS) {
            result.append(Constant.underscores(n))
        } else {
            result.append(derivationToJava())
        }

        result.append("(")
        for (i in parameters!!.indices) {
            result.append(parameters!![i].toJava()).append(if (i < parameters!!.size - 1) ", " else "")
        }
        result.append(")")

        for (subscript in subscripts) {
            result.append("[").append(subscript.integerValue().toInt()).append("]")
        }

        return result.toString()
    }

    private fun derivationToJava(): String {
        val buffer = StringBuilder()
        for (i in derivations.indices) {
            buffer.append("_").append(derivations[i])
        }
        return buffer.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        var e1: MathML
        val exponent = if (data is Int) data else 1
        if (exponent == 1) bodyToMathML(element)
        else {
            e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        e1 = element.element("mfenced")
        for (parameter in parameters!!) {
            parameter.toMathML(e1, null)
        }
        element.appendChild(e1)
    }

    private fun bodyToMathML(element: MathML) {
        var n = 0
        for (derivation in derivations) {
            n += derivation
        }
        var e1: MathML

        if (subscripts.isEmpty()) {
            if (n == 0) {
                nameToMathML(element)
            } else {
                e1 = element.element("msup")
                nameToMathML(e1)
                derivationToMathML(e1, n)
                element.appendChild(e1)
            }
        } else {
            if (n == 0) {
                e1 = element.element("msub")
                nameToMathML(e1)
                val e2 = element.element("mrow")
                for (subscript in subscripts) {
                    subscript.toMathML(e2, null)
                }
                e1.appendChild(e2)
                element.appendChild(e1)
            } else {
                e1 = element.element("msubsup")
                nameToMathML(e1)
                val e2 = element.element("mrow")
                for (subscript in subscripts) {
                    subscript.toMathML(e2, null)
                }
                e1.appendChild(e2)
                derivationToMathML(e1, n)
                element.appendChild(e1)
            }
        }
    }

    private fun derivationToMathML(element: MathML, n: Int) {
        if (parameters!!.size == 1 && n <= Constant.PRIME_CHARS) {
            Constant.primeCharsToMathML(element, n)
        } else {
            val e1 = element.element("mfenced")
            for (derivation in derivations) {
                val e2 = element.element("mn")
                e2.appendChild(element.text(derivation.toString()))
                e1.appendChild(e2)
            }
            element.appendChild(e1)
        }
    }

    override fun newInstance(): Variable {
        return ImplicitFunction(name, Array(parameters!!.size) { JsclInteger.valueOf(0) }, derivations, subscripts)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }

    companion object {
        @JvmStatic
        fun compareDerivation(c1: IntArray, c2: IntArray): Int {
            val n = c1.size
            for (i in n - 1 downTo 0) {
                if (c1[i] < c2[i]) return -1
                else if (c1[i] > c2[i]) return 1
            }
            return 0
        }
    }
}
