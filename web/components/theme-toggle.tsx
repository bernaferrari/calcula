"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";

/** Flips between light and dark from whatever is showing now, system included. */
export function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();
  return (
    <button
      type="button"
      onClick={() => setTheme(resolvedTheme === "dark" ? "light" : "dark")}
      className="pressable relative rounded-full p-2 text-muted hover:text-foreground"
      aria-label="Switch between light and dark"
    >
      <Sun className="size-5 dark:hidden" />
      <Moon className="hidden size-5 dark:block" />
    </button>
  );
}
