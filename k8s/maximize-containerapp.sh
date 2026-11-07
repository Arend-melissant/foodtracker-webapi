#!/usr/bin/env bash
# --------------------------------------------------------------
# Start the FoodTracker Azure Container App by scaling it back up.
# Adjust MIN_REPLICAS / MAX_REPLICAS to match your desired capacity.
# --------------------------------------------------------------

set -euo pipefail

# ---------- Configuration (adjust if needed) ----------
REGION="belgiumcentral"
RESOURCE_GROUP="foodtracker-rg"
CONTAINERAPP_NAME="foodtracker-webapi"

# Desired replica range when the app is running
MIN_REPLICAS=1
MAX_REPLICAS=1   # adjust based on expected load / cost constraints

# ---------- Helper ----------
info() { echo -e "[INFO] $*"; }

# ---------- Start (scale up) ----------
info "Starting Container App \"$CONTAINERAPP_NAME\" (scaling to $MIN_REPLICAS-$MAX_REPLICAS replicas)..."
az containerapp update \
  --resource-group "$RESOURCE_GROUP" \
  --name "$CONTAINERAPP_NAME" \
  --min-replicas "$MIN_REPLICAS" \
  --max-replicas "$MAX_REPLICAS"

info "Container App started."

