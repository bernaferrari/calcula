import type { Metadata } from "next";
import { Outfit } from "next/font/google";
import "./globals.css";
import { cn } from "@/lib/utils";
import { ThemeProvider } from "@/components/theme-provider";

const outfit = Outfit({
  subsets: ["latin"],
  variable: "--font-outfit",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Calculator++ | The Scientific Calculator, Reimagined",
  description:
    "A modern Kotlin Multiplatform port of the legendary 13-year-old open source calculator. LaTeX rendering, function plotting, multiple themes, and powerful scientific computing — all in one beautiful app.",
  metadataBase: new URL("https://bernaferrari.github.io"),
  keywords: [
    "calculator",
    "scientific calculator",
    "android calculator",
    "ios calculator",
    "kotlin multiplatform",
    "open source",
    "latex",
    "graphing calculator",
    "math",
    "engineering calculator",
  ],
  authors: [{ name: "Bernardo Ferrari" }],
  openGraph: {
    type: "website",
    url: "https://bernaferrari.github.io/android-calculatorpp/",
    title: "Calculator++ | Scientific Calculator Reimagined",
    description:
      "13 years of open source excellence, now modernized with Kotlin Multiplatform. LaTeX, plotting, themes, and more.",
    images: [{ url: "/android-calculatorpp/og-image.png" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "Calculator++ | Scientific Calculator Reimagined",
    description:
      "13 years of open source excellence, now modernized with Kotlin Multiplatform.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={cn(outfit.variable, "antialiased min-h-screen")}>
        <ThemeProvider
          attribute="class"
          defaultTheme="dark"
          enableSystem
          disableTransitionOnChange
        >
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}
