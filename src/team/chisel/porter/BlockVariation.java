package team.chisel.porter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class BlockVariation {

    public File textureFile;
    
    public String name;

    public RenderType renderType;

    public List<String> mcmeta = Lists.newArrayList();
    
    public Path relPath;

    public BlockVariation(String name, RenderType type, List<String> mcmeta, Path relPath){
        this.name = name;
        this.renderType = type;
        this.mcmeta = mcmeta;
        this.relPath = relPath;
    }

    public BlockVariation(File file) {
        this.textureFile = file;
        this.relPath = ChiselBlockPorter.INPUT_FOLDER.relativize(file.toPath());
        this.renderType = RenderType.forPath(file.getPath());
        // System.out.println("With suffix chopped is" + this.renderType.chopSuffix(path));
        String[] parts = this.renderType.chopSuffix(file.getPath()).split(File.separator+File.separator);
        this.name = parts[parts.length - 1];
        //System.out.println("Variation name "+name);
    }

    public void setMeta(List<String> mcmeta){
        this.mcmeta = mcmeta;
    }

    public static final List<String> FACE_FILE_BEFORE = tabsToSpaces("{", "\t\"textures\":[");
    public static final String FACE_FILE_TEX = "\t\t\"./%s.ctx\"";
    public static final List<String> FACE_FILE_AFTER = tabsToSpaces("\t]", "}");
    
    public List<String> getFaceFile(String name){
        List<String> ret = Lists.newArrayList(FACE_FILE_BEFORE);
        ret.addAll(tabsToSpaces(String.format(FACE_FILE_TEX, name)));
        ret.addAll(FACE_FILE_AFTER);
        return ret;
    }

    public static final String TEX_FILE_SIMPLE = "{\"type\":\"%s\"}";
    public static final String TEX_FILE_BEFORE = "{";
    public static final String TEX_FILE_TYPE = "\t\"type\":\"%s\",";
    public static final String TEX_FILE_1 = "\t\"textures\":[";
    public static final String TEX_FILE_TEX = "\t\t\"./%s\"";
    public static final List<String> TEX_FILE_AFTER = tabsToSpaces("\t]", "}");

    public List<String> getTexFile() {

        if (renderType == RenderType.CTM || renderType == RenderType.CTMH || renderType == RenderType.CTMV) {
            List<String> ret = Lists.newArrayList(TEX_FILE_BEFORE);
            ret.add(String.format(TEX_FILE_TYPE, renderType));
            ret.add(TEX_FILE_1);
            File[] additionalFiles = renderType.getAdditionalFiles(textureFile);
            String[] textures = new String[additionalFiles.length + 1];
            for (int i = 0; i < textures.length - 1; i++) {
                textures[i] = String.format(TEX_FILE_TEX, additionalFiles[i].getName().replace(".png", "")) + ",";
            }
            
            textures[textures.length - 1] = String.format(TEX_FILE_TEX, textureFile.getName().replace(".png", ""));

            ret.addAll(tabsToSpaces(textures));
            ret.addAll(TEX_FILE_AFTER);
            return ret;
        } else {
            return Lists.newArrayList(String.format(TEX_FILE_SIMPLE, renderType));
        }
    }

    private static List<String> tabsToSpaces(String... lines) {
        return Lists.newArrayList(lines).stream().map(s -> s.replace("\t", "    ")).collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "BlockVariation [textureFile=" + textureFile + ", renderType=" + renderType + "]";
    }
}
