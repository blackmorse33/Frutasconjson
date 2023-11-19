package com.example.frutas2.Retrofit.model

data class FruitItem(
    val family: String,
    val genus: String,
    val id: Int,
    val name: String,
    val nutritions: Nutritions,
    val order: String
)