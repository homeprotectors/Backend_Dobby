variable "aws_region" {
  description = "AWS region containing the legacy DueIt production infrastructure."
  type        = string
  default     = "ap-northeast-2"

  validation {
    condition     = var.aws_region == "ap-northeast-2"
    error_message = "The legacy production region must be ap-northeast-2."
  }
}
