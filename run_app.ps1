# PowerShell script to run Miyad Android Application from root folder.
# This script handles the SDK paths, starts the emulator, builds the APK, installs, and launches it.

$sdkPath = if ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} elseif ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} else {
    Join-Path $env:LOCALAPPDATA "Android\Sdk"
}
$avdName = if ($env:MIYAD_AVD_NAME) { $env:MIYAD_AVD_NAME } else { "medium_phone" }
$adb = "$sdkPath\platform-tools\adb.exe"
$emulator = "$sdkPath\emulator\emulator.exe"

Write-Host "=== Verification ===" -ForegroundColor Cyan

if (-not (Test-Path $adb)) {
    Write-Error "ADB not found at $adb"
    exit 1
}

# Check if emulator is running
Write-Host "Checking emulator..." -ForegroundColor Cyan
$devices = & $adb devices
$isEmulatorRunning = $false
foreach ($line in $devices) {
    if ($line -match "^emulator-\d+\s+device$") {
        $isEmulatorRunning = $true
    }
}

if (-not $isEmulatorRunning) {
    Write-Host "Starting emulator $avdName..." -ForegroundColor Green
    Start-Process $emulator -ArgumentList "@$avdName", "-gpu", "swiftshader_indirect"
    Write-Host "Waiting for device to boot..." -ForegroundColor Yellow
    
    & $adb wait-for-device
    
    $booted = $false
    while (-not $booted) {
        $status = & $adb shell getprop sys.boot_completed
        if ($status -and $status.Trim() -eq "1") {
            $booted = $true
        } else {
            Start-Sleep -Seconds 3
        }
    }
    Write-Host "Emulator is ready!" -ForegroundColor Green
} else {
    Write-Host "Emulator is already running." -ForegroundColor Green
}

# Build application
Write-Host "=== Building Application ===" -ForegroundColor Cyan
$frontendPath = Join-Path $PSScriptRoot "frontend"
Push-Location $frontendPath

Write-Host "Compiling debug APK..." -ForegroundColor Yellow
java -classpath ".\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed!"
    Pop-Location
    exit 1
}
Pop-Location
Write-Host "Build Succeeded!" -ForegroundColor Green

# Install app
Write-Host "=== Installing APK ===" -ForegroundColor Cyan
$apkPath = Join-Path $frontendPath "app\build\outputs\apk\debug\app-debug.apk"
& $adb install -r $apkPath

if ($LASTEXITCODE -ne 0) {
    Write-Error "Installation failed!"
    exit 1
}
Write-Host "Installation successful!" -ForegroundColor Green

# Launch app
Write-Host "=== Launching Application ===" -ForegroundColor Cyan
& $adb shell am start -n com.example.miyad/com.example.miyad.MainActivity

Write-Host "Successfully launched Miyad application!" -ForegroundColor Green
