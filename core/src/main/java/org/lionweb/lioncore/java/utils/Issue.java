package org.lionweb.lioncore.java.utils;

import java.util.Objects;

public class Issue {
    private String message;
    private IssueSeverity severity;

    private Object subject;

    public Issue(IssueSeverity severity, String message, Object subject) {
        this.message = message;
        this.severity = severity;
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    // TODO once each element of the Metamodel implement the Node interface this method could return a Node
    public Object getSubject() {
        return subject;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, severity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return Objects.equals(message, issue.message) && severity == issue.severity && subject == issue.subject;
    }


    @Override
    public String toString() {
        return "Issue{" +
                "message='" + message + '\'' +
                ", severity=" + severity +
                ", subject=" + subject +
                '}';
    }


    public boolean isError() {
        return severity == IssueSeverity.Error;
    }
}
