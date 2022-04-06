import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

@Properties(identifier = "PlayerT", context = Context.GLOBAL)
public class Player extends PipelineData {

    private String name;
    private int age;

    public Player(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    public Player(@NotNull Pipeline pipeline, String name, int age) {
        super(pipeline);
        this.name = name;
        this.age = age;
    }

    public String name() {
        return this.name;
    }

    public int age() {
        return this.age;
    }
}
