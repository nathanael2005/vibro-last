import re

with open('app/src/main/java/com/example/MarketplaceApp.kt', 'r') as f:
    lines = f.read().split('\n')

for i in range(len(lines)):
    line = lines[i]
    if "var " in line and "rememberSaveable" in line and "editingProduct" in line:
        # Extract variable name
        var_name = line.split("var ")[1].split(" by")[0]
        
        if var_name in ["currentSubView", "selectorTitle", "selectorOptions", "selectorSelectedValue", "selectorTargetProperty", "showPhoneNumber", "allowVibroChats", "allowWhatsApp", "selectedPremiumTier", "activeSubScreen", "title", "condition", "location", "imageUrl"]:
            continue
            
        if "price" in var_name:
            if "AdDraftStore" not in line:
                lines[i] = line.replace('?) ?: "") }', f'?) ?: AdDraftStore.getString("{var_name}", "")) }}')
            continue
        
        if "categoryId" in var_name:
            if "AdDraftStore" not in line:
                lines[i] = line.replace('?: "2")', '?: AdDraftStore.getString("categoryId", "2"))')
            continue

        if "description" in var_name:
            # handled later manually or skipped
            continue

        # For the others like vehicleBrand:
        # var vehicleBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("...").find(it)?.groupValues?.get(1)?.trim() } ?: "") }
        
        # We need to replace the last `?: "default") }` with `?: AdDraftStore.getString(var_name, "default")) }`
        if "?: " in line and "AdDraftStore" not in line:
            # find the last ?: 
            parts = line.rsplit("?: ", 1)
            if len(parts) == 2:
                default_val_with_bracket = parts[1]
                default_val = default_val_with_bracket.replace(") }", "").replace(")}", "").strip()
                lines[i] = parts[0] + f'?: AdDraftStore.getString("{var_name}", {default_val})) }}'

with open('app/src/main/java/com/example/MarketplaceApp.kt', 'w') as f:
    f.write("\n".join(lines))
