package com.ss.ssoj.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JudgeOutputComparatorTest {

    @Test
    @DisplayName("完全一致时判定为通过")
    void matchesIdenticalOutput() {
        assertTrue(JudgeOutputComparator.matches("3\n", "3\n"));
        assertTrue(JudgeOutputComparator.matches("1 2 3", "1 2 3"));
    }

    @Test
    @DisplayName("忽略行末空白与文末换行")
    void ignoresTrailingWhitespaceAndNewline() {
        assertTrue(JudgeOutputComparator.matches("15", "15\n"));
        assertTrue(JudgeOutputComparator.matches("15\n", "15   \n\n"));
        assertTrue(JudgeOutputComparator.matches("a b\nc\n", "a b  \r\nc\r\n"));
    }

    @Test
    @DisplayName("内容不一致时判定为未通过")
    void detectsDifferentOutput() {
        assertFalse(JudgeOutputComparator.matches("15", "16"));
        assertFalse(JudgeOutputComparator.matches("1 2", "12"));
        assertFalse(JudgeOutputComparator.matches("a\nb", "a\nb\nc"));
        assertFalse(JudgeOutputComparator.matches("a\nb", "a b"));
    }

    @Test
    @DisplayName("空输出与 null 都按空处理")
    void handlesEmptyOutput() {
        assertTrue(JudgeOutputComparator.matches(null, ""));
        assertTrue(JudgeOutputComparator.matches("", "\n\n"));
        assertFalse(JudgeOutputComparator.matches("0", ""));
        assertEquals(0, JudgeOutputComparator.normalize(null).size());
        assertEquals(0, JudgeOutputComparator.normalize("  \n \n").size());
    }

    @Test
    @DisplayName("行内空格与空行位置敏感")
    void keepsInnerSpaces() {
        assertFalse(JudgeOutputComparator.matches("1 2", "1  2"));
        assertFalse(JudgeOutputComparator.matches("a\n\nb", "a\nb"));
    }
}
