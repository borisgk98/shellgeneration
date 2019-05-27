package space.borisgk.shellgeneration;

import org.gradle.api.GradleException;
import space.borisgk.shellgeneration.exception.GenerationPluginException;

import java.util.HashSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Generator generator = new Generator();
        try {
            generator.setUp(new GenerationPluginExtension(){{
                srcRoot = "/home/boris/progs/work/autoshowroom/abstract_client/out/production/classes";
                srcPackage = "com.mera.borisgk98.autoshowroom.client.models";
                generationRoot = "/home/boris/progs/work/autoshowroom/abstract_client/src/main/java";
                generationPackage = "com.mera.borisgk98.autoshowroom.client.shell.components";
                excludeModels = new HashSet<String>(){{
//                    add("Auto");
                }};
            }});
        }
        catch (GenerationPluginException e) {
            throw new GradleException("Bad generation params", e);
        }
        generator.generate();
    }
}
