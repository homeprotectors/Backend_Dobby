[CmdletBinding()]
param(
    [string]$Profile = "dueit-local",
    [string]$Region = "ap-northeast-2",
    [string]$AccountId = "146414906510"
)

$ErrorActionPreference = "Stop"
$OutputDirectory = Join-Path $PSScriptRoot "..\docs\inventory.raw"
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

function Save-AwsJson {
    param(
        [Parameter(Mandatory)]
        [string]$Name,
        [Parameter(Mandatory)]
        [string[]]$Arguments,
        [switch]$AllowMissing
    )

    $output = & aws @Arguments --profile $Profile --region $Region --output json
    if ($LASTEXITCODE -ne 0) {
        if ($AllowMissing) {
            Write-Warning "Optional AWS read was unavailable for $Name."
            return $null
        }
        throw "AWS CLI read failed for $Name."
    }

    $path = Join-Path $OutputDirectory "$Name.json"
    $output | Set-Content -Path $path -Encoding utf8
    return $output | ConvertFrom-Json
}

$identity = Save-AwsJson -Name "caller-identity" -Arguments @("sts", "get-caller-identity")
if ($identity.Account -ne $AccountId) {
    throw "Expected AWS account $AccountId but authenticated to $($identity.Account)."
}

$repository = Save-AwsJson -Name "ecr-repository" -Arguments @("ecr", "describe-repositories", "--repository-names", "homeprotectors")
$repositoryArn = $repository.Repositories[0].RepositoryArn
Save-AwsJson -Name "ecr-tags" -Arguments @("ecr", "list-tags-for-resource", "--resource-arn", $repositoryArn) | Out-Null
Save-AwsJson -Name "ecr-repository-policy" -Arguments @("ecr", "get-repository-policy", "--repository-name", "homeprotectors") -AllowMissing | Out-Null
Save-AwsJson -Name "ecr-lifecycle-policy" -Arguments @("ecr", "get-lifecycle-policy", "--repository-name", "homeprotectors") -AllowMissing | Out-Null

$instance = Save-AwsJson -Name "ec2-instance" -Arguments @("ec2", "describe-instances", "--instance-ids", "i-01abcb431e529fad4")
$volumeIds = @($instance.Reservations.Instances.BlockDeviceMappings.Ebs.VolumeId)
if ($volumeIds.Count -gt 0) {
    Save-AwsJson -Name "ec2-volumes" -Arguments (@("ec2", "describe-volumes", "--volume-ids") + $volumeIds) | Out-Null
}

Save-AwsJson -Name "ec2-instance-profile-association" -Arguments @("ec2", "describe-iam-instance-profile-associations", "--filters", "Name=instance-id,Values=i-01abcb431e529fad4") | Out-Null
Save-AwsJson -Name "security-group" -Arguments @("ec2", "describe-security-groups", "--group-ids", "sg-049412302037542d8") | Out-Null
Save-AwsJson -Name "security-group-network-interfaces" -Arguments @("ec2", "describe-network-interfaces", "--filters", "Name=group-id,Values=sg-049412302037542d8") | Out-Null

$instanceProfile = Save-AwsJson -Name "ec2-instance-profile" -Arguments @("iam", "get-instance-profile", "--instance-profile-name", "EC2-ECRReadOnlyRole")
$ec2RoleName = $instanceProfile.InstanceProfile.Roles[0].RoleName
if (-not $ec2RoleName) {
    throw "Instance profile EC2-ECRReadOnlyRole has no IAM role."
}

Save-AwsJson -Name "ec2-role" -Arguments @("iam", "get-role", "--role-name", $ec2RoleName) | Out-Null
$ec2Attached = Save-AwsJson -Name "ec2-role-attached-policies" -Arguments @("iam", "list-attached-role-policies", "--role-name", $ec2RoleName)
$ec2Inline = Save-AwsJson -Name "ec2-role-inline-policies" -Arguments @("iam", "list-role-policies", "--role-name", $ec2RoleName)

foreach ($policyName in $ec2Inline.PolicyNames) {
    Save-AwsJson -Name "ec2-role-inline-$policyName" -Arguments @("iam", "get-role-policy", "--role-name", $ec2RoleName, "--policy-name", $policyName) | Out-Null
}

$oidcArn = "arn:aws:iam::${AccountId}:oidc-provider/token.actions.githubusercontent.com"
Save-AwsJson -Name "oidc-providers" -Arguments @("iam", "list-open-id-connect-providers") | Out-Null
Save-AwsJson -Name "github-oidc-provider" -Arguments @("iam", "get-open-id-connect-provider", "--open-id-connect-provider-arn", $oidcArn) | Out-Null

$githubRoleName = "GitHubActionsPushToECR"
Save-AwsJson -Name "github-actions-role" -Arguments @("iam", "get-role", "--role-name", $githubRoleName) | Out-Null
$githubAttached = Save-AwsJson -Name "github-actions-role-attached-policies" -Arguments @("iam", "list-attached-role-policies", "--role-name", $githubRoleName)
$githubInline = Save-AwsJson -Name "github-actions-role-inline-policies" -Arguments @("iam", "list-role-policies", "--role-name", $githubRoleName)

foreach ($policyName in $githubInline.PolicyNames) {
    Save-AwsJson -Name "github-actions-role-inline-$policyName" -Arguments @("iam", "get-role-policy", "--role-name", $githubRoleName, "--policy-name", $policyName) | Out-Null
}

$managedPolicies = @($ec2Attached.AttachedPolicies) + @($githubAttached.AttachedPolicies)
foreach ($policy in $managedPolicies) {
    $safeName = ($policy.PolicyArn -replace "[^A-Za-z0-9_.-]", "_")
    $policyMetadata = Save-AwsJson -Name "managed-policy-$safeName" -Arguments @("iam", "get-policy", "--policy-arn", $policy.PolicyArn)
    Save-AwsJson -Name "managed-policy-version-$safeName" -Arguments @("iam", "get-policy-version", "--policy-arn", $policy.PolicyArn, "--version-id", $policyMetadata.Policy.DefaultVersionId) | Out-Null
    Save-AwsJson -Name "managed-policy-attachments-$safeName" -Arguments @("iam", "list-entities-for-policy", "--policy-arn", $policy.PolicyArn) | Out-Null
}

Write-Host "Read-only inventory written to $OutputDirectory"
