$logPath = "C:\Users\asus\.gemini\antigravity-ide\brain\3408e3a4-da0f-4715-8c46-82cd4f37059f\.system_generated\logs\transcript.jsonl"
$outputDir = "H:\java spring\interviewai\src\main\resources\templates"

$filesToRecover = @{
    "index.html" = $false
    "questions.html" = $false
    "records.html" = $false
    "resume.html" = $false
    "copilot.html" = $false
}

# We can find questions.html fully intact in scratch/questions_output.html
$questionsScratch = "C:\Users\asus\.gemini\antigravity-ide\brain\3408e3a4-da0f-4715-8c46-82cd4f37059f\scratch\questions_output.html"
if (Test-Path $questionsScratch) {
    Copy-Item $questionsScratch -Destination (Join-Path $outputDir "questions.html") -Force
    Write-Host "Recovered questions.html from scratch"
    $filesToRecover["questions.html"] = $true
}

# The others are relatively small and were printed in full via view_file!
# Let's read the transcript lines from bottom to top
$lines = [System.IO.File]::ReadAllLines($logPath)
for ($i = $lines.Length - 1; $i -ge 0; $i--) {
    $line = $lines[$i]
    if ($line -match "view_file") {
        try {
            $data = ConvertFrom-Json $line
            # For view_file tool output, it might be in content or output
            $content = $data.output
            if ($null -eq $content) { $content = $data.content }
            
            if ($content -match "Showing lines 1 to ") {
                foreach ($fname in $filesToRecover.Keys) {
                    if ($filesToRecover[$fname] -eq $false -and $content.Contains($fname)) {
                        # We found a full printout of the file!
                        # The lines are prefixed with "<line_number>: "
                        $cleanLines = @()
                        $rawLines = $content -split "`n"
                        $isCode = $false
                        foreach ($r in $rawLines) {
                            if ($r -match "^The following code has been modified") { $isCode = $true; continue }
                            if ($r -match "^The above content shows the entire") { break }
                            if ($r -match "^The above content does NOT show") { break }
                            if ($isCode) {
                                # Remove the line number prefix
                                $cleanLine = $r -replace "^\d+:\s", ""
                                $cleanLines += $cleanLine
                            }
                        }
                        
                        if ($cleanLines.Length -gt 10) {
                            $outPath = Join-Path $outputDir $fname
                            [System.IO.File]::WriteAllLines($outPath, $cleanLines, [System.Text.Encoding]::UTF8)
                            Write-Host "Recovered $fname from view_file at line $i"
                            $filesToRecover[$fname] = $true
                        }
                    }
                }
            }
        } catch { }
    }
}
