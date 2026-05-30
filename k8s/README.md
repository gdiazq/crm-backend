# CRM backend on Kubernetes (EKS)

Manifests to run the microservices on EKS. Postgres (Supabase) and Redis (Upstash) stay
**external**, so the cluster runs only the stateless services.

## Layout

One folder per microservice, each with its `deployment.yaml` and `service.yaml` split out
(the gateway also has `ingress.yaml`). Shared cluster objects live under `shared/`.

```
k8s/
├── kustomization.yaml          # ties it together + centralizes the ECR registry
├── secret.example.yaml         # reference only — real Secret created from .env
├── shared/
│   ├── namespace.yaml          # crm namespace
│   ├── serviceaccount.yaml     # crm-aws SA (annotate for IRSA in prod)
│   └── configmap.yaml          # in-cluster discovery URLs (override .env localhost)
├── mcsv-eureka/                # deployment + service (TCP probes)
├── mcsv-config/                # deployment + service (TCP probes)
├── mcsv-auth/                  # deployment + service (HTTP /actuator/health, IRSA SA)
├── mcsv-user/                  # "
├── mcsv-rrhh/                  # "
├── mcsv-project/               # "
├── mcsv-recruitment/           # "
└── mcsv-gateway/               # deployment + service + ALB ingress
```

## Prerequisites on the cluster

- **EKS** cluster + `kubectl` context pointing at it.
- **AWS Load Balancer Controller** installed (the gateway `Ingress` needs it to create the ALB).
- Images built and pushed to **ECR** (see below).

## Deploy

```bash
# 1. Build + push images to ECR (one per service)
aws ecr get-login-password | docker login --username AWS --password-stdin <ECR_REGISTRY>
for s in eureka config gateway auth user rrhh project recruitment; do
  docker build -f mcsv-$s/Dockerfile -t <ECR_REGISTRY>/crm/mcsv-$s:latest .
  docker push <ECR_REGISTRY>/crm/mcsv-$s:latest
done

# 2. Point kustomize at your registry (edit kustomization.yaml, or:)
cd k8s && kustomize edit set image \
  crm/mcsv-auth=<ECR_REGISTRY>/crm/mcsv-auth:latest   # ...repeat per image

# 3. Create the namespace + secret from your existing .env
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic crm-secrets --from-env-file=.env -n crm

# 4. Apply everything
kubectl apply -k k8s/

# 5. Get the ALB address
kubectl get ingress mcsv-gateway -n crm
```

## How it maps from docker-compose

- **Service discovery**: kept Eureka (no code change). Pods register their IP
  (`prefer-ip-address: true`), reachable cluster-wide, so `lb://mcsv-*` keeps working.
  Eureka/config are also reachable by their Service DNS (`mcsv-eureka`, `mcsv-config`).
- **Config**: `CONFIG_SERVER_URI` / `EUREKA_SERVER` come from the ConfigMap (cluster DNS),
  overriding the `.env` localhost values. `envFrom` lists the Secret first, the ConfigMap
  second, so the ConfigMap wins for those keys.
- **Secrets**: created from `.env` (`REDIS_URL` → Upstash, DB → Supabase, AWS keys, JWT).
- **Ingress**: only the gateway is exposed (ALB); everything else is ClusterIP.

## Production hardening (talking points)

- **IRSA over static keys**: annotate `crm-aws` with an IAM role
  (`eks.amazonaws.com/role-arn`) and drop `AWS_ACCESS_KEY/SECRET` — pods assume the role.
- **External Secrets Operator**: sync `crm-secrets` from AWS Secrets Manager instead of
  `kubectl create secret`.
- **Autoscaling**: add an `HorizontalPodAutoscaler` (gateway already runs 2 replicas).
- **Drop Eureka**: on k8s you can use native Service DNS + Spring Cloud Kubernetes and
  remove the discovery server entirely.
- **TLS**: attach an ACM cert to the Ingress (`certificate-arn` annotation).
