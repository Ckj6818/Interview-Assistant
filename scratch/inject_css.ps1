$dir = "H:\java spring\interviewai\src\main\resources\templates"
$link = "    <link rel=`"stylesheet`" th:href=`"@{/css/brutalist-theme.css}`">`r`n</head>"
Get-ChildItem -Path $dir -Filter *.html | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName)
    if (-not $content.Contains("brutalist-theme.css")) {
        $content = $content.Replace("</head>", $link)
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
        Write-Host "Updated $($_.Name)"
    }
}
