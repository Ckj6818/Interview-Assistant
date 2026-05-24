$fixes = @{
    "MindSpark 闈箣?" = "MindSpark 闈箣鍏?
    "閫€鍑虹櫥?" = "閫€鍑虹櫥褰?
    "绯荤粺鎬婚?" = "绯荤粺鎬婚搴?
    "浠〃?" = "浠〃鐩?
    "蹇€熸寚?" = "蹇€熸寚鍗?
    "AI绠€鍘嗗畾?璇婃墍" = "AI绠€鍘嗗畾鍒?璇婃墍"
    "寮€濮嬪叏鐪熷杞闊抽潰?" = "寮€濮嬪叏鐪熷杞闊抽潰璇?
    "楣呮潵闈㈢郴缁熸牳蹇冧紭?" = "楣呮潵闈㈢郴缁熸牳蹇冧紭鍔?
    "AI绠€鍘嗗畾?& 娣卞害绮句慨" = "AI绠€鍘嗗畾鍒?& 娣卞害绮句慨"
    "瀹炴椂闈㈣瘯鐏垫劅?" = "瀹炴椂闈㈣瘯鐏垫劅搴?
    "閫€鍑虹櫥?</button>" = "閫€鍑虹櫥褰?/button>"
    "AI??" = "AI绠€鍘嗚瘖鏂?
    "顺??" = "閫€鍑虹櫥褰?
    "AI??& " = "AI绠€鍘嗗畾鍒?& 绮句慨"
    "??" = "绠€鍘嗗唴瀹?
    "目职位JD位??- 选" = "鐩爣鑱屼綅JD - 閫夊～"
    "?? ????? (????????)" = "姝ｅ父妯″紡 (姝ｅ父杩介棶)"
    "?? ?????? (?????/????)" = "绠楁硶妯″紡 (鑰冨療绠楁硶/鎵嬫挄浠ｇ爜)"
    "??? ??????? (???/????)" = "绯荤粺璁捐妯″紡 (鑰冨療鏋舵瀯/璁捐)"
    "?? ????? (???????/????)" = "鍘嬪姏娴嬭瘯妯″紡 (鑰冨療鎶楀帇/鏋侀檺)"
    "页" = "棣栭〉"
    "全模" = "鍏ㄧ湡妯℃嫙闈㈣瘯"
    "实时" = "瀹炴椂鎻愯瘝鍔╂墜"
    "AI/" = "AI绠€鍘嗗畾鍒?璇婃墍"
    "约录" = "闈㈣瘯璁板綍"
}

$files = @("index.html", "questions.html", "resume.html", "records.html", "copilot.html")

foreach ($file in $files) {
    $path = "H:\java spring\interviewai\src\main\resources\templates\recovered_$file"
    if (Test-Path $path) {
        $content = Get-Content $path -Raw -Encoding UTF8
        foreach ($key in $fixes.Keys) {
            if ($content.Contains($key)) {
                $content = $content.Replace($key, $fixes[$key])
            }
        }
        # Special fix for the sidebars which got fully mangled into something like " "
        # Actually it's better to manually overwrite the sidebars if they are broken.
        
        $dest = "H:\java spring\interviewai\src\main\resources\templates\$file"
        [System.IO.File]::WriteAllText($dest, $content, [System.Text.Encoding]::UTF8)
        Write-Host "Fixed and saved $file"
    }
}
