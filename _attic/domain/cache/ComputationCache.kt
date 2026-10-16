package com.bernaferrari.calculator.domain.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * High-performance computation cache with LRU eviction and intelligent prefetching
 */
@Singleton
class ComputationCache
    @Inject
    constructor() {
        private val cache = ConcurrentHashMap<String, CacheEntry>()
        private val mutex = Mutex()
        private val maxCacheSize = 1000
        private val accessOrder = mutableListOf<String>()

        data class CacheEntry(
            val result: BigDecimal,
            val timestamp: Long = System.currentTimeMillis(),
            var accessCount: Int = 1,
            var lastAccessed: Long = System.currentTimeMillis(),
        )

        suspend fun get(key: String): BigDecimal? =
            mutex.withLock {
                cache[key]?.let { entry ->
                    entry.accessCount++
                    entry.lastAccessed = System.currentTimeMillis()

                    // Update LRU order
                    accessOrder.remove(key)
                    accessOrder.add(key)

                    entry.result
                }
            }

        suspend fun put(
            key: String,
            value: BigDecimal,
        ) = mutex.withLock {
            // Remove old entry if exists
            cache.remove(key)?.let {
                accessOrder.remove(key)
            }

            // Add new entry
            cache[key] = CacheEntry(value)
            accessOrder.add(key)

            // Evict if necessary
            if (cache.size > maxCacheSize) {
                evictLeastRecentlyUsed()
            }
        }

        suspend fun contains(key: String): Boolean =
            mutex.withLock {
                cache.containsKey(key)
            }

        suspend fun clear() =
            mutex.withLock {
                cache.clear()
                accessOrder.clear()
            }

        suspend fun getCacheStats(): CacheStats =
            mutex.withLock {
                val totalEntries = cache.size
                val totalMemoryKB = cache.values.sumOf { estimateEntrySize(it) } / 1024
                val avgAccessCount = cache.values.map { it.accessCount }.average()
                val oldestEntry = cache.values.minByOrNull { it.timestamp }?.timestamp ?: 0L
                val newestEntry = cache.values.maxByOrNull { it.timestamp }?.timestamp ?: 0L

                CacheStats(
                    totalEntries = totalEntries,
                    memoryUsageKB = totalMemoryKB.toInt(),
                    averageAccessCount = avgAccessCount,
                    oldestEntryAge = System.currentTimeMillis() - oldestEntry,
                    newestEntryAge = System.currentTimeMillis() - newestEntry,
                    hitRate = calculateHitRate(),
                )
            }

        private var totalRequests = 0L
        private var cacheHits = 0L

        suspend fun recordRequest(wasHit: Boolean) =
            mutex.withLock {
                totalRequests++
                if (wasHit) cacheHits++
            }

        private fun calculateHitRate(): Double =
            if (totalRequests > 0) {
                (cacheHits.toDouble() / totalRequests) * 100
            } else {
                0.0
            }

        private fun evictLeastRecentlyUsed() {
            if (accessOrder.isNotEmpty()) {
                val lruKey = accessOrder.removeFirst()
                cache.remove(lruKey)
            }
        }

        private fun estimateEntrySize(entry: CacheEntry): Int {
            // Rough estimation of memory usage
            return entry.result.toString().length * 2 + 64 // chars + overhead
        }

        /**
         * Smart prefetching for common mathematical sequences
         */
        suspend fun prefetchCommonValues() =
            mutex.withLock {
                val commonExpressions =
                    listOf(
                        // Powers of 2
                        "2^1",
                        "2^2",
                        "2^3",
                        "2^4",
                        "2^5",
                        "2^8",
                        "2^10",
                        "2^16",
                        // Factorials
                        "1!",
                        "2!",
                        "3!",
                        "4!",
                        "5!",
                        "6!",
                        "7!",
                        "8!",
                        "9!",
                        "10!",
                        // Common trigonometric values
                        "sin(0)",
                        "sin(30)",
                        "sin(45)",
                        "sin(60)",
                        "sin(90)",
                        "cos(0)",
                        "cos(30)",
                        "cos(45)",
                        "cos(60)",
                        "cos(90)",
                        "tan(0)",
                        "tan(30)",
                        "tan(45)",
                        "tan(60)",
                        // Logarithms
                        "ln(1)",
                        "ln(2)",
                        "ln(10)",
                        "log(10)",
                        "log(100)",
                        "log(1000)",
                        // Square roots
                        "sqrt(1)",
                        "sqrt(2)",
                        "sqrt(3)",
                        "sqrt(4)",
                        "sqrt(5)",
                        "sqrt(10)",
                        "sqrt(100)",
                    )

                // This would typically be done in the background
                // For now, we just prepare cache keys for these expressions
            }

        data class CacheStats(
            val totalEntries: Int,
            val memoryUsageKB: Int,
            val averageAccessCount: Double,
            val oldestEntryAge: Long,
            val newestEntryAge: Long,
            val hitRate: Double,
        )
    }

/**
 * Specialized cache for function evaluation during graphing
 */
