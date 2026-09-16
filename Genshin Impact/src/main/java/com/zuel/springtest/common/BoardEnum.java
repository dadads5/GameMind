package com.zuel.springtest.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 论坛板块枚举
 *
 * <p>对应 {@code post.board_id} 字段与 {@code board} 字典表。
 */
@Getter
@AllArgsConstructor
public enum BoardEnum {

    MONDSTADT(1, "蒙德"),
    LIYUE(2, "璃月"),
    INAZUMA(3, "稻妻"),
    SUMERU(4, "须弥"),
    FONTAINE(5, "枫丹"),
    NATLAN(6, "纳塔"),
    NARUTO(7, "火影忍者"),
    ;

    private final Integer id;
    private final String name;

    /**
     * 板块 ID 是否合法
     */
    public static boolean exists(Integer id) {
        if (id == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(board -> board.getId().equals(id));
    }

    /**
     * 获取板块名称，不存在时返回 null
     */
    public static String getName(Integer id) {
        return Arrays.stream(values())
                .filter(board -> board.getId().equals(id))
                .map(BoardEnum::getName)
                .findFirst()
                .orElse(null);
    }
}
