package com.example.model

const val SPLASH_ROUTE = "splash"
const val WELCOME_ROUTE = "welcome"
const val LOGIN_ROUTE = "login"
const val MAIN_SHELL_ROUTE = "main"
const val PRODUCT_DETAIL_ROUTE = "product/{productId}"
const val SELLER_PROFILE_ROUTE = "seller_profile/{sellerName}"

fun createProductDetailRoute(id: String) = "product/$id"
fun createSellerProfileRoute(sellerName: String) = "seller_profile/$sellerName"
