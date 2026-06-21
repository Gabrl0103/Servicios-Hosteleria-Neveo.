export function downloadBackup() {
  const link = document.createElement('a')
  link.href = 'http://localhost:8080/api/backup/database'
  link.download = ''
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
