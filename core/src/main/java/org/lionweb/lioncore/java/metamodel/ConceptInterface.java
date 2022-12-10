package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.self.LionCore;

import java.util.LinkedList;
import java.util.List;

/**
 * A ConceptInterface represents a category of entities sharing some similar characteristics.
 *
 * For example, Named would be a ConceptInterface.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (with the <code>isInterface</code> flag set to <code>true</code>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">MPS equivalent <i>Concept Interface</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125989551">MPS equivalent <i>InterfaceConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SInterfaceConcept MPS equivalent <i>SInterfaceConcept</i> in SModel
 */
public class ConceptInterface extends FeaturesContainer {
    private List<ConceptInterface> extended = new LinkedList<>();

    public ConceptInterface() {
        super();
    }

    public ConceptInterface(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<ConceptInterface> getExtendedInterface() {
        return this.extended;
    }

    @Override
    public List<Feature> allFeatures() {
        // TODO Should this return features which are overriden?
        // TODO Should features be returned in a particular order?
        List<Feature> result = new LinkedList<>();
        result.addAll(this.getFeatures());
        for (ConceptInterface superInterface: extended) {
            result.addAll(superInterface.allFeatures());
        }
        return result;
    }

    @Override
    public Concept getConcept() {
        // We cannot simply set the field concept because we have a problem of circular dependency
        return LionCore.getConceptInterface();
    }
}