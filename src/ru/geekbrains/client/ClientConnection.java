package ru.geekbrains.client;

import ru.geekbrains.common.ServerConst;
import ru.geekbrains.common.Server_API;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnection implements ServerConst, Server_API { //отвечает за логику подключения
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private boolean isAuthrozied = false;
    public boolean isAuthrozied(){
        return isAuthrozied;
    }
    public void setAuthrozied(boolean authrozied){
        isAuthrozied = authrozied;
    }
    public ClientConnection(){
    }
    public void init(ChatWindow view){ //lazy init. передается графический клиент
        try{
            this.socket = new Socket(SERVER_URL, PORT); //сокет, который открывается на определенный сервер
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            new Thread(()-> {
                try{
                    while(true){ //зеркальный цикл авторизаци
                        String message = in.readUTF();
                        if(message.startsWith(AUTH_SUCCESSFUl)){ //автаризация завершится, если от сервера придет данное сообщение
                            setAuthrozied(true);
                            view.switchWindows(); //вызываем метод выдимости панелек
                            break;
                        }
                        view.showMessage(message);
                    }
                    while(true){ //цикл обмена соощений
                        String message = in.readUTF();
                        String[] elements = message.split(" ");
                        if(message.startsWith(SYSTEM_SYMBOL)){
                            if(elements[0].equals(CLOSE_CONNECTION)){ //если ввели систем. сообщение на закрытие то
                                setAuthrozied(false); //закрываем соединение
                                view.showMessage(message.substring(CLOSE_CONNECTION.length() + 1));
                                view.switchWindows();//отправляем клиента на окно автаризации
                            }else if(message.startsWith(USERS_LIST)){ //если сообщение начинается с юзерлист
                                String[] users = message.split(" "); //разделяем
                                Arrays.sort(users);
                                System.out.println(Arrays.toString(users));
                                view.showUsersList(users); //вызываем мотод из окна чата для добавление клиентов
                            }

                        }else{
                            view.showMessage(message);
                        }
                    }
                }catch(IOException e){
                }finally{
                    disconnect();
                }
            }).start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void sendMessage(String message){
        try{
            out.writeUTF(message);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void auth(String login, String password){
        try{
            out.writeUTF(AUTH + " " + login + " " + password); //введенные логин и пароль отправляем на сервер для автаризации
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void disconnect(){
        try{
            out.writeUTF(CLOSE_CONNECTION);
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
