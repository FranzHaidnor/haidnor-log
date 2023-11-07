package haidnor.remoting.common;

import java.util.HashSet;
import java.util.Set;

/**
 * 接口路径注册器
 */
public class CommandRegistrar {

    private final Set<Integer> set = new HashSet<>();

    public void register(String command) {
        int hashCode = command.hashCode();
        boolean containsKey = set.contains(hashCode);
        if (containsKey) {
            throw new Error("already exists command hashCode");
        }
        set.add(hashCode);
    }

    public void checkCommandHash(String command) {
        int hashCode = command.hashCode();
        boolean containsKey = set.contains(hashCode);
        if (!containsKey) {
            throw new Error("command '" + command + "' not register");
        }
    }

}
