package team.chisel.porter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.Lists;

public class ChiselBlockPorter {

    public static final String ENUM_START = "%s {";
    public static final List<String> ENUM_BEFORE = Lists.newArrayList("", "@Override", "void addBlocks(ChiselBlockFactory factory) {");
    public static final String ENUM_AFTER = "}";
    public static final String ENUM_END = "},";
    public static final String BLOCK_CODE = "factory.newBlock(Material.rock, \"%s\", creator, BlockCarvable.class)";
    public static final String NEW_VARIATION_CODE = ".newVariation(\"%s\")";
    public static final String PARENT_FOLDER_CODE = ".setParentFolder(\"%s\")";
    public static final String BUILD = ".build();";
    public static final String NEXT_VARIATION_CODE = ".next(\"%s\")";
    public static final String SET_TEXTURE_LOCATION = ".setTextureLocation(%s)";
    public static final String SIDE_TEXTURE_LOCATION_CODE = String.format(SET_TEXTURE_LOCATION, "\"%s\"");
    public static final String SPECIAL_TEXTURE_LOCATION_CODE = String.format(SET_TEXTURE_LOCATION, "\"%s\", %s");

    public static final String Y_AXIS = "Axis.Y", UP = "EnumFacing.UP", DOWN = "EnumFacing.DOWN";

    public static final Path INPUT_FOLDER = Paths.get("blocks");
    public static final Path OUTPUT_FOLDER = Paths.get("output");

    public static final Path TEXTURE_PATH = OUTPUT_FOLDER.resolve(Paths.get("textures", "blocks"));
    public static final Path MODEL_PATH = OUTPUT_FOLDER.resolve(Paths.get("models", "block"));

    public static void main(String[] args) throws IOException {
        FileUtils.deleteDirectory(OUTPUT_FOLDER.toFile());

        Set<BlockData> blocks = forDirectory(INPUT_FOLDER.toFile());
        List<String> allCode = Lists.newArrayList();
        System.out.println("There are " + blocks.size() + " blocks");
        for (BlockData data : blocks.stream().filter(d -> !d.name.equals("blocks")).toArray(BlockData[]::new)) {
            char[] name = data.name.replace('-', '_').toCharArray();
            for (int i = 0; i < name.length; i++) {
                if (Character.isUpperCase(name[i])) {
                    name = ArrayUtils.add(name, i++, '_');
                }
                name[i] = Character.toUpperCase(name[i]);
            }
            allCode.add(String.format(ENUM_START, new String(name)));
            allCode.addAll(ENUM_BEFORE);

            String code = String.format(BLOCK_CODE, data.name);
            System.out.println("Block " + data.name + " has " + data.variations.size() + " different variations");

            if (data.relPath.getParent() != null) {
                code += String.format(PARENT_FOLDER_CODE, data.relPath.getParent().toString().replace('\\', '/'));
            }

            for (int i = 0; i < data.variations.size(); i++) {
                BlockVariation var = data.variations.get(i);
                File[] additionalFiles = var.renderType.getAdditionalFiles(var.textureFile);

                String template = i == 0 ? NEW_VARIATION_CODE : NEXT_VARIATION_CODE;
                code += String.format(template, var.name);
                if (var.renderType == RenderType.TOP) {
                    String path = var.relPath.getParent().resolve(var.name).toString().replace('\\', '/');
                    code += String.format(SIDE_TEXTURE_LOCATION_CODE, path + "-side");
                    if (additionalFiles.length > 2) {
                        code += String.format(SPECIAL_TEXTURE_LOCATION_CODE, path + "-top", UP);
                        code += String.format(SPECIAL_TEXTURE_LOCATION_CODE, path + "-bot", DOWN);
                    } else {
                        code += String.format(SPECIAL_TEXTURE_LOCATION_CODE, path + "-top", Y_AXIS);
                    }
                }

                if (i == data.variations.size() - 1) {
                    code += BUILD;
                }

                Path cbOutput = MODEL_PATH.resolve(var.relPath.getParent());
                String simpleFileName = var.textureFile.getName().replace(".png", "");
                if (var.renderType != RenderType.TOP) {
                    simpleFileName = var.renderType.chopSuffix(simpleFileName);
                }
                FileUtils.writeLines(cbOutput.resolve(simpleFileName + ".cf").toFile(), var.getFaceFile(simpleFileName));
                if (var.renderType == RenderType.TOP) {
                    for (File f : additionalFiles) {
                        simpleFileName = f.getName().replace(".png", "");
                        FileUtils.writeLines(cbOutput.resolve(simpleFileName + ".cf").toFile(), var.getFaceFile(simpleFileName));
                    }
                }

                Path textureFolder = TEXTURE_PATH.resolve(var.relPath.getParent());
                if (var.renderType != RenderType.NORMAL && var.renderType != RenderType.TOP) {
                    FileUtils.writeLines(textureFolder.resolve(var.name + ".ctx").toFile(), var.getTexFile());
                }

                String texName = var.textureFile.getName();
                texName = additionalFiles.length == 0 ? var.renderType.chopSuffix(texName) + ".png" : texName;
                FileUtils.copyFile(var.textureFile, textureFolder.resolve(texName).toFile());
                if (!var.mcmeta.isEmpty()) {
                    FileUtils.writeLines(textureFolder.resolve(texName + ".mcmeta").toFile(), var.mcmeta);
                }
                for (File f : additionalFiles) {
                    try {
                        FileUtils.copyFile(f, textureFolder.resolve(f.getName()).toFile());
                        File mcmeta = new File(f.getPath() + ".mcmeta");
                        if (mcmeta.exists()) {
                            FileUtils.copyFile(mcmeta, textureFolder.resolve(f.getName() + ".mcmeta").toFile());
                        }
                    } catch (FileNotFoundException e) {
                        // Apparently there are some invalid setups
                        System.out.println("Skipping additional file " + f.getPath() + " because it does not exist.");
                    }
                }
            }
            allCode.add(code);
            allCode.add(ENUM_AFTER);
            allCode.add(ENUM_END);
            allCode.add("");
        }
        System.out.println("Writing code");
        FileUtils.writeLines(OUTPUT_FOLDER.resolve("code.txt").toFile(), allCode);
    }

    private static Set<BlockData> forDirectory(File file) throws IOException {
        Set<BlockData> data = new TreeSet<>();
        if (file.isDirectory()) {
            List<BlockVariation> variationList = new ArrayList<BlockVariation>();
            List<File> children = Lists.newArrayList(file.listFiles());
            for (File child : children) {
                if (child.getName().endsWith(".png")) {
                    System.out.println("Doing for file " + child.getAbsolutePath());
                    RenderType type = RenderType.forPath(child.getPath());
                    if (type != null) {
                        BlockVariation variation = new BlockVariation(child);
                        File mcmetaFile = new File(child.getPath() + ".mcmeta");
                        if (mcmetaFile.exists()) {
                            variation.setMeta(FileUtils.readLines(mcmetaFile));
                            if (variation.mcmeta.isEmpty()) {
                                mcmetaFile.delete();
                            }
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
                BlockData block = new BlockData(file, variationList);
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
