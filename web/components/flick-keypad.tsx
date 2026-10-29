"use client";

import { useRef, useState } from "react";
import { evaluate, format } from "@/lib/calc";

type Alt = { label: string; insert: string };
type Spec = {
  label: string;
  insert: string;
  tone: "digit" | "fn" | "clear" | "op" | "eq";
  up?: Alt;
  down?: Alt;
};

// The app's keypad and its flicks, key for key.
const ROWS: Spec[][] = [
  [
    { label: "C", insert: "AC", tone: "clear" },
    { label: "%", insert: "%", tone: "fn" },
    { label: "⌫", insert: "⌫", tone: "fn" },
    { label: "÷", insert: "÷", tone: "op", up: { label: "1/x", insert: "⁻¹" } },
  ],
  [
    { label: "7", insert: "7", tone: "digit", up: { label: "sin", insert: "sin(" }, down: { label: "sin⁻¹", insert: "sin⁻¹(" } },
    { label: "8", insert: "8", tone: "digit", up: { label: "cos", insert: "cos(" }, down: { label: "cos⁻¹", insert: "cos⁻¹(" } },
    { label: "9", insert: "9", tone: "digit", up: { label: "tan", insert: "tan(" }, down: { label: "tan⁻¹", insert: "tan⁻¹(" } },
    { label: "×", insert: "×", tone: "op", up: { label: "xʸ", insert: "^" }, down: { label: "ʸ√x", insert: "^(1÷" } },
  ],
  [
    { label: "4", insert: "4", tone: "digit", up: { label: "ln", insert: "ln(" }, down: { label: "eˣ", insert: "e^(" } },
    { label: "5", insert: "5", tone: "digit", up: { label: "log", insert: "log(" }, down: { label: "10ˣ", insert: "10^(" } },
    { label: "6", insert: "6", tone: "digit", up: { label: "π", insert: "π" }, down: { label: "e", insert: "e" } },
    { label: "−", insert: "−", tone: "op" },
  ],
  [
    { label: "1", insert: "1", tone: "digit", up: { label: "x!", insert: "!" }, down: { label: "|x|", insert: "abs(" } },
    { label: "2", insert: "2", tone: "digit", up: { label: "x²", insert: "²" }, down: { label: "√", insert: "√" } },
    { label: "3", insert: "3", tone: "digit", up: { label: "x³", insert: "³" }, down: { label: "∛", insert: "∛" } },
    { label: "+", insert: "+", tone: "op" },
  ],
  [
    { label: "( )", insert: "()", tone: "fn", up: { label: "(", insert: "(" }, down: { label: ")", insert: ")" } },
    { label: "0", insert: "0", tone: "digit", up: { label: "00", insert: "00" }, down: { label: "000", insert: "000" } },
    { label: ".", insert: ".", tone: "digit" },
    { label: "=", insert: "=", tone: "eq" },
  ],
];

const OPERATORS = "+−×÷^";
const POSTFIX = "!²³%";

/** Keeps the expression sensible the way the app does: one operator at a time, postfixes after operands. */
function apply(text: string, insert: string): string {
  const last = text.at(-1) ?? "";
  const operand = /[0-9.)πe!²³%¹]/.test(last);
  if (insert === "AC") return "";
  if (insert === "⌫") {
    const fn = text.match(/(sin⁻¹|cos⁻¹|tan⁻¹|sin|cos|tan|log|ln|abs)\($/);
    if (fn) return text.slice(0, -fn[0].length);
    if (text.endsWith("⁻¹")) return text.slice(0, -2);
    return text.slice(0, -1);
  }
  if (insert === "()") {
    const open = (text.match(/\(/g) ?? []).length - (text.match(/\)/g) ?? []).length;
    return text + (open > 0 && operand ? ")" : "(");
  }
  if (OPERATORS.includes(insert)) {
    if (!text) return insert === "−" ? "−" : text;
    if (OPERATORS.includes(last)) return text.slice(0, -1) + insert;
    return text + insert;
  }
  if (POSTFIX.includes(insert) || insert === "⁻¹" || insert === "^(1÷") return operand ? text + insert : text;
  return text + insert;
}

/** True when a flick would do nothing (1/x with nothing to invert): the app dims those. */
function inert(text: string, insert: string) {
  return apply(text, insert) === text;
}

