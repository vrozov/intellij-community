package com.intellij.execution.junit;

import com.intellij.execution.Location;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public abstract class RuntimeConfigurationProducer implements Comparable {
  public static final ExtensionPointName<RuntimeConfigurationProducer> RUNTIME_CONFIGURATION_PRODUCER = ExtensionPointName.create("com.intellij.configurationProducer"); 

  public static final Comparator<RuntimeConfigurationProducer> COMPARATOR = new ProducerComparator();
  protected static final int PREFERED = -1;
  private final ConfigurationFactory myConfigurationFactory;
  private RunnerAndConfigurationSettingsImpl myConfiguration;

  public RuntimeConfigurationProducer(final ConfigurationType configurationType) {
    myConfigurationFactory = configurationType.getConfigurationFactories()[0];
  }

  public RuntimeConfigurationProducer createProducer(final Location location, final ConfigurationContext context) {
    final RuntimeConfigurationProducer result = clone();
    result.myConfiguration = location != null ? result.createConfigurationByElement(location, context) : null;
    return result;
  }

  public abstract PsiElement getSourceElement();

  public RunnerAndConfigurationSettingsImpl getConfiguration() {
    return myConfiguration;
  }

  @Nullable
  protected abstract RunnerAndConfigurationSettingsImpl createConfigurationByElement(Location location, ConfigurationContext context);

  public RuntimeConfigurationProducer clone() {
    try {
      return (RuntimeConfigurationProducer)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  protected RunnerAndConfigurationSettingsImpl cloneTemplateConfiguration(final Project project, final ConfigurationContext context) {
    if (context != null) {
      final RuntimeConfiguration original = context.getOriginalConfiguration(myConfigurationFactory.getType());
      if (original != null){
        return RunManagerEx.getInstanceEx(project).createConfiguration(original.clone(), myConfigurationFactory);
      }
    }
    return RunManagerEx.getInstanceEx(project).createConfiguration("", myConfigurationFactory);
  }

  protected void copyStepsBeforeRun(Project project, RunConfiguration runConfiguration) {
    final RunManagerImpl manager = RunManagerImpl.getInstanceImpl(project);
    final RunnerAndConfigurationSettingsImpl template = manager.getConfigurationTemplate(myConfigurationFactory);
    manager.createStepsBeforeRun(template, runConfiguration);
  }

  protected ConfigurationFactory getConfigurationFactory() {
    return myConfigurationFactory;
  }

  public ConfigurationType getConfigurationType() {
    return myConfigurationFactory.getType();
  }

  public static RuntimeConfigurationProducer getInstance(final Class aClass) {
    final RuntimeConfigurationProducer[] configurationProducers = Extensions.getExtensions(RUNTIME_CONFIGURATION_PRODUCER);
    for (RuntimeConfigurationProducer configurationProducer : configurationProducers) {
      if (configurationProducer.getClass() == aClass) {
        return configurationProducer;
      }
    }
    return null;
  }

  private static class ProducerComparator implements Comparator<RuntimeConfigurationProducer> {
    public int compare(final RuntimeConfigurationProducer producer1, final RuntimeConfigurationProducer producer2) {
      final PsiElement psiElement1 = producer1.getSourceElement();
      final PsiElement psiElement2 = producer2.getSourceElement();
      if (doesContains(psiElement1, psiElement2)) return -PREFERED;
      if (doesContains(psiElement2, psiElement1)) return PREFERED;
      return producer1.compareTo(producer2);
    }

    private static boolean doesContains(final PsiElement container, PsiElement element) {
      while ((element = element.getParent()) != null)
        if (container.equals(element)) return true;
      return false;
    }
  }
}
