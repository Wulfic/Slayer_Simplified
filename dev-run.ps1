# Slayer Simplified - Dev Mode Launcher
# Uses RuneLite's --insecure-write-credentials to save Jagex account login,
# then launches via gradlew run (developer mode + plugin auto-loaded).
#
# First time:  .\dev-run.ps1 -Setup    (one-time credential save)
# Each test:   .\dev-run.ps1           (build + run with plugin loaded)

param(
    [switch]$Setup,
    [switch]$Clean
)

$CredsFile = "$env:USERPROFILE\.runelite\credentials.properties"

# === SETUP: Save Jagex credentials ===
if ($Setup) {
    Write-Host ""
    Write-Host "=== One-Time Jagex Credential Setup ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "This saves your Jagex login so gradlew run can use it." -ForegroundColor White
    Write-Host ""

    # Launch RuneLite (configure) to add the argument
    $RLExe = "$env:LOCALAPPDATA\RuneLite\RuneLite.exe"
    if (Test-Path $RLExe) {
        Write-Host "Opening RuneLite configuration..." -ForegroundColor Cyan
        Start-Process $RLExe -ArgumentList "--configure"
        Write-Host ""
        Write-Host "In the window that opens:" -ForegroundColor Yellow
        Write-Host '  1. In "Client arguments" box, add:  --insecure-write-credentials' -ForegroundColor White
        Write-Host "  2. Click Save" -ForegroundColor White
        Write-Host "  3. Launch RuneLite through the Jagex Launcher (click Play)" -ForegroundColor White
        Write-Host "  4. Log in once, then close RuneLite" -ForegroundColor White
        Write-Host ""
        Write-Host "After that, run .\dev-run.ps1 to test the plugin!" -ForegroundColor Green
        Write-Host "(Credentials saved to $CredsFile)" -ForegroundColor Gray
    } else {
        Write-Host "RuneLite.exe not found. Open 'RuneLite (configure)' from Start Menu." -ForegroundColor Yellow
        Write-Host 'Add --insecure-write-credentials to Client arguments, Save, then launch via Jagex Launcher.' -ForegroundColor White
    }
    return
}

# === CLEAN: Remove saved credentials ===
if ($Clean) {
    if (Test-Path $CredsFile) {
        Remove-Item $CredsFile -Force
        Write-Host "Removed saved credentials." -ForegroundColor Green
    } else {
        Write-Host "No saved credentials to remove." -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "You should also remove --insecure-write-credentials from RuneLite config:" -ForegroundColor Yellow
    $RLExe = "$env:LOCALAPPDATA\RuneLite\RuneLite.exe"
    if (Test-Path $RLExe) {
        Write-Host "Opening RuneLite configuration..." -ForegroundColor Cyan
        Start-Process $RLExe -ArgumentList "--configure"
        Write-Host 'Remove --insecure-write-credentials from "Client arguments" and Save.' -ForegroundColor White
    }
    return
}

# === RUN: Build and launch with plugin ===
if (-not (Test-Path $CredsFile)) {
    Write-Host "No saved credentials found." -ForegroundColor Red
    Write-Host "Run .\dev-run.ps1 -Setup first to save your Jagex account login." -ForegroundColor Yellow
    exit 1
}

Write-Host "Building and launching RuneLite with plugin..." -ForegroundColor Cyan
& "$PSScriptRoot\gradlew.bat" run

