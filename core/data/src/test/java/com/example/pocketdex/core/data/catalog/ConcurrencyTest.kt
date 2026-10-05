package com.example.pocketdex.core.data.catalog

import com.example.pocketdex.core.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ConcurrencyTest {
    @Test fun metadataRepairHasAtMostFourConcurrentRequestsAndKeepsOrder() = runTest {
        var active = 0
        var maximum = 0
        val result = boundedMap((1..15).toList()) { value ->
            active++; maximum = maxOf(maximum, active)
            try { delay((16 - value).toLong()); value } finally { active-- }
        }
        assertEquals(4, maximum)
        assertEquals((1..15).toList(), result)
        assertEquals(0, active)
    }

    @Test fun offlineIsExplicitAndCancellationPropagates() = runTest {
        assertEquals(DataResult.Failure(DataFailure.Offline), dataResult<Unit> { throw java.net.UnknownHostException() })
        try {
            dataResult<Unit> { throw CancellationException("changed query") }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
