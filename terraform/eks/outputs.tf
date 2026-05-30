output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "configure_kubectl" {
  description = "Run this to point kubectl at the new cluster"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

output "app_irsa_role_arn" {
  description = "Annotate the k8s ServiceAccount crm-aws with this (eks.amazonaws.com/role-arn)"
  value       = module.app_irsa.iam_role_arn
}

output "ecr_repository_urls" {
  description = "Push images here; then set them in k8s/kustomization.yaml"
  value       = { for k, r in aws_ecr_repository.services : k => r.repository_url }
}
