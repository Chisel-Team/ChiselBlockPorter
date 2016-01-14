package team.chisel.porter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class BlockData implements Comparable<BlockData> {

    public String name;

    public List<BlockVariation> variations;
    
    public Path relPath;

    public BlockData(File file, List<BlockVariation> variations) {
        this.name = file.getName();
        this.variations = variations;
        this.relPath = ChiselBlockPorter.INPUT_FOLDER.relativize(file.toPath());
    }

    @Override
    public String toString() {
        return "BlockData [name=" + name + ", variations=" + variations + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockData other = (BlockData) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(BlockData o) {
        return this.name.compareTo(o.name);
    }
}
