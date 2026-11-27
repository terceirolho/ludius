package br.com.ludius.core.domain

import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

// Mesmos casos de supabase/tests/database/01_regras.test.sql
class CancellationPolicyTest {
    private fun t(s: String) = OffsetDateTime.parse(s).toInstant()

    @Test
    fun ateSeteDiasDepoisDeContratarDevolveTudo() = assertEquals(
        100,
        CancellationPolicy.clientRefundPercent(
            t("2026-12-01T10:00-03:00"), t("2026-12-05T20:00-03:00"), t("2026-12-04T10:00-03:00"),
        ),
    )

    @Test
    fun maisDeSeteDiasAntesDoEventoDevolveTudo() = assertEquals(
        100,
        CancellationPolicy.clientRefundPercent(
            t("2026-10-01T10:00-03:00"), t("2026-12-20T20:00-03:00"), t("2026-12-01T10:00-03:00"),
        ),
    )

    @Test
    fun entreSeteDiasEQuarentaEOitoHorasDevolveMetade() = assertEquals(
        50,
        CancellationPolicy.clientRefundPercent(
            t("2026-10-01T10:00-03:00"), t("2026-12-05T20:00-03:00"), t("2026-12-01T10:00-03:00"),
        ),
    )

    @Test
    fun menosDeQuarentaEOitoHorasNaoDevolve() = assertEquals(
        0,
        CancellationPolicy.clientRefundPercent(
            t("2026-10-01T10:00-03:00"), t("2026-12-05T20:00-03:00"), t("2026-12-04T21:00-03:00"),
        ),
    )
}
