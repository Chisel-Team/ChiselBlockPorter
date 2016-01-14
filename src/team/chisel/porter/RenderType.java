package team.chisel.porter;

import java.io.File;
import java.util.function.Function;

import org.apache.commons.lang.ArrayUtils;

public enum RenderType {
    NORMAL(""),
    CTM("-ctm", f -> new File[] { new File(f.getPath().replace("-ctm", "")) }),
    CTMV("-ctmv", f -> new File[] { new File(f.getPath().replace("-ctmh", "-top")) }),
    CTMH("-ctmh", CTMV.fileFunc),
    V4("-v4"),
    V9("-v9"),
    R4("-r4"),
    R9("-r9"),
    R16("-r16"),
    TOP("-top", f -> {
        File side = new File(f.getPath().replace("-top", "-side"));
        File bot = new File(f.getPath().replace("-top", "-bot"));
        if (bot.exists()) {
            return new File[] { side, bot };
        }
        return new File[] { side };
    }),
    BOT("-bot"),
    SIDE("-side");

    private static final RenderType[] ROOTS = new RenderType[] { NORMAL, CTM, CTMV, CTMH, V4, V9, R4, R9, R16, TOP };

    private String suffix;
    private Function<File, File[]> fileFunc;

    private RenderType(String suffix) {
        this(suffix, f -> new File[0]);
    }

    private RenderType(String suffix, Function<File, File[]> fileFunc) {
        this.suffix = suffix;
        this.fileFunc = fileFunc;
    }

    public boolean isValid(String path) {
        path = path.replace(".png", "");
        return path.endsWith(this.suffix);
    }
    
    public File[] getAdditionalFiles(File root) {
        return fileFunc.apply(root);
    }

    public static RenderType forPath(String path) {
        for (RenderType type : values()) {
            if (type != NORMAL) {
                if (type.isValid(path)) {
                    return ArrayUtils.contains(ROOTS, type) ? type : null;
                }
            }
        }
        return NORMAL;
    }

    public String chopSuffix(String path){
        if (path.endsWith(".png")){
            path = path.substring(0 , path.length()-4);
        }
        return path.substring(0, path.length() - this.suffix.length());
    }

    public String getName(){
        return this.name();
    }
}
