package com.lernhero.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntitlementsTest {

    private val now = Instant.parse("2026-09-22T12:00:00Z")

    private fun body(entitlements: String) =
        Json.parseToJsonElement("""{"subscriber":{"entitlements":$entitlements}}""").jsonObject

    @Test
    fun activeWhenExpiryInFuture() {
        assertTrue(Entitlements.isActive(body("""{"premium":{"expires_date":"2026-10-22T12:00:00Z"}}"""), now))
    }

    @Test
    fun inactiveWhenExpired() {
        assertFalse(Entitlements.isActive(body("""{"premium":{"expires_date":"2026-09-01T12:00:00Z"}}"""), now))
    }

    @Test
    fun lifetimeHasNoExpiry() {
        assertTrue(Entitlements.isActive(body("""{"premium":{"expires_date":null}}"""), now))
    }

    @Test
    fun otherEntitlementDoesNotCount() {
        assertFalse(Entitlements.isActive(body("""{"gold_pack":{"expires_date":null}}"""), now))
    }
}
