$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"email":"admin@lumora.com","password":"admin123"}'
$token = $login.token

function Measure-Api {
    param($Path)
    $time = Measure-Command {
        try {
            Invoke-RestMethod -Uri "http://localhost:8080$Path" -Headers @{"Authorization"="Bearer $token"} | Out-Null
        } catch {
            Write-Host "Error for $Path : $_"
        }
    }
    Write-Host "$Path : $($time.TotalMilliseconds) ms"
}

Measure-Api "/api/products"
Measure-Api "/api/categories"
Measure-Api "/api/products/1"
Measure-Api "/api/cart"
Measure-Api "/api/wishlists"
Measure-Api "/api/users/me"
Measure-Api "/api/orders"
