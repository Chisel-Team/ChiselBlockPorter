package team.chisel.porter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;



public class DefaultFaceStripper {

    public static void main(String[] args) throws IOException {
        File dir = new File(args[0]);
        final Pattern pattern = Pattern.compile("        \\\"\\.\\/.+\\.ctx\\\"");
        Files.walk(dir.toPath()).forEach(p -> {
            if (p.toString().endsWith(".cf")) {
                try {
                    List<String> lines = FileUtils.readLines(p.toFile());
                    if (lines.get(0).equals("{") && lines.get(1).equals("    \"textures\":[") && lines.get(3).equals("    ]") && lines.get(4).equals("}")) {
                        if (pattern.matcher(lines.get(2)).find()) {
                            p.toFile().delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (p.toFile().isDirectory() && p.toFile().listFiles().length == 0) {
                p.toFile().delete();
            }
        });
    }

}
