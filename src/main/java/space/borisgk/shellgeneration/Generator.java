package space.borisgk.shellgeneration;

import space.borisgk.shellgeneration.classloader.ClassLoader;
import space.borisgk.shellgeneration.exception.GenerationPluginException;
import space.borisgk.shellgeneration.util.PackageToPathConverter;

import org.antlr.stringtemplate.StringTemplate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Generator {
    private File srcPackageDir, generationPackageDir, srcDir, generationDir;
    private String generationPackage, srcPackage;

    private PackageToPathConverter converter = new PackageToPathConverter();
    private ClassLoader classLoader;
    private Logger logger = Logger.getLogger("gl");
    private List<Class> models;
    private StringTemplate template;

    public void setUp(GenerationPluginExtension e) throws GenerationPluginException {
        generationPackage = e.getGenerationPackage();
        srcPackage = e.getSrcPackage();
        srcDir = Paths.get(e.getSrcRoot()).toFile();
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
        File templateSrc;
        try {
            templateSrc = new File(getClass().getResource("ShellComponent").toURI());
            if (!templateSrc.exists()) {
                throw new FileNotFoundException("Not found " + templateSrc.toString());
            }
            template = new StringTemplate(getFileData(templateSrc));
        }
        catch (Exception ex) {
            throw new GenerationPluginException(ex);
        }
    }

    protected String getFileData(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private List<Class> getModelClasses() {
        List<Class> apiClasses = new ArrayList<>();
        for (File file : srcPackageDir.listFiles()) {
            try {
                apiClasses.add(classLoader.loadClass(getClassName(file)));
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

    private String getClassName(File file) {
        String fileName = file.getName();
        if (!fileName.substring(fileName.length() - 6).equals(".class")) {
            throw new IllegalArgumentException();
        }
        return srcPackage + "." + fileName.substring(0, fileName.length() - 6);
    }

    public void generate() {
        for (Class model : models) {
            generateShellComponent(model);
        }
    }

    private void generateShellComponent(Class m) {
        
    }
}
