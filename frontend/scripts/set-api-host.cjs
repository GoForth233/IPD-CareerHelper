#!/usr/bin/env node
/**
 * Auto-detect this Mac's primary LAN IPv4 and (re)write .env.development
 * with VITE_API_BASE_URL pointing at it.
 *
 * Why: WeChat real-device debugging cannot reach `localhost` on the dev
 * machine; it must use the LAN IP. DHCP rotates that IP, so doing it by
 * hand drifts. Run this before `dev:mp-weixin` (already wired into the
 * `dev:mp-weixin` npm script as a pre-step).
 */
const os = require('os');
const fs = require('fs');
const path = require('path');

const PORT = process.env.BACKEND_PORT || 8080;
const ENV_FILE = path.resolve(__dirname, '..', '.env.development');

function pickLanIp() {
  const ifaces = os.networkInterfaces();
  // Prefer en0 (Wi-Fi/Ethernet on macOS), then any non-internal IPv4.
  const order = ['en0', 'en1', 'en2'];
  for (const name of order) {
    const list = ifaces[name] || [];
    for (const it of list) {
      if (it.family === 'IPv4' && !it.internal) return it.address;
    }
  }
  for (const list of Object.values(ifaces)) {
    for (const it of list || []) {
      if (it.family === 'IPv4' && !it.internal && !it.address.startsWith('169.254.')) {
        return it.address;
      }
    }
  }
  return null;
}

const ip = pickLanIp();
if (!ip) {
  console.error('[set-api-host] No LAN IPv4 detected. Are you connected to a network?');
  process.exit(1);
}

const baseUrl = `http://${ip}:${PORT}`;
const line = `VITE_API_BASE_URL=${baseUrl}\n`;

let prev = '';
try { prev = fs.readFileSync(ENV_FILE, 'utf8'); } catch {}
if (prev.trim() === line.trim()) {
  console.log(`[set-api-host] Unchanged: ${baseUrl}`);
  process.exit(0);
}
fs.writeFileSync(ENV_FILE, line);
console.log(`[set-api-host] Updated .env.development -> ${baseUrl}`);
