/**
 * build.mjs — Build KrinikCam APK via Gradle.
 *
 * Usage:
 *   node tools/build.mjs           → debug build
 *   node tools/build.mjs --release → release build (minified)
 *
 * Output: app/build/outputs/apk/{debug|release}/
 *
 * Prerequisites: JDK 17, Android SDK, Gradle wrapper (run setup.mjs once first)
 */

import { execSync } from 'child_process'
import { existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { readVersion, formatVersion } from './version.mjs'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')

const args = process.argv.slice(2)
const isRelease = args.includes('--release')
const flavor = isRelease ? 'Release' : 'Debug'
const flavorLower = flavor.toLowerCase()

const gradlew = process.platform === 'win32' ? 'gradlew.bat' : './gradlew'
const gradlewPath = resolve(ROOT, gradlew)

if (!existsSync(gradlewPath)) {
  console.error(`\n❌ Gradle wrapper not found. Run: node tools/setup.mjs first\n`)
  process.exit(1)
}

// Auto-detect JAVA_HOME: prefer system JDK, fall back to Android Studio bundled JBR
if (!process.env.JAVA_HOME) {
  const asJdk = '/Applications/Android Studio.app/Contents/jbr/Contents/Home'
  if (existsSync(asJdk)) {
    process.env.JAVA_HOME = asJdk
  }
}

const v = readVersion()
console.log(`\n🎬 KrinikCam — Building ${flavor} ${formatVersion(v)}\n`)

try {
  execSync(`${gradlew} assemble${flavor} --stacktrace`, {
    cwd: ROOT,
    stdio: 'inherit',
  })
  console.log(`\n✅ Build complete → app/build/outputs/apk/${flavorLower}/\n`)
} catch {
  console.error(`\n❌ Build failed. Check the Gradle output above.\n`)
  process.exit(1)
}
