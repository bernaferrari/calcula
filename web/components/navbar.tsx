"use client";

import { Github, Calculator } from "lucide-react";
import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import Link from "next/link";
import { ThemeToggle } from "@/components/theme-toggle";

export function Navbar() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <nav
      className={cn(
        "fixed top-0 left-0 right-0 z-50 transition-all duration-300 border-b border-transparent",
        scrolled && "bg-background/80 backdrop-blur-md border-border"
      )}
    >
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2 font-semibold text-lg">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-orange-500 to-amber-500 flex items-center justify-center text-white">
            <Calculator size={18} strokeWidth={2} />
          </div>
          <span>Calculator++</span>
        </Link>

        <div className="flex items-center gap-5">
          <a
            href="#features"
            className="hidden sm:block text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Features
          </a>
          <a
            href="#comparison"
            className="hidden sm:block text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Compare
          </a>
          <div className="flex items-center gap-1">
            <a
              href="https://github.com/nicolarevelant/android-calculatorpp"
              target="_blank"
              rel="noopener noreferrer"
              className="p-2 text-muted-foreground hover:text-foreground transition-colors"
              aria-label="View on GitHub"
            >
              <Github size={18} />
            </a>
            <ThemeToggle />
          </div>
        </div>
      </div>
    </nav>
  );
}
