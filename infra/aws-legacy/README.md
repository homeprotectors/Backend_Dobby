# AWS legacy infrastructure

This directory preserves the read-only Terraform inventory that was previously
stored in `infra/prod`. Keep it until the OCI cutover has been stable for at
least one week and the final RDS snapshot and database dump have been verified.

Do not apply destructive changes from this directory. AWS retirement is a
separate, manually reviewed operation.
