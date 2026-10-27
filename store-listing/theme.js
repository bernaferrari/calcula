// Calcula: flat card colors that bleed into their neighbours, one giant math glyph per card, the redrawn sin key,
// and frames whose bezel and corners scale with the captured screen.

/**
 * The wave is drawn in panorama coordinates (all cards side by side) and each card shows its own
 * window, so the stroke meets itself exactly at every seam. The area under the curve is a shared
 * ground; the card colors sit above it. Units are canvas pixels.
 */
function brand(main) {
  const node = document.createElement('div');
  node.className = 'brand';
  node.dataset.checkBounds = '';
  node.innerHTML = '<img src="../../branding/calcula-icon.svg" alt="">';
  node.append(document.createTextNode('Calcula'));
  main.append(node);
}

/** The sin/7 key, redrawn as vector type with the app's font and the capture's sampled colors so it stays sharp when enlarged. */
function key(main, alt) {
  const node = document.createElement('figure');
  node.className = 'key';
  node.style.margin = '0';
  node.setAttribute('role', 'img');
  node.setAttribute('aria-label', alt);
  node.innerHTML = '<svg viewBox="0 0 244 226"><rect width="244" height="226" rx="40" fill="#F4E6D6"/><text x="122" y="33" font-size="30" fill="#8E8375" text-anchor="middle">sin</text><text x="122" y="142" font-size="86" fill="#3A3125" text-anchor="middle">7</text><circle cx="122" cy="203" r="4.5" fill="#8E8375"/></svg>';
  main.append(node);
}

export default {
  // One continuous field: a single gradient spans the whole strip with each card's colour pinned at
  // its centre, and every card shows its own window onto it, so there are no seams to hide.
  art({W, H, index, slides}) {
    const N = slides.length, x0 = -index * W;
    const stops = slides.map((s, k) => `<stop offset="${(k + .5) / N}" stop-color="${s.vars?.['--background'] || '#F1E267'}"/>`).join('');
    // Graph paper: a faint grid in panorama coordinates, so its lines continue across every seam.
    const cell = Math.round(Math.min(W, H) / 18), major = cell * 5, off = ((x0 % major) + major) % major;
    const grid = `<pattern id="minor" width="${cell}" height="${cell}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${cell} 0V${cell}H0" fill="none" stroke="#ffffff" stroke-opacity=".16" stroke-width="1.5"/></pattern>` +
      `<pattern id="major" width="${major}" height="${major}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${major} 0V${major}H0" fill="none" stroke="#ffffff" stroke-opacity=".3" stroke-width="2.5"/></pattern>`;
    return `<defs><linearGradient id="field" gradientUnits="userSpaceOnUse" x1="${x0}" x2="${x0 + N * W}" y1="0" y2="0">${stops}</linearGradient>${grid}</defs>` +
      `<rect width="${W}" height="${H}" fill="url(#field)"/><rect width="${W}" height="${H}" fill="url(#minor)"/><rect width="${W}" height="${H}" fill="url(#major)"/>`;
  },

  compose({kind, index, slide, main}) {
    if (kind === 'icon') {
      main.insertAdjacentHTML('beforeend', '<img class="app-icon" src="../../branding/calcula-icon.svg" alt="Calcula app icon">');
      return;
    }
    if (!slide.title) return;
    // The app name opens the set; later cards carry only their headline.
    if (index === 0) brand(main);
    if (slide.key) key(main, slide.key);
    // One giant, faint math glyph per card: a different symbol each time, like a page turning.
    if (slide.glyph && kind !== 'feature') {
      const glyph = document.createElement('div');
      glyph.className = 'glyph';
      glyph.setAttribute('aria-hidden', 'true');
      glyph.textContent = slide.glyph;
      main.append(glyph);
    }
  },

  /** Frames follow the captured pixels: the bezel and corner radius scale with the frame width. */
  layout({main}) {
    for (const node of main.querySelectorAll('.device:not(.device-frame-none)')) {
      const image = node.querySelector('img'), width = node.clientWidth, landscape = image.naturalWidth > image.naturalHeight;
      node.style.setProperty('--bezel', `${Math.round(width * (landscape ? .011 : .022))}px`);
      node.style.setProperty('--radius', `${width * (landscape ? .03 : .075)}px`);
    }
  },

  // App frames keep their real status bar and sides inside the export. They may run off the
  // bottom edge, but most of each view stays visible.
  decorate({W, H, slide, devices}) {
    for (const {box, spec} of devices) {
      if (spec.frame === 'none') continue;
      const bleeds = /zoomed/.test(slide.layout || '');
      if (box.left < 12 || box.top < 12 || (!bleeds && box.left + box.width > W - 12)) throw new Error(`App capture extends outside the export: ${spec.raw}`);
      if (!/zoomed/.test(slide.layout || '') && box.top + box.height > H - 12 && (H - box.top) / box.height < .7) throw new Error(`App capture runs too far off the bottom: ${spec.raw}`);
    }
  }
};
