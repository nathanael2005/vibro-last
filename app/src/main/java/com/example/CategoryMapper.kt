package com.example

import java.util.Locale

object CategoryMapper {

    private val vehiclesKeywords = listOf(
        "vitz", "toyota", "camry", "hilux", "byd", "car", "fuel", "suzuki", "motorcycle", "transmission",
        "vehicle", "truck", "pickup", "van", "suv", "sedan", "hatchback", "coupe", "convertible", "wagon",
        "bus", "minibus", "taxi", "scooter", "vespa", "moped", "bicycle", "cycle", "engine", "gearbox",
        "motor", "tire", "wheel", "tyre", "battery", "radiator", "exhaust", "bumper", "headlamp", "headlight",
        "spark", "diesel", "petrol", "oil", "hybrid", "electric", "ev", "drive-train", "suspension", "brake",
        "steering", "honda", "hyundai", "ford", "bmw", "benz", "mercedes", "audi", "nissan", "kia", "mitsubishi",
        "chevrolet", "volkswagen", "jetour", "chery", "lifan", "geely", "tesla", "lexus", "mote"
    )

    private val phonesKeywords = listOf(
        "iphone", "samsung", "galaxy", "phone", "pixel", "redmi", "smartwatch", "storage", "tecno",
        "smartphone", "mobile", "cell", "cellphone", "telephone", "landline", "tablet", "ipad", "android", "ios",
        "note", "ultra", "xiaomi", "infinix", "realme", "huawei", "oppo", "vivo", "oneplus", "nokia", "lenovo",
        "motorola", "honor", "sony", "smartband", "airpods", "earbuds", "charger", "charging", "cable", "usb",
        "case", "cover", "protector", "screen", "lcd", "glass", "powerbank", "power-bank", "sd", "sim", "memory"
    )

    private val propertyKeywords = listOf(
        "rent", "apartment", "flat", "room", "real estate", "villa", "house", "land", "rental", "lease", "sale",
        "sell", "buy", "purchase", "owner", "studio", "bedspace", "condominium", "condo", "penthouse", "home",
        "mansion", "duplex", "cottage", "bungalow", "property", "realtor", "broker", "plot", "site", "farm-land",
        "field", "acreage", "backyard", "compound", "office", "shop", "store", "warehouse", "showroom", "commercial",
        "garage", "workspace", "desk"
    )

    private val fashionKeywords = listOf(
        "shoes", "nike", "jacket", "dress", "watch", "bag", "jean", "shirt", "fashion", "shoe", "sneaker",
        "boot", "heel", "sandal", "slipper", "footwear", "loafer", "coat", "blazer", "suit", "tuxedo", "sweater",
        "hoodie", "cardigan", "pullover", "gown", "skirt", "frock", "t-shirt", "tshirt", "polo", "top", "blouse",
        "crop", "vest", "denim", "pant", "pants", "trouser", "trousers", "short", "shorts", "sweatpants", "leggings",
        "watches", "jewelry", "jewelries", "ring", "necklace", "bracelet", "earrings", "chain", "diamond", "gold",
        "silver", "bags", "backpack", "handbag", "purse", "wallet", "suitcase", "briefcase", "apparel", "clothing",
        "clothes", "wear", "outfit", "boutique", "style", "designer", "brand", "belt", "sunglasses", "hat", "cap",
        "scarf", "glove", "gloves", "socks"
    )

    private val furnitureKeywords = listOf(
        "sofa", "chair", "bed", "table", "furniture", "cabinet", "appliances", "couch", "armchair", "recliner",
        "sectional", "settee", "futon", "stool", "bench", "mattress", "pillow", "blanket", "duvet", "bedsheet",
        "desk", "dining", "wardrobe", "closet", "cupboard", "shelf", "drawer", "dresser", "sideboard", "bookshelf",
        "furnishing", "decor", "curtain", "carpet", "rug", "mirror", "lamp", "chandelier", "appliance", "fridge",
        "refrigerator", "freezer", "stove", "oven", "microwave", "blender", "mixer", "toaster", "kettle", "cooker",
        "juicer", "dishwasher", "washer", "dryer", "washing machine", "vacuum", "iron"
    )

