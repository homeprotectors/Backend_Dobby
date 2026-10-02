terraform {
  backend "oci" {
    bucket    = "dueit-prod-terraform-state-zulfdzwjmowupwwmrael"
    namespace = "nrjnac83xd20"
    region    = "ap-tokyo-1"
    key       = "homeprotectors/production.tfstate"
  }
}
