package red.mohist.common.remap.remappers;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.objectweb.asm.commons.Remapper;
import red.mohist.common.remap.ASMUtils;

/**
 *
 * @author pyz
 * @date 2019/7/2 8:05 PM
 */
public class NMSVersionRemapper extends Remapper implements ClassRemapperSupplier {
    public static final String targetVersion = "v1_12_R1";
    private static final Pattern cbPattern = Pattern.compile("org/bukkit/craftbukkit/(v\\d_\\d+_R\\d)/[\\w/]+");
    private static final Pattern nmsPattern = Pattern.compile("net/minecraft/server/(v\\d_\\d+_R\\d)/[\\w/]+");

    @Override
    public String map(String typeName) {
        String str = typeName;
        Matcher m = cbPattern.matcher(str);
        if (m.find()) {
            String srcVersion = m.group(1);
            if (!Objects.equals(srcVersion, targetVersion)) {
                str = str.replace(srcVersion, targetVersion);
            }
        } else {
            m = nmsPattern.matcher(typeName);
            if (m.find()) {
                String srcVersion = m.group(1);
                if (!Objects.equals(srcVersion, targetVersion)) {
                    str = str.replace(srcVersion, targetVersion);
                }
            }
        }
        return str;
    }

    @Override
    public String mapSignature(String signature, boolean typeSignature) {
        if (ASMUtils.isValidSingnature(signature)) {
            return super.mapSignature(signature, typeSignature);
        } else {
            return signature;
        }
    }
}
