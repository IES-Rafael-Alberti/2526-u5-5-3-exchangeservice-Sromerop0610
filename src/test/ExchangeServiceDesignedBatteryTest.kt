package com.example.exchange

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.iesra.revilofe.ExchangeRateProvider
import org.iesra.revilofe.ExchangeService
import org.iesra.revilofe.InMemoryExchangeRateProvider
import org.iesra.revilofe.Money

class ExchangeServiceDesignedBatteryTest : DescribeSpec({

    afterTest {
        clearAllMocks()
    }

    describe("battery designed from equivalence classes for ExchangeService") {

        describe("input validation") {
            val provider = mockk<ExchangeRateProvider>()
            val service = ExchangeService(provider)

            it("throws an exception when the amount is zero") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(0, "USD"), "EUR")
                }
            }

            it("throws an exception when the amount is negative") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(-100, "USD"), "EUR")
                }
            }

            it("throws an exception when the source currency code is invalid") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(100, "US"), "EUR")
                }
            }

            it("throws an exception when the target currency code is invalid") {
                shouldThrow<IllegalArgumentException> {
                    service.exchange(Money(100, "USD"), "EURO")
                }
            }
        }
        describe("same currency") {

            it("returns the same amount and does not call the provider") {
                val realProvider = InMemoryExchangeRateProvider(
                    mapOf("USDEUR" to 0.92)
                )
                val spy = spyk(realProvider)

                val service = ExchangeService(spy)

                val result = service.exchange(Money(1000, "USD"), "USD")

                result shouldBe 1000

                verify(exactly = 0) { spy.rate(any()) }
                confirmVerified(spy)
            }
        }
    })
