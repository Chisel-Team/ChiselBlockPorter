package team.chisel.porter;

import java.io.File;

public class BlockVariation {


    public String name;

    public EnumRenderType renderType;

    public String mcmeta;

    public BlockVariation(String name, EnumRenderType type, String mcmeta){
        this.name = name;
        this.renderType = type;
        this.mcmeta = mcmeta;
    }

    public BlockVariation(String path){
        this.renderType = EnumRenderType.forPath(path);
        String[] parts = this.renderType.chopSuffix(path).split(File.separator);
        this.name = parts[parts.length - 1];
    }

    public void setMeta(String mcmeta){
        this.mcmeta = mcmeta;
    }
}
