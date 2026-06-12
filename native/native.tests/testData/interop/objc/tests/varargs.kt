import kotlinx.cinterop.*
import kotlin.test.*
import objcTests.*

@Test fun testVarargs() {
    assertEquals(
            "a b -1",
            TestVarargs.testVarargsWithFormat(
                    "%@ %s %d",
                    "a" as NSString, "b".cstr, (-1).toByte()
            ).formatted
    )

    assertEquals(
            "2 3 9223372036854775807",
            TestVarargs(
                    "%d %d %lld",
                    2.toShort(), 3, Long.MAX_VALUE
            ).formatted
    )

    assertEquals(
            "0.1 0.2 1 0",
            TestVarargs.create(
                    "%.1f %.1lf %d %d",
                    0.1.toFloat(), 0.2, true, false
            ).formatted
    )

    assertEquals(
            "1 2 3",
            TestVarargs(
                    format = "%d %d %d",
                    args = *arrayOf(1, 2, 3)
            ).formatted
    )

    assertEquals(
            "4 5 6",
            TestVarargs(
                    args = *arrayOf(4, *arrayOf(5, 6)),
                    format = "%d %d %d"
            ).formatted
    )

    assertEquals(
            "7",
            TestVarargsSubclass.stringWithFormat(
                    "%d",
                    7
            )
    )
}

// KT-82852: collection literals in spread position work the same as *arrayOf(...) in variadic
// ObjC calls, because the literal desugars to a kotlin.arrayOf(...) call.
//
// This test is expected to break once KT-81722 adds Array.Companion.of (tried before the stdlib
// arrayOf fallback): the literal will desugar to a different symbol that the interop gate rejects.
// Updating the gate to keep these calls working is part of KT-82852.
@Test fun testVarargsWithCollectionLiterals() {
    assertEquals(
            "1 2 3",
            TestVarargs(
                    format = "%d %d %d",
                    args = *[1, 2, 3]
            ).formatted
    )

    assertEquals(
            "10 11",
            TestVarargs(
                    format = "%d %d",
                    args = [10, 11]
            ).formatted
    )

    assertEquals(
            "4 5",
            TestVarargs.testVarargsWithFormat("%d %d", *[4, 5]).formatted
    )

    assertEquals(
            "6 7 8",
            TestVarargs.testVarargsWithFormat("%d %d %d", 6, *[7], 8).formatted
    )

    assertEquals(
            "empty",
            TestVarargs.testVarargsWithFormat("empty", *[]).formatted
    )

    assertEquals(
            "9",
            TestVarargsSubclass.stringWithFormat("%d", *[9])
    )
}