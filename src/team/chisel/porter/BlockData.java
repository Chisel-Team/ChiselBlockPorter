package team.chisel.porter;

import java.io.File;
import java.util.List;

public class BlockData {

    public String name;

    public List<BlockVariation> variations;

    public BlockData(String name, List<BlockVariation> variations){
        this.name = name;
        this.variations = variations;
    }
}