    private val electronicsKeywords = listOf(
        "tv", "television", "laptop", "computer", "console", "playstation", "xbox", "speaker", "soundbar",
        "headphone", "camera", "tvs", "smart-tv", "led", "lcd", "oled", "qled", "screen", "display", "monitor",
        "macbook", "thinkpad", "notebook", "ultrabook", "chromebook", "pc", "desktop", "mac", "imac", "ps4", "ps5",
        "nintendo", "switch", "gaming", "gamer", "play-station", "soundbar", "subwoofer", "audio", "sound", "mic",
        "microphone", "amplifier", "receiver", "hifi", "home theatre", "home theater", "earphone", "earbuds",
        "pod", "pods", "dslr", "mirrorless", "lens", "tripod", "gopro", "drone", "video", "recorder", "camcorder",
        "router", "modem", "wifi", "hub", "printer", "scanner", "projector", "ups", "hard-drive", "ssd", "hdd",
        "flash-drive", "usb-drive"
    )

    private val beautyKeywords = listOf(
        "sunscreen", "sunblock", "sun-screen", "sun-block", "uv", "perfume", "cream", "makeup", "shampoo", "hair",
        "lotion", "beauty", "skin", "cosmetic", "cologne", "fragrance", "scent", "deodorant", "spray", "body-mist",
        "moisturizer", "serum", "toner", "cleanser", "wash", "scrub", "exfoliant", "mask", "face-mask", "gel", "aloe",
        "make-up", "lipstick", "lip-balm", "lip-gloss", "mascara", "eyeliner", "eyeshadow", "foundation", "concealer",
        "powder", "blush", "primer", "conditioner", "hair-dryer", "straightener", "brush", "comb", "wig", "wigs",
        "extensions", "beard", "shave", "razor", "blade", "cosmetics", "skincare", "skin-care", "face", "body", "nail",
        "nails", "polish", "manicure", "pedicure", "health", "supplement", "supplements", "vitamin", "vitamins",
        "protein", "whey", "creatine", "herb", "herbal", "medicine", "medical", "sanitizer", "bandage", "soap",
        "toothpaste", "toothbrush"
    )

    private val servicesKeywords = listOf(
        "repair", "cleaning", "plumbing", "electrician", "transport", "delivery", "service", "fixing",
        "maintenance", "technician", "mechanic", "plumber", "electrical", "carpentry", "carpenter", "mason",
        "masonry", "cleaner", "laundry", "dry-cleaning", "janitor", "pest-control", "fumigation", "transportation",
        "shipping", "courier", "logistics", "moving", "movers", "haulage", "taxi", "driver", "chauffeur", "rent-a-car",
        "services", "consulting", "consultant", "advisor", "coaching", "tutor", "tutoring", "teacher", "class",
        "lesson", "course", "training", "design", "designer", "developer", "development", "coding", "website",
        "app", "graphics", "logo", "marketing", "advertising", "seo", "writer", "writing", "translation",
        "translator", "editing", "photography", "videography", "video-editing", "event", "events", "catering",
        "caterer", "cook", "chef", "wedding", "party", "decoration", "decorator", "dj", "sound-system",
        "makeup-artist", "salon", "spa", "massage", "therapy", "therapist"
    )

    private val jobsKeywords = listOf(
        "job", "hiring", "vacancy", "full-time", "part-time", "salary", "work", "jobs", "hire", "vacancies",
        "career", "recruitment", "recruit", "seeker", "seekers", "contract", "freelance", "internship", "intern",
        "apprentice", "gig", "remote", "work-from-home", "wfh", "hybrid", "office-job", "wage", "wages", "payment",
        "pay", "income", "earn", "earning", "benefits", "commission", "employment", "position", "role", "associate",
        "specialist", "manager", "officer", "executive", "director", "assistant", "coordinator", "developer",
        "designer", "engineer", "accountant", "sales", "support", "customer service", "representative", "agent",
        "clerk", "cashier", "teller", "security", "guard", "nurse", "teacher", "scientist", "data scientist", "team",
        "staff", "contractor", "analyst", "programmer", "physician", "doctor"
    )

    private val petsKeywords = listOf(
        "dog", "cat", "puppy", "kitten", "pet", "animal", "dogs", "cats", "puppies", "kittens", "canine",
        "hound", "retriever", "shepherd", "bulldog", "poodle", "feline", "meow", "persian", "siamese", "bird",
        "birds", "parrot", "parrots", "canary", "lovebird", "pigeon", "dove", "cage", "cages", "fish", "fishes",
        "aquarium", "aquariums", "tank", "gold-fish", "guppy", "betta", "pets", "animals", "livestock", "horse",
        "horses", "pony", "rabbit", "rabbits", "hamster", "guinea pig", "turtle", "tortoise", "veterinary", "vet",
        "clinic", "animal food", "kibble", "cat-food", "dog-food", "collar", "leash", "pet-toy", "pet-grooming"
    )

