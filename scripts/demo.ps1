# Demo script for FraudRuleEngine
# How to run: powershell -ExecutionPolicy Bypass -File .\scripts\demo.ps1
# Ensure script is run from project root

$baseUrl = "http://localhost:8080/v1/transactions/evaluate"
$headers = @{ "Content-Type" = "application/json" }

function Post-Tx($body) {
  $json = $body | ConvertTo-Json -Depth 10
  $resp = Invoke-RestMethod -Method Post -Uri $baseUrl -Headers $headers -Body $json
  $resp | ConvertTo-Json -Depth 10
}

Write-Host "`n=== 1) HIGH AMOUNT ==="
Post-Tx @{
  transactionId = "demo-high-" + [guid]::NewGuid().ToString("N").Substring(0,8)
  customerId    = "cust-demo"
  amount        = 70000
  currency      = "ZAR"
  merchant      = "SHOPRITE"
  category      = "groceries"
  eventTime     = (Get-Date).ToUniversalTime().ToString("o")
}

Write-Host "`n=== 2) WATCHLIST MERCHANT (ACME / BINANCE) ==="
Post-Tx @{
  transactionId = "demo-watch-" + [guid]::NewGuid().ToString("N").Substring(0,8)
  customerId    = "cust-demo"
  amount        = 35000
  currency      = "ZAR"
  merchant      = "ACME"
  category      = "electronics"
  eventTime     = (Get-Date).ToUniversalTime().ToString("o")
}

Write-Host "`n=== 3) VELOCITY BURST (6 tx quickly, triggers on 6th if maxCount=5) ==="
$customer = "cust-velo-demo"
for ($i=1; $i -le 6; $i++) {
  $result = Post-Tx @{
    transactionId = "demo-velo-$i-" + [guid]::NewGuid().ToString("N").Substring(0,8)
    customerId    = $customer
    amount        = 100
    currency      = "ZAR"
    merchant      = "PUMA"
    category      = "clothing"
    eventTime     = (Get-Date).ToUniversalTime().ToString("o")
  }
  Write-Host "Velocity request $i response:`n$result"
  Start-Sleep -Milliseconds 200
}
