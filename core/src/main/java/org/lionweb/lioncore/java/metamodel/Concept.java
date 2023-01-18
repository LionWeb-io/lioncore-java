package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private boolean isAbstract;
    // DOUBT: would this be null only for BaseConcept? Would this be null for all Concept that do not explicitly extend
    //        another concept?
    private Concept extended;
    private List<ConceptInterface> implemented = new LinkedList<>();

    public Concept() {
        super();
        setConcept(LionCore.getConcept());
    }

    public Concept(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        super(metamodel, simpleName);
        setConcept(LionCore.getConcept());
    }

    public Concept(@Nullable String simpleName) {
        super(null, simpleName);
        setConcept(LionCore.getConcept());
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    // TODO should this return BaseConcept when extended is equal null?
    public @Nullable Concept getExtendedConcept() {
        return this.extended;
    }

    public @Nonnull List<ConceptInterface> getImplemented() {
        return this.implemented;
    }

    public void addImplementedInterface(@Nonnull ConceptInterface conceptInterface) {
        this.implemented.add(conceptInterface);
    }

    // TODO should we verify the Concept does not extend itself, even indirectly?
    public void setExtendedConcept(@Nullable Concept extended) {
        this.extended = extended;
    }

    @Override
    public String toString() {
        return "Concept(" + this.qualifiedName() + ")";
    }

    @Override
    public @Nonnull List<Feature> allFeatures() {
        // TODO Should this return features which are overriden?
        // TODO Should features be returned in a particular order?
        List<Feature> result = new LinkedList<>();
        result.addAll(this.getFeatures());
        if (this.extended != null) {
            result.addAll(this.extended.allFeatures());
        }
        for (ConceptInterface superInterface: implemented) {
            result.addAll(superInterface.allFeatures());
        }
        return result;
    }
}
