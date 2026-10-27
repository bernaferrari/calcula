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
  // A crisp cream disc sits on every seam, half on each card, in panorama coordinates: one shape
  // shared by two neighbours, so the strip reads as continuous. Heights and sizes alternate.
  art({W, H, index, slides}) {
    const portrait = H > W;
    let out = '';
    for (const seam of [index, index + 1]) {
      if (seam <= 0 || seam >= slides.length) continue;
      const cx = (seam - index) * W;
      const cy = H * (seam % 2 ? (portrait ? .86 : .9) : (portrait ? .7 : .78));
      const r = (portrait ? W : H) * (seam % 2 ? .34 : .26);
      out += `<circle cx="${cx}" cy="${cy}" r="${r}" fill="#FFFBF2"/>`;
    }
    return out;
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
