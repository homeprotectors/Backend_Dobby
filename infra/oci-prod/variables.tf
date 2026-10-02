variable "tenancy_ocid" {
  description = "OCI tenancy OCID."
  type        = string
}

variable "compartment_ocid" {
  description = "OCI compartment OCID for the production resources."
  type        = string
}

variable "oci_region" {
  description = "OCI home region. Tokyo is ap-tokyo-1."
  type        = string
  default     = "ap-tokyo-1"

  validation {
    condition     = var.oci_region == "ap-tokyo-1"
    error_message = "Production must be provisioned in the tenancy home region, Tokyo (ap-tokyo-1)."
  }
}

variable "availability_domain_index" {
  description = "Zero-based availability-domain index; change this when A1 capacity is unavailable."
  type        = number
  default     = 0

  validation {
    condition     = var.availability_domain_index >= 0 && floor(var.availability_domain_index) == var.availability_domain_index
    error_message = "availability_domain_index must be a non-negative integer."
  }
}

variable "ssh_public_key" {
  description = "OpenSSH public key installed for the ubuntu user."
  type        = string
  sensitive   = true
}

variable "ssh_allowed_cidr" {
  description = "Trusted IPv4 CIDR allowed to reach SSH. Never use 0.0.0.0/0 in production."
  type        = string

  validation {
    condition     = can(cidrnetmask(var.ssh_allowed_cidr)) && var.ssh_allowed_cidr != "0.0.0.0/0"
    error_message = "ssh_allowed_cidr must be a valid, restricted IPv4 CIDR."
  }
}

variable "instance_shape" {
  description = "Always Free Ampere shape."
  type        = string
  default     = "VM.Standard.A1.Flex"
}

variable "instance_ocpus" {
  description = "Ampere OCPUs allocated to the application."
  type        = number
  default     = 2


  validation {
    condition     = var.instance_ocpus > 0 && var.instance_ocpus <= 2
    error_message = "instance_ocpus must remain within the planned Always Free allocation (1-2 OCPUs)."
  }
}

variable "instance_memory_gbs" {
  description = "Ampere memory in GiB."
  type        = number
  default     = 12


  validation {
    condition     = var.instance_memory_gbs > 0 && var.instance_memory_gbs <= 12
    error_message = "instance_memory_gbs must remain within the planned Always Free allocation (up to 12 GiB)."
  }
}

variable "boot_volume_size_gbs" {
  description = "Boot volume size kept inside the Always Free block-volume allowance."
  type        = number
  default     = 50


  validation {
    condition     = var.boot_volume_size_gbs >= 50 && var.boot_volume_size_gbs <= 200
    error_message = "boot_volume_size_gbs must be between 50 and 200 GiB."
  }
}

variable "app_domain" {
  description = "Public API hostname for Caddy, without scheme."
  type        = string

  validation {
    condition     = can(regex("^[A-Za-z0-9](?:[A-Za-z0-9.-]*[A-Za-z0-9])?$", var.app_domain)) && !strcontains(var.app_domain, "://")
    error_message = "app_domain must be a hostname without a URL scheme or path."
  }
}

variable "backup_bucket_name" {
  description = "Globally unique Object Storage bucket name for encrypted database dumps."
  type        = string
}

variable "state_bucket_name" {
  description = "Private, versioned Object Storage bucket for Terraform state."
  type        = string
}

variable "backup_retention_days" {
  description = "Number of days to retain encrypted database dumps."
  type        = number
  default     = 14

  validation {
    condition     = var.backup_retention_days >= 7 && floor(var.backup_retention_days) == var.backup_retention_days
    error_message = "backup_retention_days must be an integer of at least 7 days."
  }
}

variable "budget_amount" {
  description = "Monthly OCI budget amount in the tenancy billing currency."
  type        = number
  default     = 5

  validation {
    condition     = var.budget_amount > 0
    error_message = "budget_amount must be greater than zero."
  }
}

variable "budget_alert_email" {
  description = "Email address that receives OCI cost alerts."
  type        = string
}

variable "manage_supabase_settings" {
  description = "Manage imported Supabase project settings. Enable only after setting SUPABASE_ACCESS_TOKEN."
  type        = bool
  default     = false
}

variable "supabase_access_token" {
  description = "Supabase personal access token, required only when managing project settings. Set with TF_VAR_supabase_access_token."
  type        = string
  sensitive   = true
  default     = "unused-when-settings-are-unmanaged"

  validation {
    condition     = !var.manage_supabase_settings || var.supabase_access_token != "unused-when-settings-are-unmanaged"
    error_message = "Set TF_VAR_supabase_access_token when manage_supabase_settings is true."
  }
}

variable "supabase_project_ref" {
  description = "Existing Supabase project reference created in ap-northeast-2."
  type        = string
  default     = ""

  validation {
    condition     = !var.manage_supabase_settings || length(var.supabase_project_ref) > 0
    error_message = "supabase_project_ref is required when manage_supabase_settings is true."
  }
}
