/**
 * build.mjs — Build KrinikCam APK via Gradle.
 *
 * Usage:
 *   node tools/build.mjs              → debug build  (opens browser progress UI)
 *   node tools/build.mjs --release    → release APK  (also shows UI)
 *   node tools/build.mjs --no-ui      → headless / CI — terminal output only
 *
 * Output: app/build/outputs/apk/{debug|release}/
 *
 * Prerequisites: JDK 17, Android SDK, Gradle wrapper (run setup.mjs once)
 * UI details: build-ui.mjs — SSE-based browser progress bar + live log
 */

import { execSync } from 'child_process'
import { existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { readVersion, formatVersion } from './version.mjs'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')

const args      = process.argv.slice(2)
const isRelease = args.includes('--release')
const noUi      = args.includes('--no-ui')
const flavor    = isRelease ? 'Release' : 'Debug'

const gradlew     = process.platform === 'win32' ? 'gradlew.bat' : './gradlew'
const gradlewPath = resolve(ROOT, gradlew)

if (!existsSync(gradlewPath)) {
  console.error('\n❌ Gradle wrapper not found. Run: node tools/setup.mjs first\n')
  process.exit(1)
}

// Auto-detect JAVA_HOME: prefer system JDK, fall back to Android Studio bundled JBR
if (!process.env.JAVA_HOME) {
  const asJdk = '/Applications/Android Studio.app/Contents/jbr/Contents/Home'
  if (existsSync(asJdk)) process.env.JAVA_HOME = asJdk
}

const v    = readVersion()
const vStr = formatVersion(v)
console.log(`\n🎬 KrinikCam — Building ${flavor} ${vStr}`)

// ─── Headless mode (CI, scripting, release.mjs) ────────────────────────────

if (noUi) {
  try {
    execSync(`${gradlew} assemble${flavor} --no-daemon`, { cwd: ROOT, stdio: 'inherit' })
    console.log(`\n✅ Build complete → app/build/outputs/apk/${flavor.toLowerCase()}/\n`)
  } catch {
    console.error('\n❌ Build failed. Check the Gradle output above.\n')
    process.exit(1)
  }
  process.exit(0)
}

// ─── UI mode (default) ──────────────────────────────────────────────────────
// Starts a local HTTP server, opens a browser with a live progress bar,
// and streams Gradle output via SSE. See build-ui.mjs for details.

const { runWithUi } = await import('./build-ui.mjs')

try {
  await runWithUi({
    flavor,
    vStr,
    gradlew: gradlew,
    root:    ROOT,
    env:     { ...process.env },
  })
  process.exit(0)
} catch {
  process.exit(1)
}
