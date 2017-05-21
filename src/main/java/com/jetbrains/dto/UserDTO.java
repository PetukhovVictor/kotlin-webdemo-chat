package com.jetbrains.dto;

/**
 * DTO-модель пользователя. Отдается на клиент.
 */
public class UserDTO {
    /**
     * ID пользователя.
     */
    private Integer id;

    /**
     * Имя пользователя.
     */
    private String name;

    /**
     * Аватар пользователя.
     */
    private String picture;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }
}
