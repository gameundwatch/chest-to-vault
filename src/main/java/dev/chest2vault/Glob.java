package dev.chest2vault;

import org.apache.commons.io.FilenameUtils;

/**
 * ルートテーブルの ID の glob。`*` は `/` を含む任意の文字の並び（空も含む）、`?` は任意の1文字。ほかの文字はそのまま。ID 全体と照合する。
 * 照合は commons-io（Minecraft 自身が使っているライブラリ）に任せる。大文字と小文字は区別する。
 */
final class Glob {
    private Glob() {}

    static boolean matches(String glob, String id) {
        return FilenameUtils.wildcardMatch(id, glob);
    }
}
