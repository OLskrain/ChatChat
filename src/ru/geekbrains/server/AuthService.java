package ru.geekbrains.server;

public interface AuthService { //реализация автаризации. должна работать с базой данных.отдельный процесс на сервере
    void start();
    void stop();
    String getNickByLoginPass(String login, String pass); //запрашиваем логин и пароль
}
