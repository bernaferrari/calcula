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
  node.textContent = 'Calcula';
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
  // The graph paper runs across the store strips' seams.
  continuous: ['phone', 'tablet7', 'tablet10'],

  // Paper: a faint graph-paper grid in panorama coordinates, so the cards still join when shown together.
  art({W, H, index, slide}) {
    if (slide.identity) {
      // The README header: plain honey, and each phone stands on one of Calcula's keys, its function written at the
      // top of the cap like every key in the app: f(x) over the graph, sin over the keypad, °F over the converter.
      const caps = [['f(x)', 225, 400, 650], ['sin', 860, 280, 680], ['°F', 1525, 400, 650]].map(([label, x, y, w]) =>
        `<rect x="${x}" y="${y + 16}" width="${w}" height="${H}" rx="96" fill="#9a7410" opacity=".16"/>` +
        `<rect x="${x}" y="${y}" width="${w}" height="${H}" rx="96" fill="url(#cap)"/>` +
        `<text x="${x + w / 2}" y="${y + 58}" font-size="40" font-family="Calcula" font-weight="600" fill="#6e7fc8" text-anchor="middle">${label}</text>`).join('');
      return `<defs><linearGradient id="field" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fffae4"/><stop offset=".55" stop-color="#fbecb0"/><stop offset="1" stop-color="#f2d878"/></linearGradient>` +
        `<linearGradient id="cap" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fffaf0"/><stop offset=".5" stop-color="#f6ead6"/></linearGradient></defs>` +
        `<rect width="${W}" height="${H}" fill="url(#field)"/>${caps}`;
    }
    const cell = Math.round(Math.min(W, H) / 18), major = cell * 5, x0 = -index * W, off = ((x0 % major) + major) % major;
    const gap = slide.layout === 'big-screen-feature'
      ? `<rect x="1180" width="40" height="${H}" fill="#FBF5EA"/><rect x="1180" width="40" height="${H}" fill="url(#gap-minor)"/><rect x="1180" width="40" height="${H}" fill="url(#gap-major)"/>`
      : '';
    return `<defs><pattern id="minor" width="${cell}" height="${cell}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${cell} 0V${cell}H0" fill="none" stroke="#22307f" stroke-opacity=".05" stroke-width="1.5"/></pattern>` +
      `<pattern id="major" width="${major}" height="${major}" patternUnits="userSpaceOnUse" x="${off}"><path d="M${major} 0V${major}H0" fill="none" stroke="#22307f" stroke-opacity=".09" stroke-width="2"/></pattern>` +
      `<pattern id="gap-minor" width="${cell}" height="${cell}" patternUnits="userSpaceOnUse"><path d="M0 ${cell}H${cell}" fill="none" stroke="#22307f" stroke-opacity=".05" stroke-width="1.5"/></pattern>` +
      `<pattern id="gap-major" width="${major}" height="${major}" patternUnits="userSpaceOnUse"><path d="M0 ${major}H${major}" fill="none" stroke="#22307f" stroke-opacity=".09" stroke-width="2"/></pattern></defs>` +
      `<rect width="${W}" height="${H}" fill="url(#minor)"/><rect width="${W}" height="${H}" fill="url(#major)"/>${gap}`;
  },

  compose({kind, index, slide, main}) {
    if (slide.layout === 'big-screen-feature') {
      main.insertAdjacentHTML('beforeend', `<section class="big-card history-card"><div class="big-title">More room<br>to think.</div><div class="big-caption">History beside your work.</div></section><section class="big-card graph-card"><div class="big-title">See the<br>bigger picture.</div><div class="big-caption">More space to explore a graph.</div></section>`);
      for (const name of ['history', 'graph']) {
        const card = main.querySelector(`.${name}-card`);
        card.insertAdjacentHTML('beforeend', `<svg class="big-wave" viewBox="0 0 2400 1260" aria-hidden="true"><path d="M0 850 C220 850 330 615 600 670 S985 955 1210 850 S1530 610 1760 670 S2140 935 2400 790 V1260 H0 Z"/></svg>`);
        card.append(main.querySelector(`.device.${name}`));
      }
      return;
    }
    if (slide.layout === 'customize-feature') {
      main.insertAdjacentHTML('beforeend', `<section class="custom-card appearance-card"><div class="custom-title">A look that<br>feels like you.</div><div class="custom-caption">Colors, themes and styles.</div></section><section class="custom-card shortcuts-card"><div class="custom-title">Your keys.<br>Your rules.</div><div class="custom-caption">Your favorites, a flick away.</div></section>`);
      for (const name of ['appearance', 'shortcuts']) {
        const card = main.querySelector(`.${name}-card`);
        card.insertAdjacentHTML('beforeend', `<svg class="custom-wave" viewBox="0 0 2400 1260" aria-hidden="true"><path d="M0 820 C240 820 350 600 620 650 S980 940 1210 850 S1530 590 1760 650 S2140 935 2400 790 V1260 H0 Z"/></svg>`);
        card.append(main.querySelector(`.device.${name}`));
      }
      return;
    }
    if (slide.identity) return;
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
      main.insertAdjacentHTML('beforeend', '<img class="app-icon" src="../brand/icon.svg" alt="Calcula app icon">');
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
  }
};
