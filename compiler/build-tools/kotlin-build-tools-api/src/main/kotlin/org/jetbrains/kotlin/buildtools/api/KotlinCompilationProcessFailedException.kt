/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api

/**
 * Thrown to indicate that a Kotlin compilation process has failed internally.
 *
 * This exception is **not** thrown for compilation errors due to problems in sources!
 *
 * @param message A description of the exception.
 */
@ExperimentalBuildToolsApi
public class KotlinCompilationProcessFailedException(
    message: String
) : KotlinBuildToolsException(message)
