param(
    [string]$Url = "http://localhost:8080/api/businesses",
    [int]$Requests = 100,
    [int]$Concurrency = 10
)

if ($Requests -lt 1 -or $Concurrency -lt 1) { throw "Requests and Concurrency must be positive." }

Add-Type -ReferencedAssemblies "System.Net.Http.dll" -ErrorAction Stop -TypeDefinition @'
using System; using System.Diagnostics; using System.Linq; using System.Net.Http; using System.Threading; using System.Threading.Tasks;
public static class SmokeLoad {
  public static async Task<long[]> Run(string url, int requests, int concurrency) {
    using (var client = new HttpClient()) using (var gate = new SemaphoreSlim(concurrency)) {
      var tasks = Enumerable.Range(0, requests).Select(async _ => {
        await gate.WaitAsync(); var watch = Stopwatch.StartNew();
        try { var response = await client.GetAsync(url); response.EnsureSuccessStatusCode(); return watch.ElapsedMilliseconds; }
        finally { gate.Release(); }
      });
      return await Task.WhenAll(tasks);
    }
  }
}
'@

$all = [Diagnostics.Stopwatch]::StartNew()
$latencies = [SmokeLoad]::Run($Url, $Requests, $Concurrency).GetAwaiter().GetResult() | Sort-Object
$all.Stop()
$percentile = { param($ratio) $latencies[[Math]::Min($latencies.Count - 1, [Math]::Ceiling($latencies.Count * $ratio) - 1)] }
[pscustomobject]@{
    requests = $Requests; concurrency = $Concurrency; elapsedMs = $all.ElapsedMilliseconds
    qps = [Math]::Round($Requests / $all.Elapsed.TotalSeconds, 2)
    p50Ms = & $percentile 0.50; p95Ms = & $percentile 0.95; p99Ms = & $percentile 0.99
} | ConvertTo-Json -Compress
