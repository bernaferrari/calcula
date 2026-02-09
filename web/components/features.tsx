"use client";

import { motion } from "framer-motion";
import { useState } from "react";
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
  ChevronRight,
} from "lucide-react";

const categories = [
  { id: "math", label: "Math" },
  { id: "visual", label: "Visual" },
  { id: "tools", label: "Tools" },
];

const features = [
  {
    icon: Sigma,
    title: "Scientific",
    short: "sin, cos, tan, ln, log & more",
    category: "math",
    gradient: "from-orange-500 to-amber-500",
  },
  {
    icon: LineChart,
    title: "Plotting",
    short: "Visualize any function",
    category: "visual",
    gradient: "from-blue-500 to-cyan-500",
  },
  {
    icon: Sparkles,
    title: "LaTeX",
    short: "Beautiful math output",
    category: "visual",
    gradient: "from-purple-500 to-pink-500",
  },
  {
    icon: Palette,
    title: "Themes",
    short: "Material You & more",
    category: "visual",
    gradient: "from-amber-500 to-yellow-500",
  },
  {
    icon: Binary,
    title: "Base Conv.",
    short: "Bin, Oct, Dec, Hex",
    category: "tools",
    gradient: "from-emerald-500 to-teal-500",
  },
  {
    icon: Layers,
    title: "3 Modes",
    short: "Simple to Engineer",
    category: "tools",
    gradient: "from-rose-500 to-pink-500",
  },
  {
    icon: ArrowUpDown,
    title: "Units",
    short: "Length, mass, temp...",
    category: "tools",
    gradient: "from-blue-500 to-indigo-500",
  },
  {
    icon: History,
    title: "History",
    short: "Never lose calculations",
    category: "tools",
    gradient: "from-orange-500 to-red-500",
  },
  {
    icon: Variable,
    title: "Variables",
    short: "Custom + π, e, i",
    category: "math",
    gradient: "from-violet-500 to-purple-500",
  },
  {
    icon: Calculator,
    title: "Simplify",
    short: "Algebraic reduction",
    category: "math",
    gradient: "from-green-500 to-emerald-500",
  },
  {
    icon: Compass,
    title: "Angles",
    short: "Deg, rad, grad, turns",
    category: "math",
    gradient: "from-amber-500 to-orange-500",
  },
  {
    icon: Infinity,
    title: "Complex",
    short: "Imaginary numbers",
    category: "math",
    gradient: "from-rose-500 to-red-500",
  },
];

export function Features() {
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  const filteredFeatures = activeCategory
    ? features.filter((f) => f.category === activeCategory)
    : features;

  return (
    <section id="features" className="py-20 px-6 border-t border-border">
      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="text-center mb-10"
        >
          <h2 className="text-3xl md:text-4xl font-bold mb-3">
            Packed with <span className="text-gradient">Power</span>
          </h2>
          <p className="text-muted-foreground max-w-lg mx-auto">
            13 years of features. Hover to explore.
          </p>
        </motion.div>

        {/* Category Filter */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="flex justify-center gap-2 mb-8"
        >
          <button
            onClick={() => setActiveCategory(null)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
              activeCategory === null
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground hover:text-foreground"
            }`}
          >
            All
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => setActiveCategory(cat.id)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
                activeCategory === cat.id
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground hover:text-foreground"
              }`}
            >
              {cat.label}
            </button>
          ))}
        </motion.div>

        {/* Features Grid - Compact */}
        <motion.div
          layout
          className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3"
        >
          {filteredFeatures.map((feature, index) => (
            <motion.div
              key={feature.title}
              layout
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              transition={{ duration: 0.2, delay: index * 0.02 }}
              onMouseEnter={() => setHoveredIndex(index)}
              onMouseLeave={() => setHoveredIndex(null)}
              className="group relative"
            >
              <div
                className={`glass p-4 h-full cursor-pointer transition-all duration-300 ${
                  hoveredIndex === index ? "scale-105 z-10" : ""
                }`}
              >
                {/* Icon with gradient background on hover */}
                <div
                  className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 transition-all duration-300 ${
                    hoveredIndex === index
                      ? `bg-gradient-to-br ${feature.gradient} text-white shadow-lg`
                      : "bg-muted text-muted-foreground"
                  }`}
                >
                  <feature.icon size={20} strokeWidth={1.5} />
                </div>

                {/* Title */}
                <h3 className="font-semibold text-sm mb-1">{feature.title}</h3>

                {/* Short description - shows on hover */}
                <p
                  className={`text-xs text-muted-foreground transition-all duration-300 ${
                    hoveredIndex === index
                      ? "opacity-100 max-h-20"
                      : "opacity-0 max-h-0 overflow-hidden sm:opacity-60 sm:max-h-20"
                  }`}
                >
                  {feature.short}
                </p>
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Interactive Demo Hint */}
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          transition={{ delay: 0.3 }}
          className="mt-10 text-center"
        >
          <a
            href="#comparison"
            className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-primary transition-colors group"
          >
            See how we compare
            <ChevronRight
              size={16}
              className="group-hover:translate-x-1 transition-transform"
            />
          </a>
        </motion.div>
      </div>
    </section>
  );
}
