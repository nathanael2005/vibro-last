with open('app/src/main/java/com/example/MarketplaceApp.kt', 'r') as f:
    lines = f.read().split('\n')

effect_code = """
    DisposableEffect(Unit) {
        onDispose {
            if (editingProduct == null) {
                AdDraftStore.data["categoryId"] = categoryId
                AdDraftStore.data["subcategory"] = subcategory
                AdDraftStore.data["title"] = title
                AdDraftStore.data["price"] = price
                AdDraftStore.data["condition"] = condition
                AdDraftStore.data["location"] = location
                AdDraftStore.data["imageUrl"] = imageUrl
                AdDraftStore.data["description"] = description
                
                AdDraftStore.data["vehicleBrand"] = vehicleBrand
                AdDraftStore.data["vehicleModel"] = vehicleModel
                AdDraftStore.data["vehicleYear"] = vehicleYear
                AdDraftStore.data["vehicleTransmission"] = vehicleTransmission
                AdDraftStore.data["vehicleFuel"] = vehicleFuel
                
                AdDraftStore.data["phoneBrand"] = phoneBrand
                AdDraftStore.data["phoneStorage"] = phoneStorage
                AdDraftStore.data["phoneRam"] = phoneRam
                AdDraftStore.data["phoneColor"] = phoneColor
                
                AdDraftStore.data["propertyBedrooms"] = propertyBedrooms
                AdDraftStore.data["propertyBathrooms"] = propertyBathrooms
                AdDraftStore.data["propertyFurnishing"] = propertyFurnishing
                
                AdDraftStore.data["fashionGender"] = fashionGender
                AdDraftStore.data["fashionSize"] = fashionSize
                AdDraftStore.data["fashionBrand"] = fashionBrand
                
                AdDraftStore.data["furnitureMaterial"] = furnitureMaterial
                AdDraftStore.data["furnitureBrand"] = furnitureBrand
                
                AdDraftStore.data["elecBrand"] = elecBrand
                AdDraftStore.data["elecType"] = elecType
                
                AdDraftStore.data["beautyBrand"] = beautyBrand
                AdDraftStore.data["beautyType"] = beautyType
                
                AdDraftStore.data["serviceType"] = serviceType
                AdDraftStore.data["serviceExperience"] = serviceExperience
                
                AdDraftStore.data["jobType"] = jobType
                AdDraftStore.data["jobExperience"] = jobExperience
                
                AdDraftStore.data["petType"] = petType
                AdDraftStore.data["petAge"] = petAge
                
                AdDraftStore.data["agriType"] = agriType
                AdDraftStore.data["agriUnit"] = agriUnit
            }
        }
    }
"""

for i in range(len(lines)):
    if "BackHandler(enabled = true) {" in lines[i] and "// Hardware Back gestures" in lines[i-1]:
        lines.insert(i-1, effect_code)
        break

with open('app/src/main/java/com/example/MarketplaceApp.kt', 'w') as f:
    f.write('\n'.join(lines))
