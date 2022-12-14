package org.lionweb.lioncore.java.metamodel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (which is used both for classes and interfaces)
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">MPS equivalent <i>AbstractConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractConcept MPS equivalent <i>SAbstractConcept</i> in SModel
 */
public abstract class FeaturesContainer extends MetamodelElement implements NamespaceProvider {
    private List<Feature> features = new LinkedList<>();

    public FeaturesContainer() {
        super();
    }

    public FeaturesContainer(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public @Nullable Feature getFeatureByName(@Nonnull String simpleName) {
        return allFeatures().stream().filter(feature -> feature.getSimpleName().equals(simpleName)).findFirst()
                .orElse(null);
    }

    public abstract @Nonnull List<Feature> allFeatures();

    public @Nonnull List<Property> allProperties() {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property)f).collect(Collectors.toList());
    }

    public @Nonnull List<Containment> allContainments() {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment)f).collect(Collectors.toList());
    }

    public @Nonnull List<Reference> allReferences() {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference)f).collect(Collectors.toList());
    }

    // TODO should this expose an immutable list to force users to use methods on this class
    //      to modify the collection?
    public @Nonnull List<Feature> getFeatures() {
        return this.features;
    }

    public void addFeature(@Nonnull Feature feature) {
        this.features.add(feature);
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }

    public void addProperty(@Nullable String simpleName, @Nullable DataType dataType, boolean optional, boolean derived) {
        Property property = new Property(simpleName, this);
        property.setType(dataType);
        property.setOptional(optional);
        property.setDerived(derived);
        addFeature(property);
    }

    public void addOptionalProperty(@Nullable String simpleName, @Nullable DataType dataType) {
        addProperty(simpleName, dataType, true, false);
    }

    public void addRequiredProperty(@Nullable String simpleName, @Nullable DataType dataType) {
        addProperty(simpleName, dataType, false, false);
    }

    public void addReference(@Nullable String simpleName, @Nullable FeaturesContainer type, boolean optional, boolean multiple) {
        Reference reference = new Reference(simpleName, this);
        reference.setType(type);
        reference.setDerived(false);
        reference.setOptional(optional);
        reference.setMultiple(multiple);
        addFeature(reference);
    }

    public void addOptionalReference(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addReference(simpleName, type, true, false);
    }

    public void addRequiredReference(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addReference(simpleName, type, false, false);
    }

    public void addMultipleReference(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addReference(simpleName, type, true, true);
    }

    public void addMultipleAndRequiredReference(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addReference(simpleName, type, false, true);
    }

    public void addContainment(@Nullable String simpleName, @Nullable FeaturesContainer type, boolean optional, boolean multiple) {
        Containment containment = new Containment(simpleName, this);
        containment.setType(type);
        containment.setDerived(false);
        containment.setOptional(optional);
        containment.setMultiple(multiple);
        addFeature(containment);
    }

    public void addOptionalContainment(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addContainment(simpleName, type, true, false);
    }

    public void addRequiredContainment(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addContainment(simpleName, type, false, false);
    }

    public void addMultipleContainment(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addContainment(simpleName, type, true, true);
    }

    public void addMultipleAndRequiredContainment(@Nullable String simpleName, @Nullable FeaturesContainer type) {
        addContainment(simpleName, type, false, true);
    }

}
