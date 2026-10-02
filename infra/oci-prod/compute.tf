resource "oci_core_instance" "app" {
  availability_domain = data.oci_identity_availability_domains.available.availability_domains[var.availability_domain_index].name
  compartment_id      = var.compartment_ocid
  display_name        = "homeprotectors-prod-app"
  shape               = var.instance_shape

  shape_config {
    ocpus         = var.instance_ocpus
    memory_in_gbs = var.instance_memory_gbs
  }

  create_vnic_details {
    assign_public_ip = false
    display_name     = "homeprotectors-prod-app-vnic"
    hostname_label   = "api"
    nsg_ids          = [oci_core_network_security_group.app.id]
    subnet_id        = oci_core_subnet.public.id
  }

  source_details {
    source_id               = data.oci_core_images.ubuntu.images[0].id
    source_type             = "image"
    boot_volume_size_in_gbs = var.boot_volume_size_gbs
    boot_volume_vpus_per_gb = 10
  }

  metadata = {
    ssh_authorized_keys = var.ssh_public_key
    user_data           = base64encode(local.cloud_init)
  }

  freeform_tags = local.common_tags
}

data "oci_core_vnic_attachments" "app" {
  availability_domain = oci_core_instance.app.availability_domain
  compartment_id      = var.compartment_ocid
  instance_id         = oci_core_instance.app.id
}

data "oci_core_private_ips" "app" {
  vnic_id = data.oci_core_vnic_attachments.app.vnic_attachments[0].vnic_id
}

resource "oci_core_public_ip" "app" {
  compartment_id = var.compartment_ocid
  display_name   = "homeprotectors-prod-public-ip"
  lifetime       = "RESERVED"
  private_ip_id  = one([for ip in data.oci_core_private_ips.app.private_ips : ip.id if ip.is_primary])

  freeform_tags = local.common_tags

  lifecycle {
    prevent_destroy = true
  }
}
