<template>
  <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" fill="none">
    <defs>
      <linearGradient id="chatOrb" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="var(--accent)" stop-opacity="0.9" />
        <stop offset="100%" stop-color="var(--accent-strong)" stop-opacity="0.9" />
      </linearGradient>
    </defs>
    <circle cx="100" cy="100" r="72" fill="url(#chatOrb)" opacity="0.14" />
    <circle cx="100" cy="100" r="48" fill="url(#chatOrb)" opacity="0.4" />
    <g transform="translate(56, 58)">
      <rect x="0" y="6" width="60" height="36" rx="10" fill="var(--surface-base)" stroke="var(--accent-border)" stroke-width="1.5"/>
      <circle cx="14" cy="24" r="3" fill="var(--accent)"/>
      <circle cx="26" cy="24" r="3" fill="var(--accent)"/>
      <circle cx="38" cy="24" r="3" fill="var(--accent)"/>
      <rect x="30" y="46" width="60" height="36" rx="10" fill="var(--surface-base)" stroke="var(--accent-border)" stroke-width="1.5"/>
      <rect x="42" y="60" width="36" height="4" rx="2" fill="var(--accent-soft)"/>
      <rect x="42" y="68" width="24" height="4" rx="2" fill="var(--accent-soft)"/>
    </g>
    <circle cx="40" cy="52" r="3" fill="var(--accent)" opacity="0.6"/>
    <circle cx="160" cy="80" r="4" fill="var(--accent-strong)" opacity="0.5"/>
    <circle cx="150" cy="150" r="3" fill="var(--accent)" opacity="0.5"/>
    <circle cx="46" cy="150" r="4" fill="var(--accent-strong)" opacity="0.5"/>
  </svg>
</template>