export function FlickKeypad() {
  const [text, setText] = useState("");
  const [shown, setShown] = useState<string | null>(null);
  const [hint, setHint] = useState(true);
  const value = evaluate(text);

  const press = (insert: string) => {
    setHint(false);
    if (insert === "=") {
      if (value != null) {
        setShown(text);
        setText(format(value));
      }
      return;
    }
    setShown(null);
    setText((t) => apply(shown && /^[0-9.(πe√∛]|^(sin|cos|tan|log|ln|abs)/.test(insert) ? "" : t, insert));
  };

  return (
    <div className="w-full max-w-[380px] select-none rounded-[36px] bg-surface p-3 shadow-[0_1px_0_var(--outline),0_30px_80px_-30px_color-mix(in_srgb,var(--primary)_45%,transparent)]">
      <div className="flex h-36 flex-col items-end justify-end gap-1 overflow-hidden px-4 pb-3" aria-live="polite">
        <div className="h-6 truncate text-lg text-muted">{shown ? `${shown} =` : ""}</div>
        <div className="max-w-full truncate text-5xl tracking-tight">
          {text || <span className="text-muted/50">0</span>}
        </div>
        <div className="h-7 text-2xl text-muted">{!shown && value != null && text && format(value) !== text ? `= ${format(value)}` : ""}</div>
      </div>
      <div className="grid grid-cols-4 gap-1.5">
        {ROWS.flat().map((spec) => (
          <Key key={spec.label} spec={spec} onPress={press} isInert={(i) => inert(text, i)} />
        ))}
      </div>
      <p className={`px-2 pt-3 text-center text-sm text-muted transition-opacity duration-300 ${hint ? "opacity-100" : "opacity-0"}`}>
        Flick a key up or down. Try the 7.
      </p>
    </div>
  );
}

function Key({ spec, onPress, isInert }: { spec: Spec; onPress: (insert: string) => void; isInert: (insert: string) => boolean }) {
  const start = useRef<number | null>(null);
  const hold = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [pick, setPick] = useState<"up" | "down" | null>(null);
  const [open, setOpen] = useState(false);
  const [pressed, setPressed] = useState(false);
  const flicks = spec.up || spec.down;

  const reset = () => {
    start.current = null;
    if (hold.current) clearTimeout(hold.current);
    setPick(null);
    setOpen(false);
    setPressed(false);
  };

  const tone = {
    digit: "bg-key text-foreground",
    fn: "bg-surface-high text-muted",
    // Like the app: C is marked by its ink, the operators carry the family's secondary tint.
    clear: "bg-surface-high text-tertiary",
    op: "bg-key-op text-on-primary-soft",
    eq: "bg-primary text-on-primary",
  }[spec.tone];

  return (
    <button
      type="button"
      aria-label={spec.label + (spec.up ? `, flick up for ${spec.up.label}` : "") + (spec.down ? `, flick down for ${spec.down.label}` : "")}
      className={`relative h-[62px] touch-none rounded-[22px] text-[26px] outline-none transition-[transform,border-radius] duration-200 ease-[var(--ease-out)] focus-visible:ring-2 focus-visible:ring-primary ${tone} ${pressed ? "scale-[0.96] rounded-[31px]" : ""}`}
      onPointerDown={(e) => {
        e.currentTarget.setPointerCapture(e.pointerId);
        start.current = e.clientY;
        setPressed(true);
        if (flicks) hold.current = setTimeout(() => setOpen(true), 380);
      }}
      onPointerMove={(e) => {
        if (start.current == null || !flicks) return;
        const dy = e.clientY - start.current;
        const next = dy < -14 && spec.up ? "up" : dy > 14 && spec.down ? "down" : null;
        if (next !== pick) setPick(next);
      }}
      onPointerUp={() => {
        if (pick === "up" && spec.up && !isInert(spec.up.insert)) onPress(spec.up.insert);
        else if (pick === "down" && spec.down && !isInert(spec.down.insert)) onPress(spec.down.insert);
        else if (!pick && !open) onPress(spec.insert);
        reset();
      }}
      onPointerCancel={reset}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onPress(spec.insert);
        }
      }}
    >
      <span className={`transition-opacity duration-150 ${pick ? "opacity-30" : ""}`}>{spec.label}</span>
      {flicks && (
        <>
          {spec.up && <span className="absolute left-1/2 top-[5px] size-[3.5px] -translate-x-1/2 rounded-full bg-current opacity-35" />}
          {spec.down && <span className="absolute bottom-[5px] left-1/2 size-[3.5px] -translate-x-1/2 rounded-full bg-current opacity-35" />}
          {spec.up && <Pill label={spec.up.label} show={open || pick != null} picked={pick === "up"} dim={isInert(spec.up.insert)} side="up" />}
          {spec.down && <Pill label={spec.down.label} show={open || pick != null} picked={pick === "down"} dim={isInert(spec.down.insert)} side="down" />}
        </>
      )}
    </button>
  );
}

/** The function waiting above or below the key; the one the finger heads to fills with the accent. */
function Pill({ label, show, picked, dim, side }: { label: string; show: boolean; picked: boolean; dim: boolean; side: "up" | "down" }) {
  return (
    <span
      className={`pointer-events-none absolute left-1/2 z-10 flex h-11 min-w-16 items-center justify-center rounded-full px-3 text-[19px] shadow-lg transition-[opacity,transform,background-color] duration-200 ease-[var(--ease-out)] ${
        side === "up" ? "bottom-[calc(100%+6px)] origin-bottom" : "top-[calc(100%+6px)] origin-top"
      } -translate-x-1/2 ${!show ? "scale-90 opacity-0" : picked && !dim ? "scale-110 opacity-100" : dim ? "scale-100 opacity-50" : "scale-100 opacity-100"} ${
        // Waiting options read like tooltips, as in the app; the one the finger heads to takes the accent.
        picked && !dim ? "bg-primary text-on-primary" : "bg-foreground text-background"
      }`}
    >
      {label}
    </span>
  );
}
