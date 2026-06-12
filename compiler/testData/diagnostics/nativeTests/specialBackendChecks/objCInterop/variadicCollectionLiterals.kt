// RUN_PIPELINE_TILL: FRONTEND
// ISSUE: KT-82852
// LANGUAGE: +CollectionLiterals
// WITH_PLATFORM_LIBS
import platform.darwin.*
import platform.Foundation.*

// Collection literals in spread position currently work in variadic C/ObjC calls because the
// literal desugars to a kotlin.arrayOf(...) call, which both interop gates accept.
//
// These tests are expected to break once KT-81722 introduces Array.Companion.of: it is tried
// before the stdlib arrayOf fallback, so the literal will desugar to a different symbol and the
// gates will stop accepting it. Updating the gates to keep these calls working is part of KT-82852.
fun objcLiteral() = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, *[1, 2, 3])
fun objcLiteralEmpty() = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, *[])
fun objcLiteralInsideArrayOf() = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, *arrayOf(*[1, 2]))
fun cLiteral() = NSLog("zzz", *[1, 2, 3])
fun cLiteralEmpty() = NSLog("zzz", *[])

// In named form the literal can be assigned to the vararg parameter directly, with or without spread.
fun objcNamed() = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, args = [1, 2])
fun objcNamedSpread() = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, args = <!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>*<!>[1, 2])
fun cNamed() = NSLog("zzz", variadicArguments = [1, 2])

// Spreading anything else is still forbidden, in both positional and named form.
fun objcSpread(s: Array<Any?>) = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, <!VARIADIC_OBJC_SPREAD_IS_SUPPORTED_ONLY_FOR_ARRAYOF!>*s<!>)
fun objcNamedRuntime(s: Array<Any?>) = NSAssertionHandler().handleFailureInFunction("zzz", "zzz", 0, null, <!VARIADIC_OBJC_SPREAD_IS_SUPPORTED_ONLY_FOR_ARRAYOF!>args = s<!>)
fun cSpread(s: Array<Any?>) = NSLog("zzz", <!VARIADIC_C_SPREAD_IS_SUPPORTED_ONLY_FOR_ARRAYOF!>*s<!>)
fun cNamedRuntime(s: Array<Any?>) = NSLog("zzz", <!VARIADIC_C_SPREAD_IS_SUPPORTED_ONLY_FOR_ARRAYOF!>variadicArguments = s<!>)
