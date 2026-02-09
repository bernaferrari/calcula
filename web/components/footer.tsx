import { Calculator, Github, Heart } from "lucide-react";
import Link from "next/link";

export function Footer() {
  return (
    <footer className="border-t border-border py-12 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row justify-between items-start gap-8 mb-8">
          {/* Brand */}
          <div className="max-w-xs">
            <Link
              href="/"
              className="flex items-center gap-2 font-semibold text-lg mb-3"
            >
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-orange-500 to-amber-500 flex items-center justify-center text-white">
                <Calculator size={18} strokeWidth={2} />
              </div>
              <span>Calculator++</span>
            </Link>
            <p className="text-sm text-muted-foreground">
              The scientific calculator, reimagined. 13+ years of open source
              excellence, now modernized with Kotlin Multiplatform.
            </p>
          </div>

          {/* Links */}
          <div className="flex flex-wrap gap-12">
            <div>
              <h4 className="font-medium mb-3">Product</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>
                  <a
                    href="#features"
                    className="hover:text-foreground transition-colors"
                  >
                    Features
                  </a>
                </li>
                <li>
                  <a
                    href="#comparison"
                    className="hover:text-foreground transition-colors"
                  >
                    Comparison
                  </a>
                </li>
                <li>
                  <a
                    href="https://play.google.com/store/apps/details?id=org.solovyev.android.calculator"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-foreground transition-colors"
                  >
                    Google Play
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="font-medium mb-3">Resources</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>
                  <a
                    href="https://github.com/nicolarevelant/android-calculatorpp"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-foreground transition-colors"
                  >
                    GitHub
                  </a>
                </li>
                <li>
                  <a
                    href="https://github.com/nicolarevelant/android-calculatorpp/issues"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-foreground transition-colors"
                  >
                    Report Issue
                  </a>
                </li>
                <li>
                  <a
                    href="https://github.com/nicolarevelant/android-calculatorpp/releases"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-foreground transition-colors"
                  >
                    Releases
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="font-medium mb-3">Legal</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>
                  <a
                    href="https://github.com/nicolarevelant/android-calculatorpp/blob/master/LICENSE"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-foreground transition-colors"
                  >
                    Apache 2.0 License
                  </a>
                </li>
                <li>
                  <span className="text-muted-foreground/60">
                    No tracking. No ads.
                  </span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="pt-8 border-t border-border flex flex-col sm:flex-row justify-between items-center gap-4">
          <p className="text-sm text-muted-foreground">
            © {new Date().getFullYear()} Calculator++. Open source under Apache
            2.0.
          </p>
          <p className="text-sm text-muted-foreground flex items-center gap-1">
            Made with <Heart size={14} className="text-rose-500 fill-rose-500" />{" "}
            by the community
          </p>
        </div>
      </div>
    </footer>
  );
}
