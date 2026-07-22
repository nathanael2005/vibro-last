package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class VibroArticle(
    val id: String,
    val title: String,
    val excerpt: String,
    val rTime: String,
    val fullContent: String,
    val icon: ImageVector
)

val mockArticlesByCat = mapOf(
    "1" to listOf(
        VibroArticle("v1", "Crucial Guide: Inspecting Used Cars in Addis", "Don't get scammed. Learn how to inspect the engine, verify custom clearance, and structure payments safely.", "4 min read", "When buying a used car in Addis Ababa:\n\n1. Check the body paint for signs of accident reconstruction.\n2. Verify the custom clearance paperwork / Libret from the transport bureau directly before paying a deposit.\n3. Hire an independent mechanic to perform a compression check.\n4. Ensure the seller is either the owner on the Libret or has an officially verified Power of Attorney.", Icons.Default.Build),
        VibroArticle("v2", "How to Verify Car Ownership Docs in Ethiopia", "Comprehensive checklist for transferring ownership, checking active bank collaterals, and transport bureau processes.", "3 min read", "Step 1: Get a copy of the Libret (registration folder).\nStep 2: Take it to the nearest driver & vehicle licensing bureau to run a search for active liens or active bank collateral.\nStep 3: Draft an official sales contract via the public notary office.\nStep 4: Do not pay full cash in hand; use certified bank payments (CPO) to prevent double transfers.", Icons.Default.Assignment)
    ),
    "2" to listOf(
        VibroArticle("p1", "Is Your iPhone Original? Checking IMEI Status", "How to use local telecom (Ethio Telecom) and global databases to verify IMEI, warranty, and network locks.", "2 min read", "1. Dial *#06# to display the IMEI on the device screen.\n2. Cross-reference the IMEI with the physical engraving on the SIM card tray and the box barcode.\n3. Use global portals to check for lock status / iCloud lock.\n4. Avoid buying iPhones in a locked state or with signed-in Apple IDs.", Icons.Default.Smartphone),
        VibroArticle("p2", "How to Avoid Refurbished and Replica Android Phones", "Spotting cloned displays, replica boxes, or counterfeit software builds in modern electronic markets.", "3 min read", "Some local sellers package high-quality replicas (clones) as originals.\n\n- Test the standard camera quality; replicas have poor lenses and slow focus.\n- Use hardware benchmarking apps like CPU-Z to verify the processor model fits the specifications.\n- Verify charging speed matching the actual model specifications.", Icons.Default.Info)
    ),
    "3" to listOf(
        VibroArticle("r1", "Tenant Checklist for Bole & Kazanchis Apartments", "What to look for in your rental agreement, security deposits, water access, and utility bill sharing options.", "5 min read", "Before committing to a rental in Addis Ababa:\n\n- Water & Backup: Ensure the building has active water reservoirs/tanks and automatic generator switchover.\n- Security: Verify the door locking mechanisms and gate security rules.\n- Agreement: Get a written, stamp-notarized agreement. Word-of-mouth is not legally binding for leases.\n- Utilities: Clarify how electricity (prepaid tokens) is loaded and divided in multi-story villas.", Icons.Default.Home),
        VibroArticle("r2", "Evaluating Land Values and House Titles in Ethiopia", "Essential legal procedures for verifying property deeds, avoiding multiple-owner scams, and bank validation.", "4 min read", "Verify the 'Carta' (Property Title Deed) with the district land management bureau (Woreda) to confirm the seller is registered and has no mortgage default. Always proceed through authorized government legal channels.", Icons.Default.Assignment)
    ),
    "4" to listOf(
        VibroArticle("f1", "Finding Your Perfect Fit: Ethiopian Sizing Guide", "International size charts explained for Italian, US, and UK conversions commonly seen in Addis boutiques.", "2 min read", "Ensure correct fits when ordering fashion:\n- Italian luxury cuts usually run slimmer than US cuts.\n- For shoes, Euro sizes (39-45) are the standard in Ethiopian retail; ask for measurements in centimeters if uncertain.", Icons.Default.Checkroom),
        VibroArticle("f2", "How to Spot Luxury Replica Sneakers & Designer Clothing", "Materials, stitching patterns, and branding indicators to tell authentic premium wear from clever clones.", "3 min read", "Inspect the inner stitching: authentic boutique clothing has secure twin stitching. Replica printing wears off after the first wash; feel the weight and quality of the underlying fabric.", Icons.Default.ShoppingBag)
    ),
    "5" to listOf(
        VibroArticle("u1", "Inspecting Wooden Joinery & Upholstery Like a Pro", "A checklist for assessing solid wood frames, local wanza/wood types, and high density fabric durability.", "4 min read", "1. Frame Material: Ask if the frame is made of solid Wanza, Korke, or cheap chipboard.\n2. Joint Strength: Shake the chairs to check for dowelled and glued joints vs staples.\n3. Foam Density: Low-quality foam sinks permanently within 3 months; specify 30+ density foam.", Icons.Default.Build),
        VibroArticle("u2", "Designing a Spacious Living Room: Sofa Dimensions Guide", "How to measure available floor space, door layouts, and corner clearances before delivery in apartments.", "3 min read", "Ensure you measure entry doors, lift widths, and hallway turns in modern apartments before purchasing large L-shaped sofas, so they physically fit during delivery.", Icons.Default.Home)
    )
)
