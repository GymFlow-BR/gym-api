package br.com.gymflow.api.domain.enums;

public enum WorkoutStatus {

    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    ARCHIVED("Arquivado");

    private final String description;

    WorkoutStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
