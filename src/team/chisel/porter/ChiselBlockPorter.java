package team.chisel.porter;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ChiselBlockPorter {

    public static void main(String[] args) {
        List<BlockData> blocks = forDirectory(new File("blocks"));
        String allCode = "";
        //System.out.println("There are "+blocks.size()+" blocks");
        for (BlockData data : blocks){
            String code = "factory.newBlock(\""+data.name+"\").";
            //System.out.println("Block "+data.name+" has "+data.variations.size()+" different variations");
            for (int i = 0 ; i < data.variations.size() ; i++ ){
                BlockVariation var = data.variations.get(i);
                if (i != data.variations.size()-1){
                    code = code + "newVariation(\""+var.name+"\",\""+var.name+"\").setTextureLocation(" +
                            "new ResourceLocation(\"chisel\",\""+data.name+"/"+var.name+"\")).buildVariation.";
                }
                else {
                    code = code + "newVariation(\""+var.name+"\",\""+var.name+"\").setTextureLocation(" +
                            "new ResourceLocation(\"chisel\",\""+data.name+"/"+var.name+"\")).buildVariation.build()";
                }
                //File cbFile = new File(System.getProperty("user.dir")+File.separator+"output"+File.separator+"models"+File.separator+"blocks"+File.separator+data.name, var.name+".cb");
                //write(cbFile, var.getCBFile());

                //File ctxFile = new File(System.getProperty("user.dir")+File.separator+"output"+File.separator+"textures"+File.separator+"blocks"+File.separator+data.name, var.name+".ctx");
                //write(ctxFile, var.getCTXFile());
            }
            allCode = allCode + code + '\n';
        }
        System.out.println("Writing code");
        File codeFile = new File(System.getProperty("user.dir")+File.separator+"code.txt");
        write(codeFile, allCode);
    }

    private static List<BlockData> forDirectory(File file){
        List<BlockData> data = new ArrayList<BlockData>();
        if (file.isDirectory()){
            List<BlockVariation> variationList = new ArrayList<BlockVariation>();
            for (File child : file.listFiles()){
                if (child.getName().endsWith(".png")){
                    //System.out.println("Doing for file "+child.getAbsolutePath());
                    BlockVariation variation = new BlockVariation(child.getAbsolutePath());
                    File mcmetaFile = new File(child.getAbsolutePath()+".mcmeta");
                    if (mcmetaFile.exists()){
                        variation.setMeta(readFile(mcmetaFile.getAbsolutePath()));
                    }
                    variationList.add(variation);
                    BlockData block = new BlockData(file.getName(), variationList);
                    data.add(block);
                }
                else if (child.isDirectory()){
                    data.addAll(forDirectory(child));
                }
            }
        }
        return data;
    }

    public static String readFile(String path){
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception exception){
            exception.printStackTrace();
            return "";
        }
    }

    public static void write(File file, String text){
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            if (file.isDirectory()){
                System.out.println("HMM??");
            }
            PrintWriter writer = new PrintWriter(file);
            writer.write(text);
            writer.close();
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
