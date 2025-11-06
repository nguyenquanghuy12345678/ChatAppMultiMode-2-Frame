# Download Emoji Icons Script
# This script downloads 50 emoji icons from a free source

$iconFolder = "resources/icons"
$baseUrl = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72"

# Create icons folder if not exists
if (-not (Test-Path $iconFolder)) {
    New-Item -ItemType Directory -Path $iconFolder -Force | Out-Null
    Write-Host "Created folder: $iconFolder" -ForegroundColor Green
}

# Define 50 emoji icons with their Unicode codepoints
$emojiList = @{
    # Emotions (1-15)
    "emoji_smile.png" = "1f604"      # 😄
    "emoji_laugh.png" = "1f602"      # 😂
    "emoji_wink.png" = "1f609"       # 😉
    "emoji_love.png" = "1f60d"       # 😍
    "emoji_heart.png" = "2764"       # ❤️
    "emoji_kiss.png" = "1f618"       # 😘
    "emoji_thinking.png" = "1f914"   # 🤔
    "emoji_cool.png" = "1f60e"       # 😎
    "emoji_star.png" = "2b50"        # ⭐
    "emoji_sad.png" = "1f622"        # 😢
    "emoji_cry.png" = "1f62d"        # 😭
    "emoji_angry.png" = "1f620"      # 😠
    "emoji_surprised.png" = "1f62e"  # 😮
    "emoji_sleepy.png" = "1f634"     # 😴
    "emoji_sick.png" = "1f912"       # 🤒
    
    # Celebrations (16-25)
    "emoji_party.png" = "1f389"      # 🎉
    "emoji_celebrate.png" = "1f38a"  # 🎊
    "emoji_fire.png" = "1f525"       # 🔥
    "emoji_clap.png" = "1f44f"       # 👏
    "emoji_thumbsup.png" = "1f44d"   # 👍
    "emoji_thumbsdown.png" = "1f44e" # 👎
    "emoji_ok.png" = "1f44c"         # 👌
    "emoji_peace.png" = "270c"       # ✌️
    "emoji_muscle.png" = "1f4aa"     # 💪
    "emoji_pray.png" = "1f64f"       # 🙏
    
    # Nature (26-35)
    "emoji_sun.png" = "2600"         # ☀️
    "emoji_moon.png" = "1f319"       # 🌙
    "emoji_star2.png" = "1f31f"      # 🌟
    "emoji_cloud.png" = "2601"       # ☁️
    "emoji_rain.png" = "1f327"       # 🌧️
    "emoji_snow.png" = "2744"        # ❄️
    "emoji_thunder.png" = "26a1"     # ⚡
    "emoji_rainbow.png" = "1f308"    # 🌈
    "emoji_flower.png" = "1f338"     # 🌸
    "emoji_tree.png" = "1f333"       # 🌳
    
    # Animals (36-40)
    "emoji_cat.png" = "1f431"        # 🐱
    "emoji_dog.png" = "1f436"        # 🐶
    "emoji_bird.png" = "1f426"       # 🐦
    "emoji_fish.png" = "1f41f"       # 🐟
    "emoji_butterfly.png" = "1f98b"  # 🦋
    
    # Food (41-45)
    "emoji_pizza.png" = "1f355"      # 🍕
    "emoji_cake.png" = "1f382"       # 🎂
    "emoji_coffee.png" = "2615"      # ☕
    "emoji_beer.png" = "1f37a"       # 🍺
    "emoji_fruit.png" = "1f34e"      # 🍎
    
    # System (46-50)
    "emoji_check.png" = "2705"       # ✅
    "emoji_cross.png" = "274c"       # ❌
    "emoji_warning.png" = "26a0"     # ⚠️
    "emoji_info.png" = "2139"        # ℹ️
    "emoji_question.png" = "2753"    # ❓
}

Write-Host "`n=== Downloading 50 Emoji Icons ===" -ForegroundColor Cyan
Write-Host "Source: Twitter Twemoji (Open Source)`n" -ForegroundColor Yellow

$downloadedCount = 0
$failedCount = 0

foreach ($emoji in $emojiList.GetEnumerator()) {
    $fileName = $emoji.Key
    $unicode = $emoji.Value
    $url = "$baseUrl/$unicode.png"
    $outputPath = Join-Path $iconFolder $fileName
    
    try {
        Write-Host "Downloading: $fileName... " -NoNewline
        
        # Download with error handling
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadFile($url, $outputPath)
        
        if (Test-Path $outputPath) {
            $downloadedCount++
            Write-Host "OK" -ForegroundColor Green
        } else {
            $failedCount++
            Write-Host "FAILED" -ForegroundColor Red
        }
        
        # Small delay to avoid overwhelming the server
        Start-Sleep -Milliseconds 100
        
    } catch {
        $failedCount++
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Download Summary ===" -ForegroundColor Cyan
Write-Host "Total icons: 50" -ForegroundColor White
Write-Host "Downloaded: $downloadedCount" -ForegroundColor Green
Write-Host "Failed: $failedCount" -ForegroundColor Red

if ($downloadedCount -eq 50) {
    Write-Host "`n✅ All emoji icons downloaded successfully!" -ForegroundColor Green
} elseif ($downloadedCount -gt 0) {
    Write-Host "`n⚠️ Some icons downloaded. You can retry for failed ones." -ForegroundColor Yellow
} else {
    Write-Host "`n❌ No icons downloaded. Check internet connection." -ForegroundColor Red
}

Write-Host "`nIcons saved to: $iconFolder" -ForegroundColor Cyan
Write-Host "You can now run your ChatApp!" -ForegroundColor Green
