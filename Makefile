.PHONY: ship smoke

# Usage:
# make ship VERSION=4.0.5 SUMMARY="notes" EXTRA="--keep=3 --yes-purge"
ship:
	./scripts/ship.sh $(VERSION) "$(SUMMARY)" $(EXTRA)

smoke:
	./scripts/release-smoke.sh
