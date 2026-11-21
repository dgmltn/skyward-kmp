package app.skyward.data.location

/**
 * Prefers a fresh fix, falling back to the newest cached one. Approximate sun and moon times barely
 * change with an hours-old position, so an old fix beats none.
 */
internal fun <T : Any> freshOrNewest(fresh: T?, cached: List<T?>, timeMs: (T) -> Long): T? =
    fresh ?: cached.filterNotNull().maxByOrNull(timeMs)
