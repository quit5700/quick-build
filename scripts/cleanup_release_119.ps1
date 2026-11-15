$ErrorActionPreference = 'Stop'
$Delivery = 'H:\Codex交接文档库\快捷建筑模组_两项修复接管_20261027\1.1.9_正式版'
$Working = 'H:\CodexWork\快捷建筑模组'
$Canonical = Join-Path $Delivery '源码'
$OldC = 'C:\Users\quit5700\Documents\mc虚拟线路模组\快捷建筑模组'
$Report = Get-Content -LiteralPath (Join-Path $Delivery '正式版交付核验.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$Published = Get-Content -LiteralPath (Join-Path $Delivery 'GitHub发布核验.json') -Raw -Encoding UTF8 | ConvertFrom-Json
if ($Published.url -ne 'https://github.com/quit5700/quick-build/releases/tag/v1.1.9' -or $Published.verifiedAssets -ne 20 -or $Published.draft) { throw '正式发行版和20个附件尚未核验，禁止清理。' }
if (-not (Test-Path -LiteralPath (Join-Path $Delivery '验证\ALL_FINAL_CHECKS_PASSED.json'))) { throw '最终源码验证尚未完成。' }
$Entries = @()
$ProtectedRuntimes = @()
foreach ($Property in $Report.PSObject.Properties) {
    foreach ($Installation in $Property.Value.installations) {
        if (-not $Installation.installed) { throw '存在尚未安装的实例。' }
        $Path = [IO.Path]::GetFullPath($Installation.installed)
        if ((Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() -ne $Property.Value.runtime_sha256) { throw "安装文件不一致：$Path" }
        $ProtectedRuntimes += $Path
    }
}
if ($ProtectedRuntimes.Count -ne 11) { throw '安装实例数量错误。' }
$Head = (& git -C $Working rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $Head -ne $Published.commit) { throw '保留源码提交与发布提交不一致。' }
$Dirty = & git -C $Working status --porcelain -uno
if ($Dirty) { throw '保留源码存在未提交改动，禁止清理。' }
$FreeBefore = [IO.DriveInfo]::new('C').AvailableFreeSpace
function Extended([string]$Path) {
    $Full = [IO.Path]::GetFullPath($Path)
    if ($Full.StartsWith('\\?\')) { return $Full }
    if ($Full.StartsWith('\\')) { return '\\?\UNC\' + $Full.Substring(2) }
    return '\\?\' + $Full
}
function Remove-Checked([string]$Path, [string]$AllowedRoot, [bool]$Directory = $true) {
    $Full = [IO.Path]::GetFullPath($Path).TrimEnd('\')
    $Allowed = [IO.Path]::GetFullPath($AllowedRoot).TrimEnd('\')
    if (-not ($Full.Equals($Allowed,[StringComparison]::OrdinalIgnoreCase) -or $Full.StartsWith($Allowed+'\',[StringComparison]::OrdinalIgnoreCase))) { throw "清理路径越界：$Full" }
    foreach ($Keep in $script:ProtectedRuntimes) { if ($Full.Equals($Keep,[StringComparison]::OrdinalIgnoreCase)) { throw '试图删除正式运行包。' } }
    if (-not (Test-Path -LiteralPath (Extended $Full))) { return }
    $Item = Get-Item -LiteralPath (Extended $Full) -Force
    $IsLink = (($Item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0)
    if ($Directory -and -not $IsLink) { Remove-Item -LiteralPath (Extended $Full) -Recurse -Force -Confirm:$false }
    else { Remove-Item -LiteralPath (Extended $Full) -Force -Confirm:$false }
    $script:Entries += [pscustomobject]@{ path=$Full; directory=$Directory; linkOnly=$IsLink; removed=$true }
}
# All final binaries have independent copies in the delivery folder.
foreach ($Version in $Report.PSObject.Properties.Name) {
    $Project = Join-Path $Working ("versions\1.1.9\mc"+$Version)
    foreach ($Name in @('.gradle','build','run','logs','dist')) { Remove-Checked (Join-Path $Project $Name) $Working }
}
if (-not (Test-Path -LiteralPath $Canonical)) {
    $ResolvedWorking = [IO.Path]::GetFullPath($Working)
    $ResolvedCanonical = [IO.Path]::GetFullPath($Canonical)
    if ($ResolvedWorking -ne 'H:\CodexWork\快捷建筑模组' -or -not $ResolvedCanonical.StartsWith([IO.Path]::GetFullPath($Delivery)+'\',[StringComparison]::OrdinalIgnoreCase)) { throw '源码移动路径核验失败。' }
    Move-Item -LiteralPath $ResolvedWorking -Destination $ResolvedCanonical
    New-Item -ItemType Junction -Path $Working -Target $Canonical | Out-Null
}
if ((& git -C $Canonical rev-parse HEAD).Trim() -ne $Published.commit) { throw 'H盘最终源码移动后校验失败。' }
$OldItem = Get-Item -LiteralPath $OldC -Force -ErrorAction SilentlyContinue
if ($OldItem -and (($OldItem.Attributes -band [IO.FileAttributes]::ReparsePoint) -eq 0)) {
    $Audit = Get-Content -LiteralPath (Join-Path $Delivery '清理前C盘清单.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([IO.Path]::GetFullPath($Audit.root) -ne $OldC) { throw '旧C盘工程清单根目录不匹配。' }
    # Remove junction entries themselves before recursively removing the old physical tree.
    foreach ($Link in $Audit.links) { Remove-Checked $Link $OldC }
    Remove-Checked $OldC $OldC
}
if (-not (Test-Path -LiteralPath $OldC)) { New-Item -ItemType Junction -Path $OldC -Target $Canonical | Out-Null }
# Dedicated historical workspaces. Shared cache targets remain available to other projects.
$OldWorks = @('快捷建筑_旧版蓝图操作_20261115','快捷建筑_蓝图操作链修复_20261115','快捷建筑_实例目录同步_20261115','快捷建筑_拖动与生成箱_20261114')
foreach ($Name in $OldWorks) { Remove-Checked (Join-Path 'H:\CodexWork' $Name) (Join-Path 'H:\CodexWork' $Name) }
$OldSettings = 'H:\CodexWork\快捷建筑_设置台修复_20261114'
if (Test-Path -LiteralPath $OldSettings) {
    foreach ($Child in Get-ChildItem -LiteralPath $OldSettings -Force) {
        if ($Child.Name -notin @('Gradle缓存','临时')) { Remove-Checked $Child.FullName $OldSettings ([bool]$Child.PSIsContainer) }
    }
}
$History = Split-Path -Parent $Delivery
foreach ($Child in Get-ChildItem -LiteralPath $History -Force) {
    if ($Child.FullName -eq $Delivery -or $Child.Name -eq '最新接管入口.md') { continue }
    Remove-Checked $Child.FullName $History $Child.PSIsContainer
}
# Keep exactly one formal runtime per active instance; remove only this mod's old files.
$ModFolders = @()
foreach ($Property in $Report.PSObject.Properties) { foreach ($Installation in $Property.Value.installations) { $ModFolders += $Installation.mods } }
$Extra = Get-Content -LiteralPath (Join-Path $Delivery '旧安装入口清单.json') -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($Item in $Extra) { $ModFolders += $Item.mods }
foreach ($Mods in ($ModFolders | Select-Object -Unique)) {
    foreach ($File in Get-ChildItem -LiteralPath $Mods -File -Filter 'quick-build*' -Force) {
        if ([IO.Path]::GetFullPath($File.FullName) -in $ProtectedRuntimes) { continue }
        Remove-Checked $File.FullName $Mods $false
    }
}
Remove-Checked (Join-Path $Delivery '安装过渡备份') (Join-Path $Delivery '安装过渡备份')
$Temps = Get-Content -LiteralPath (Join-Path $Delivery 'C盘项目专用测试临时目录.json') -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($Temp in $Temps.owned) { Remove-Checked $Temp 'C:\Users\quit5700\AppData\Local\Temp\quick-build-test' }
$References = Get-Content -LiteralPath (Join-Path $Canonical 'docs\MIGRATED_REFERENCE_FILES.json') -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($Original in $References) {
    $Saved = Join-Path $Canonical ('assets\user-reference\'+[IO.Path]::GetFileName($Original))
    if ((Test-Path -LiteralPath $Original) -and ((Get-FileHash -LiteralPath $Original -Algorithm SHA256).Hash -eq (Get-FileHash -LiteralPath $Saved -Algorithm SHA256).Hash)) { Remove-Checked $Original 'C:\Users\quit5700\AppData\Local\Temp' $false }
}
$FreeAfter = [IO.DriveInfo]::new('C').AvailableFreeSpace
$Result = [pscustomobject]@{ completed=$true; source=$Canonical; github=$Published.url; cFreeBefore=$FreeBefore; cFreeAfter=$FreeAfter; cRecoveredGiB=[Math]::Round(($FreeAfter-$FreeBefore)/1GB,3); removed=$Entries; sharedToolCachesPreserved=$true; originalGameSavesUnchanged=$true }
$Json = $Result | ConvertTo-Json -Depth 8
[IO.File]::WriteAllText((Join-Path $Delivery '清理完成报告.json'),$Json,[Text.UTF8Encoding]::new($false))
$Entry = "# 快捷建筑最新接管入口`n`n当前正式版：1.1.9。`n`n软件、最新完整源码、图标及验证记录：$Delivery`n`n源码实体：$Canonical。C盘原入口和H盘工作入口仅为目录链接。`n`nGitHub（代码托管平台）发行版：$($Published.url)`n"
[IO.File]::WriteAllText((Join-Path $History '最新接管入口.md'),$Entry,[Text.UTF8Encoding]::new($false))
Write-Output ("C盘释放GiB："+$Result.cRecoveredGiB)
Write-Output ("清理完成，源码实体："+$Canonical)
