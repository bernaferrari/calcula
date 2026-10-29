/**
 * A small evaluator for the site's keypad demo, speaking the app's language: × ÷ − ^, √ ∛, x² x³
 * x! x⁻¹ %, sin cos tan (in degrees, the app's default) and their inverses, ln log abs, π e.
 * Open parentheses close themselves at the end, as in the app. Returns null while incomplete.
 */
export function evaluate(text: string): number | null {
  if (!text.trim()) return null;
  try {
    const parser = new Parser(text);
    const value = parser.expression();
    parser.end();
    return Number.isFinite(value) ? value : null;
  } catch {
    return null;
  }
}

/** Up to 12 significant digits, without trailing zeros or float noise. */
export function format(value: number): string {
  if (Object.is(value, -0)) value = 0;
  const rounded = Number.parseFloat(value.toPrecision(12));
  const text = Math.abs(rounded) >= 1e12 || (rounded !== 0 && Math.abs(rounded) < 1e-9) ? rounded.toExponential(6) : String(rounded);
  return text.replace("e", "E").replace("-", "−");
}

const FUNCTIONS = ["sin⁻¹", "cos⁻¹", "tan⁻¹", "sin", "cos", "tan", "log", "abs", "ln"] as const;
const RAD = Math.PI / 180;

class Parser {
  private i = 0;
  private percent = false;
  constructor(private readonly s: string) {}

  end() {
    // Unclosed parentheses are fine; anything else left over is not.
    while (this.peek() === ")") this.i++;
    if (this.i < this.s.length) throw new Error("trailing");
  }

  expression(): number {
    let v = this.term();
    for (;;) {
      if (this.eat("+")) { const r = this.term(); v += this.percent ? v * r : r; }
      else if (this.eat("−")) { const r = this.term(); v -= this.percent ? v * r : r; }
      else return v;
    }
  }

  private term(): number {
    let v = this.unary();
    for (;;) {
      if (this.eat("×")) { v *= this.unary(); this.percent = false; }
      else if (this.eat("÷")) { v /= this.unary(); this.percent = false; }
      // 2π, 3(4), 2sin(30): side by side means multiply.
      else if (this.startsPrimary()) { v *= this.power(); this.percent = false; }
      else return v;
    }
  }

  private unary(): number {
    if (this.eat("−")) return -this.unary();
    return this.power();
  }

  private power(): number {
    const base = this.postfix();
    if (this.eat("^")) return base ** this.unary();
    return base;
  }

  private postfix(): number {
    let percent = false;
    let v = this.primary();
    for (;;) {
      if (this.eat("²")) v = v ** 2;
      else if (this.eat("³")) v = v ** 3;
      else if (this.eat("⁻¹")) v = 1 / v;
      else if (this.eat("%")) { v /= 100; percent = true; }
      else if (this.eat("!")) v = factorial(v);
      else { this.percent = percent; return v; }
    }
  }

  private primary(): number {
    if (this.eat("(")) return this.group();
    if (this.eat("π")) return Math.PI;
    if (this.eat("√")) return Math.sqrt(this.postfix());
    if (this.eat("∛")) return Math.cbrt(this.postfix());
    for (const name of FUNCTIONS) {
      if (this.s.startsWith(name + "(", this.i)) {
        this.i += name.length + 1;
        return apply(name, this.group());
      }
    }
    if (this.eat("e")) return Math.E;
    const match = /^(\d+\.?\d*|\.\d+)(?:E[+−-]?\d+)?/.exec(this.s.slice(this.i));
    if (!match) throw new Error("expected a number");
    this.i += match[0].length;
    return Number.parseFloat(match[0].replace("−", "-"));
  }

  /** The inside of a parenthesis; its `)` may still be missing at the end. */
  private group(): number {
    const v = this.expression();
    if (!this.eat(")") && this.i < this.s.length) throw new Error("expected )");
    return v;
  }

  private startsPrimary() {
    const c = this.peek();
    return c != null && /[0-9.(π√∛e]/.test(c) || FUNCTIONS.some((f) => this.s.startsWith(f + "(", this.i));
  }

  private peek() {
    return this.s[this.i];
  }

  private eat(token: string) {
    if (!this.s.startsWith(token, this.i)) return false;
    this.i += token.length;
    return true;
  }
}

function apply(name: (typeof FUNCTIONS)[number], x: number): number {
  switch (name) {
    case "sin": return Math.sin(x * RAD);
    case "cos": return Math.cos(x * RAD);
    case "tan": return Math.abs(Math.cos(x * RAD)) < 1e-15 ? Number.NaN : Math.tan(x * RAD);
    case "sin⁻¹": return Math.asin(x) / RAD;
    case "cos⁻¹": return Math.acos(x) / RAD;
    case "tan⁻¹": return Math.atan(x) / RAD;
    case "ln": return Math.log(x);
    case "log": return Math.log10(x);
    case "abs": return Math.abs(x);
  }
}

function factorial(n: number): number {
  if (!Number.isInteger(n) || n < 0 || n > 170) return Number.NaN;
  let r = 1;
  for (let k = 2; k <= n; k++) r *= k;
  return r;
}
