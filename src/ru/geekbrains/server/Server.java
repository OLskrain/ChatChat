package ru.geekbrains.server;

import ru.geekbrains.common.ServerConst;
import ru.geekbrains.common.Server_API;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server implements ServerConst, Server_API{
    private Vector<ClientHandler> clients;//тип списка "вектор"(устаревший),который со но вообще не стоит его использовать
    // потому что все его методы сынхонайз, что замедляет приложение. Когда один поток делает что то с членом "вектора".
    // ни один другой поток работаеть не будет в это время. Но зато будет полная потокобезопасность.

    private AuthService authService;
    public AuthService getAuthService(){ //конструктор для автаризации
        return authService;
    }
    public Server(){       //конструктор сервера
        ServerSocket serverSocket = null; //сам сервер это серверный сокет.который ждет соединений.принимает на себя соодинение клиенские.
        Socket socket = null;
        clients = new Vector<>(); //вектор нужно заменять на коллекцию из java.util.concurrent или Collections.synchronized. массив клиентов
        try{
            serverSocket = new ServerSocket(PORT); //стартуем серверный сокет на определенный порт
            authService = new BaseAuthService();   //placeholder(заглушка для сервиса автаризации)
            authService.start();                  //placeholder(заглушка для сервиса автаризации)
            System.out.println("Сервер запущен, ждем клиентов");
            while(true){
                socket = serverSocket.accept();       //ждем подключений клиентов по сокету, сервер становится на паузу
                clients.add(new ClientHandler(this, socket)); //добавляем клиента, и открываем канал связи
                System.out.println("Клиент подключился");
            }
        }catch(IOException e){
            System.out.println("Ошибка инициализации");
        }finally{
            try{
                serverSocket.close();
                authService.stop();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void broadcast(String message){ //отправляем всем клиентам сообщение
        for(ClientHandler client : clients){ //проходим по массиву клиентов
            client.sendMessage(message);
        }
    }
    public void broadcastUsersList(){ //метод для
        StringBuffer sb = new StringBuffer(USERS_LIST); //StringBuffer, потому что он потокобезопасен
        for(ClientHandler client : clients){ //спрашиваем кто у нас сейчас в массиве
            sb.append(" " + client.getNick());
        }
        broadcast(sb.toString()); //рассылаем всем
    }
    public void sendPrivateMessage(ClientHandler from, String to, String msg){ //реализация приватного сообщения
        boolean nickFound = false; //чтобы не идти до конца массива
        for(ClientHandler client : clients){
            if(client.getNick().equals(to)){
                nickFound = true;
                client.sendMessage("from: " + from.getNick() + ": " + msg); //от кого сообщение
                from.sendMessage("to: " + to + " msg: " + msg); //копия себе
                break;
            }
        }
        if(!nickFound) from.sendMessage("User not found!");
    }
    public void unsubscribeMe(ClientHandler c){ //если клиент вышел из чата, то удаляем его из списка
        clients.remove(c);
        broadcastUsersList();
    }
    public boolean isNickBusy(String nick){ //метод для того, чтобы нельзя было залогиниться под одним и тем же ником
        for(ClientHandler client : clients){
            if(client.getNick().equals(nick)) return true; //ник занят
        }
        return false;
    }

}
