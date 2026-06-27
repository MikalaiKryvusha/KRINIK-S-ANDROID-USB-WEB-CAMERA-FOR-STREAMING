/**
 * readme-pdf.mjs — Convert README.md to README.pdf for GitHub release attachments.
 *
 * Usage:
 *   node tools/readme-pdf.mjs
 *
 * Output: README.pdf at project root (gitignored — regenerated on each release)
 *
 * Prerequisites: npm install (run setup.mjs once first — installs md-to-pdf)
 * Note: md-to-pdf uses headless Chromium, first run downloads it automatically.
 *
 * Related: release.mjs uploads README.pdf to GitHub Releases alongside APK
 */

import { mdToPdf } from 'md-to-pdf'
import { existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')
const inputPath = resolve(ROOT, 'README.md')
const outputPath = resolve(ROOT, 'README.pdf')

if (!existsSync(inputPath)) {
  console.error('\n❌ README.md not found at project root — create it first.\n')
  process.exit(1)
}

console.log('\n📄 Converting README.md → README.pdf\n')

try {
  await mdToPdf(
    { path: inputPath },
    {
      dest: outputPath,
      pdf_options: {
        format: 'A4',
        margin: { top: '20mm', right: '20mm', bottom: '20mm', left: '20mm' },
        printBackground: true,
      },
      // KrinikCam brand styling for the PDF
      css: `
        body {
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
          font-size: 14px;
          line-height: 1.7;
          color: #1a1a1a;
        }
        h1 { color: #FF1A8C; font-size: 28px; border-bottom: 3px solid #FF1A8C; padding-bottom: 8px; }
        h2 { color: #CC0070; font-size: 22px; border-bottom: 1px solid #FF80C8; padding-bottom: 4px; }
        h3 { color: #1a1a1a; font-size: 18px; }
        code { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
        pre  { background: #1a1a2e; color: #e0e0e0; padding: 16px; border-radius: 8px; overflow-x: auto; }
        pre code { background: transparent; color: inherit; padding: 0; }
        table { border-collapse: collapse; width: 100%; margin: 16px 0; }
        th { background: #FF1A8C; color: white; padding: 8px 12px; text-align: left; }
        td { padding: 8px 12px; border-bottom: 1px solid #e0e0e0; }
        tr:nth-child(even) td { background: #fafafa; }
        blockquote { border-left: 4px solid #FF1A8C; margin: 0; padding-left: 16px; color: #555; }
        a { color: #FF1A8C; }
      `,
    }
  )
  console.log(`✅ README.pdf → ${outputPath}\n`)
} catch (e) {
  console.error('❌ PDF conversion failed:', e.message)
  process.exit(1)
}
