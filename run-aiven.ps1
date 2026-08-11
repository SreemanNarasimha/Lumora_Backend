$envFile = ".env.aiven"
if (Test-Path $envFile) {
    Write-Host "Loading variables from .env file..."
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^([^#=]+)=(.*)") {
            $key = $matches[1].Trim()
            $val = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $val, "Process")
        }
    }
}

$requiredVars = @("JWT_SECRET", "FRONTEND_URL", "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD")
$missing = $false

foreach ($var in $requiredVars) {
    if (-not [Environment]::GetEnvironmentVariable($var, "Process")) {
        Write-Error "Missing required environment variable: $var"
        $missing = $true
    } else {
        Write-Host "${var}: SET"
    }
}

if ($missing) {
    Write-Error "Please set the missing variables in a .env file or your terminal before running this script."
    exit 1
}

Write-Host "Starting Spring Boot..."
.\mvnw.cmd spring-boot:run
