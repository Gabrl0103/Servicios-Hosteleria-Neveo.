#!/usr/bin/env node
const fs = require('fs')
const path = require('path')

function copyRecursive(src, dest) {
  if (!fs.existsSync(src)) {
    console.error(`No existe: ${src}`)
    process.exit(1)
  }
  fs.cpSync(src, dest, { recursive: true })
}

const root = path.join(__dirname, '..')

const backendJar = path.join(root, 'backend', 'target', 'heladeria-tpv.jar')
const backendDest = path.join(__dirname, 'resources', 'backend', 'heladeria-tpv.jar')

const frontendDist = path.join(root, 'frontend', 'dist')
const frontendDest = path.join(__dirname, 'resources', 'frontend')

fs.mkdirSync(path.dirname(backendDest), { recursive: true })
fs.rmSync(frontendDest, { recursive: true, force: true })

copyRecursive(backendJar, backendDest)
copyRecursive(frontendDist, frontendDest)

console.log('Listo: backend y frontend copiados a electron/resources')
