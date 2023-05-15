package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A ConceptInterface represents a category of entities sharing some similar characteristics.
 *
 * <p>For example, Named would be a ConceptInterface.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (with the <code>isInterface
 *     </code> flag set to <code>true</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">MPS
 *     equivalent <i>Concept Interface</i> in documentation</a>
 * @see <a
 *     href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125989551">MPS
 *     equivalent <i>InterfaceConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SInterfaceConcept MPS equivalent <i>SInterfaceConcept</i>
 *     in SModel
 */
public class ConceptInterface extends FeaturesContainer<ConceptInterface> {
  public ConceptInterface() {
    super();
  }

  public ConceptInterface(@Nullable Language language, @Nullable String name, @Nonnull String id) {
    super(language, name, id);
  }

  public ConceptInterface(@Nullable Language language, @Nullable String name) {
    super(language, name);
  }

  public ConceptInterface(@Nullable String name) {
    super(null, name);
  }

  public ConceptInterface(@Nullable String name, @Nonnull String id) {
    super(null, name, id);
  }

  public @Nonnull List<ConceptInterface> getExtendedInterfaces() {
    return getReferenceMultipleValue("extends");
  }

  public void addExtendedInterface(@Nonnull ConceptInterface extendedInterface) {
    Objects.requireNonNull(extendedInterface, "extendedInterface should not be null");
    this.addReferenceMultipleValue(
        "extends", new ReferenceValue(extendedInterface, extendedInterface.getName()));
  }

  @Override
  public @Nonnull List<Feature> allFeatures() {
    // TODO Should this return features which are overriden?
    // TODO Should features be returned in a particular order?
    List<Feature> result = new LinkedList<>();
    result.addAll(this.getFeatures());
    for (ConceptInterface superInterface : getExtendedInterfaces()) {
      result.addAll(superInterface.allFeatures());
    }
    return result;
  }

  @Override
  public Concept getConcept() {
    return LionCore.getConceptInterface();
  }

  @Nonnull
  @Override
  public List<FeaturesContainer<?>> directAncestors() {
    return (List<FeaturesContainer<?>>) (Object) this.getExtendedInterfaces();
  }
}