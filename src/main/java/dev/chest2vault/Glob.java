package dev.chest2vault;

import java.util.regex.Pattern;

/** ルートテーブルの ID の glob。`*` は `/` を含む任意の文字の並び（空も含む）。ほかの文字はそのまま。ID 全体と照合する。 */
final class Glob {
    private Glob() {}

    static boolean matches(String glob, String id) {
        StringBuilder regex = new StringBuilder();
        for (String literal : glob.split("\\*", -1)) {
            if (!regex.isEmpty()) {
                regex.append(".*");
            }
            regex.append(Pattern.quote(literal));
        }
        return Pattern.matches(regex.toString(), id);
    }
}
