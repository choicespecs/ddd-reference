(function () {
  const root = document.documentElement;

  // ---- theme ----
  function isDark() {
    const t = root.dataset.theme;
    return t ? t === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches;
  }
  function applyHl() {
    const dark = isDark();
    const l = document.getElementById('hl-light'), d = document.getElementById('hl-dark');
    if (l && d) { l.disabled = dark; d.disabled = !dark; }
  }
  applyHl();
  document.getElementById('theme')?.addEventListener('click', () => {
    root.dataset.theme = isDark() ? 'light' : 'dark';
    try { localStorage.setItem('theme', root.dataset.theme); } catch (e) {}
    applyHl();
    renderMermaid(true);
  });

  // ---- mobile menu ----
  const rail = document.getElementById('rail');
  document.getElementById('menu')?.addEventListener('click', () => rail.classList.toggle('open'));
  document.addEventListener('click', (e) => {
    if (rail.classList.contains('open') && !rail.contains(e.target) && e.target.id !== 'menu') rail.classList.remove('open');
  });

  // ---- syntax highlight + copy buttons ----
  if (window.hljs) document.querySelectorAll('.code pre code').forEach((el) => hljs.highlightElement(el));
  document.querySelectorAll('.code .copy').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const code = btn.closest('.code').querySelector('code').innerText;
      try { await navigator.clipboard.writeText(code); btn.textContent = 'Copied'; }
      catch (e) { btn.textContent = 'Select & copy'; }
      setTimeout(() => (btn.textContent = 'Copy'), 1500);
    });
  });

  // ---- mermaid ----
  // Disable auto-start immediately; we render manually once fonts are ready.
  if (window.mermaid) mermaid.initialize({ startOnLoad: false });
  const sources = new Map();
  document.querySelectorAll('pre.mermaid').forEach((el, i) => sources.set(el, el.textContent));
  function renderMermaid(rerender) {
    if (!window.mermaid) return;
    if (rerender) sources.forEach((src, el) => { el.removeAttribute('data-processed'); el.innerHTML = ''; el.textContent = src; });
    mermaid.initialize({ startOnLoad: false, theme: isDark() ? 'dark' : 'neutral', securityLevel: 'strict', fontFamily: 'Inter, sans-serif' });
    mermaid.run({ querySelector: 'pre.mermaid' });
  }
  // Wait for web fonts so Mermaid measures label widths with the real font (avoids clipped labels).
  (document.fonts && document.fonts.ready ? document.fonts.ready : Promise.resolve()).then(() => renderMermaid(false));

  // ---- on-page TOC with scroll spy ----
  const toc = document.getElementById('toc');
  const heads = [...document.querySelectorAll('article h2[id], article h3[id]')];
  if (toc) {
    heads.forEach((h) => {
      const li = document.createElement('li');
      li.className = h.tagName.toLowerCase();
      li.innerHTML = `<a href="#${h.id}">${h.textContent}</a>`;
      toc.appendChild(li);
    });
    const links = [...toc.querySelectorAll('a')];
    const spy = () => {
      let idx = 0;
      heads.forEach((h, i) => { if (h.getBoundingClientRect().top < 120) idx = i; });
      links.forEach((a, i) => a.classList.toggle('active', i === idx));
    };
    addEventListener('scroll', spy, { passive: true });
    spy();
  }

  // ---- heading anchors ----
  heads.forEach((h) => {
    h.style.cursor = 'pointer';
    h.addEventListener('click', () => history.replaceState(null, '', '#' + h.id));
  });

  // ---- search ----
  const input = document.getElementById('search');
  const results = document.getElementById('search-results');
  const index = window.SEARCH_INDEX || [];
  let sel = -1;
  function search(q) {
    q = q.trim().toLowerCase();
    if (!q) { results.hidden = true; return; }
    const terms = q.split(/\s+/);
    const hits = index
      .map((e) => {
        const hay = (e.t + ' ' + e.p).toLowerCase();
        if (!terms.every((t) => hay.includes(t))) return null;
        return { e, score: e.t.toLowerCase().startsWith(terms[0]) ? 0 : 1 };
      })
      .filter(Boolean)
      .sort((a, b) => a.score - b.score)
      .slice(0, 12);
    results.innerHTML = hits.length
      ? hits.map(({ e }) => `<li><a href="${e.u}">${e.t}<small>${e.g} › ${e.p}</small></a></li>`).join('')
      : '<li><a>No matches</a></li>';
    results.hidden = false;
    sel = -1;
  }
  input?.addEventListener('input', () => search(input.value));
  input?.addEventListener('keydown', (e) => {
    const items = [...results.querySelectorAll('a[href]')];
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault();
      sel = (sel + (e.key === 'ArrowDown' ? 1 : -1) + items.length) % items.length;
      items.forEach((a, i) => a.classList.toggle('sel', i === sel));
    } else if (e.key === 'Enter' && items.length) {
      location.href = items[Math.max(sel, 0)].getAttribute('href');
    } else if (e.key === 'Escape') {
      input.value = ''; results.hidden = true; input.blur();
    }
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === '/' && document.activeElement !== input) { e.preventDefault(); input.focus(); }
  });
  document.addEventListener('click', (e) => { if (!e.target.closest('.search')) results.hidden = true; });
})();
