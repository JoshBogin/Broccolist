package com.broccolist.checklist.domain

sealed class GrocerySection(open val displayName: String) {
    data object Produce : GrocerySection("Produce")
    data object Deli : GrocerySection("Deli")
    data object Meat : GrocerySection("Meat")
    data object Bakery : GrocerySection("Bakery")
    data object Pantry : GrocerySection("Pantry")
    data object Snacks : GrocerySection("Snacks")
    data object Beverages : GrocerySection("Beverages")
    data object Frozen : GrocerySection("Frozen")
    data object Dairy : GrocerySection("Dairy")
    data object Other : GrocerySection("Other")

    // Custom section
    data class Custom(override val displayName: String) : GrocerySection(displayName)

    companion object{
        fun getPredefinedSections(): List<GrocerySection> {
            return listOf(
                Produce,
                Deli,
                Meat,
                Bakery,
                Pantry,
                Snacks,
                Beverages,
                Frozen,
                Dairy,
                Other
            )
        }

        fun fromString(name: String): GrocerySection {
            return getPredefinedSections().find {
                it.displayName.equals(name, ignoreCase = true)
            } ?: Custom(name)
        }
    }

}
