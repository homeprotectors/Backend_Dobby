resource "oci_core_vcn" "production" {
  compartment_id = var.compartment_ocid
  cidr_blocks    = ["10.20.0.0/16"]
  display_name   = "homeprotectors-prod-vcn"
  dns_label      = "homeprod"

  freeform_tags = local.common_tags
}

resource "oci_core_internet_gateway" "production" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.production.id
  display_name   = "homeprotectors-prod-igw"
  enabled        = true

  freeform_tags = local.common_tags
}

resource "oci_core_route_table" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.production.id
  display_name   = "homeprotectors-prod-public-routes"

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.production.id
  }

  freeform_tags = local.common_tags
}

resource "oci_core_security_list" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.production.id
  display_name   = "homeprotectors-prod-subnet-egress"

  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "all"
  }

  freeform_tags = local.common_tags
}

resource "oci_core_subnet" "public" {
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.production.id
  cidr_block                 = "10.20.10.0/24"
  display_name               = "homeprotectors-prod-public-subnet"
  dns_label                  = "app"
  prohibit_public_ip_on_vnic = false
  route_table_id             = oci_core_route_table.public.id
  security_list_ids          = [oci_core_security_list.public.id]

  freeform_tags = local.common_tags
}
