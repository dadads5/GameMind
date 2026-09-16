package com.zuel.springtest.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Character {
    private LocalDateTime createdAt;
    private Integer id;
    private String name;
    private String icon;
    private String subtitle;
    private String weapons;
    private String artifacts;
    private String stats;
    private String talents;
    private String element;
}