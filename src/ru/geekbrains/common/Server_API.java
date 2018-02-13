package ru.geekbrains.common;

public interface Server_API {
    String SYSTEM_SYMBOL = "/";//константы для реализации командт в чате
    String CLOSE_CONNECTION = "/end";
    String AUTH = "/auth";
    String AUTH_SUCCESSFUl = "/authok";
    String PRIVATE_MESSAGE = "/w";
    String USERS_LIST = "/userslist";
}
