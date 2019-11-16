// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.gradle.service.resolve

import com.intellij.lang.properties.references.PropertyReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.jetbrains.annotations.NotNull
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

class GradlePropertyReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement,
                                      context: ProcessingContext): Array<PsiReference> {
    val gradleExtensionsData = GradleExtensionsContributor.getExtensionsFor(element)
    if (gradleExtensionsData == null) return PsiReference.EMPTY_ARRAY
    val props = gradleExtensionsData.properties
    if (props.isNotEmpty()) {
      val pps = props.values.filter { it.name == element.text }
      return pps.map { PropertyReference(it.name, element, null, true) }.toTypedArray()
    }

    return PsiReference.EMPTY_ARRAY
  }
}