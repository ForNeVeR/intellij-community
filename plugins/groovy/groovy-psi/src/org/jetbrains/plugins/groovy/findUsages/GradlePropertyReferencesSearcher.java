// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.findUsages;

import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

public class GradlePropertyReferencesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

  public GradlePropertyReferencesSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    if (element instanceof Property
        && element.getContainingFile().getName().equalsIgnoreCase("gradle.properties")
        && queryParameters.getEffectiveSearchScope() instanceof GlobalSearchScope
    ) {
      final Property property = (Property)element;
      final GlobalSearchScope gradleSearchScope =
        new FileByExtensionSearchScope(((GlobalSearchScope)queryParameters.getEffectiveSearchScope()), "gradle");
      final short searchContext = (short)(UsageSearchContext.IN_CODE | UsageSearchContext.IN_STRINGS);
      final MyProcessor processor = new MyProcessor(property);

      queryParameters.getOptimizer().searchWord(
        property.getName(),
        gradleSearchScope,
        searchContext,
        false,
        property,
        processor);
    }
  }

  private static class MyProcessor extends RequestResultProcessor {
    final PsiElement target;

    MyProcessor(PsiElement target) {
      super(target);
      this.target = target;
    }

    @Override
    public boolean processTextOccurrence(@NotNull PsiElement element,
                                         int offsetInElement,
                                         @NotNull Processor<? super PsiReference> consumer) {
      if (element instanceof GrReferenceExpression) {
        if (!consumer.process((GrReferenceExpression)element)) {
          return false;
        }
      }
      return true;
    }
  }

  private class FileByExtensionSearchScope extends DelegatingGlobalSearchScope {
    @NotNull
    private final String extension;

    FileByExtensionSearchScope(GlobalSearchScope scope,
                                      @NotNull String extension) {
      super(scope);
      this.extension = extension;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
      return super.contains(file) && extension.equalsIgnoreCase(file.getExtension());
    }
  }
}
