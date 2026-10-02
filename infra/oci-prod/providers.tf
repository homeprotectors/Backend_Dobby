provider "oci" {
  region = var.oci_region
}

# The OCI stack can run before Supabase management is enabled. Supply the real
# token through TF_VAR_supabase_access_token only when importing settings.
provider "supabase" {
  access_token = var.supabase_access_token
}

data "oci_identity_availability_domains" "available" {
  compartment_id = var.tenancy_ocid
}

data "oci_objectstorage_namespace" "current" {
  compartment_id = var.compartment_ocid
}

data "oci_core_images" "ubuntu" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Canonical Ubuntu"
  operating_system_version = "24.04"
  shape                    = var.instance_shape
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"
}
