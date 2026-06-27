/**
 * setup.mjs — One-time environment setup after cloning the repo.
 *
 * Run this ONCE before using any other tools:
 *   node tools/setup.mjs
 *
 * What it does:
 *   1. Checks that required tools are installed (JDK 17, Node.js, gh CLI)
 *   2. Generates the Gradle wrapper (gradlew / gradlew.bat + wrapper jar)
 *   3. Installs Node.js dependencies for tools/ (md-to-pdf, etc.)
 *   4. Checks gh CLI authentication status
 *
 * Related: build.mjs, commit.mjs, release.mjs all depend on this having run once.
 */

import { execSync, spawnSync } from 'child_process'
import { existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')

let allOk = true

function check(label, fn) {
  try {
    fn()
    console.log(`  ✅ ${label}`)
  } catch (e) {
    console.error(`  ❌ ${label}: ${e.message}`)
    allOk = false
  }
}

function run(cmd, opts = {}) {
  execSync(cmd, { cwd: ROOT, stdio: 'inherit', ...opts })
}

console.log('\n🔧 KrinikCam — Environment Setup\n')

// --- 1. Check JDK 17+ ---
console.log('Checking prerequisites...')
check('JDK 17+', () => {
  const result = spawnSync('java', ['-version'], { encoding: 'utf-8' })
  const output = result.stderr + result.stdout
  if (!output.includes('17') && !output.includes('21') && !output.includes('23')) {
    throw new Error('JDK 17+ required. Install via: brew install --cask temurin@17')
  }
})

// --- 2. Check Node.js ---
check('Node.js 18+', () => {
  const result = spawnSync('node', ['--version'], { encoding: 'utf-8' })
  const major = parseInt(result.stdout.replace('v', '').split('.')[0])
  if (major < 18) throw new Error('Node.js 18+ required')
})

// --- 3. Check gh CLI ---
check('GitHub CLI (gh)', () => {
  const result = spawnSync('gh', ['--version'], { encoding: 'utf-8' })
  if (result.status !== 0) throw new Error('gh CLI not found. Install: brew install gh')
})

console.log('')

// --- 4. Generate Gradle wrapper ---
if (!existsSync(resolve(ROOT, 'gradle/wrapper/gradle-wrapper.jar'))) {
  console.log('Generating Gradle wrapper...')
  try {
    run('gradle wrapper --gradle-version=8.10', { stdio: 'inherit' })
    // Make gradlew executable on macOS/Linux
    run('chmod +x gradlew')
    console.log('  ✅ Gradle wrapper generated')
  } catch {
    console.error('  ❌ Failed to generate Gradle wrapper.')
    console.error('     Ensure Gradle is installed: brew install gradle')
    console.error('     Or open the project in Android Studio — it handles this automatically.')
    allOk = false
  }
} else {
  console.log('  ✅ Gradle wrapper already present')
}

// --- 5. Install Node.js dependencies ---
console.log('Installing Node.js dependencies...')
try {
  run('npm install', { cwd: __dirname })
  console.log('  ✅ Node.js dependencies installed')
} catch {
  console.error('  ❌ npm install failed')
  allOk = false
}

console.log('')

// --- 6. Check gh auth ---
console.log('Checking GitHub CLI authentication...')
const ghAuth = spawnSync('gh', ['auth', 'status'], { encoding: 'utf-8' })
if (ghAuth.status === 0) {
  console.log('  ✅ GitHub CLI authenticated')
} else {
  console.warn('  ⚠️  GitHub CLI not authenticated.')
  console.warn('     Run: gh auth login')
  console.warn('     (Required for release.mjs — not needed for local builds)')
}

console.log('')
if (allOk) {
  console.log('✅ Setup complete! You can now use:\n')
  console.log('   node tools/build.mjs          — build debug APK')
  console.log('   node tools/build.mjs --release — build release APK')
  console.log('   node tools/commit.mjs "msg"    — commit + push + bump build')
  console.log('   node tools/release.mjs         — release candidate to GitHub')
  console.log('   node tools/readme-pdf.mjs      — generate README.pdf\n')
} else {
  console.log('⚠️  Setup completed with warnings. Fix the issues above before building.\n')
}
