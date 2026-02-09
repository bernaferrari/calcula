"use client";

import { motion } from "framer-motion";
import { Github, Sparkles, Clock, Bug, Smartphone } from "lucide-react";
import { Button } from "@/components/ui/button";

export function Hero() {
  return (
    <section className="relative hero-gradient overflow-hidden">
      {/* Subtle grid background */}
      <div className="absolute inset-0 grid-background opacity-50" />

      <div className="relative max-w-6xl mx-auto px-6 pt-32 pb-24">
        <div className="text-center">
          {/* Badges */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="flex flex-wrap items-center justify-center gap-3 mb-8"
          >
            <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-border bg-card text-sm text-muted-foreground">
              <Clock size={14} className="text-orange-500" />
              13+ Years of Development
            </span>
            <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-primary/30 bg-primary/10 text-sm text-primary badge-glow">
              <Sparkles size={14} />
              Now with Kotlin Multiplatform
            </span>
          </motion.div>

          {/* Headline */}
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight mb-6 leading-[1.1]"
          >
            The Scientific Calculator,{" "}
            <span className="text-gradient">Reimagined</span>
          </motion.h1>

          {/* Description */}
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="text-lg md:text-xl text-muted-foreground max-w-3xl mx-auto mb-6 leading-relaxed"
          >
            A legendary open-source calculator with over a decade of refinement,
            now modernized with{" "}
            <span className="text-foreground font-medium">Jetpack Compose</span>{" "}
            and{" "}
            <span className="text-foreground font-medium">
              Kotlin Multiplatform
            </span>
            . Plot functions, render LaTeX, switch themes, and experience
            scientific computing the way it should be.
          </motion.p>

          {/* Stats */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.25 }}
            className="flex flex-wrap items-center justify-center gap-6 mb-10 text-sm"
          >
            <div className="flex items-center gap-2 text-muted-foreground">
              <Bug size={16} className="text-emerald-500" />
              <span>
                <span className="text-foreground font-semibold">100+</span> bugs
                fixed from original
              </span>
            </div>
            <div className="flex items-center gap-2 text-muted-foreground">
              <Smartphone size={16} className="text-blue-500" />
              <span>
                <span className="text-foreground font-semibold">Android</span> &{" "}
                <span className="text-foreground font-semibold">iOS</span>{" "}
                support
              </span>
            </div>
          </motion.div>

          {/* CTAs */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-4"
          >
            <Button size="lg" className="min-w-[200px]" asChild>
              <a
                href="https://play.google.com/store/apps/details?id=org.solovyev.android.calculator"
                target="_blank"
                rel="noopener noreferrer"
              >
                <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
                  <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 0 1-.61-.92V2.734a1 1 0 0 1 .609-.92zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.198l2.807 1.626a1 1 0 0 1 0 1.73l-2.808 1.626L15.206 12l2.492-2.491zM5.864 2.658L16.8 8.99l-2.302 2.302-8.634-8.634z" />
                </svg>
                Get on Google Play
              </a>
            </Button>
            <Button variant="outline" size="lg" className="min-w-[200px]" asChild>
              <a
                href="https://github.com/nicolarevelant/android-calculatorpp"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Github className="w-5 h-5" />
                View on GitHub
              </a>
            </Button>
          </motion.div>

          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="text-sm text-muted-foreground mb-16"
          >
            Free & Open Source under Apache 2.0
          </motion.p>
        </div>

        {/* Hero Visual - Calculator Preview */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.5 }}
          className="relative max-w-4xl mx-auto"
        >
          {/* Decorative elements */}
          <div className="absolute -top-8 -left-8 w-32 h-32 bg-gradient-to-br from-orange-500/20 to-amber-500/10 rounded-full blur-3xl" />
          <div className="absolute -bottom-8 -right-8 w-40 h-40 bg-gradient-to-br from-blue-500/15 to-purple-500/10 rounded-full blur-3xl" />

          {/* Main screenshot container */}
          <div className="relative screenshot-container">
            <div className="screenshot-inner bg-gradient-to-br from-slate-900 to-slate-800">
              {/* Calculator mockup UI */}
              <div className="aspect-[16/10] flex items-center justify-center p-8">
                <div className="w-full max-w-2xl">
                  {/* Display area */}
                  <div className="bg-slate-950/50 rounded-xl p-6 mb-6 border border-white/5">
                    <div className="text-right">
                      <div className="text-slate-400 text-lg mb-2 font-mono">
                        sin(45°) + cos(30°)
                      </div>
                      <div className="text-4xl md:text-5xl font-bold text-white font-mono">
                        1.573576...
                      </div>
                    </div>
                  </div>

                  {/* Calculator buttons grid */}
                  <div className="grid grid-cols-5 gap-2">
                    {/* Scientific functions row */}
                    {["sin", "cos", "tan", "ln", "log"].map((btn) => (
                      <div
                        key={btn}
                        className="bg-slate-700/50 hover:bg-slate-600/50 rounded-lg p-3 text-center text-slate-300 text-sm font-medium transition-colors border border-white/5"
                      >
                        {btn}
                      </div>
                    ))}
                    {/* More functions */}
                    {["x²", "√", "π", "e", "("].map((btn) => (
                      <div
                        key={btn}
                        className="bg-slate-700/50 hover:bg-slate-600/50 rounded-lg p-3 text-center text-slate-300 text-sm font-medium transition-colors border border-white/5"
                      >
                        {btn}
                      </div>
                    ))}
                    {/* Numbers row 1 */}
                    {["7", "8", "9", "÷", ")"].map((btn) => (
                      <div
                        key={btn}
                        className={`rounded-lg p-3 text-center text-sm font-medium transition-colors border border-white/5 ${
                          btn === "÷"
                            ? "bg-orange-500/20 text-orange-400 hover:bg-orange-500/30"
                            : "bg-slate-800/50 hover:bg-slate-700/50 text-white"
                        }`}
                      >
                        {btn}
                      </div>
                    ))}
                    {/* Numbers row 2 */}
                    {["4", "5", "6", "×", "^"].map((btn) => (
                      <div
                        key={btn}
                        className={`rounded-lg p-3 text-center text-sm font-medium transition-colors border border-white/5 ${
                          btn === "×"
                            ? "bg-orange-500/20 text-orange-400 hover:bg-orange-500/30"
                            : "bg-slate-800/50 hover:bg-slate-700/50 text-white"
                        }`}
                      >
                        {btn}
                      </div>
                    ))}
                    {/* Numbers row 3 */}
                    {["1", "2", "3", "−", "!"].map((btn) => (
                      <div
                        key={btn}
                        className={`rounded-lg p-3 text-center text-sm font-medium transition-colors border border-white/5 ${
                          btn === "−"
                            ? "bg-orange-500/20 text-orange-400 hover:bg-orange-500/30"
                            : "bg-slate-800/50 hover:bg-slate-700/50 text-white"
                        }`}
                      >
                        {btn}
                      </div>
                    ))}
                    {/* Bottom row */}
                    {["0", ".", "=", "+", "C"].map((btn) => (
                      <div
                        key={btn}
                        className={`rounded-lg p-3 text-center text-sm font-medium transition-colors border border-white/5 ${
                          btn === "="
                            ? "bg-orange-500 text-white hover:bg-orange-600"
                            : btn === "+" || btn === "C"
                              ? "bg-orange-500/20 text-orange-400 hover:bg-orange-500/30"
                              : "bg-slate-800/50 hover:bg-slate-700/50 text-white"
                        }`}
                      >
                        {btn}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Feature callouts */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.8 }}
            className="absolute -left-4 top-1/4 hidden lg:block"
          >
            <div className="glass px-4 py-3 text-sm">
              <div className="font-medium mb-1">LaTeX Output</div>
              <div className="text-muted-foreground text-xs">
                Beautiful math rendering
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.9 }}
            className="absolute -right-4 top-1/3 hidden lg:block"
          >
            <div className="glass px-4 py-3 text-sm">
              <div className="font-medium mb-1">Function Plotting</div>
              <div className="text-muted-foreground text-xs">
                Visualize equations instantly
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 1 }}
            className="absolute -bottom-4 left-1/4 hidden lg:block"
          >
            <div className="glass px-4 py-3 text-sm">
              <div className="font-medium mb-1">Multiple Themes</div>
              <div className="text-muted-foreground text-xs">
                Light, dark & Material You
              </div>
            </div>
          </motion.div>
        </motion.div>
      </div>
    </section>
  );
}
