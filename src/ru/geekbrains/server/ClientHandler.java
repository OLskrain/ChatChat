package ru.geekbrains.server;

import ru.geekbrains.common.Server_API;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Server_API { //отвечает за подключенных клиентов
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    public ClientHandler(Server server, Socket socket){
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.nick = "undefined";
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(()-> { //поток с лямда выражением "()->". в отличие от ананимного класса контекст не меняется
            try{
                //Auth
                while(true){
                    String message = in.readUTF(); //цикл для авторизации
                    if(message.startsWith(AUTH)){
                        String[] elements = message.split(" "); //разделяем 1 строку с данными от пользователя на элементы по пробелу
                        String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]); //передаем логин и пароль
                        if(nick != null){                              //если есть такой ник
                            if(!server.isNickBusy(nick)){
                                sendMessage(AUTH_SUCCESSFUl + " " + nick); //сообщение "авторизация успешна"
                                this.nick = nick;
                                server.broadcastUsersList();
                                server.broadcast(this.nick + " has entered the chat room");
                                break;
                            }else sendMessage("This account is already in use!");
                        }else sendMessage("Wrong login/password!");
                    }else sendMessage("You should authorize first!");
                }
                while(true){
                    String message = in.readUTF();
                    if(message.startsWith(SYSTEM_SYMBOL)){ //реализация системных символов
                        if(message.equalsIgnoreCase(CLOSE_CONNECTION)) break;
                        else if(message.startsWith(PRIVATE_MESSAGE)){ // /w nick message
                            String nameTo = message.split(" ")[1];
                            String messageText = message.substring(PRIVATE_MESSAGE.length() + nameTo.length() + 2);
                            server.sendPrivateMessage(this, nameTo, messageText);
                        }else sendMessage("Command doesn't exist!");
                    }else {
                        System.out.println("client " + message);
                        server.broadcast(this.nick + " " + message);
                    }
                }
            }catch(IOException e){
            }finally{
                disconnect();
            }
        }).start();
    }
    public void sendMessage(String msg){ //метод для отправки сообшения от сервера к клиенту
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
