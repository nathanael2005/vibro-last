import re

with open('app/src/main/java/com/example/MarketplaceApp.kt', 'r') as f:
    content = f.read()

# We need to find the PostAdDialog and replace the initialization of variables.
# For example:
# var categoryId by rememberSaveable { mutableStateOf(editingProduct?.categoryId ?: "2") }
# becomes:
# var categoryId by rememberSaveable { mutableStateOf(editingProduct?.categoryId ?: AdDraftStore.getString("categoryId", "2")) }

lines = content.split("\n")
inside_postad = False
out_lines = []
for i, line in enumerate(lines):
    if "fun PostAdDialog(" in line:
        inside_postad = True
    
    if inside_postad and "var " in line and "rememberSaveable" in line:
        # Match: var name by rememberSaveable { mutableStateOf(editingProduct?.some ?: "default") }
        # or var name by rememberSaveable { mutableStateOf("default") }
        
        # We only want to modify variables that are part of the draft
        if any(v in line for v in ["wizardStep", "currentSubView", "selectorTitle", "selectorOptions", "selectorSelectedValue", "selectorTargetProperty", "showPhoneNumber", "allowVibroChats", "allowWhatsApp", "selectedPremiumTier", "activeSubScreen"]):
            pass
        elif "editingProduct" in line:
            # It already has editingProduct fallback, let's extract it.
            # Example: var title by rememberSaveable { mutableStateOf(editingProduct?.title ?: "") }
            # Match: editingProduct\?\.([a-zA-Z0-9]+)\s*\?:\s*(.*)\)
            match = re.search(r'editingProduct\?\.([a-zA-Z0-9]+)(\?\.[a-zA-Z0-9_\(\)\".]+)?\s*\?:\s*(.*)\)\s*}', line)
            if not match:
                 # It might be the complex description or price one
                 if "price" in line and "replace" in line:
                     line = line.replace('?) ?: "") }', '?) ?: AdDraftStore.getString("price", "")) }')
                 elif "description" in line and "desc.substringAfter" in line:
                     pass # handled manually below
                 elif "location" in line:
                     line = line.replace('?: "Addis Ababa, Bole")', '?: AdDraftStore.getString("location", "Addis Ababa, Bole"))')
                 elif "condition" in line:
                     line = line.replace('?: "New")', '?: AdDraftStore.getString("condition", "New"))')
                 elif "imageUrl" in line:
                     line = line.replace('?: "")', '?: AdDraftStore.getString("imageUrl", ""))')
                 elif "categoryId" in line:
                     line = line.replace('?: "2")', '?: AdDraftStore.getString("categoryId", "2"))')
                 elif "title" in line:
                     line = line.replace('?: "")', '?: AdDraftStore.getString("title", ""))')
            else:
                 # general match
                 prop = match.group(1)
                 default_val = match.group(3)
                 # Handle specific generic ones
                 var_name = line.split("var ")[1].split(" by ")[0].strip()
                 line = line.replace(default_val, f'AdDraftStore.getString("{var_name}", {default_val})')
        else:
            pass # others we don't care

    out_lines.append(line)

with open('app/src/main/java/com/example/MarketplaceApp.kt', 'w') as f:
    f.write("\n".join(out_lines))

