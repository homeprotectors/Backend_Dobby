output "public_ip" {
  description = "Reserved public IPv4 address to place in DNS."
  value       = oci_core_public_ip.app.ip_address
}

output "ssh_command" {
  description = "SSH command for the OCI application host."
  value       = "ssh ubuntu@${oci_core_public_ip.app.ip_address}"
}

output "backup_bucket" {
  description = "Object Storage bucket receiving encrypted database backups."
  value       = oci_objectstorage_bucket.backups.name
}

output "state_bucket" {
  description = "Versioned Object Storage bucket for remote Terraform state."
  value       = oci_objectstorage_bucket.terraform_state.name
}

output "object_storage_namespace" {
  description = "OCI Object Storage namespace for configuring the remote backend."
  value       = data.oci_objectstorage_namespace.current.namespace
}

output "supabase_session_pooler_url_template" {
  description = "JDBC URL for the Supabase Seoul session pooler; set credentials separately."
  value       = "jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require"
}
