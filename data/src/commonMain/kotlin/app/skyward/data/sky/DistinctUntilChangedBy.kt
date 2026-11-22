/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.data.sky

internal fun <T, K> List<T>.distinctUntilChangedBy(keySelector: (T) -> K): List<T> {
    var previous: K? = null
    return buildList {
        this@distinctUntilChangedBy.forEach {
            val key = keySelector(it)
            if (key != previous) {
                add(it)
            }
            previous = key
        }
    }
}
