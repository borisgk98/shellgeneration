package space.borisgk.shellgeneration;

import java.util.Set;

public class GenerationPluginExtension {
    protected String srcRoot;
    protected String generationPackage;
    protected String srcPackage;
    protected String generationRoot;
    protected Set<String> excludeModels;
    private String templateSrc;

    public String getTemplateSrc() {
        return templateSrc;
    }

    public void setTemplateSrc(String templateSrc) {
        this.templateSrc = templateSrc;
    }

    public Set<String> getExcludeModels() {
        return excludeModels;
    }

    public void setExcludeModels(Set<String> excludeModels) {
        this.excludeModels = excludeModels;
    }

    public String getGenerationPackage() {
        return generationPackage;
    }

    public void setGenerationPackage(String generationPackage) {
        this.generationPackage = generationPackage;
    }

    public String getSrcPackage() {
        return srcPackage;
    }

    public void setSrcPackage(String srcPackage) {
        this.srcPackage = srcPackage;
    }

    public String getGenerationRoot() {
        return generationRoot;
    }

    public void setGenerationRoot(String generationRoot) {
        this.generationRoot = generationRoot;
    }

    public String getSrcRoot() {
        return srcRoot;
    }

    public void setSrcRoot(String srcRoot) {
        this.srcRoot = srcRoot;
    }

    public boolean checkSetUp() {
        return srcRoot != null && srcPackage != null && generationPackage != null && generationRoot != null;
    }
}
