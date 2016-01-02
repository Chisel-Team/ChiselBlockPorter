package team.chisel.porter;

public enum EnumRenderType {
    NORMAL(""),
    CTM("-ctm"),
    CTMV("-ctmv"),
    CTMH("-ctmh"),
    V4("-v4"),
    V9("-v9"),
    R4("-r4"),
    R9("-r9"),
    R16("-r16");

    private String suffix;

    EnumRenderType(String suffix){
       this.suffix = suffix;
    }

    public boolean isValid(String path){
        if (path.endsWith(".png")){
            path = path.substring(path.length()-4, path.length());
        }
        return path.endsWith(this.suffix);
    }

    public static EnumRenderType forPath(String path){
        for (EnumRenderType type : values()){
            if (type != NORMAL){
                if (type.isValid(path)){
                    return type;
                }
            }
        }
        return NORMAL;
    }

    public String chopSuffix(String path){
        if (path.endsWith(".png")){
            path = path.substring(path.length()-4, path.length());
        }
        return path.substring(path.length() - this.suffix.length(), path.length());
    }
}
