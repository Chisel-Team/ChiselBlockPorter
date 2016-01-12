package team.chisel.porter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class ChiselBlockPorter {

    public static final String BLOCK_CODE = "factory.newBlock(Material.rock, \"%s\", creator, BlockCarvable.class)";
    public static final String NEW_VARIATION_CODE = ".newVariation(\"%s\",\"%s\")";
    public static final String ONLY_VARIATION_CODE = NEW_VARIATION_CODE+".build();";
    public static final String NEXT_VARIATION_CODE = ".next(\"%s\",\"%s\")";
    public static final String FINAL_VARIATION_CODE = NEXT_VARIATION_CODE + ".build();";

    public static final Path INPUT_FOLDER = Paths.get("blocks");
    public static final Path OUTPUT_FOLDER = Paths.get("output");
    
    public static final Path TEXTURE_PATH = OUTPUT_FOLDER.resolve(Paths.get("textures", "blocks"));
    public static final Path MODEL_PATH = OUTPUT_FOLDER.resolve(Paths.get("models", "block"));

    public static void main(String[] args) throws IOException {
        FileUtils.deleteDirectory(OUTPUT_FOLDER.toFile());

        List<BlockData> blocks = forDirectory(INPUT_FOLDER.toFile());
        List<String> allCode = Lists.newArrayList();
        System.out.println("There are " + blocks.size() + " blocks");
        for (BlockData data : blocks) {
            String code = String.format(BLOCK_CODE, data.name);
            System.out.println("Block " + data.name + " has " + data.variations.size() + " different variations");
            if (data.variations.size() == 1){
                BlockVariation var = data.variations.get(0);
                code += String.format(ONLY_VARIATION_CODE, var.name, data.name);
                allCode.add(code);
                continue;
            }
            for (int i = 0; i < data.variations.size(); i++) {
                BlockVariation var = data.variations.get(i);
                String template = i == 0 ? NEW_VARIATION_CODE : i == data.variations.size() - 1 ? FINAL_VARIATION_CODE : NEXT_VARIATION_CODE;
                code += String.format(template, var.name, data.name);
                
//                Path cbOutput = MODEL_PATH.resolve(Paths.get(data.name, var.name + ".cf"));
//                FileUtils.writeLines(cbOutput.toFile(), var.getFaceFile());
//                
//                Path textureFolder = TEXTURE_PATH.resolve(data.name);
//                FileUtils.writeLines(textureFolder.resolve(var.name + ".ctx").toFile(), var.getTexFile());
//                FileUtils.copyFile(var.textureFile, textureFolder.resolve(var.name + ".png").toFile());
            }
            allCode.add(code);
        }
        System.out.println("Writing code");
        FileUtils.writeLines(OUTPUT_FOLDER.resolve("code.txt").toFile(), allCode);
    }

    private static List<BlockData> forDirectory(File file) {
        List<BlockData> data = new ArrayList<BlockData>();
        if (file.isDirectory()) {
            List<BlockVariation> variationList = new ArrayList<BlockVariation>();
            for (File child : file.listFiles()) {
                if (child.getName().endsWith(".png")) {
                    System.out.println("Doing for file " + child.getAbsolutePath());
                    BlockVariation variation = new BlockVariation(child);
                    File mcmetaFile = new File(child.getPath() + ".mcmeta");
                    if (mcmetaFile.exists()) {
                        variation.setMeta(readFile(mcmetaFile.getPath()));
                    }
                    variationList.add(variation);
                } else if (child.isDirectory()) {
                    data.addAll(forDirectory(child));
                }
            }
            if (!variationList.isEmpty()) {
                BlockData block = new BlockData(file.getName(), variationList);
                data.add(block);
            }
        }
        return data;
    }

    public static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }
}
