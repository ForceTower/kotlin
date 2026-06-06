/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal.types

import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.metadata.KmClass
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.jvm.internal.KTypeParameterOwnerImpl

/**
 * A [kotlin.reflect.KClass] implementation for mutable collection classes (i.e. `kotlin.collections.MutableList`).
 *
 * Currently, this class is only used in the type checker implementation for kotlin-reflect,
 * but one day it should probably be used to implement KT-11754.
 *
 * @param readonlyClass the read-only collection class (i.e. `kotlin.collections.List`)
 */
internal class MutableCollectionKClass<T : Any>(
    val readonlyClass: KClass<T>,
    override val qualifiedName: String,
    val mutableKmClass: KmClass?,
    createTypeParameters: (MutableCollectionKClass<T>) -> List<KTypeParameter>,
    createSupertypes: () -> List<KType>,
) : KClass<T> by readonlyClass, TypeConstructorMarker, KTypeParameterOwnerImpl {
    override val typeParameters: List<KTypeParameter> by lazy(PUBLICATION) {
        createTypeParameters(this)
    }

    override val supertypes: List<KType> by lazy(PUBLICATION, createSupertypes)

    override val simpleName: String
        get() = qualifiedName.substringAfterLast(".")

    override fun equals(other: Any?): Boolean =
        other is MutableCollectionKClass<*> && readonlyClass == other.readonlyClass

    override fun hashCode(): Int =
        readonlyClass.hashCode()

    override fun toString(): String =
        "MutableCollectionKClass($readonlyClass)"
}
