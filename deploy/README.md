# OCI production deployment

Copy this directory to `/srv/homeprotectors` over SSH from the restricted
operator address. Keep the host-managed `.env` and `secrets/` directory there.
Before the first deployment:

1. Copy `env.example` to `/srv/homeprotectors/.env` and fill every value.
2. Add `jwt_private.pem`, `jwt_public.pem`, `firebase-service-account.json`,
   `ghcr-deploy-token` (a GitHub token with read-only package access),
   `cloudflare-tunnel-token` (the existing Dueit tunnel token), and a strong
   random `backup-passphrase` under `/srv/homeprotectors/secrets`.
3. Set `.env`, the GHCR token, database password, and backup passphrase to
   mode `600`. The app container runs as UID 999, so set the three mounted
   JWT/Firebase files to owner `999:999` and mode `400` on the OCI host.
4. Install `systemd/homeprotectors-update.*` into `/etc/systemd/system/`
   and run `sudo systemctl daemon-reload`.
5. Set `GHCR_DEPLOY_USER` and `IMAGE_REPOSITORY` in `.env`, then run
   `sudo /srv/homeprotectors/scripts/check-update.sh` once. This pulls the
   latest published ARM64 image and enables the five-minute update timer.

Do not enable the update timer before the final database cutover: the production
app can run scheduled push jobs. Before cutover, build the current source as
`ghcr.io/homeprotectors/backend_dobby:prod-local` on the OCI host and run
`scripts/smoke-oci-local.sh`; its Compose override disables push jobs.

`api.dueit.date` is routed through the existing Cloudflare Tunnel, not a
Cloudflare A record. Production Compose binds the app only to localhost:8080
and starts `cloudflared` on the host network to reach it. The Caddy service is
reserved for the optional `direct` profile; the normal deployment does not
open the public web ports. Do not start the OCI tunnel replica while the AWS
connector is still serving traffic, or requests could reach both databases.
If the GHCR production tag has not been published yet, after the final restore
run `deploy.sh` with `DEPLOY_FROM_LOCAL=true`, `APP_VERSION=prod-local`, and
`IMAGE_REPOSITORY=ghcr.io/homeprotectors/backend_dobby` as root. This leaves
the update timer disabled until a published production image is available.

GitHub Actions publishes `prod-latest` to GHCR. The OCI host checks that tag
every five minutes and deploys it when its image changes. GitHub-hosted runner
IP addresses change, so the host polls outbound while inbound SSH stays
restricted to the operator CIDR. Copy this directory again when deployment
scripts or the Compose configuration change. After DNS points to OCI, run
`scripts/smoke-test.sh https://<APP_DOMAIN>` to verify the public HTTPS path.

`DATABASE_URL` is the JDBC URL used by Spring. `BACKUP_DATABASE_URL` must also be
added to `.env` as a libpq URL (`postgresql://...?...sslmode=require`) without a
password; the backup script reads `DATABASE_PASSWORD` separately. Object Storage coordinates are
injected into `/etc/homeprotectors/infra.env` by cloud-init. Set the optional
`BACKUP_FAILURE_WEBHOOK_URL` to receive systemd backup-failure notifications.

For the migration, use `restore-supabase.sh` on the empty project and compare
`table-counts.sh` results from AWS (`source`) and OCI (`target`). During the
planned write freeze, stop the OCI smoke app, take a fresh RDS dump, and run
`restore-supabase.sh --replace fresh.dump`. This refuses to replace a different
set of public tables and restores transactionally. Compare all rows and sequence
values again, then start the production image. Keep the AWS app read-only until
the domain switch has been verified.
Use `inspect-db.sh` before migration to record the RDS version, size, extensions,
tables, sequences, and active connections.
Run `restore-backup.sh` monthly with `RESTORE_DATABASE_URL` pointing to an empty,
disposable database; the script intentionally has no option to clean or overwrite
an existing production schema.
