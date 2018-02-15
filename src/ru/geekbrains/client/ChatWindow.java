package ru.geekbrains.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatWindow extends JFrame{
    static final int WIDTH = 1024;
    static final int HEINGHT = 768;
    private JTextField message;
    private JTextArea chatHistory;
    private JTextArea usersList;
    private JScrollPane scrollPane1;
    private JScrollPane scrollPane2;
    private JScrollPane scrollPane3;
    private JPanel jPanel;
    private JPanel[] jpeast;
    private JTextField login;
    private JPasswordField password;
    private JPanel top; //панелька авторизации
    private ClientConnection clientConnection;


    public ChatWindow() {
        clientConnection = new ClientConnection();
        clientConnection.init(this);

        setTitle("BriZzChat");
        setSize(WIDTH, HEINGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        //верхняя менюшка
        JMenuBar mainMenu = new JMenuBar();
        JMenu mFile = new JMenu("File");
        JMenu mEdit = new JMenu("Edit");
        JMenuItem miFileNew = new JMenuItem("New");
        JMenuItem miFileExit = new JMenuItem("Exit");
        setJMenuBar(mainMenu);
        mainMenu.add(mFile);
        mainMenu.add(mEdit);
        mFile.add(miFileNew);
        mFile.addSeparator();
        mFile.add(miFileExit);

        miFileExit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Bye");
            }
        });

        jpeast = new JPanel[3];
        for (int i = 0; i < jpeast.length ; i++) {
            jpeast[i] = new JPanel();
            jpeast[i].setLayout(new FlowLayout());
        }

        jpeast[0].setPreferredSize(new Dimension(200,400));
        jpeast[1].setPreferredSize(new Dimension(400,400));
        jpeast[2].setPreferredSize(new Dimension(100,50));

        jpeast[0].setBackground(new Color(34+2*40,34+2*40,34+2*40));
        jpeast[1].setBackground(new Color(34+2*40,34+2*40,34+2*40));
        jpeast[2].setBackground(new Color(100+3*40,100+3*40,99+40));

        usersList = new JTextArea();
        usersList.setEnabled(false);
        message = new JTextField();
        chatHistory = new JTextArea();
        chatHistory.setLineWrap(true);
        chatHistory.setEditable(false);


        scrollPane1 = new JScrollPane(usersList);
        scrollPane1.setPreferredSize(new Dimension(190, 645));
        scrollPane2 = new JScrollPane(chatHistory);
        scrollPane2.setPreferredSize(new Dimension(800, 645));
        scrollPane3 = new JScrollPane(message);
        scrollPane3.setPreferredSize(new Dimension(900, 40));

        JButton send = new JButton("Send");

        //панель авторизаци
        jPanel = new JPanel();
        login = new JTextField();
        password = new JPasswordField();
        JButton auth = new JButton("Login");
        top = new JPanel(new GridLayout(1,3)); //расставляем по сетке
        top.setBackground(new Color(100+3*40,100+3*40,99+40));
        top.add(login);
        top.add(password);
        top.add(auth);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        message.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        auth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                auth();
            }
        });

        jpeast[0].add(scrollPane1);
        jpeast[1].add(scrollPane2);
        jpeast[2].add(scrollPane3);
        jpeast[2].add(send);

        add(jpeast[0], BorderLayout.EAST);
        add(jpeast[1], BorderLayout.CENTER);
        add(jpeast[2], BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        switchWindows(); //вызываем , чтобы правильно определить что показывать
        setVisible(true);
    }
    public void sendMessage(){
        String message = this.message.getText(); //здесь message локальная
        this.message.setText("");
        clientConnection.sendMessage(message);
    }
    public void auth(){
        clientConnection.auth(login.getText(), new String(password.getPassword()));
        login.setText("");
        password.setText("");
    }
    public void showMessage(String message){
        chatHistory.append(message + "\n");
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }
    public void switchWindows(){ //метод для видимости панелек
         // панель автаризац видна, если клиент не автаризован
        top.setVisible(!clientConnection.isAuthrozied());
        for (int i = 0; i < jpeast.length ; i++) {    //если клиент авторизован
            jpeast[i].setVisible(clientConnection.isAuthrozied());
        }
    }
    public void showUsersList(String[] users){ //метод для обновления юзерлист
        usersList.setText("");
        for(int i = 1; i < users.length; i++){
            usersList.append(users[i] + "\n");
        }
    }
}
