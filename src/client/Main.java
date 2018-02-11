package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static final int SERVER_PORT = 8189;
    public static final String SERVER_ADDRESS = "localhost";

    public static void main(String[] args){
        Socket socket;
        Scanner in;
        PrintWriter out;

        try{
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run(){
                    while(true){
                        if(in.hasNext()){
                            String string = in.nextLine();
                            System.out.println(string);
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner in = new Scanner(System.in);
                    while (true) {
                        String string = in.nextLine();
                        out.println(string);
                        out.flush();
                    }
                }
            }).start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
