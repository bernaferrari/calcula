"use client";

import { motion } from "framer-motion";
import { Check, X, Zap, Heart, Shield, Code2 } from "lucide-react";

const comparisonData = [
  { feature: "Scientific Functions", us: true, stock: false, others: true },
  { feature: "Function Graphing", us: true, stock: false, others: false },
  { feature: "LaTeX Output", us: true, stock: false, others: false },
  { feature: "Multiple Themes", us: true, stock: false, others: true },
  { feature: "Complex Numbers", us: true, stock: false, others: true },
  { feature: "Expression Simplify", us: true, stock: false, others: false },
  { feature: "Unit Converter", us: true, stock: false, others: true },
  { feature: "Calculation History", us: true, stock: true, others: true },
  { feature: "Custom Variables", us: true, stock: false, others: false },
  { feature: "Base Conversion", us: true, stock: false, others: true },
  { feature: "Gesture Controls", us: true, stock: false, others: false },
  { feature: "Open Source", us: true, stock: false, others: false },
  { feature: "No Ads", us: true, stock: true, others: false },
  { feature: "Multiplatform", us: true, stock: false, others: false },
];

const highlights = [
  {
    icon: Zap,
    title: "Lightning Fast",
    description:
      "Native performance with Jetpack Compose. No WebView, no lag, just pure speed.",
    gradient: "from-amber-500 to-orange-500",
  },
  {
    icon: Heart,
    title: "Community Driven",
    description:
      "13+ years of community contributions. Built by users, for users.",
    gradient: "from-rose-500 to-pink-500",
  },
  {
    icon: Shield,
    title: "Privacy First",
    description:
      "No tracking, no analytics, no data collection. Your calculations stay yours.",
    gradient: "from-emerald-500 to-teal-500",
  },
  {
    icon: Code2,
    title: "Truly Open",
    description:
      "Apache 2.0 licensed. Fork it, modify it, learn from it, contribute to it.",
    gradient: "from-blue-500 to-indigo-500",
  },
];

export function Showcase() {
  return (
    <section id="comparison" className="py-24 px-6 border-t border-border">
      <div className="max-w-6xl mx-auto">
        {/* Why Calculator++ Section */}
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="text-center mb-16"
        >
          <span className="inline-block text-sm font-medium text-primary mb-4">
            Why Choose Us
          </span>
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Not Just Another <span className="text-gradient">Calculator</span>
          </h2>
          <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
            We&apos;ve spent over a decade perfecting the calculator experience.
            Here&apos;s why thousands of users trust Calculator++.
          </p>
        </motion.div>

        {/* Highlights Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-24">
          {highlights.map((item, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              className="glass p-6 text-center"
            >
              <div
                className={`w-12 h-12 rounded-xl bg-gradient-to-br ${item.gradient} flex items-center justify-center mx-auto mb-4 text-white`}
              >
                <item.icon size={24} strokeWidth={1.5} />
              </div>
              <h3 className="font-semibold mb-2">{item.title}</h3>
              <p className="text-sm text-muted-foreground">
                {item.description}
              </p>
            </motion.div>
          ))}
        </div>

        {/* Comparison Table */}
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="text-center mb-12"
        >
          <h3 className="text-2xl md:text-3xl font-bold mb-4">
            The Complete Package
          </h3>
          <p className="text-muted-foreground max-w-xl mx-auto">
            See how Calculator++ stacks up against the competition.
          </p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="glass overflow-hidden"
        >
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left p-4 font-medium text-muted-foreground">
                    Feature
                  </th>
                  <th className="p-4 text-center">
                    <div className="font-bold text-primary">Calculator++</div>
                    <div className="text-xs text-muted-foreground">
                      Open Source
                    </div>
                  </th>
                  <th className="p-4 text-center">
                    <div className="font-medium">Stock Calculator</div>
                    <div className="text-xs text-muted-foreground">
                      Android/iOS
                    </div>
                  </th>
                  <th className="p-4 text-center">
                    <div className="font-medium">Other Apps</div>
                    <div className="text-xs text-muted-foreground">
                      Paid / Ads
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody>
                {comparisonData.map((row, index) => (
                  <tr
                    key={index}
                    className="border-b border-border/50 last:border-0 hover:bg-muted/30 transition-colors"
                  >
                    <td className="p-4 text-sm">{row.feature}</td>
                    <td className="p-4 text-center">
                      {row.us ? (
                        <Check className="w-5 h-5 mx-auto comparison-check" />
                      ) : (
                        <X className="w-5 h-5 mx-auto comparison-x" />
                      )}
                    </td>
                    <td className="p-4 text-center">
                      {row.stock ? (
                        <Check className="w-5 h-5 mx-auto comparison-check" />
                      ) : (
                        <X className="w-5 h-5 mx-auto comparison-x" />
                      )}
                    </td>
                    <td className="p-4 text-center">
                      {row.others ? (
                        <Check className="w-5 h-5 mx-auto comparison-check" />
                      ) : (
                        <X className="w-5 h-5 mx-auto comparison-x" />
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </motion.div>

        {/* History Section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="mt-24 text-center"
        >
          <div className="glass p-8 md:p-12 max-w-4xl mx-auto">
            <h3 className="text-2xl md:text-3xl font-bold mb-6">
              A Legacy of <span className="text-gradient">Excellence</span>
            </h3>
            <div className="prose prose-lg dark:prose-invert mx-auto text-muted-foreground">
              <p className="mb-4">
                Calculator++ started in 2011 as a simple Android calculator.
                Over 13 years and thousands of commits later, it has evolved
                into a comprehensive scientific computing tool used by students,
                engineers, and professionals worldwide.
              </p>
              <p className="mb-4">
                The recent modernization effort brought the entire codebase to{" "}
                <span className="text-foreground font-medium">
                  Kotlin Multiplatform
                </span>{" "}
                with{" "}
                <span className="text-foreground font-medium">
                  Jetpack Compose
                </span>
                , fixing over 100 bugs from the original Java implementation
                while adding new features like function plotting and improved
                LaTeX rendering.
              </p>
              <p>
                Today, Calculator++ runs natively on Android and iOS, sharing
                95% of its codebase while delivering platform-native performance
                and user experience.
              </p>
            </div>

            <div className="flex flex-wrap justify-center gap-8 mt-8 pt-8 border-t border-border">
              <div className="text-center">
                <div className="text-3xl font-bold text-gradient">2011</div>
                <div className="text-sm text-muted-foreground">Founded</div>
              </div>
              <div className="text-center">
                <div className="text-3xl font-bold text-gradient">13+</div>
                <div className="text-sm text-muted-foreground">
                  Years Active
                </div>
              </div>
              <div className="text-center">
                <div className="text-3xl font-bold text-gradient">100+</div>
                <div className="text-sm text-muted-foreground">Bugs Fixed</div>
              </div>
              <div className="text-center">
                <div className="text-3xl font-bold text-gradient">KMP</div>
                <div className="text-sm text-muted-foreground">
                  Multiplatform
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
