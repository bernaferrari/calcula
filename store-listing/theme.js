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
  // Paper: a faint graph-paper grid in panorama coordinates, so the cards still join when shown together.
  art({W, H, index}) {
    const cell = Math.round(Math.min(W, H) / 18), major = cell * 5, x0 = -index * W, off = ((x0 % major) + major) % major;
    return `<defs><pattern id="minor" width="${cell}" height="${cell}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${cell} 0V${cell}H0" fill="none" stroke="#22307f" stroke-opacity=".05" stroke-width="1.5"/></pattern>` +
      `<pattern id="major" width="${major}" height="${major}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${major} 0V${major}H0" fill="none" stroke="#22307f" stroke-opacity=".09" stroke-width="2"/></pattern></defs>` +
      `<rect width="${W}" height="${H}" fill="url(#minor)"/><rect width="${W}" height="${H}" fill="url(#major)"/>`;
  },

  compose({kind, index, slide, main}) {
    if (slide.layout === 'customize-feature') {
      main.insertAdjacentHTML('beforeend', `<section class="custom-card appearance-card"><div class="custom-title">A look that<br>feels like you.</div><div class="custom-caption">Colors, themes and styles.</div></section><section class="custom-card shortcuts-card"><div class="custom-title">Your keys.<br>Your rules.</div><div class="custom-caption">Your favorites, a flick away.</div></section>`);
      for (const name of ['appearance', 'shortcuts']) {
        const card = main.querySelector(`.${name}-card`);
        card.insertAdjacentHTML('beforeend', `<svg class="custom-wave" viewBox="0 0 2400 1260" aria-hidden="true"><path d="M0 820 C240 820 350 600 620 650 S980 940 1210 850 S1530 590 1760 650 S2140 935 2400 790 V1260 H0 Z"/></svg>`);
        card.append(main.querySelector(`.device.${name}`));
      }
      return;
    }
    if (slide.layout === 'tools') {
      main.insertAdjacentHTML('beforeend', `<section class="tools-feature graph-feature"><div class="tools-title">Watch it<br>take shape.</div><div class="tools-caption">Plot anything. Drag a parameter.<br>See it move.</div></section><section class="tools-feature convert-feature"><div class="tools-title">From °C<br>to °F.</div><div class="tools-caption">Units, converted<br>as you type.</div></section><section class="tools-feature programmer-feature"><div class="tools-title">Hex,<br>sorted.</div><div class="tools-caption">Programmer mode with<br>bits, shifts and bases.</div></section>`);
      for (const name of ['graph', 'convert', 'programmer']) {
        const card = main.querySelector(`.${name}-feature`);
        card.insertAdjacentHTML('beforeend', `<svg class="tools-wave" viewBox="0 0 2400 1260" aria-hidden="true"><path d="M0 760 C200 600 400 600 600 760 S1000 920 1200 760 S1600 600 1800 760 S2200 920 2400 760 V1260 H0 Z"/></svg>`);
        card.append(main.querySelector(`.device.${name}`));
      }
      return;
    }
    if (kind === 'icon') {
      main.insertAdjacentHTML('beforeend', '<img class="app-icon" src="../../branding/calcula-icon.svg" alt="Calcula app icon">');
      return;
    }
    if (!slide.title) return;
    // The app name opens the set; later cards carry only their headline.
    if (index === 0) brand(main);
    if (slide.key) key(main, slide.key);
    // One giant, faint math glyph per card: a different symbol each time, like a page turning.
    const block = document.createElement('div');
    block.className = 'block';
    block.setAttribute('aria-hidden', 'true');
    main.append(block);
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
      if (main.classList.contains('compare')) break;
      const image = node.querySelector('img'), width = node.clientWidth, landscape = image.naturalWidth > image.naturalHeight;
      node.style.setProperty('--bezel', `${Math.round(width * (landscape ? .011 : .022))}px`);
      node.style.setProperty('--radius', `${width * (landscape ? .03 : .075)}px`);
    }
  },

  // App frames keep their real status bar and sides inside the export. They may run off the
  // bottom edge, but most of each view stays visible.
  decorate({W, H, slide, devices}) {
    if (slide.layout === 'compare') {
      // Each name sits just under its screen.
      for (const label of devices[0].node.parentElement.querySelectorAll('.device-label')) label.style.setProperty('--h', `${devices[0].box.height}px`);
      return;
    }
    for (const {box, spec} of devices) {
      if (spec.frame === 'none') continue;
      if (/stack/.test(slide.layout || '')) continue;
      const bleeds = /zoomed/.test(slide.layout || '');
      if (box.left < 12 || box.top < 12 || (!bleeds && box.left + box.width > W - 12)) throw new Error(`App capture extends outside the export: ${spec.raw}`);
      if (!/zoomed/.test(slide.layout || '') && box.top + box.height > H - 12 && (H - box.top) / box.height < .7) throw new Error(`App capture runs too far off the bottom: ${spec.raw}`);
    }
  }
};
