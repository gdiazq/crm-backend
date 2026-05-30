# EKS cluster — Terraform

Provisions the **infrastructure** to run the CRM microservices on EKS. The application
manifests themselves live in [`../../k8s/`](../../k8s) and are applied with kustomize after
the cluster exists — infra (Terraform) and app config (kustomize) stay cleanly separated.

## What it creates

| Resource | Purpose |
|---|---|
| **VPC** (`terraform-aws-modules/vpc`) | 3 AZs, public + private subnets, single NAT, ELB discovery tags |
| **EKS** (`terraform-aws-modules/eks` v20) | control plane + managed node group + core addons + OIDC (IRSA) |
| **ECR** ×8 | one repo per service image (`crm/mcsv-*`), scan-on-push, lifecycle expiry |
| **App IRSA role** | for the `crm:crm-aws` ServiceAccount — S3/SQS/SNS/EventBridge/SES, no static keys |
| **ALB controller** (Helm + IRSA) | turns the gateway Ingress into an ALB |

## Deploy order

```bash
# 1. Provision the cluster + ECR + ALB controller
cd terraform/eks
cp terraform.tfvars.example terraform.tfvars   # edit if needed
terraform init
terraform apply

# 2. Point kubectl at it (use the output)
$(terraform output -raw configure_kubectl)

# 3. Build + push images to the ECR repos (terraform output ecr_repository_urls)
#    (Dockerfiles already exist per service)

# 4. Wire IRSA into the app ServiceAccount, then deploy the app
#    Put `terraform output -raw app_irsa_role_arn` into k8s/shared/serviceaccount.yaml
#    annotation eks.amazonaws.com/role-arn, then:
kubectl create secret generic crm-secrets --from-env-file=../../.env -n crm
kubectl apply -k ../../k8s/
```

## Why the app isn't applied by Terraform

Stuffing Kubernetes manifests into Terraform couples two very different lifecycles (infra
changes are rare and risky; app deploys are frequent) and makes `terraform plan` noisy.
The clean pattern is:

- **Terraform** owns the cluster, networking, IAM/IRSA, ECR and the ALB controller.
- **kustomize / kubectl** owns the workloads (`../../k8s/`).
- For continuous delivery, drive the `k8s/` apply from CI or a GitOps controller
  (**Argo CD / Flux**) — not from Terraform.

The bridge between the two is the `app_irsa_role_arn` output → the `crm-aws` SA annotation.

## Notes

- State is local here; use an **S3 backend + DynamoDB lock** for real use.
- `single_nat_gateway = true` is cost-friendly for dev; switch to one-per-AZ for prod HA.
- Requires AWS creds with permissions to create VPC/EKS/IAM/ECR, plus `kubectl`, `helm`
  and the `aws` CLI locally.
