param([string]$BaseUrl = 'http://localhost:8080', [int]$Requests = 50)
$cookie = Join-Path $env:TEMP 'lab16-benchmark-cookie.txt'
try { curl.exe -s -c $cookie -d 'username=manager01&password=123456' "$BaseUrl/login" | Out-Null } catch { throw }
$times = @()
1..$Requests | ForEach-Object {
    $watch = [Diagnostics.Stopwatch]::StartNew()
    curl.exe -s -o $null -b $cookie "$BaseUrl/api/dashboard"
    $watch.Stop(); $times += $watch.Elapsed.TotalMilliseconds
}
$sorted = $times | Sort-Object
[pscustomobject]@{ Requests = $Requests; MinMs = [math]::Round($sorted[0], 2); AvgMs = [math]::Round(($times | Measure-Object -Average).Average, 2); P95Ms = [math]::Round($sorted[[math]::Floor($Requests * .95) - 1], 2); MaxMs = [math]::Round($sorted[-1], 2) } | Format-List
