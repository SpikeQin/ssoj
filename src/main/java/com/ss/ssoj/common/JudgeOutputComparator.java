package com.ss.ssoj.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 判题结果比对：忽略每行行末空白与文末换行后逐行比较。
 * 单独抽出便于单元测试，判题逻辑与比对规则互不影响。
 */
public final class JudgeOutputComparator {

    private JudgeOutputComparator() {
    }

    public static boolean matches(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    public static List<String> normalize(String text) {
        if (text == null) {
            return List.of();
        }
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        List<String> result = new ArrayList<>(lines.length);
        for (String line : lines) {
            result.add(line.replaceAll("[ \\t]+$", ""));
        }
        while (!result.isEmpty() && result.get(result.size() - 1).isEmpty()) {
            result.remove(result.size() - 1);
        }
        return result;
    }
}
