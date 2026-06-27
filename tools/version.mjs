/**
 * version.mjs — single source of truth for KrinikCam versioning.
 *
 * Reads and writes version.json at the project root.
 * Format: { "major": X, "minor": Y, "build": N }
 * Display: "X.Y (N)" — shown in APK versionName, GitHub releases, commit messages.
 *
 * Used by: build.mjs, commit.mjs, release.mjs
 */

import { readFileSync, writeFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const VERSION_FILE = resolve(__dirname, '..', 'version.json')

/** Read current version object from version.json */
export function readVersion() {
  return JSON.parse(readFileSync(VERSION_FILE, 'utf-8'))
}

/** Write version object back to version.json */
export function writeVersion(v) {
  writeFileSync(VERSION_FILE, JSON.stringify(v, null, 2) + '\n')
}

/** Format version for display: "0.1 (42)" */
export function formatVersion(v) {
  return `${v.major}.${v.minor} (${v.build})`
}

/** Format version as git tag: "v0.1" */
export function formatTag(v) {
  return `v${v.major}.${v.minor}`
}

/** Bump build number — called on every commit */
export function bumpBuild() {
  const v = readVersion()
  v.build++
  writeVersion(v)
  return v
}

/** Bump minor version + reset build — called on release */
export function bumpMinor() {
  const v = readVersion()
  v.minor++
  v.build = 0
  writeVersion(v)
  return v
}

/** Bump major version + reset minor and build — called on major release */
export function bumpMajor() {
  const v = readVersion()
  v.major++
  v.minor = 0
  v.build = 0
  writeVersion(v)
  return v
}
