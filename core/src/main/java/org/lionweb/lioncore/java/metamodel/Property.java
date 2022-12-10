package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This indicates a simple value associated to an entity.
 *
 * For example, an Invoice could have a date or an amount.
 *
 * @see org.eclipse.emf.ecore.EAttribute Ecore equivalent <i>EAttribute</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#properties">MPS equivalent <i>Property</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288299">MPS equivalent <i>PropertyDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SProperty MPS equivalent <i>SProperty</i> in SModel
 */
public class Property extends Feature {
    private DataType type;

    public Property() {
        super();
        setConcept(LionCore.getProperty());
    }

    public Property(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
        setConcept(LionCore.getProperty());
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Property{" +
                "simpleName=" + getSimpleName() + ", " +
                "type=" + type +
                '}';
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (reference == LionCore.getProperty().getReferenceByName("type")) {
            return Arrays.asList(getType()).stream().filter(e -> e != null).collect(Collectors.toList());
        }
        return super.getReferredNodes(reference);
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (reference == LionCore.getProperty().getReferenceByName("type")) {
            this.setType((DataType) referredNode);
            return;
        }
        super.addReferredNode(reference, referredNode);
    }
}
