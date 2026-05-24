$utf8NoBom = New-Object System.Text.UTF8Encoding $False
foreach ($f in @('copilot.html', 'mock_interview.html', 'resume.html', 'records.html')) {
    $path = 'src\main\resources\templates\' + $f
    $content = [IO.File]::ReadAllText($path, $utf8NoBom)
    $content = $content.Replace('text-muted', 'text-white-50').Replace('text-secondary', 'text-white-50')
    [IO.File]::WriteAllText($path, $content, $utf8NoBom)
}
Write-Output "Files updated successfully."
