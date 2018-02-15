package ru.geekbrains.server;

import ru.geekbrains.common.Server_API;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Server_API { //отвечает за подключенных клиентов
    private Server server; //передался от сервера
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    public ClientHandler(Server server, Socket socket){
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream()); //передаем инфу с сервер
            out = new DataOutputStream(socket.getOutputStream()); //на сервер
            this.nick = "undefined";
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(()-> { //поток с лямда выражением "()->". в отличие от ананимного класса контекст не меняется. ЦИКЛ АВТВРИЗАЦИИ
            try{
                //Auth
                while(true){                          //цикл для авторизации
                    String message = in.readUTF();    //принимаем входяшую инфу
                    if(message.startsWith(AUTH)){    //проверяем что сообщение начинается с AUTH
                        String[] elements = message.split(" "); //разделяем 1 строку с данными от пользователя на элементы по пробелу
                        String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]); //передаем логин и пароль на сервер на проверку.
                        if(nick != null){                              //если есть такой ник
                            if(!server.isNickBusy(nick)){  //если ник не занят
                                sendMessage(AUTH_SUCCESSFUl + " " + nick); //сообщение "авторизация успешна"
                                this.nick = nick;
                                server.broadcastUsersList(); //человек автаризовл. и говорим всем это
                                server.broadcast(this.nick + " has entered the chat room");
                                break;
                            }else sendMessage("This account is already in use!"); //если нк занят
                        }else sendMessage("Wrong login/password!");
                    }else sendMessage("You should authorize first!");
                }
                while(true){ //ЦИКЛ ОБМЕНА СООБЩЕНИЯМИ
                    String message = in.readUTF();
                    if(message.startsWith(SYSTEM_SYMBOL)){ //реализация системных символов(пока что только 2
                        if(message.equalsIgnoreCase(CLOSE_CONNECTION)) break; //выход из чата
                        else if(message.startsWith(PRIVATE_MESSAGE)){ // /w nick message.отправка приватного сообщения
                            String nameTo = message.split(" ")[1]; //выделяем из сообщения ник получателя
                            String messageText = message.substring(PRIVATE_MESSAGE.length() + nameTo.length() + 2);//substring возвращает строчку с заданного индекса
                            server.sendPrivateMessage(this, nameTo, messageText);//кто кому отправляет
                        }else sendMessage("Command doesn't exist!"); //кроме 2 команд остальные не работают
                    }else {
                        System.out.println("client " + message); //отправка обычных сообщений
                        server.broadcast(this.nick + " " + message);
                    }
                }
            }catch(IOException e){
            }finally{
                disconnect();
            }
        }).start();
    }
    public void sendMessage(String msg){ //метод для отправки сообшения от сервера к клиенту. самому себе отправка
        try{
            out.writeUTF(msg);//складываем сообщения в буфер
            out.flush();  //отчищаем буфер и отсылаем сообщение клиенту
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void disconnect(){ // метод. если клиент вышел. то не получает сообщение
        sendMessage(CLOSE_CONNECTION + " You have been disconnected!");
        server.unsubscribeMe(this);
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public String getNick(){
        return nick;
    }
}
