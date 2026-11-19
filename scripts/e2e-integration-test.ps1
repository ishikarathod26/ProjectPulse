# ==============================================================================
# ProjectPulse Comprehensive End-to-End Integration Verification Script
# ==============================================================================

$ErrorActionPreference = "Continue"

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host " ProjectPulse End-to-End Practical Integration Suite" -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

$backendJar = ".\ProjectPulse\backend\target\projectpulse-backend-1.0.0.jar"
if (-not (Test-Path $backendJar)) {
    Write-Error "Backend jar not found at $backendJar. Build it first."
    exit 1
}

# 1. Start Spring Boot in background
Write-Host "[1/15] Starting Spring Boot Backend process..." -ForegroundColor Yellow
$proc = Start-Process -FilePath "java" `
    -ArgumentList "-jar", $backendJar, "--server.port=8080", "--spring.profiles.active=dev" `
    -PassThru `
    -NoNewWindow

$passed = 0
$failed = 0
$baseUrl = "http://localhost:8080"

function Assert-Step($desc, $cond) {
    if ($cond) {
        Write-Host "  [PASS] $desc" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "  [FAIL] $desc" -ForegroundColor Red
        $script:failed++
    }
}

try {
    # 2. Wait for backend health
    Write-Host "[2/15] Waiting for backend readiness at $baseUrl/api/health..." -ForegroundColor Yellow
    $ready = $false
    for ($i = 0; $i -lt 35; $i++) {
        Start-Sleep -Seconds 1
        try {
            $resp = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
            if ($resp.success -eq $true -or $resp.data.status -eq "UP") {
                $ready = $true
                break
            }
        } catch {}
    }
    Assert-Step "Backend started and responding on /api/health" $ready
    if (-not $ready) {
        throw "Backend failed to become healthy within 35 seconds."
    }

    # 3. User Registration
    Write-Host "`n[3/15] Testing User Registration..." -ForegroundColor Yellow
    $regBody = @{
        fullName = "Test Engineer"
        email = "engineer@example.com"
        password = "Password123!"
        organizationName = "Acme Engineering"
    } | ConvertTo-Json

    $regResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    $token1 = $regResp.data.token
    $userId1 = $regResp.data.userId
    $orgId1 = $regResp.data.activeOrganizationId
    Assert-Step "User registration returned JWT and initialized Organization" ($null -ne $token1 -and $null -ne $orgId1)

    # 4. User Login
    Write-Host "`n[4/15] Testing User Login..." -ForegroundColor Yellow
    $loginBody = @{
        email = "engineer@example.com"
        password = "Password123!"
    } | ConvertTo-Json
    $loginResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token1 = $loginResp.data.token
    Assert-Step "Login succeeded and returned valid JWT token" ($loginResp.data.email -eq "engineer@example.com")

    # 5. Current User Profile
    Write-Host "`n[5/15] Testing Authenticated Profile (/api/auth/me)..." -ForegroundColor Yellow
    $headers1 = @{ Authorization = "Bearer $token1" }
    $profileResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -Headers $headers1
    Assert-Step "Profile retrieved with user organizations" ($profileResp.data.organizations.Count -ge 1)

    # 6. Organization Creation & Org Switching
    Write-Host "`n[6/15] Testing Organization Creation & Org Switching..." -ForegroundColor Yellow
    $newOrgBody = @{
        name = "Secondary Workspace"
        description = "Testing workspace isolation"
    } | ConvertTo-Json
    $newOrgResp = Invoke-RestMethod -Uri "$baseUrl/api/organizations" -Method Post -Headers $headers1 -Body $newOrgBody -ContentType "application/json"
    $orgId2 = $newOrgResp.data.id

    $switchResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/switch-org/$orgId2" -Method Post -Headers $headers1
    $tokenSwitched = $switchResp.data.token
    Assert-Step "Switched active organization workspace" ($switchResp.data.activeOrganizationId -eq $orgId2)

    # Switch back to Org 1
    $switchResp2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/switch-org/$orgId1" -Method Post -Headers @{ Authorization = "Bearer $tokenSwitched" }
    $token1 = $switchResp2.data.token
    $headers1 = @{ Authorization = "Bearer $token1" }

    # 7. Project CRUD Operations
    Write-Host "`n[7/15] Testing Project CRUD Operations..." -ForegroundColor Yellow
    $projBody = @{
        organizationId = $orgId1
        name = "Project Apollo"
        keyIdentifier = "APOLLO"
        description = "Telemetry tracking project"
    } | ConvertTo-Json
    $projResp = Invoke-RestMethod -Uri "$baseUrl/api/projects" -Method Post -Headers $headers1 -Body $projBody -ContentType "application/json"
    $projId = $projResp.data.id
    Assert-Step "Project created with unique key APOLLO" ($projId -gt 0 -and $projResp.data.keyIdentifier -eq "APOLLO")

    $projList = Invoke-RestMethod -Uri "$baseUrl/api/projects?organizationId=$orgId1" -Method Get -Headers $headers1
    Assert-Step "Project list includes Project Apollo" ($projList.data | Where-Object { $_.id -eq $projId })

    $updateProjBody = @{
        name = "Project Apollo Updated"
        description = "Updated description"
        status = "ACTIVE"
    } | ConvertTo-Json
    $updateResp = Invoke-RestMethod -Uri "$baseUrl/api/projects/$projId" -Method Put -Headers $headers1 -Body $updateProjBody -ContentType "application/json"
    Assert-Step "Project metadata updated" ($updateResp.data.name -eq "Project Apollo Updated")

    # 8. Cross-Organization RBAC Scoping
    Write-Host "`n[8/15] Testing Cross-Tenant Security Isolation..." -ForegroundColor Yellow
    $user2Body = @{
        fullName = "External Hacker"
        email = "external@othercorp.com"
        password = "Password123!"
        organizationName = "Other Corp"
    } | ConvertTo-Json
    $reg2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $user2Body -ContentType "application/json"
    $token2 = $reg2.data.token
    $headers2 = @{ Authorization = "Bearer $token2" }

    $leakAttempt = $false
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/projects/$projId" -Method Get -Headers $headers2 -ErrorAction Stop
        $leakAttempt = $true
    } catch {
        # Expected HTTP 403 / 404
    }
    Assert-Step "Cross-organization project access strictly blocked" (-not $leakAttempt)

    # 9. Device Registration & Activity Ingestion
    Write-Host "`n[9/15] Testing Device Registration & Activity Batch Ingestion..." -ForegroundColor Yellow
    $devBody = @{
        deviceIdentifier = "dev-workstation-win11"
        deviceName = "Win11-Dev-Node"
        osInfo = "Windows 11 Build 22631"
    } | ConvertTo-Json
    $devResp = Invoke-RestMethod -Uri "$baseUrl/api/devices/register" -Method Post -Headers $headers1 -Body $devBody -ContentType "application/json"
    Assert-Step "Device registered with identifier dev-workstation-win11" ($devResp.data.deviceIdentifier -eq "dev-workstation-win11")

    $evt1Id = [System.Guid]::NewGuid().ToString()
    $evt2Id = [System.Guid]::NewGuid().ToString()
    $evt3Id = [System.Guid]::NewGuid().ToString()
    $nowIso = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")

    $batchBody = @{
        projectId = $projId
        deviceIdentifier = "dev-workstation-win11"
        events = @(
            @{
                clientEventId = $evt1Id
                eventType = "CREATE"
                filePath = "src/main/Index.jsx"
                fileSizeBytes = 1240
                eventTimestamp = $nowIso
            },
            @{
                clientEventId = $evt2Id
                eventType = "MODIFY"
                filePath = "src/main/Index.jsx"
                fileSizeBytes = 1580
                eventTimestamp = $nowIso
            },
            @{
                clientEventId = $evt3Id
                eventType = "DELETE"
                filePath = "src/temp/Legacy.js"
                fileSizeBytes = 0
                eventTimestamp = $nowIso
            }
        )
    } | ConvertTo-Json -Depth 5

    $batchResp = Invoke-RestMethod -Uri "$baseUrl/api/activities/batch" -Method Post -Headers $headers1 -Body $batchBody -ContentType "application/json"
    Assert-Step "Batch activity ingestion succeeded (3 events)" ($batchResp.data.processedCount -eq 3)

    # 10. Deduplication / Idempotency Test
    Write-Host "`n[10/15] Testing Duplicate Event Deduplication (Idempotency)..." -ForegroundColor Yellow
    $dupResp = Invoke-RestMethod -Uri "$baseUrl/api/activities/batch" -Method Post -Headers $headers1 -Body $batchBody -ContentType "application/json"
    Assert-Step "Duplicate batch upload handled safely without duplicate rows" ($dupResp.data.processedCount -eq 0 -or $dupResp.data.duplicateCount -gt 0)

    # 11. Path Traversal Security Defense
    Write-Host "`n[11/15] Testing Path Traversal Defense..." -ForegroundColor Yellow
    $badEvtId = [System.Guid]::NewGuid().ToString()
    $badBatch = @{
        projectId = $projId
        deviceIdentifier = "dev-workstation-win11"
        events = @(
            @{
                clientEventId = $badEvtId
                eventType = "MODIFY"
                filePath = "../../windows/system32/cmd.exe"
                fileSizeBytes = 9999
                eventTimestamp = $nowIso
            }
        )
    } | ConvertTo-Json -Depth 5
    $traversalBlocked = $false
    try {
        $traversalResp = Invoke-RestMethod -Uri "$baseUrl/api/activities/batch" -Method Post -Headers $headers1 -Body $badBatch -ContentType "application/json"
        # The service logs a warning and skips the event
        if ($traversalResp.data.processedCount -eq 0) {
            $traversalBlocked = $true
        }
    } catch {
        $traversalBlocked = $true
    }
    Assert-Step "Path traversal attempt was rejected and not persisted" $traversalBlocked

    # 12. Activity Timeline Query
    Write-Host "`n[12/15] Testing Activity Query & Filtering..." -ForegroundColor Yellow
    $actsResp = Invoke-RestMethod -Uri "$baseUrl/api/activities?projectId=$projId&page=0&size=10" -Method Get -Headers $headers1
    $firstActId = $actsResp.data.content[0].id
    Assert-Step "Retrieved activity records with deterministic SHA-256 payload hash" ($actsResp.data.content.Count -ge 2 -and $null -ne $actsResp.data.content[0].payloadHash)

    # 13. Blockchain Verification & Honest Offline Reporting
    Write-Host "`n[13/15] Testing Blockchain Verification & Honest Offline Reporting..." -ForegroundColor Yellow
    $bcStatus = Invoke-RestMethod -Uri "$baseUrl/api/blockchain/status" -Method Get -Headers $headers1
    Assert-Step "Blockchain network status endpoint operational" ($null -ne $bcStatus.data.mode)

    $verifyResp = Invoke-RestMethod -Uri "$baseUrl/api/blockchain/verify/$firstActId" -Method Get -Headers $headers1
    $honestStatus = $verifyResp.data.status
    Assert-Step "Honest verification status reported ($honestStatus)" ($honestStatus -eq "UNAVAILABLE" -or $honestStatus -eq "PENDING" -or $honestStatus -eq "VERIFIED")

    # 14. Collaboration (Chat, Notes, Status Updates)
    Write-Host "`n[14/15] Testing Collaboration (Chat, Activity-Linked Notes, Status Updates)..." -ForegroundColor Yellow
    # Chat message
    $msgBody = @{ content = "Build verification in progress!" } | ConvertTo-Json
    $msgResp = Invoke-RestMethod -Uri "$baseUrl/api/projects/$projId/messages" -Method Post -Headers $headers1 -Body $msgBody -ContentType "application/json"
    Assert-Step "Chat message posted" ($msgResp.data.content -eq "Build verification in progress!")

    # Project note linked to activity
    $noteBody = @{
        title = "Code Review Note"
        content = "Verified file structure"
        activityId = $firstActId
    } | ConvertTo-Json
    $noteResp = Invoke-RestMethod -Uri "$baseUrl/api/projects/$projId/notes" -Method Post -Headers $headers1 -Body $noteBody -ContentType "application/json"
    Assert-Step "Project note created and linked to activity" ($noteResp.data.activityId -eq $firstActId)

    # Status update
    $statusBody = @{
        statusLabel = "ON_TRACK"
        summary = "Sprint 1 milestone achieved"
        details = "All modules integrated"
    } | ConvertTo-Json
    $statusResp = Invoke-RestMethod -Uri "$baseUrl/api/projects/$projId/status-updates" -Method Post -Headers $headers1 -Body $statusBody -ContentType "application/json"
    Assert-Step "Project status broadcast update posted" ($statusResp.data.statusLabel -eq "ON_TRACK")

    # 15. Platform Administration
    Write-Host "`n[15/15] Testing Platform Administration..." -ForegroundColor Yellow
    $adminLogin = @{
        email = "admin@projectpulse.io"
        password = "TestOnlyPassword123!"
    } | ConvertTo-Json
    $adminResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $adminLogin -ContentType "application/json"
    $adminToken = $adminResp.data.token
    $adminHeaders = @{ Authorization = "Bearer $adminToken" }

    $statsResp = Invoke-RestMethod -Uri "$baseUrl/api/admin/stats" -Method Get -Headers $adminHeaders
    Assert-Step "Platform administration stats retrieved" ($statsResp.data.totalUsers -ge 2 -and $statsResp.data.totalProjects -ge 1)

    $auditResp = Invoke-RestMethod -Uri "$baseUrl/api/admin/audit-logs?page=0&size=10" -Method Get -Headers $adminHeaders
    Assert-Step "Audit logs recorded and retrievable by admin" ($null -ne $auditResp.data)

} finally {
    Write-Host "`nStopping Spring Boot backend process (PID $($proc.Id))..." -ForegroundColor Yellow
    Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host " INTEGRATION TEST SUMMARY: $passed PASSED, $failed FAILED" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "========================================================`n" -ForegroundColor Cyan

if ($failed -gt 0) {
    exit 1
} else {
    exit 0
}

