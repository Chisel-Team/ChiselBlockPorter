package team.chisel.porter;

import java.io.File;

public class BlockVariation {


    public String name;

    public EnumRenderType renderType;

    public BlockVariation(String name, EnumRenderType type){
        this.name = name;
        this.renderType = type;
    }

    public BlockVariation(String path){
        this.renderType = EnumRenderType.forPath(path);
        String[] parts = this.renderType.chopSuffix(path).split(File.separator);
        this.name = parts[parts.length - 1];
    }
}
