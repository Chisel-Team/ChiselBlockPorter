package team.chisel.porter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class BlockVariation {

    public File textureFile;
    
    public String name;

    public EnumRenderType renderType;

    public String mcmeta;
    
    public Path relPath;

    public BlockVariation(String name, EnumRenderType type, String mcmeta, Path relPath){
        this.name = name;
        this.renderType = type;
        this.mcmeta = mcmeta;
        this.relPath = relPath;
    }

    public BlockVariation(File file) {
        this.textureFile = file;
        this.relPath = ChiselBlockPorter.INPUT_FOLDER.relativize(file.toPath());
        this.renderType = EnumRenderType.forPath(file.getPath());
        // System.out.println("With suffix chopped is" + this.renderType.chopSuffix(path));
        String[] parts = this.renderType.chopSuffix(file.getPath()).split(File.separator+File.separator);
        this.name = parts[parts.length - 1];
        //System.out.println("Variation name "+name);
    }

    public void setMeta(String mcmeta){
        this.mcmeta = mcmeta;
    }

    public List<String> getFaceFile(){
        return tabsToSpaces(
                "{", 
                "\t\"textures\":[", 
                "\t\t\"./" + name + ".ctx\"", 
                "\t]", 
                "}"
        );
    }

    public List<String> getTexFile(){
        if (renderType == EnumRenderType.CTM) {
            return tabsToSpaces(
                    "{", 
                    "\t\"type\":\"CTM\",", 
                    "\t\"textures\":[", 
                    "\t\t\"./" + name + "\",",  
                    "\t\t\"./" + name + "-ctm\"", 
                    "\t]\n", 
                    "}"
            );
        } else {
            return Lists.newArrayList("{\"type\":\""+this.renderType.getName()+"\"}");
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
