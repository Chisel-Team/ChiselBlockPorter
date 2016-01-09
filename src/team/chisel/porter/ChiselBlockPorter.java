package team.chisel.porter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ChiselBlockPorter {

    public static void main(String[] args) {

    }

    private static List<BlockData> forDirectory(File file){
        List<BlockData> data = new ArrayList<BlockData>();
        if (file.isDirectory()){
            List<BlockVariation> variationList = new ArrayList<BlockVariation>();
            for (File child : file.listFiles()){
                if (child.getName().endsWith(".png")){
                    BlockVariation variation = new BlockVariation(child.getAbsolutePath());
                    File mcmetaFile = new File(child.getAbsolutePath()+".mcmeta");
                    if (mcmetaFile.exists()){
                        variation.setMeta(readFile(mcmetaFile.getAbsolutePath()));
                    }
                    variationList.add(variation);
                }
                else if (child.isDirectory()){
                    data.addAll(forDirectory(child));
                }
            }
        }
        return data;
    }

    static String readFile(String path){
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception exception){
            exception.printStackTrace();
            return "";
        }
    }
}
