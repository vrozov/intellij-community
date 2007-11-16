/*
 * @author max
 */
package com.intellij.lang;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class LanguageStructureViewBuilder extends LanguageExtension<PsiStructureViewFactory>{
  public static final LanguageStructureViewBuilder INSTANCE = new LanguageStructureViewBuilder();

  private LanguageStructureViewBuilder() {
    super("com.intellij.lang.psiStructureViewFactory");
  }

  @Nullable
  public StructureViewBuilder getStructureViewBuilder(PsiFile file) {
    return forLanguage(file.getLanguage()).getStructureViewBuilder(file);
  }
}