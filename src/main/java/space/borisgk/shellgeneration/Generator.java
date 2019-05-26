package space.borisgk.shellgeneration;

import space.borisgk.shellgeneration.classloader.ClassLoader;
import space.borisgk.shellgeneration.exception.GenerationPluginException;
import space.borisgk.shellgeneration.util.PackageToPathConverter;

import org.antlr.stringtemplate.StringTemplate;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Generator {
    private File srcPackageDir, generationPackageDir, srcDir, generationDir;
    private String generationPackage, srcPackage;

    private PackageToPathConverter converter = new PackageToPathConverter();
    private ClassLoader classLoader;
    private Logger logger = Logger.getLogger("gl");
    private List<Class> models;
    private Set<String> excludeModels;
    private StringTemplate template;

    public void setUp(GenerationPluginExtension e) throws GenerationPluginException {
        generationPackage = e.getGenerationPackage();
        srcPackage = e.getSrcPackage();
        srcDir = Paths.get(e.getSrcRoot()).toFile();
        excludeModels = e.getExcludeModels();
        if (!srcDir.exists()) {
            throw new GenerationPluginException(String.format("srcRoot %s does not exist", e.getSrcRoot()));
        }
        if (srcDir.isFile()) {
            throw new GenerationPluginException(String.format("srcRoot %s should be dir", e.getSrcRoot()));
        }
        srcPackageDir = srcDir.toPath().resolve(converter.convert(e.getSrcPackage())).toFile();
        if (!srcPackageDir.exists() || srcPackageDir.isFile()) {
            throw new GenerationPluginException(String.format("srcPackage %s does not exist", e.getSrcPackage()));
        }
        generationDir = Paths.get(e.getGenerationRoot()).toFile();
        if (!generationDir.exists()) {
            generationDir.mkdirs();
        }
        generationPackageDir = srcDir.toPath().resolve(converter.convert(e.getGenerationPackage())).toFile();
        if (!generationPackageDir.exists()) {
            generationPackageDir.mkdirs();
        }
        classLoader = new ClassLoader(srcDir, srcPackage);
        models = getModelClasses();
        try {
            String templateSrc = inputStreamToString(getClass().getClassLoader().getResourceAsStream(("ShellComponent")));
            template = new StringTemplate(templateSrc);
        }
        catch (Exception ex) {
            throw new GenerationPluginException(ex);
        }
    }

    public String inputStreamToString(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

    protected String getFileData(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private List<Class> getModelClasses() {
        List<Class> apiClasses = new ArrayList<>();
        for (File file : srcPackageDir.listFiles()) {
            if (excludeModels.contains(fileModelName(file))) {
                continue;
            }
            try {
                Class c = classLoader.loadClass(getClassName(file));
                if (c.isEnum() || c.isInterface()) {
                    continue;
                }
                apiClasses.add(c);
            }
            catch (Exception e) {
                logger.warning(String.format("Cannot get class from %s", file.getAbsolutePath()));
            }
            catch (LinkageError e) {
                logger.warning(e.toString());
            }
        }
        return apiClasses;
    }

    private String fileModelName(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.length() - 6);
    }

    private String getClassName(File file) {
        String modelName = fileModelName(file);
        return srcPackage + "." + modelName;
    }

    public void generate() {
        for (Class model : models) {
            generateShellComponent(model);
        }
    }

    private void generateShellComponent(Class m) {
        List<String> fields = getFields(m);
        String name = m.getSimpleName();
        System.out.println(name + ":");
        for (String s : fields) {
            System.out.println("\t" + s);
        }
        System.out.println();
    }

    private List<String> getFields(Class m) {
        List<String> res = new ArrayList<>();
        for (Field field : m.getDeclaredFields()) {
            res.add(field.getName());
        }
        return res;
    }
}
