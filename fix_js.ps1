foreach ($f in @('copilot.html', 'mock_interview.html', 'resume.html', 'records.html')) {
    $path = 'src\main\resources\templates\' + $f
    if (Test-Path $path) {
        $c = [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
        $c = $c -replace '\?+?/([a-zA-Z0-9]+)>', '</$1>'
        [IO.File]::WriteAllText($path, $c, [Text.Encoding]::UTF8)
    }
}
Write-Output "Tags repaired."
