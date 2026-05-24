$logPath = "C:\Users\asus\.gemini\antigravity-ide\brain\e019ca05-a167-4b4a-b0df-2623390a3909\.system_generated\logs\transcript.jsonl"
$targetFile = "mock_interview.html"
$outputFile = "h:\java spring\interviewai\transcript_matches.txt"

Write-Host "Parsing log..."
Get-Content $logPath -Tail 1000 | ForEach-Object {
    if ($_ -match "mock_interview.html" -and $_ -match "The following code has been modified to include a line number") {
        # This is a view_file output!
        # Let's extract the created_at to see when it happened
        if ($_ -match '"created_at":"([^"]+)"') {
            Add-Content -Path $outputFile -Value "Found at $($matches[1])"
        }
    }
}
Write-Host "Done"
