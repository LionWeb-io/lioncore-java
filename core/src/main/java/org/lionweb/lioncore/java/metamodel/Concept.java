package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A Concept represents a category of entities sharing the same structure.
 *
 * For example, Invoice would be a Concept. Single entities could be Concept instances, such as Invoice #1/2022.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (with the <code>isInterface</code> flag set to <code>false</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">MPS equivalent <i>Concept</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489090640">MPS equivalent <i>ConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SConcept MPS equivalent <i>SConcept</i> in SModel
 */
public class Concept extends FeaturesContainer {
    // DOUBT: would extended be null only for BaseConcept? Would this be null for all Concept that do not explicitly extend
    //        another concept?

    public Concept() {
        super();
    }

    public Concept(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
    }

    public Concept(@Nullable String simpleName) {
        super(null, simpleName);
    }

    public boolean isAbstract() {
        return (boolean) this.getPropertyValue("abstract", Boolean.class, false);
    }

    public void setAbstract(boolean value) {
        this.setPropertyValue("abstract", value);
    }

    // TODO should this return BaseConcept when extended is equal null?
    public @Nullable Concept getExtendedConcept() {
        return (Concept) this.getLinkSingleValue("extended");
    }

    public @Nonnull List<ConceptInterface> getImplemented() {
        return (List<ConceptInterface>) this.getLinkMultipleValue("implemented");
    }

    public void addImplementedInterface(@Nonnull ConceptInterface conceptInterface) {
        this.addLinkMultipleValue("implemented", conceptInterface, false);
    }

    // TODO should we verify the Concept does not extend itself, even indirectly?
    public void setExtendedConcept(@Nullable Concept extended) {
        this.setLinkSingleValue("extended", extended, true);
    }

    @Override
    public String toString() {
        String qn;
        try {
            qn = this.qualifiedName();
        } catch (Throwable e) {
            qn = "...";
        }
        return "Concept(" + qn + ")";
    }

    @Override
    public @Nonnull List<Feature> allFeatures() {
        // TODO Should this return features which are overriden?
        // TODO Should features be returned in a particular order?
        List<Feature> result = new LinkedList<>();
        result.addAll(this.getFeatures());
        if (this.getExtendedConcept() != null) {
            result.addAll(this.getExtendedConcept().allFeatures());
        }
        for (ConceptInterface superInterface: this.getImplemented()) {
            result.addAll(superInterface.allFeatures());
        }
        return result;
    }

    @Override
    public Concept getConcept() {
        return LionCore.getConcept();
    }

    public @Nullable Property getPropertyByName(String propertyName) {
        return allFeatures().stream().filter(f -> f instanceof Property).map(f -> (Property)f)
                .filter(p -> p.getSimpleName().equals(propertyName)).findFirst().orElse(null);
    }

    public @Nullable Containment getContainmentByName(String containmentName) {
        return allFeatures().stream().filter(f -> f instanceof Containment).map(f -> (Containment)f)
                .filter(c -> c.getSimpleName().equals(containmentName)).findFirst().orElse(null);
    }

    public @Nullable Reference getReferenceByName(String referenceName) {
        return allFeatures().stream().filter(f -> f instanceof Reference).map(f -> (Reference)f)
                .filter(c -> c.getSimpleName().equals(referenceName)).findFirst().orElse(null);
    }

    public @Nullable Link getLinkByName(String linkName) {
        return allFeatures().stream().filter(f -> f instanceof Link).map(f -> (Link)f)
                .filter(c -> c.getSimpleName().equals(linkName)).findFirst().orElse(null);
    }
}
