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
        //System.out.println("With suffix chopped is" + this.renderType.chopSuffix(path));
        String[] parts = this.renderType.chopSuffix(path).split(File.separator+File.separator);
        this.name = parts[parts.length - 1];
        //System.out.println("Variation name "+name);
    }

    public void setMeta(String mcmeta){
        this.mcmeta = mcmeta;
    }

    public String getCBFile(){
        return "{\n" +
                "\t\"children\":[\n" +
                "\t\t\"./name.ctx\"\n" +
                "\t]\n" +
                "}";
    }

    public String getCTXFile(){
        if (renderType == EnumRenderType.CTM){
            return "{\n" +
                    "\t\"type\":\"CTM\",\n" +
                    "\t\"textures\":[\n" +
                    "\t\t\"./"+name+"\",\n" +
                    "\t\t\"./"+name+"-ctm\"\n" +
                    "\t]\n" +
                    "}";
        }
        else {
            return "{\"type\":\""+this.renderType.getName()+"\"}";
        }
    }
}
