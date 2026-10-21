"use client";

import { useEffect, useRef, useState } from "react";

const W = 640;
const H = 320;
const SPAN = 4 * Math.PI; // the app's default window: 4π across

function path(f: (x: number) => number) {
  const unit = W / SPAN;
  let d = "";
  for (let px = 0; px <= W; px += 3) {
    const x = (px - W / 2) / unit;
    const y = H / 2 - f(x) * unit;
    d += `${px === 0 ? "M" : "L"}${px.toFixed(1)},${y.toFixed(1)}`;
  }
  return d;
}

/** The graph's sliders, as in the app: drag a or b, or press play to watch a sweep. */
export function GraphDemo() {
  const [a, setA] = useState(1.5);
  const [b, setB] = useState(2);
  const [playing, setPlaying] = useState(false);
  const dir = useRef(1);

  useEffect(() => {
    if (!playing) return;
    let last = performance.now();
    let frame = requestAnimationFrame(function tick(now) {
      const dt = (now - last) / 1000;
      last = now;
      setA((v) => {
        let next = v + dir.current * 1.5 * dt;
        if (next > 3) { next = 3; dir.current = -1; }
        if (next < -3) { next = -3; dir.current = 1; }
        return next;
      });
      frame = requestAnimationFrame(tick);
    });
    return () => cancelAnimationFrame(frame);
  }, [playing]);

  const unit = W / SPAN;
  const ticks = [-2, -1, 1, 2].map((k) => k * Math.PI);

  return (
    <div className="rounded-[32px] bg-surface p-3">
      <svg viewBox={`0 0 ${W} ${H}`} className="w-full rounded-[24px] bg-background" role="img" aria-label={`Graph of sin(x) and ${a.toFixed(1)}·sin(${b.toFixed(1)}x)`}>
        {ticks.map((t) => (
          <line key={t} x1={W / 2 + t * unit} x2={W / 2 + t * unit} y1={0} y2={H} className="stroke-outline" strokeWidth={1} />
        ))}
        {[-2, -1, 1, 2].map((k) => (
          <line key={k} y1={H / 2 - k * unit} y2={H / 2 - k * unit} x1={0} x2={W} className="stroke-outline" strokeWidth={1} />
        ))}
        <line x1={0} x2={W} y1={H / 2} y2={H / 2} className="stroke-muted" strokeWidth={1.2} />
        <line y1={0} y2={H} x1={W / 2} x2={W / 2} className="stroke-muted" strokeWidth={1.2} />
        <path d={path(Math.sin)} fill="none" className="stroke-primary" strokeWidth={3} strokeLinecap="round" />
        <path d={path((x) => a * Math.sin(b * x))} fill="none" className="stroke-curve-2" strokeWidth={3} strokeLinecap="round" />
      </svg>
      <div className="space-y-1 px-2 pt-3 text-lg">
        <Legend color="bg-primary">y = sin(x)</Legend>
        <Legend color="bg-curve-2">
          y = <b className="font-medium text-primary">a</b>×sin(<b className="font-medium text-primary">b</b>×x)
        </Legend>
        <Slider name="a" value={a} min={-3} max={3} onChange={(v) => { setPlaying(false); setA(v); }} />
        <Slider name="b" value={b} min={0.2} max={4} onChange={setB} />
        <button
          type="button"
          onClick={() => setPlaying((p) => !p)}
          className="pressable mt-1 rounded-full bg-primary-soft px-4 py-2 text-sm font-medium text-on-primary-soft"
        >
          {playing ? "Pause" : "Play a"}
        </button>
      </div>
    </div>
  );
}

function Legend({ color, children }: { color: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center gap-3">
      <span className={`size-2.5 rounded-full ${color}`} />
      <span>{children}</span>
    </div>
  );
}

function Slider({ name, value, min, max, onChange }: { name: string; value: number; min: number; max: number; onChange: (v: number) => void }) {
  const fill = ((value - min) / (max - min)) * 100;
  return (
    <label className="flex items-center gap-3">
      <span className="flex size-9 items-center justify-center rounded-full bg-primary-soft font-medium text-on-primary-soft">{name}</span>
      <span className="w-14 tabular-nums">= {value.toFixed(1).replace("-", "−")}</span>
      <input
        type="range"
        className="slider flex-1"
        min={min}
        max={max}
        step={0.1}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        style={{ ["--fill" as string]: `${fill}%` }}
        aria-label={`Parameter ${name}`}
      />
    </label>
  );
}
