package app.skyward.data.location

/**
 * Prefers a fresh fix, falling back to the newest cached one. Approximate sun and moon times barely
 * change with an hours-old position, so an old fix beats none.
 */
internal fun <T : Any> freshOrNewest(fresh: T?, cached: List<T?>, timeMs: (T) -> Long): T? =
    fresh ?: cached.filterNotNull().maxByOrNull(timeMs)

/**
 * The newest cached fix if it's under [maxAgeMs] old, so there's no need to wait for a fresh one.
 */
internal fun <T : Any> recentCached(
    cached: List<T?>,
    nowMs: Long,
    maxAgeMs: Long,
    timeMs: (T) -> Long,
): T? = cached.filterNotNull().maxByOrNull(timeMs)?.takeIf { nowMs - timeMs(it) < maxAgeMs }
