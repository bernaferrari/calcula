import type { Metadata } from "next";
import { Google_Sans_Flex } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "@/components/theme-provider";

// The app's own typeface, so the site reads like the calculator.
const googleSansFlex = Google_Sans_Flex({
  subsets: ["latin"],
  variable: "--font-google-sans-flex",
  display: "swap",
});

const description =
  "A calm, exact calculator for Android, iOS and the web. Every key hides two more functions: flick up or down to reach them. Graphs, a unit converter and a history you can reuse.";

export const metadata: Metadata = {
  title: "Calcula: Flick a key. Get a function.",
  description,
  metadataBase: new URL("https://bernaferrari.github.io"),
  keywords: ["calculator", "scientific calculator", "graphing calculator", "unit converter", "android", "ios", "open source"],
  authors: [{ name: "Bernardo Ferrari" }],
  openGraph: {
    type: "website",
    url: "https://bernaferrari.github.io/android-calculatorpp/",
    title: "Calcula: Flick a key. Get a function.",
    description,
  },
  twitter: {
    card: "summary",
    title: "Calcula: Flick a key. Get a function.",
    description,
  },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${googleSansFlex.variable} font-sans antialiased min-h-screen`}>
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}
