locals {
  common_tags = {
    application = "homeprotectors"
    environment = "production"
    managed-by  = "terraform"
  }

  cloud_init = templatefile("${path.module}/templates/cloud-init.yaml", {
    app_domain       = var.app_domain
    backup_bucket    = var.backup_bucket_name
    backup_namespace = data.oci_objectstorage_namespace.current.namespace
    backup_region    = var.oci_region
  })
}
