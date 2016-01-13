package team.chisel.porter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class ChiselBlockPorter {

    public static final String BLOCK_CODE = "factory.newBlock(Material.rock, \"%s\", creator, BlockCarvable.class)";
    public static final String NEW_VARIATION_CODE = ".newVariation(\"%s\",\"%s\")";
    public static final String BUILD = ".build()";
    public static final String ONLY_VARIATION_CODE = NEW_VARIATION_CODE + BUILD;
    public static final String NEXT_VARIATION_CODE = ".next(\"%s\",\"%s\")";
    public static final String FINAL_VARIATION_CODE = NEXT_VARIATION_CODE + BUILD;

    public static final Path INPUT_FOLDER = Paths.get("blocks");
    public static final Path OUTPUT_FOLDER = Paths.get("output");
    
    public static final Path TEXTURE_PATH = OUTPUT_FOLDER.resolve(Paths.get("textures", "blocks"));
    public static final Path MODEL_PATH = OUTPUT_FOLDER.resolve(Paths.get("models", "block"));

    public static void main(String[] args) throws IOException {
        FileUtils.deleteDirectory(OUTPUT_FOLDER.toFile());

        List<BlockData> blocks = forDirectory(INPUT_FOLDER.toFile());
        List<String> allCode = Lists.newArrayList();
        System.out.println("There are " + blocks.size() + " blocks");
        for (BlockData data : blocks.stream().filter(d -> !d.name.equals("blocks")).toArray(BlockData[]::new)) {
            String code = String.format(BLOCK_CODE, data.name);
            System.out.println("Block " + data.name + " has " + data.variations.size() + " different variations");
            for (int i = 0; i < data.variations.size(); i++) {
                BlockVariation var = data.variations.get(i);
                
                String template = data.variations.size() == 1 ? ONLY_VARIATION_CODE : i == 0 ? NEW_VARIATION_CODE : i == data.variations.size() - 1 ? FINAL_VARIATION_CODE : NEXT_VARIATION_CODE;
                code += String.format(template, var.name, data.name);
                
                Path cbOutput = MODEL_PATH.resolve(var.relPath.getParent().resolve(var.name + ".cf"));
                FileUtils.writeLines(cbOutput.toFile(), var.getFaceFile());
                
                Path textureFolder = TEXTURE_PATH.resolve(var.relPath.getParent());
                FileUtils.writeLines(textureFolder.resolve(var.name + ".ctx").toFile(), var.getTexFile());
                FileUtils.copyFile(var.textureFile, textureFolder.resolve(var.textureFile.getName()).toFile());
                for (File f : var.renderType.getAdditionalFiles(var.textureFile)) {
                    try {
                        FileUtils.copyFile(f, textureFolder.resolve(f.getName()).toFile());
                    } catch (FileNotFoundException e) {
                        // Apparently there are some invalid setups
                        System.out.println("Skipping additional file " + f.getPath() + " because it does not exist.");
                    }
                }
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
            List<File> children = Lists.newArrayList(file.listFiles());
            for (File child : children) {
                if (child.getName().endsWith(".png")) {
                    System.out.println("Doing for file " + child.getAbsolutePath());
                    EnumRenderType type = EnumRenderType.forPath(child.getPath());
                    if (type != null) {
                        BlockVariation variation = new BlockVariation(child);
                        File mcmetaFile = new File(child.getPath() + ".mcmeta");
                        if (mcmetaFile.exists()) {
                            variation.setMeta(readFile(mcmetaFile.getPath()));
                        }
                        variationList.add(variation);
                    }
                } else if (child.isDirectory()) {
                    data.addAll(forDirectory(child));
                }
            }
            List<File> removed = Lists.newArrayList();
            variationList.forEach(v -> {
                File[] additional = v.renderType.getAdditionalFiles(v.textureFile);
                for (File f : additional) {
                    if (children.remove(f)) {
                        removed.add(f);
                    }
                }
            });
            variationList = variationList.stream().filter(v -> !removed.contains(v.textureFile)).collect(Collectors.toList());
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
