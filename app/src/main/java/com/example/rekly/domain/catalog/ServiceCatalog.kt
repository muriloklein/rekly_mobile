package com.example.rekly.domain.catalog

import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.domain.model.BillingPeriod

/**
 * Fixed list of common services and bills, used by the subscription form and by the
 * onboarding. Choosing one fills name, category, period and whether the amount varies;
 * the user only completes amount, start date and billing day.
 */
object ServiceCatalog {

    val services: List<CatalogService> = listOf(
        CatalogService("netflix", "Netflix", DefaultCategories.STREAMING),
        CatalogService("prime_video", "Prime Video", DefaultCategories.STREAMING),
        CatalogService("disney_plus", "Disney+", DefaultCategories.STREAMING),
        CatalogService("max", "Max", DefaultCategories.STREAMING),
        CatalogService("globoplay", "Globoplay", DefaultCategories.STREAMING),
        CatalogService("apple_tv_plus", "Apple TV+", DefaultCategories.STREAMING),
        CatalogService("youtube_premium", "YouTube Premium", DefaultCategories.STREAMING),

        CatalogService("spotify", "Spotify", DefaultCategories.MUSIC),
        CatalogService("deezer", "Deezer", DefaultCategories.MUSIC),
        CatalogService("youtube_music", "YouTube Music", DefaultCategories.MUSIC),
        CatalogService("apple_music", "Apple Music", DefaultCategories.MUSIC),

        CatalogService("rent", "Aluguel", DefaultCategories.HOUSING),
        CatalogService("condo_fee", "Condomínio", DefaultCategories.HOUSING),
        CatalogService("water", "Água", DefaultCategories.HOUSING, variableAmount = true),
        CatalogService("electricity", "Luz", DefaultCategories.HOUSING, variableAmount = true),
        CatalogService("gas", "Gás", DefaultCategories.HOUSING, variableAmount = true),

        CatalogService("internet", "Internet", DefaultCategories.TELECOM),
        CatalogService("mobile_phone", "Celular", DefaultCategories.TELECOM),
        CatalogService("pay_tv", "TV por assinatura", DefaultCategories.TELECOM),

        CatalogService("gym", "Academia", DefaultCategories.HEALTH_WELLNESS),
        CatalogService("health_plan", "Plano de saúde", DefaultCategories.HEALTH_WELLNESS),
        CatalogService("dental_plan", "Plano odontológico", DefaultCategories.HEALTH_WELLNESS),

        CatalogService("car_insurance", "Seguro do carro", DefaultCategories.INSURANCE, BillingPeriod.ANNUAL),
        CatalogService("home_insurance", "Seguro residencial", DefaultCategories.INSURANCE, BillingPeriod.ANNUAL),
        CatalogService("life_insurance", "Seguro de vida", DefaultCategories.INSURANCE),

        CatalogService("icloud", "iCloud", DefaultCategories.OTHER),
        CatalogService("google_one", "Google One", DefaultCategories.OTHER),
        CatalogService("microsoft_365", "Microsoft 365", DefaultCategories.OTHER)
    )

    fun findById(id: String): CatalogService? = services.firstOrNull { it.id == id }

    /** Services grouped by category id, keeping the order of the list above. */
    fun groupedByCategory(): Map<Long, List<CatalogService>> = services.groupBy { it.categoryId }
}
