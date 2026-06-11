/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.test.handlers

import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure
import java.io.File

internal interface WasmArtifactsCollector {
    val testServices: TestServices

    fun collectJsArtifacts(
        originalFile: File,
        mode: String,
        additionalTestServices: List<TestServices> = emptyList(),
    ): JsArtifacts {
        val jsFiles = mutableListOf<AdditionalFile>()
        val mjsFiles = mutableListOf<AdditionalFile>()
        var entryMjs: String? = "test.mjs"

        testServices.moduleStructure.modules.forEach { m ->
            m.files.forEach { file: TestFile ->
                val name = file.name
                when {
                    name.endsWith(".js") ->
                        jsFiles += AdditionalFile(file.name, file.originalContent)

                    name.endsWith(".mjs") -> {
                        mjsFiles += AdditionalFile(file.name, file.originalContent)
                        if (name == "entry.mjs") {
                            entryMjs = name
                        }
                    }
                }
            }
        }

        // Collect companion files for the primary test and all additional tests in the batch.
        // Each test may have sibling `.js`/`.mjs` files (e.g. `externalObject.mjs` for
        // `@JsModule` tests). The compiled wasm imports ALL of them, so they must all be present
        // in the output dir.
        val allOriginalFiles = listOf(originalFile) +
                additionalTestServices.flatMap { it.moduleStructure.originalTestDataFiles }
        for (origFile in allOriginalFiles) {
            origFile.parentFile.resolve(origFile.nameWithoutExtension + ".js")
                .takeIf { it.exists() }
                ?.let {
                    if (jsFiles.none { f -> f.name == it.name })
                        jsFiles += AdditionalFile(it.name, it.readText())
                }

            origFile.parentFile.resolve(origFile.nameWithoutExtension + ".mjs")
                .takeIf { it.exists() }
                ?.let {
                    if (mjsFiles.none { f -> f.name == it.name })
                        mjsFiles += AdditionalFile(it.name, it.readText())
                }

            origFile.parentFile.resolve(origFile.nameWithoutExtension + "__main.js")
                .takeIf { it.exists() }
                ?.let {
                    entryMjs = it.name
                    if (mjsFiles.none { f -> f.name == it.name })
                        mjsFiles += AdditionalFile(it.name, it.readText())
                }
        }

        WasmTypeScriptCompilationHandler.compiledTypeScriptOutput(testServices, mode)
            .takeIf { it.exists() }
            ?.let {
                entryMjs = it.name
            }

        return JsArtifacts(entryMjs, jsFiles, mjsFiles)
    }

    fun JsArtifacts.saveJsArtifacts(baseDir: File): SavedJsArtifacts {
        val mjsFilePaths = mutableListOf<String>()
        for (mjsFile: AdditionalFile in mjsFiles) {
            val file = File(baseDir, mjsFile.name)
            file.writeText(mjsFile.content)
            mjsFilePaths += file.canonicalPath
        }

        val jsFilePaths = mutableListOf<String>()
        for (jsFile: AdditionalFile in jsFiles) {
            val file = File(baseDir, jsFile.name)
            file.writeText(jsFile.content)
            jsFilePaths += file.canonicalPath
        }

        return SavedJsArtifacts(jsFilePaths, mjsFilePaths)
    }

    fun processExceptions(exceptions: List<Throwable>) {
        when (exceptions.size) {
            0 -> {} // Everything OK
            1 -> {
                throw exceptions.single()
            }
            else -> {
                throw AssertionError("Failed with several exceptions. Look at suppressed exceptions below.").apply {
                    exceptions.forEach { addSuppressed(it) }
                }
            }
        }
    }

    class AdditionalFile(val name: String, val content: String)
    class JsArtifacts(val entryPath: String?, val jsFiles: List<AdditionalFile>, val mjsFiles: List<AdditionalFile>)
    data class SavedJsArtifacts(val jsFilePaths: List<String>, val mjsFilePaths: List<String>)
}
