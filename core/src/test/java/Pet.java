public class Pet {

    private String name;
    private int age;
    private String alias;

    public Pet(String name, int age, String alias) {
        this.name = name;
        this.age = age;
        this.alias = alias;
    }

    public String name() {
        return this.name;
    }

    public int age() {
        return this.age;
    }

    public String alias() {
        return this.alias;
    }
}