    private val agriKeywords = listOf(
        "egg", "onion", "tomato", "grain", "teff", "coffee", "honey", "fertilizer", "tractor", "farm", "food",
        "eggs", "milk", "dairy", "cheese", "butter", "yogurt", "onions", "tomatoes", "potato", "potatoes", "garlic",
        "ginger", "pepper", "chili", "vegetable", "vegetables", "fruit", "fruits", "apple", "banana", "orange",
        "mango", "avocado", "grains", "wheat", "barley", "maize", "corn", "rice", "flour", "seed", "seeds", "crop",
        "crops", "bunna", "tea", "sugar", "salt", "spice", "spices", "berbere", "shiro", "fertilizers", "compost",
        "manure", "pesticide", "pesticides", "herbicide", "chemical", "chemicals", "tractors", "plow", "plough",
        "harvester", "mower", "pump", "irrigation", "farming", "agricultural", "agriculture", "chicken", "beef",
        "meat", "lamb", "sheep", "goat", "cow", "cattle"
    )

    /**
     * Map any title and description input into a valid category ID between "1" and "11".
     * High accuracy keyword matching with reliable fallback logic.
     */
    fun mapTitleToCategoryId(title: String, description: String = ""): String {
        val t = title.lowercase(Locale.getDefault())
        val d = description.lowercase(Locale.getDefault())
        val combinedText = "$t $d"

        if (combinedText.trim().isEmpty()) {
            return "2" // Default fallback to Phones & Tablets
        }

        val scores = mutableMapOf<String, Int>()

        // Split text into tokens for exact and partial matching
        val tokens = combinedText.split(Regex("[^a-zA-Z0-9]+")).filter { it.length >= 2 }

        for (word in tokens) {
            if (vehiclesKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["1"] = (scores["1"] ?: 0) + 2
            }
            if (phonesKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["2"] = (scores["2"] ?: 0) + 2
            }
            if (propertyKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["3"] = (scores["3"] ?: 0) + 2
            }
            if (fashionKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["4"] = (scores["4"] ?: 0) + 2
            }
            if (furnitureKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["5"] = (scores["5"] ?: 0) + 2
            }
            if (electronicsKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["6"] = (scores["6"] ?: 0) + 2
            }
            if (beautyKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["7"] = (scores["7"] ?: 0) + 2
            }
            if (servicesKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["8"] = (scores["8"] ?: 0) + 2
            }
            if (jobsKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["9"] = (scores["9"] ?: 0) + 2
            }
            if (petsKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["10"] = (scores["10"] ?: 0) + 2
            }
            if (agriKeywords.any { it == word || (word.length >= 4 && it.startsWith(word)) || word.startsWith(it) }) {
                scores["11"] = (scores["11"] ?: 0) + 2
            }
        }

        // Add broad phrase checking to boost context
        if (vehiclesKeywords.any { t.contains(it) }) scores["1"] = (scores["1"] ?: 0) + 3
        if (phonesKeywords.any { t.contains(it) }) scores["2"] = (scores["2"] ?: 0) + 3
        if (propertyKeywords.any { t.contains(it) }) scores["3"] = (scores["3"] ?: 0) + 3
        if (fashionKeywords.any { t.contains(it) }) scores["4"] = (scores["4"] ?: 0) + 3
        if (furnitureKeywords.any { t.contains(it) }) scores["5"] = (scores["5"] ?: 0) + 3
        if (electronicsKeywords.any { t.contains(it) }) scores["6"] = (scores["6"] ?: 0) + 3
        if (beautyKeywords.any { t.contains(it) }) scores["7"] = (scores["7"] ?: 0) + 3
        if (servicesKeywords.any { t.contains(it) }) scores["8"] = (scores["8"] ?: 0) + 3
        if (jobsKeywords.any { t.contains(it) }) scores["9"] = (scores["9"] ?: 0) + 3
        if (petsKeywords.any { t.contains(it) }) scores["10"] = (scores["10"] ?: 0) + 3
        if (agriKeywords.any { t.contains(it) }) scores["11"] = (scores["11"] ?: 0) + 3

        val highestCategory = scores.maxByOrNull { it.value }
        if (highestCategory != null && highestCategory.value > 0) {
            return highestCategory.key
        }

        // If no keyword match found, distribute fallback category deterministically using title hashcode
        // This ensures every item gets placed under a valid, well-distributed category
        val categoryOptions = listOf("2", "4", "5", "6", "7", "11") // Popular consumer categories
        val index = Math.abs(title.hashCode()) % categoryOptions.size
        return categoryOptions[index]
    }
}
