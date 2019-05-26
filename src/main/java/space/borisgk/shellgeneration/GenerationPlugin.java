package space.borisgk.shellgeneration;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GenerationPlugin implements Plugin<Project> {
    public GenerationPlugin() {
    }

    public void apply(Project project) {
        project.getExtensions().create("crudGenerationSetting", GenerationPluginExtension.class, new Object[0]);
        project.getTasks().create("crudGenerate", GenerationTask.class);
    }
}
