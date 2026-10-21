/** Calcula's mark, as on the app icon: a lowercase c between the two flick dots every key carries. */
export function Mark({ className = "", tile = false }: { className?: string; tile?: boolean }) {
  return (
    <svg viewBox="26 26 56 56" className={className} aria-hidden="true">
      {tile && <rect x="26" y="26" width="56" height="56" rx="19" className="fill-primary" />}
      <path
        d="M61.78,46.22 A11,11 0 1,0 61.78,61.78"
        fill="none"
        strokeWidth="6.5"
        strokeLinecap="round"
        className={tile ? "stroke-on-primary" : "stroke-current"}
      />
      <circle cx="54" cy="34.5" r="2.6" className={tile ? "fill-on-primary" : "fill-current"} />
      <circle cx="54" cy="73.5" r="2.6" className={tile ? "fill-on-primary" : "fill-current"} />
    </svg>
  );
}

/** GitHub's mark; the icon library no longer ships brand logos. */
export function GitHubIcon({ className = "" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" className={className} aria-hidden="true" fill="currentColor">
      <path d="M12 .5a11.5 11.5 0 0 0-3.64 22.41c.58.1.79-.25.79-.56v-2c-3.2.7-3.88-1.37-3.88-1.37-.52-1.33-1.28-1.69-1.28-1.69-1.05-.71.08-.7.08-.7 1.16.08 1.77 1.19 1.77 1.19 1.03 1.77 2.7 1.26 3.36.96.1-.75.4-1.26.73-1.55-2.55-.29-5.24-1.28-5.24-5.69 0-1.26.45-2.29 1.19-3.1-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.17 1.18a11 11 0 0 1 5.77 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.81 1.19 1.84 1.19 3.1 0 4.42-2.7 5.39-5.26 5.68.41.36.78 1.06.78 2.14v3.17c0 .31.21.67.8.56A11.5 11.5 0 0 0 12 .5Z" />
    </svg>
  );
}
