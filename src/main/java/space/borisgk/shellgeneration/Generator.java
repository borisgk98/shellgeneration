package space.borisgk.shellgeneration;

import space.borisgk.shellgeneration.classloader.ClassLoader;
import space.borisgk.shellgeneration.exception.GenerationPluginException;
import space.borisgk.shellgeneration.util.PackageToPathConverter;

import org.antlr.stringtemplate.StringTemplate;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
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
        generationPackageDir = generationDir.toPath().resolve(converter.convert(e.getGenerationPackage())).toFile();
        if (!generationPackageDir.exists()) {
            generationPackageDir.mkdirs();
        }
        classLoader = new ClassLoader(srcDir, srcPackage);
        models = getModelClasses();
        try {
            String templateSrc = getFileData(new File("/home/boris/progs/work/chellgeneration/src/main/resources/ShellComponent"));
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
        List<Field> fields = getFields(m);
        String Model = m.getSimpleName();
        String model = Model.toLowerCase();
        String settersBlock = generateSettersBlock(fields);
        String paramsBlock = generateParamsBlock(fields);

        template.setAttribute("model", model);
        template.setAttribute("Model", Model);
        template.setAttribute("paramsBlock", paramsBlock);
        template.setAttribute("settersBlock", settersBlock);

        File out = generationPackageDir.toPath().resolve(Model + "ShellComponent.java").toFile();
        writeStringDateToFile(out, template.toString());

        template.reset();
    }

    protected void writeStringDateToFile(File file, String s) {
        try {
            this.logger.info(String.format("Write new class to %s", file.toPath().toString()));
            Files.write(file.toPath(), s.getBytes(), new OpenOption[0]);
        } catch (IOException var4) {
            this.logger.warning("Cannot write file " + file);
        }

    }

    private String generateParamsBlock(List<Field> fields) {
        StringBuilder builder = new StringBuilder();
        String tabLevel = "\t\t\t";
        builder.append("\n");
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            builder.append(tabLevel);
            builder.append("@ShellOption ");
            Class ftype = f.getType();
            // примитивы
            if (Number.class.isAssignableFrom(ftype) ||
                String.class.equals(ftype)
            ) {
                builder.append(ftype.getSimpleName());
            }
            // список из объектов
            else if (List.class.isAssignableFrom(ftype)) {
                builder.append("List<Integer>");
            }
            // другой объект
            else {
                builder.append("Integer");
            }
            builder.append(" ");
            builder.append(f.getName());
            if (i != fields.size() - 1) {
                builder.append(", ");
            }
            builder.append("\n");
        }
        return builder.toString();
//        return "";
    }

    private String generateSettersBlock(List<Field> fields) {
        StringBuilder builder = new StringBuilder();
        String tabLevel = "\t\t\t";

        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            String name = f.getName();
            String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            builder.append(setterName);
            builder.append("(");
            builder.append(name);
            builder.append(")");
            builder.append(";");
            builder.append("\n");
        }

        return builder.toString();
    }

    private List<Field> getFields(Class m) {
        List<Field> res = new ArrayList<>();
        for (Field field : m.getDeclaredFields()) {
            if (field.getName().equals("id")) {
                continue;
            }
            res.add(field);
        }
        return res;
    }
}
