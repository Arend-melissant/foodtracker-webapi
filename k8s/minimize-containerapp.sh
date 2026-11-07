#!/usr/bin/env bash
# --------------------------------------------------------------
# Stop the FoodTracker Azure Container App by scaling it to 0.
# This reduces compute cost when the service is not in use.
# --------------------------------------------------------------

set -euo pipefail

# ---------- Configuration (adjust if needed) ----------
REGION="belgiumcentral"
RESOURCE_GROUP="foodtracker-rg"
CONTAINERAPP_NAME="foodtracker-webapi"

# ---------- Helper ----------
info() { echo -e "[INFO] $*"; }

# ---------- Stop (scale to zero) ----------
info "Stopping Container App \"$CONTAINERAPP_NAME\" (scaling to 0 replicas)..."
az containerapp update \
  --resource-group "$RESOURCE_GROUP" \
  --name "$CONTAINERAPP_NAME" \
  --min-replicas 0 \
  --max-replicas 1

info "Container App stop request sent. It will scale down to 0 when idle."