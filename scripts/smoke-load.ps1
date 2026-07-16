param(
    [string]$Url = "http://localhost:8080/api/businesses",
    [int]$Requests = 100,
    [int]$Concurrency = 10,
    [string]$DataScale = "local seed data",
    [string]$RateLimit = "default route policy"
)

if ($Requests -lt 1 -or $Concurrency -lt 1) { throw "Requests and Concurrency must be positive." }

Add-Type -ReferencedAssemblies "System.Net.Http.dll" -ErrorAction Stop -TypeDefinition @'
using System; using System.Diagnostics; using System.Linq; using System.Net.Http; using System.Threading; using System.Threading.Tasks;
public static class SmokeLoad {
  public sealed class Result { public long LatencyMs; public bool Success; }
  public static async Task<Result[]> Run(string url, int requests, int concurrency) {
    using (var client = new HttpClient()) using (var gate = new SemaphoreSlim(concurrency)) {
      var tasks = Enumerable.Range(0, requests).Select(async _ => {
        await gate.WaitAsync(); var watch = Stopwatch.StartNew();
        try { var response = await client.GetAsync(url); return new Result { LatencyMs = watch.ElapsedMilliseconds, Success = response.IsSuccessStatusCode }; }
        catch { return new Result { LatencyMs = watch.ElapsedMilliseconds, Success = false }; }
        finally { gate.Release(); }
      });
      return await Task.WhenAll(tasks);
    }
  }
}
'@

$all = [Diagnostics.Stopwatch]::StartNew()
$results = [SmokeLoad]::Run($Url, $Requests, $Concurrency).GetAwaiter().GetResult()
$all.Stop()
$latencies = @($results | Where-Object Success | ForEach-Object LatencyMs | Sort-Object)
if ($latencies.Count -eq 0) { throw "No successful requests." }
$percentile = { param($ratio) $latencies[[Math]::Min($latencies.Count - 1, [Math]::Ceiling($latencies.Count * $ratio) - 1)] }
[pscustomobject]@{
    url = $Url; requests = $Requests; concurrency = $Concurrency; dataScale = $DataScale; rateLimit = $RateLimit
    hardware = "$env:PROCESSOR_IDENTIFIER; logicalCores=$env:NUMBER_OF_PROCESSORS"; elapsedMs = $all.ElapsedMilliseconds
    successfulRequests = $latencies.Count; errors = $Requests - $latencies.Count; errorRate = [Math]::Round(($Requests - $latencies.Count) / $Requests, 4)
    qps = [Math]::Round($latencies.Count / $all.Elapsed.TotalSeconds, 2)
    p50Ms = & $percentile 0.50; p95Ms = & $percentile 0.95; p99Ms = & $percentile 0.99
} | ConvertTo-Json -Compress
