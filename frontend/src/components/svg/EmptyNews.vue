<template>
  <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" fill="none">
    <defs>
      <linearGradient id="newsGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--accent)" stop-opacity="0.9"/>
        <stop offset="100%" stop-color="var(--accent-strong)" stop-opacity="0.9"/>
      </linearGradient>
    </defs>
    <ellipse cx="100" cy="170" rx="72" ry="8" fill="var(--accent-soft)" />
    <g transform="translate(48, 46)">
      <rect x="0" y="12" width="96" height="108" rx="10" fill="var(--surface-base)" stroke="var(--accent-border)" stroke-width="1.5"/>
      <rect x="10" y="24" width="76" height="12" rx="3" fill="url(#newsGrad)" opacity="0.85"/>
      <rect x="10" y="44" width="76" height="4" rx="2" fill="var(--accent-soft)"/>
      <rect x="10" y="54" width="64" height="4" rx="2" fill="var(--accent-soft)"/>
      <rect x="10" y="68" width="36" height="36" rx="6" fill="var(--accent-soft)"/>
      <rect x="52" y="68" width="34" height="4" rx="2" fill="var(--accent-soft)"/>
      <rect x="52" y="78" width="34" height="4" rx="2" fill="var(--accent-soft)"/>
      <rect x="52" y="88" width="24" height="4" rx="2" fill="var(--accent-soft)"/>
    </g>
    <g transform="translate(130, 40)">
      <path d="M0 6 L6 0 L12 6 L6 12 Z" fill="var(--accent)"/>
    </g>
    <g transform="translate(46, 34)">
      <circle cx="0" cy="0" r="3" fill="var(--accent-strong)"/>
    </g>
    <g transform="translate(160, 110)">
      <circle cx="0" cy="0" r="4" fill="var(--accent)" opacity="0.6"/>
    </g>
  </svg>
</template>
