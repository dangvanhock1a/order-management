# Tạo frontend Angular 20 cho Lab 1 và chép màn hình Product mẫu vào (Windows PowerShell).
# Chạy: powershell -ExecutionPolicy Bypass -File frontend-setup\setup-frontend.ps1
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoDir = Resolve-Path (Join-Path $ScriptDir "..")
Set-Location $RepoDir

if (Test-Path "frontend") {
    Write-Error "Thư mục frontend\ đã tồn tại. Xóa hoặc đổi tên trước khi chạy lại."
}

# 1. Tạo dự án Angular (không hỏi tương tác)
npx -y "@angular/cli@20" new frontend --routing --style=css --ssr=false --skip-git --defaults

# 2. Chép code mẫu (ghi đè app.routes.ts, app.config.ts, app.html, app.css)
Copy-Item -Path (Join-Path $ScriptDir "src\app\*") -Destination "frontend\src\app" -Recurse -Force
Copy-Item -Path (Join-Path $ScriptDir "proxy.conf.json") -Destination "frontend" -Force

# 3. npm start dùng proxy /api -> http://localhost:8080
Set-Location "frontend"
npm pkg set "scripts.start=ng serve --proxy-config proxy.conf.json"

Write-Host "Xong. Chạy backend trước, sau đó: cd frontend; npm start  (http://localhost:4200)"
