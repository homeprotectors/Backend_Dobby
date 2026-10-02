# The project is deliberately created in the Supabase console so its database
# password never enters Terraform state. This resource manages only non-secret
# settings on that existing project. See README.md for the import sequence.
resource "supabase_settings" "production" {
  count = var.manage_supabase_settings ? 1 : 0

  project_ref = var.supabase_project_ref
  database = jsonencode({
    statement_timeout = "30s"
  })

  lifecycle {
    prevent_destroy = true
  }
}
