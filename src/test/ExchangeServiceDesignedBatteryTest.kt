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
    }
    describe("direct rate") {

        it("converts correctly using a direct rate") {
            val provider = mockk<ExchangeRateProvider>()

            every { provider.rate("USDEUR") } returns 0.92

            val service = ExchangeService(provider)

            val result = service.exchange(Money(1000, "USD"), "EUR")

            result shouldBe 920

            verify(exactly = 1) { provider.rate("USDEUR") }
            confirmVerified(provider)
        }
    }
}
        describe("cross conversion") {

    it("uses an intermediate currency when direct rate is missing") {
        val provider = mockk<ExchangeRateProvider>()

        every { provider.rate("GBPJPY") } throws IllegalArgumentException()
        every { provider.rate("GBPUSD") } returns 1.2
        every { provider.rate("USDJPY") } returns 150.0

        val service = ExchangeService(provider)

        val result = service.exchange(Money(2, "GBP"), "JPY")

        result shouldBe (2 * 1.2 * 150.0).toLong()

        verifySequence {
            provider.rate("GBPJPY")
            provider.rate("GBPUSD")
            provider.rate("USDJPY")
        }
    }
})