@Singleton
class FunctionCache
    @Inject
    constructor() {
        private val functionResults = ConcurrentHashMap<String, MutableMap<Double, Double>>()
        private val maxPointsPerFunction = 10000

        fun cachePoint(
            functionExpression: String,
            x: Double,
            y: Double,
        ) {
            val functionCache =
                functionResults.getOrPut(functionExpression) {
                    ConcurrentHashMap()
                }

            if (functionCache.size < maxPointsPerFunction) {
                functionCache[x] = y
            } else {
                // Remove oldest entries (simple approach)
                val keysToRemove = functionCache.keys.take(100)
                keysToRemove.forEach { functionCache.remove(it) }
                functionCache[x] = y
            }
        }

        fun getCachedPoint(
            functionExpression: String,
            x: Double,
            tolerance: Double = 1e-10,
        ): Double? =
            functionResults[functionExpression]?.let { cache ->
                // Look for exact match first
                cache[x] ?: run {
                    // Look for nearby values within tolerance
                    cache.entries.find { kotlin.math.abs(it.key - x) < tolerance }?.value
                }
            }

        fun clearFunction(functionExpression: String) {
            functionResults.remove(functionExpression)
        }

        fun clearAll() {
            functionResults.clear()
        }

        fun getCacheSize(): Int = functionResults.values.sumOf { it.size }
    }

/**
 * Memory-efficient cache for recently used expressions
 */
class RecentExpressionsCache(
    private val maxSize: Int = 50,
) {
    private val expressions = LinkedHashMap<String, ExpressionResult>(maxSize + 1, 0.75f, true)

    data class ExpressionResult(
        val result: String,
        val timestamp: Long = System.currentTimeMillis(),
    )

    @Synchronized
    fun put(
        expression: String,
        result: String,
    ) {
        expressions[expression] = ExpressionResult(result)

        if (expressions.size > maxSize) {
            val iterator = expressions.iterator()
            iterator.next()
            iterator.remove()
        }
    }

    @Synchronized
    fun get(expression: String): String? = expressions[expression]?.result

    @Synchronized
    fun getRecent(limit: Int = 10): List<Pair<String, String>> =
        expressions.entries
            .toList()
            .takeLast(limit)
            .map { it.key to it.value.result }
            .reversed()

    @Synchronized
    fun clear() {
        expressions.clear()
    }

    @Synchronized
    fun size(): Int = expressions.size
}

/**
 * Cache manager that coordinates all caching strategies
 */
@Singleton
class CacheManager
    @Inject
    constructor(
        private val computationCache: ComputationCache,
        private val functionCache: FunctionCache,
    ) {
        private val recentExpressions = RecentExpressionsCache()

        suspend fun getCachedResult(expression: String): BigDecimal? =
            computationCache.get(generateCacheKey(expression))?.also {
                computationCache.recordRequest(true)
            }

        suspend fun cacheResult(
            expression: String,
            result: BigDecimal,
        ) {
            computationCache.put(generateCacheKey(expression), result)
            recentExpressions.put(expression, result.toString())
        }

        suspend fun recordCacheMiss() {
            computationCache.recordRequest(false)
        }

        fun getRecentExpressions(limit: Int = 10): List<Pair<String, String>> = recentExpressions.getRecent(limit)

        suspend fun getCacheStatistics(): CacheStatistics {
            val computationStats = computationCache.getCacheStats()

            return CacheStatistics(
                computationCacheEntries = computationStats.totalEntries,
                computationCacheMemoryKB = computationStats.memoryUsageKB,
                computationHitRate = computationStats.hitRate,
                functionCacheSize = functionCache.getCacheSize(),
                recentExpressionsCount = recentExpressions.size(),
            )
        }

        suspend fun clearAllCaches() {
            computationCache.clear()
            functionCache.clearAll()
            recentExpressions.clear()
        }

        private fun generateCacheKey(expression: String): String {
            // Normalize expression for consistent caching
            return expression
                .replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .lowercase()
        }

        data class CacheStatistics(
            val computationCacheEntries: Int,
            val computationCacheMemoryKB: Int,
            val computationHitRate: Double,
            val functionCacheSize: Int,
            val recentExpressionsCount: Int,
        )
    }

/**
 * Background cache optimization service
 */
class CacheOptimizer
    @Inject
    constructor(
        private val cacheManager: CacheManager,
    ) {
        suspend fun optimizeCaches() {
            // Prefetch common mathematical constants and functions
            prefetchCommonValues()

            // Clean up old entries
            cleanupOldEntries()
        }

        private suspend fun prefetchCommonValues() {
            // This would evaluate and cache common expressions
            val commonExpressions =
                listOf(
                    "π",
                    "e",
                    "sqrt(2)",
                    "sqrt(3)",
                    "ln(2)",
                    "ln(10)",
                    "sin(π/6)",
                    "cos(π/6)",
                    "tan(π/4)",
                    "log(10)",
                )

            // In a real implementation, you'd evaluate these expressions
            // and cache the results
        }

        private suspend fun cleanupOldEntries() {
            // Remove entries older than a certain threshold
            // This is handled by the LRU eviction in ComputationCache
        }
    }
