import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

@Properties(identifier = "PlayerT", context = Context.GLOBAL)
public class Player extends PipelineData {

    private String name;
    private int age;
    private String email;
    private String password;
    private Pet pet;

    public Player(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    public Player(@NotNull Pipeline pipeline, String name, int age, String email, String password) {
        super(pipeline);
        this.name = name;
        this.age = age;
        this.email = email;
        this.password = password;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public String name() {
        return this.name;
    }

    public int age() {
        return this.age;
    }

    public String email() {
        return this.email;
    }

    public String password() {
        return this.password;
    }

    public Pet pet() {
        return this.pet;
    }
}
