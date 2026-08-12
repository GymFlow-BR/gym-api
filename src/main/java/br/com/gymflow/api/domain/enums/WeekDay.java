package br.com.gymflow.api.domain.enums;

public enum WeekDay {

    MONDAY("Segunda-feira"),
    TUESDAY("Terça-feira"),
    WEDNESDAY("Quarta-feira"),
    THURSDAY("Quinta-feira"),
    FRIDAY("Sexta-feira"),
    SATURDAY("Sábado"),
    SUNDAY("Domingo");

    private final String description;

    WeekDay(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}