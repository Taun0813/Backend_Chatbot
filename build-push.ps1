# Build và push tất cả service lên Docker Hub (Windows PowerShell)
# Cách dùng: .\build-push.ps1 -DockerHubUser <username> [-Tag latest]
param(
    [Parameter(Mandatory=$true)][string]$DockerHubUser,
    [string]$Tag = "latest"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$modules = @(
    @{ Module = "discovery-service"; Port = "8761" },
    @{ Module = "api-gateway"; Port = "8181" },
    @{ Module = "user-service"; Port = "8081" },
    @{ Module = "product-service"; Port = "8082" },
    @{ Module = "order-service"; Port = "8084" },
    @{ Module = "payment-service"; Port = "8085" },
    @{ Module = "cart-service"; Port = "8086" },
    @{ Module = "inventory-service"; Port = "8083" },
    @{ Module = "warranty-service"; Port = "8088" },
    @{ Module = "recommendation-service"; Port = "8089" }
)

foreach ($m in $modules) {
    $image = "${DockerHubUser}/$($m.Module):$Tag"
    Write-Host "Building $($m.Module) (port $($m.Port)) -> $image"
    docker build -f infrastructure/docker/Dockerfile `
        --build-arg MODULE=$m.Module `
        --build-arg APP_PORT=$m.Port `
        -t $image .
    Write-Host "Pushing $image"
    docker push $image
}

Write-Host "Done. Chạy stack: docker compose -f docker-compose.hub.yml --env-file .env up -d"
