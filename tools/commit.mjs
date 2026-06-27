/**
 * commit.mjs — Commit all changes, bump build number, and push to GitHub.
 *
 * Usage:
 *   node tools/commit.mjs "feat: add USB camera detection"
 *
 * What it does:
 *   1. Bumps the build number in version.json
 *   2. Stages all changed files (git add -A)
 *   3. Commits with the provided message + version stamp
 *   4. Pushes to origin/main
 *
 * Commit message format: "<your message> [0.1 (5)]"
 *
 * Related: version.mjs (version management), release.mjs (full releases)
 */

import { execSync } from 'child_process'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { bumpBuild, formatVersion } from './version.mjs'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')

const args = process.argv.slice(2)
if (args.length === 0) {
  console.error('\n❌ Provide a commit message: node tools/commit.mjs "your message"\n')
  process.exit(1)
}

const userMessage = args.join(' ')
const v = bumpBuild()
const vStr = formatVersion(v)
const fullMessage = `${userMessage} [${vStr}]`

console.log(`\n📦 Committing: ${fullMessage}\n`)

try {
  execSync('git add -A', { cwd: ROOT, stdio: 'inherit' })
  execSync(`git commit -m "${fullMessage}"`, { cwd: ROOT, stdio: 'inherit' })
  execSync('git push', { cwd: ROOT, stdio: 'inherit' })
  console.log(`\n✅ Pushed to GitHub: ${vStr}\n`)
} catch {
  console.error('\n❌ Commit or push failed. Check git output above.\n')
  process.exit(1)
}
