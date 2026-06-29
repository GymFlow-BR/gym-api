package br.com.gymflow.api.domain.enums;



public enum UserRole {

    ADMIN("Administrador"),
    TEACHER("Professor"),
    STUDENT("Aluno");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
