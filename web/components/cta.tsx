"use client";

import { motion } from "framer-motion";
import { Github, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";

export function CTA() {
  return (
    <section className="py-24 px-6 border-t border-border relative overflow-hidden">
      {/* Background decoration */}
      <div className="absolute inset-0 hero-gradient opacity-50" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-br from-orange-500/10 to-amber-500/5 rounded-full blur-3xl" />

      <div className="relative max-w-4xl mx-auto text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
        >
          <span className="inline-block text-sm font-medium text-primary mb-4">
            Ready to Calculate?
          </span>
          <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold mb-6">
            Experience the <span className="text-gradient">Best Calculator</span>{" "}
            Today
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto mb-10">
            Join thousands of users who&apos;ve upgraded from their stock
            calculator. Free, open source, and constantly improving.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Button size="lg" className="min-w-[220px] group" asChild>
              <a
                href="https://play.google.com/store/apps/details?id=org.solovyev.android.calculator"
                target="_blank"
                rel="noopener noreferrer"
              >
                <svg
                  viewBox="0 0 24 24"
                  className="w-5 h-5"
                  fill="currentColor"
                >
                  <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 0 1-.61-.92V2.734a1 1 0 0 1 .609-.92zm10.89 10.893l2.302 2.302-10.937 6.333 8.635-8.635zm3.199-3.198l2.807 1.626a1 1 0 0 1 0 1.73l-2.808 1.626L15.206 12l2.492-2.491zM5.864 2.658L16.8 8.99l-2.302 2.302-8.634-8.634z" />
                </svg>
                Download for Android
                <ArrowRight
                  size={16}
                  className="group-hover:translate-x-1 transition-transform"
                />
              </a>
            </Button>
            <Button variant="outline" size="lg" className="min-w-[220px]" asChild>
              <a
                href="https://github.com/nicolarevelant/android-calculatorpp"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Github className="w-5 h-5" />
                Star on GitHub
              </a>
            </Button>
          </div>

          <p className="text-sm text-muted-foreground mt-6">
            iOS version coming soon via Kotlin Multiplatform
          </p>
        </motion.div>
      </div>
    </section>
  );
}
