package de.notion.pipeline.example.jackson;

public class Person {

    private final String name;
    private final int age;
    private final Heart heart;

    public Person(String name, int age, Heart heart) {
        this.name = name;
        this.age = age;
        this.heart = heart;
    }
}
