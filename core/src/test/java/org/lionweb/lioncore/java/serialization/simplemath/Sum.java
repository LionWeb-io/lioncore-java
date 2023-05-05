package org.lionweb.lioncore.java.serialization.simplemath;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Containment;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.serialization.SimpleNode;

public class Sum extends SimpleNode {
  private IntLiteral left;
  private IntLiteral right;

  public Sum(IntLiteral left, IntLiteral right) {
    this.left = left;
    this.right = right;
    assignRandomID();
  }

  public Sum(IntLiteral left, IntLiteral right, String id) {
    this.left = left;
    this.right = right;
    setId(id);
  }

  @Override
  public Concept getConcept() {
    return SimpleMathMetamodel.SUM;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Sum)) return false;
    Sum sum = (Sum) o;
    return Objects.equals(getID(), sum.getID())
        && Objects.equals(left, sum.left)
        && Objects.equals(right, sum.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, right);
  }

  @Override
  public String toString() {
    return "Sum{" + "left=" + left + ", right=" + right + '}';
  }

  @Override
  protected List<? extends Node> concreteGetChildren(Containment containment) {
    if (containment.getName().equals("left")) {
      return Arrays.asList(left);
    }
    if (containment.getName().equals("right")) {
      return Arrays.asList(right);
    }
    return super.concreteGetChildren(containment);
  }
}