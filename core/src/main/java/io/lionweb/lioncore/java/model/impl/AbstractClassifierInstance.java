package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public abstract class AbstractClassifierInstance<T extends Classifier<T>>
    implements ClassifierInstance<T> {
  protected final List<AnnotationInstance> annotations = new ArrayList<>();

  @Override
  public List<AnnotationInstance> getAnnotations() {
    return Collections.unmodifiableList(annotations);
  }

  /**
   * Given a specific Annotation type it returns either the list of instances of that Annotation
   * associated to the Node.
   */
  @Nonnull
  public List<AnnotationInstance> getAnnotations(@Nonnull Annotation annotation) {
    return annotations.stream()
        .filter(a -> a.getAnnotationDefinition() == annotation)
        .collect(Collectors.toList());
  }

  /**
   * If an annotation instance was already associated under the Annotation link used by this
   * AnnotationInstance, and the annotation does not support multiple values, then the existing
   * instance will be removed and replaced by the instance specified in the call to this method.
   *
   * @throws IllegalArgumentException In case the specified Annotation link cannot be used on Nodes
   *     of this Concept.
   */
  public void addAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(this);
    }
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      // the previous setAnnotated could potentially have already set annotations
      return;
    }
    this.annotations.add(instance);
  }

  public void removeAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      throw new IllegalArgumentException();
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }

  void tryToRemoveAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }
}