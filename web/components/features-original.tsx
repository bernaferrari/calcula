"use client";

import { motion } from "framer-motion";
import {
  Binary,
  LineChart,
  Palette,
  Calculator,
  History,
  ArrowUpDown,
  Sigma,
  Sparkles,
  Layers,
  Variable,
  Compass,
  Infinity,
} from "lucide-react";

const features = [
  {
    icon: Sigma,
    title: "Scientific Functions",
    description:
      "Full suite of trigonometric, logarithmic, and exponential functions. sin, cos, tan, ln, log, and more.",
    iconClass: "icon-orange",
  },
  {
    icon: LineChart,
    title: "Function Plotting",
    description:
      "Visualize your equations instantly. Swipe down on equals to plot any function with real and imaginary support.",
    iconClass: "icon-blue",
  },
  {
    icon: Sparkles,
    title: "LaTeX Rendering",
    description:
      "Beautiful mathematical notation output. Perfect for students, engineers, and academics.",
    iconClass: "icon-purple",
  },
  {
    icon: Palette,
    title: "Multiple Themes",
    description:
      "Material You, dark mode, light mode, and Metro-inspired themes. Pick your style.",
    iconClass: "icon-amber",
  },
  {
    icon: Binary,
    title: "Base Conversion",
    description:
      "Switch between binary, octal, decimal, and hexadecimal. Perfect for developers.",
    iconClass: "icon-green",
  },
  {
    icon: Layers,
    title: "3 Calculator Modes",
    description:
      "Simple for basics, Engineer for power users, Modern for gesture-based interaction.",
    iconClass: "icon-rose",
  },
  {
    icon: ArrowUpDown,
    title: "Unit Converter",
    description:
      "Convert between length, mass, speed, temperature, pressure, energy, and more.",
    iconClass: "icon-blue",
  },
  {
    icon: History,
    title: "Calculation History",
    description:
      "Never lose your work. Browse, search, and reuse past calculations.",
    iconClass: "icon-orange",
  },
  {
    icon: Variable,
    title: "Custom Variables",
    description:
      "Define and reuse your own variables. Built-in constants like π, e, and i included.",
    iconClass: "icon-purple",
  },
  {
    icon: Calculator,
    title: "Expression Simplify",
    description:
      "Algebraic simplification engine. Reduce complex expressions to their simplest form.",
    iconClass: "icon-green",
  },
  {
    icon: Compass,
    title: "Angle Units",
    description:
      "Degrees, radians, gradians, or turns. Work in whatever unit you prefer.",
    iconClass: "icon-amber",
  },
  {
    icon: Infinity,
    title: "Complex Numbers",
    description:
      "Full imaginary number support. Calculate and plot with complex numbers naturally.",
    iconClass: "icon-rose",
  },
];

export function FeaturesOriginal() {
  return (
    <section id="features" className="py-24 px-6 border-t border-border">
      <div className="max-w-6xl mx-auto">
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <span className="inline-block text-sm font-medium text-primary mb-4">
            Powerful Features
          </span>
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Everything You Need to{" "}
            <span className="text-gradient">Calculate</span>
          </h2>
          <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
            13 years of refinement packed into one app. From simple arithmetic
            to complex scientific computing, we&apos;ve got you covered.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.4, delay: index * 0.03 }}
              className="glass feature-card p-5"
            >
              <div
                className={`w-10 h-10 rounded-lg flex items-center justify-center mb-4 ${feature.iconClass}`}
              >
                <feature.icon size={20} strokeWidth={1.5} />
              </div>
              <h3 className="font-semibold mb-2">{feature.title}</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                {feature.description}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
