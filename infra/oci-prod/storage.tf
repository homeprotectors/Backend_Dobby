resource "oci_objectstorage_bucket" "backups" {
  compartment_id = var.compartment_ocid
  namespace      = data.oci_objectstorage_namespace.current.namespace
  name           = var.backup_bucket_name
  access_type    = "NoPublicAccess"
  storage_tier   = "Standard"
  versioning     = "Enabled"

  freeform_tags = local.common_tags

  lifecycle {
    prevent_destroy = true
  }
}

resource "oci_objectstorage_bucket" "terraform_state" {
  compartment_id = var.compartment_ocid
  namespace      = data.oci_objectstorage_namespace.current.namespace
  name           = var.state_bucket_name
  access_type    = "NoPublicAccess"
  storage_tier   = "Standard"
  versioning     = "Enabled"

  freeform_tags = local.common_tags

  lifecycle {
    prevent_destroy = true
  }
}

resource "oci_objectstorage_object_lifecycle_policy" "backups" {
  namespace = data.oci_objectstorage_namespace.current.namespace
  bucket    = oci_objectstorage_bucket.backups.name

  depends_on = [oci_identity_policy.object_lifecycle]

  rules {
    action      = "DELETE"
    is_enabled  = true
    name        = "delete-old-database-backups"
    target      = "objects"
    time_amount = var.backup_retention_days
    time_unit   = "DAYS"
  }
}

resource "oci_identity_policy" "object_lifecycle" {
  compartment_id = var.tenancy_ocid
  name           = "homeprotectors_prod_backup_lifecycle"
  description    = "Allow Tokyo Object Storage to delete expired backups in this bucket."
  statements = [
    "Allow service objectstorage-${var.oci_region} to manage object-family in compartment id ${var.compartment_ocid} where all {target.bucket.name='${oci_objectstorage_bucket.backups.name}', any {request.permission='BUCKET_INSPECT', request.permission='BUCKET_READ', request.permission='OBJECT_INSPECT', request.permission='OBJECT_DELETE', request.permission='OBJECT_VERSION_DELETE'}}"
  ]
}

resource "oci_identity_dynamic_group" "backup_uploader" {
  compartment_id = var.tenancy_ocid
  name           = "homeprotectors_prod_backup_uploader"
  description    = "Production application instance allowed to upload database backups."
  matching_rule  = "instance.id = '${oci_core_instance.app.id}'"
}

resource "oci_identity_policy" "backup_uploader" {
  compartment_id = var.compartment_ocid
  name           = "homeprotectors_prod_backup_upload"
  description    = "Permit the production instance to manage objects only in its backup bucket."
  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.backup_uploader.name} to manage objects in compartment id ${var.compartment_ocid} where target.bucket.name='${oci_objectstorage_bucket.backups.name}'"
  ]
}

resource "oci_budget_budget" "production" {
  compartment_id = var.tenancy_ocid
  amount         = var.budget_amount
  reset_period   = "MONTHLY"
  display_name   = "homeprotectors-production-budget"
  description    = "Alert when non-free OCI usage appears."
  target_type    = "COMPARTMENT"
  targets        = [var.compartment_ocid]
}

resource "oci_budget_alert_rule" "actual_spend" {
  budget_id      = oci_budget_budget.production.id
  display_name   = "homeprotectors-actual-spend"
  description    = "Actual production OCI spend reached 20 percent of the monthly budget."
  message        = "Homeprotectors OCI production resources are incurring charges."
  recipients     = var.budget_alert_email
  threshold      = 20
  threshold_type = "PERCENTAGE"
  type           = "ACTUAL"
}
