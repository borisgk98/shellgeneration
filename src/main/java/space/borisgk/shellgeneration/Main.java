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
                srcRoot = "/home/boris/progs/autoshowroom/soap_client/build/classes/java/main";
                srcPackage = "com.mera.borisgk98.autoshowroom.soapclient.model";
                generationRoot = "/home/boris/progs/autoshowroom/soap_client/src/main/java";
                generationPackage = "com.mera.borisgk98.autoshowroom.soapclient.component";
                excludeModels = new HashSet<String>(){{
//                    add("Auto");
                }};
                setTemplateSrc("/home/boris/progs/autoshowroom/soap_client/src/main/resources/templates/ShellComponent");
            }});
        }
        catch (GenerationPluginException e) {
            throw new GradleException("Bad generation params", e);
        }
        generator.generate();
    }
}
