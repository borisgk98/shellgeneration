package space.borisgk.shellgeneration.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PackageToPathConverter implements Converter<String, Path> {
    @Override
    public Path convert(String o) {
        return Paths.get(o.replace('.', '/'));
    }
}
