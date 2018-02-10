package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static final int SERVER_PORT = 8189;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Сервер запущен, ждем клиентов");
            socket = serverSocket.accept();
            System.out.println("Клиент подключился");
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String recieve = scanner.nextLine();
                        System.out.println(recieve);
                        String string = "Echo: " + recieve;
                        printWriter.println(string);
                        printWriter.flush();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner in = new Scanner(System.in);
                    while (true) {
                        String string = in.nextLine();
                        printWriter.println("Server: " + string);
                        printWriter.flush();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Ошибка инициализации");
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
