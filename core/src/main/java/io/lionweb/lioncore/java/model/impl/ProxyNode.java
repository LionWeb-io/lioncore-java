package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is basic an ID holder adapted as a Node. It is used as a placeholder to indicate that we
 * know which Node should be used in a particular point, but at this time we cannot/do not want to
 * retrieve the data necessary to properly instantiate it.
 */
public class ProxyNode implements Node {

  private String id;

  public ProxyNode(String id) {
    this.id = id;
  }

  @Override
  public ClassifierInstance<Concept> getParent() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public Object getPropertyValue(Property property) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void setPropertyValue(Property property, Object value) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public List<? extends Node> getChildren() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public List<? extends Node> getChildren(Containment containment) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void addChild(Containment containment, Node child) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void removeChild(Node node) {
    throw cannotDoBecauseProxy();
  }

  @Nonnull
  @Override
  public List<Node> getReferredNodes(@Nonnull Reference reference) {
    throw cannotDoBecauseProxy();
  }

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues(@Nonnull Reference reference) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void addReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referredNode) {
    throw cannotDoBecauseProxy();
  }

  @Nullable
  @Override
  public String getID() {
    return id;
  }

  @Override
  public Partition getPartition() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public Concept getConcept() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public List<AnnotationInstance> getAnnotations() {
    throw cannotDoBecauseProxy();
  }

  @Override
  public Containment getContainmentFeature() {
    throw cannotDoBecauseProxy();
  }

  @Nonnull
  @Override
  public List<AnnotationInstance> getAnnotations(Annotation annotation) {
    throw cannotDoBecauseProxy();
  }

  @Override
  public void addAnnotation(AnnotationInstance instance) {
    throw cannotDoBecauseProxy();
  }

  private IllegalStateException cannotDoBecauseProxy() {
    return new IllegalStateException(
        "Replace the proxy node with a real node to perform this operation");
  }
}
