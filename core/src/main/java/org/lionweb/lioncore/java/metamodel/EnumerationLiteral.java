package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.self.LionCore;

import javax.annotation.Nullable;

public class EnumerationLiteral extends M3Node implements NamespacedEntity {
    private String simpleName;
    private Enumeration enumeration;

    public EnumerationLiteral() {
    }

    public EnumerationLiteral(@Nullable String simpleName) {
        this.simpleName = simpleName;
    }

    @Override
    public @Nullable String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(@Nullable String simpleName) {
        this.simpleName = simpleName;
    }

    public @Nullable Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(@Nullable Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public @Nullable Enumeration getContainer() {
        return enumeration;
    }

    @Override
    public Concept getConcept() {
        return LionCore.getEnumerationLiteral();
    }
}
