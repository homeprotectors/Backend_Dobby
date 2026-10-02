output "aws_account_id" {
  description = "AWS account ID selected by the active credentials."
  value       = data.aws_caller_identity.current.account_id
}

output "aws_region" {
  description = "AWS region selected by the provider."
  value       = data.aws_region.current.region
}
