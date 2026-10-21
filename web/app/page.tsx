import { ArrowUpRight, ChartSpline, Download, History, Ruler, ShieldCheck, Sigma, SquareFunction, TextCursorInput } from "lucide-react";
import { FlickKeypad } from "@/components/flick-keypad";
import { GraphDemo } from "@/components/graph-demo";
import { GitHubIcon, Mark } from "@/components/mark";
import { ThemeToggle } from "@/components/theme-toggle";

const REPO = "https://github.com/bernaferrari/android-calculatorpp";
const RELEASES = `${REPO}/releases`;

const FEATURES = [
  {
    icon: Sigma,
    title: "Exact by default",
    body: "Decimal math to 40 significant digits. 0.1 + 0.2 is 0.3, the way it should be.",
  },
  {
    icon: History,
    title: "A history you can reuse",
    body: "Results stay on a tape above the keys. Tap one to use it again; pull down, or swipe two fingers, for all of it.",
  },
  {
    icon: SquareFunction,
    title: "Functions, one flick away",
    body: "sin, log, √, x², π and more live on the digits. Or switch to ƒ and the keys turn into them, dimming the ones that wouldn't do anything yet.",
  },
  {
    icon: ChartSpline,
    title: "Graphs that explain themselves",
    body: "Up to six functions, sliders for a, b, c, d, roots and turns you can tap. Recent graphs are kept, and any one shares as an image.",
  },
  {
    icon: Ruler,
    title: "A converter, too",
    body: "Length, mass, temperature, data and more, with exact factors, one tap from the keypad.",
  },
  {
    icon: TextCursorInput,
    title: "Edits like text",
    body: "Double-tap to select, then wrap it in parentheses or √, or copy, cut and paste like anywhere else.",
  },
];

export default function Home() {
  return (
    <>
      <header className="sticky top-0 z-20 border-b border-transparent bg-background/80 backdrop-blur-md">
        <nav className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-5">
          <a href="#" className="flex items-center gap-2.5 text-xl font-semibold tracking-tight">
            <Mark tile className="size-8" />
            Calcula
          </a>
          <div className="ml-auto hidden items-center gap-6 text-[15px] text-muted sm:flex">
            <a href="#features" className="hover:text-foreground">Features</a>
            <a href="#graph" className="hover:text-foreground">Graph</a>
            <a href="#story" className="hover:text-foreground">Story</a>
          </div>
          <div className="ml-auto flex items-center gap-1 sm:ml-0">
            <ThemeToggle />
            <a href={REPO} aria-label="Source on GitHub" className="pressable rounded-full p-2 text-muted hover:text-foreground">
              <GitHubIcon className="size-5" />
            </a>
          </div>
        </nav>
      </header>

      <main>
        {/* Hero: the idea, and the keypad to try it on. */}
        <section className="mx-auto grid max-w-6xl items-center gap-14 px-5 pb-24 pt-14 md:grid-cols-[1.1fr_1fr] md:pt-24">
          <div>
            <p className="mb-5 inline-flex rounded-full bg-primary-soft px-3.5 py-1.5 text-sm font-medium text-on-primary-soft">
              Calculator for Android, iOS and the web
            </p>
            <h1 className="text-[clamp(2.75rem,7vw,5rem)] font-semibold leading-[0.98] tracking-[-0.035em]">
              Flick a key.
              <br />
              <span className="text-primary">Get a function.</span>
            </h1>
            <p className="mt-6 max-w-[34rem] text-xl leading-relaxed text-muted">
              Calcula is a calm, exact calculator. Every key hides two more functions: flick it up or down to reach them. The keypad here is real, give it a try.
            </p>
            <div className="mt-9 flex flex-wrap gap-3">
              <a href={RELEASES} className="pressable inline-flex items-center gap-2 rounded-full bg-primary px-6 py-3.5 text-[17px] font-medium text-on-primary">
                <Download className="size-5" />
                Get Calcula
              </a>
              <a href={REPO} className="pressable inline-flex items-center gap-2 rounded-full bg-surface-high px-6 py-3.5 text-[17px] font-medium">
                <GitHubIcon className="size-5" />
                Source code
              </a>
            </div>
          </div>
          <div className="flex justify-center md:justify-end">
            <FlickKeypad />
          </div>
        </section>

        <section id="features" className="mx-auto max-w-6xl scroll-mt-20 px-5 pb-24">
          <h2 className="max-w-2xl text-[clamp(2rem,4.5vw,3.25rem)] font-semibold leading-tight tracking-[-0.03em]">
            Everything a calculator should do, and nothing in your way.
          </h2>
          <div className="mt-12 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map(({ icon: Icon, title, body }) => (
              <article key={title} className="rounded-[28px] bg-surface p-7">
                <span className="mb-5 flex size-12 items-center justify-center rounded-2xl bg-primary-soft text-on-primary-soft">
                  <Icon className="size-6" strokeWidth={1.8} />
                </span>
                <h3 className="text-xl font-semibold tracking-tight">{title}</h3>
                <p className="mt-2 text-[17px] leading-relaxed text-muted">{body}</p>
              </article>
            ))}
          </div>
        </section>

        <section id="graph" className="mx-auto grid max-w-6xl scroll-mt-20 items-center gap-12 px-5 pb-24 md:grid-cols-[1fr_1.15fr]">
          <div>
            <h2 className="text-[clamp(2rem,4.5vw,3.25rem)] font-semibold leading-tight tracking-[-0.03em]">
              Move a slider. Watch the curve.
            </h2>
            <p className="mt-5 max-w-md text-xl leading-relaxed text-muted">
              Type a function with the same keys, and every letter in it gets a slider. Drag one, or press play and see what it does. This one runs right here too.
            </p>
          </div>
          <GraphDemo />
        </section>

        <section className="mx-auto max-w-6xl px-5 pb-24">
          <div className="flex flex-col items-start gap-5 rounded-[32px] bg-primary px-8 py-10 text-on-primary sm:flex-row sm:items-center">
            <ShieldCheck className="size-10 shrink-0" strokeWidth={1.6} />
            <p className="text-2xl font-medium leading-snug tracking-tight">
              No ads. No accounts. No tracking. Your calculations never leave your device.
            </p>
          </div>
        </section>

        <section id="story" className="mx-auto max-w-3xl scroll-mt-20 px-5 pb-28">
          <h2 className="text-[clamp(2rem,4.5vw,3.25rem)] font-semibold leading-tight tracking-[-0.03em]">From Calculator++ to Calcula</h2>
          <div className="mt-6 space-y-5 text-xl leading-relaxed text-muted">
            <p>
              Calcula grows out of Calculator++, the swipe-key calculator Sergey Solovyev started in 2011. Its best idea, that a key can hold more than one thing, is still the heart of it.
            </p>
            <p>
              Everything else is rebuilt from scratch with Compose Multiplatform, so the same calculator runs on Android, iOS and the web, and it stays open source.
            </p>
          </div>
          <a href={REPO} className="mt-8 inline-flex items-center gap-1.5 text-lg font-medium text-primary hover:underline">
            Read the code on GitHub
            <ArrowUpRight className="size-5" />
          </a>
        </section>
      </main>

      <footer className="border-t border-outline">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-5 py-10 text-[15px] text-muted sm:flex-row sm:items-center">
          <span className="flex items-center gap-2.5 font-medium text-foreground">
            <Mark tile className="size-6" />
            Calcula
          </span>
          <span>By Bernardo Ferrari. Based on Calculator++ by Sergey Solovyev.</span>
          <a href={REPO} className="hover:text-foreground sm:ml-auto">GitHub</a>
        </div>
      </footer>
    </>
  );
}
