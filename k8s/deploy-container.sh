#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Deploy FoodTracker WebAPI to Azure Container Apps (Belgium region).
# This script uses Azure CLI (az) to create the required resources and deploy
# the container image from Azure Container Registry (ACR).
#
# Prerequisites:
#   - Azure CLI installed and logged in (`az login`).
#   - Docker installed (for building the image locally before pushing).
#   - An Azure subscription with permission to create Resource Groups, ACR,
#     and Container Apps.
#   - Adjust the variables below as needed.
# ---------------------------------------------------------------------------

# need: az provider register -n Microsoft.OperationalInsights --wait

set -euo pipefail

# --------------------------- Configuration ---------------------------
# Azure region that corresponds to Belgium – the closest is "northeurope".
REGION="belgiumcentral"
# Names – change these if you want different identifiers.
RESOURCE_GROUP="foodtracker-rg"
CONTAINERAPP_ENV="foodtracker-env"
CONTAINERAPP_NAME="foodtracker-webapi"
# Image name (including tag) to be stored in ACR.
IMAGE_NAME="arendmelissant/foodtracker-webapi:latest"
# Path to Dockerfile (project root).
DOCKERFILE_PATH="."
STORAGE_ACCOUNT_NAME="foodtrackersa"
STORAGE_ACCOUNT_KEY="`az storage account keys list -n $STORAGE_ACCOUNT_NAME --query "[0].value" -o tsv`"
STORAGE_FILE_SHARE="foodtracker-share"

# ---------------------------------------------------------------

# Helper function for printing status.
info() { echo -e "[INFO] $*"; }
error() { echo -e "[ERROR] $*" >&2; exit 1; }

# 1. Create resource group if it does not exist.
info "Creating (or updating) resource group \"$RESOURCE_GROUP\" in $REGION..."
az group create --name "$RESOURCE_GROUP" --location "$REGION"

# 3. Create Container Apps environment (if not present).
info "Creating Container Apps environment \"$CONTAINERAPP_ENV\"..."
if ! az containerapp env show --name "$CONTAINERAPP_ENV" --resource-group "$RESOURCE_GROUP" > /dev/null 2>&1; then
  az containerapp env create \
    --name "$CONTAINERAPP_ENV" \
    --resource-group "$RESOURCE_GROUP" \
    --location "$REGION"
else
  info "Environment $CONTAINERAPP_ENV already exists."
fi

# 4. Deploy the Container App using the Docker Hub image.
info "Deploying Container App \"$CONTAINERAPP_NAME\" using image $IMAGE_NAME..."
if ! az containerapp show -g $RESOURCE_GROUP -n $CONTAINERAPP_NAME > /dev/null 2>&1; then
  az containerapp create \
    --name "$CONTAINERAPP_NAME" \
    --resource-group "$RESOURCE_GROUP" \
    --environment "$CONTAINERAPP_ENV" \
    --image "$IMAGE_NAME" \
    --ingress 'external' \
    --target-port 8080 \
    --min-replicas 1 --max-replicas 1 \
    --cpu 0.5 --memory 1.0Gi \
    --env-vars SERVER_HOST=0.0.0.0 SERVER_PORT=8080 DATABASE_URL=\"jdbc:sqlite:/app/data/foodtracker.db\"
else
  info "Container app $CONTAINERAPP_NAME already exists."
fi

az containerapp env storage set \
  --name "$CONTAINERAPP_ENV" \
  --resource-group "$RESOURCE_GROUP" \
  --storage-name "$STORAGE_ACCOUNT_NAME" \
  --azure-file-account-name "$STORAGE_ACCOUNT_NAME" \
  --azure-file-account-key "$STORAGE_ACCOUNT_KEY" \
  --azure-file-share-name "$STORAGE_FILE_SHARE" \
  --access-mode ReadWrite

info "Deployment completed!"
info "You can view the app with: az containerapp show -g $RESOURCE_GROUP -n $CONTAINERAPP_NAME"