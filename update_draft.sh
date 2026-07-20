#!/bin/bash
# A script to grep all the rememberSaveable lines in PostAdDialog
grep -rn "var .* by rememberSaveable" app/src/main/java/com/example/MarketplaceApp.kt | grep -v "editingProduct"
