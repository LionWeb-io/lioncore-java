package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.BaseNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.utils.Naming;
import org.lionweb.lioncore.java.utils.Validatable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Metamodel will provide the {@link Concept}s necessary to describe data in a particular domain together with supporting
 * elements necessary for the definition of those Concepts.
 *
 * It also represents the namespace within which Concepts and other supporting elements are organized.
 * For example, a Metamodel for accounting could collect several Concepts such as Invoice, Customer, InvoiceLine,
 * Product. It could also contain related elements necessary for the definitions of the concepts. For example, a
 * {@link DataType} named Currency.
 *
 * @see org.eclipse.emf.ecore.EPackage Ecore equivalent <i>EPackage</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html">MPS equivalent <i>Language's structure aspect</i> in documentation</a>
 */
public class Metamodel extends BaseNode implements NamespaceProvider, Validatable {
    // TODO add ID, once details are cleare

    private String qualifiedName;
    private List<Metamodel> dependsOn = new LinkedList<>();
    private List<MetamodelElement> elements = new LinkedList<>();

    public Metamodel() {
    }

    public Metamodel(String qualifiedName) {
        Naming.validateQualifiedName(qualifiedName);
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String namespaceQualifier() {
        return qualifiedName;
    }

    public List<Metamodel> dependsOn() {
        return this.dependsOn;
    }
    public List<MetamodelElement> getElements() {
        return this.elements;
    }

    public void addElement(MetamodelElement element) {
        if (element.getMetamodel() != this) {
            throw new IllegalArgumentException("The given element is not associated to this metamodel: " + element);
        }
        this.elements.add(element);
    }

    public Concept addConcept(String simpleName) {
        Concept concept = new Concept(this, simpleName);
        addElement(concept);
        return concept;
    }

    public ConceptInterface addConceptInterface(String simpleName) {
        ConceptInterface conceptInterface = new ConceptInterface(this, simpleName);
        addElement(conceptInterface);
        return conceptInterface;
    }

    public String getQualifiedName() {
        return this.qualifiedName;
    }

    public MetamodelElement getElementByName(String name) {
        return getElements().stream().filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public Concept getConceptByName(String name) {
        return getElements().stream().filter(element -> element instanceof Concept)
                .map(element -> (Concept)element)
                .filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public ConceptInterface getConceptInterfaceByName(String name) {
        return getElements().stream().filter(element -> element instanceof ConceptInterface)
                .map(element -> (ConceptInterface)element)
                .filter(element -> element.getSimpleName().equals(name)).findFirst()
                .orElse(null);
    }

    public PrimitiveType getPrimitiveTypeByName(String name) {
        MetamodelElement element = this.getElementByName(name);
        if (element == null) {
            return null;
        }
        if (element instanceof PrimitiveType) {
            return (PrimitiveType) element;
        } else {
            throw new RuntimeException("Element " + name + " is not a PrimitiveType");
        }
    }

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getQualifiedName() == null, "Qualified name not set");
    }

    @Override
    public Concept getConcept() {
        // We cannot simply set the field concept because we have a problem of circular dependency
        return LionCore.getMetamodel();
    }

    @Override
    public List<Node> getChildren(Containment containment) {
        if (containment == LionCore.getFeaturesContainer().getContainmentByName("elements")) {
            return this.getElements().stream().collect(Collectors.toList());
        }
        return super.getChildren(containment);
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (reference == LionCore.getAnnotation().getReferenceByName("dependsOn")) {
            return dependsOn().stream().collect(Collectors.toList());
        }
        return super.getReferredNodes(reference);
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (reference == LionCore.getAnnotation().getReferenceByName("dependsOn")) {
            this.dependsOn.add((Metamodel) referredNode);
            return;
        }
        super.addReferredNode(reference, referredNode);
    }
}
