# OCI + Supabase production infrastructure

This stack provisions the Tokyo OCI application host, its network controls,
reserved public IP, encrypted-backup bucket, instance-principal upload policy,
retention policy, and a cost alert. It intentionally does **not** create the
Supabase project because the required database password would be persisted in
Terraform state.

## Prerequisites

- Terraform 1.8 or newer and an OCI CLI profile with permission to manage
  networking, compute, Object Storage, IAM dynamic groups/policies, and budgets.
- A restricted operator CIDR for SSH.
- A Supabase Free project created in Seoul (`ap-northeast-2`). Select the IPv4
  Session Pooler on port 5432 in the Supabase Connect screen.
- The versioned OCI state bucket configured in `backend.tf`. This bucket was
  bootstrapped with local state and then migrated with `terraform init
  -migrate-state`. Never commit `.tfvars`, state, OCI API
  keys, Supabase tokens, database passwords, JWT keys, or Firebase credentials.

## Provision OCI

```bash
cd infra/oci-prod
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform fmt -check
terraform validate
terraform plan -out=production.tfplan
terraform apply production.tfplan
```

If A1 capacity is unavailable, change `availability_domain_index` and retry.
Do not destroy AWS resources until the new environment has run successfully for
at least one week.

The application host is in OCI Tokyo (`ap-tokyo-1`) while the database remains
in Supabase Seoul. Validate cross-region latency with the application smoke and
workflow tests before cutover; move the Supabase region only by creating and
restoring into a replacement project if latency is unacceptable.

Cloud-init installs the PostgreSQL 17 client from the official PGDG repository.
This is intentional: Ubuntu 24.04's distribution-default client may be an older
major release and cannot dump the PostgreSQL 17 RDS source safely.

After cloud-init finishes, create `/srv/homeprotectors/.env` and the secret
files described in `deploy/README.md`. The OCI host pulls images from GHCR;
GitHub Actions does not need inbound SSH access to it.

The existing `api.dueit.date` record points to a Cloudflare Tunnel. Its OCI
replica connects outbound, so the OCI network security group needs only the
operator-restricted SSH ingress rule; no public 80/443 ingress is required.

## Manage existing Supabase settings

The provider's project resource requires `database_password`, so it is not used.
To manage the non-secret settings of the console-created project:

```bash
export TF_VAR_supabase_access_token=replace-me
terraform import 'supabase_settings.production[0]' PROJECT_REF
terraform apply -var='manage_supabase_settings=true' -var='supabase_project_ref=PROJECT_REF'
```

Keep `manage_supabase_settings=true` in the encrypted workspace variables after
the import. The resource has `prevent_destroy` enabled.

## Cutover checklist

1. Confirm RDS size is comfortably below the Supabase Free limit and inventory
   PostgreSQL version, extensions, roles, sequences, indexes, and connections.
2. Restore a preliminary dump to the Supabase project with
   `deploy/scripts/restore-supabase.sh`, then compare source and target with
   `table-counts.sh`.
3. Run the OCI app using `smoke-oci-local.sh`; this disables push jobs. Verify
   health and schema validation before changing live traffic.
4. Confirm control of the existing `api.dueit.date` Cloudflare Tunnel,
   announce a write freeze, and stop writes and the tunnel connector on the AWS app. Create a
   final RDS snapshot and custom-format dump.
5. Stop the OCI smoke app, restore the final dump with
   `restore-supabase.sh --replace`, and compare every table row count and
   sequence value with the frozen source.
6. Start the OCI production app and its tunnel replica, verify that Cloudflare
   shows only the OCI replica, and run the public HTTPS smoke test. Publish the
   production ARM64 image and enable the update timer once CI is ready.
7. Keep AWS live but read-only for one week. Test an Object Storage restore before
   retiring RDS, EC2, ECR, and chargeable VPC resources in a separate review.

To test a backup restore, run `deploy/scripts/restore-backup.sh` with an encrypted
object name and a disposable, empty PostgreSQL database. Perform this test at
least monthly and record the result.
