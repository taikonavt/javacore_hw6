package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatWindow extends JFrame{
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    private JTextArea messageField;

    public ChatWindow(){
        try{
            socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());
        }catch(IOException e){
            e.printStackTrace();
        }

        setTitle(Constants.chatTitle);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setBounds(300, 300, 600, 430);
        JTextPane dialogField = new JTextPane();
        dialogField.setEditorKit(new WrapEditorKit());
        dialogField.setEditable(false);
        JScrollPane dialogScrollPane = new JScrollPane();
        dialogScrollPane.getViewport().add(dialogField);
        messageField = new JTextArea();

        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == '\n')
                    sendMessage();
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == '\n')
                    messageField.setText("");
            }
        });

        messageField.setLineWrap(true);
        JScrollPane messageScrollPane = new JScrollPane(messageField);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                dialogScrollPane, messageScrollPane);
        splitPane.setDividerLocation(250);
        splitPane.setPreferredSize(new Dimension(590, 350));
        JPanel panelForSplitPane = new JPanel();
        panelForSplitPane.setPreferredSize(new Dimension(600, 350));
        panelForSplitPane.add(splitPane);
        add(panelForSplitPane);
        JButton sendMessageButton = new JButton(Constants.sendText);

        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendMessage();
            }
        });

        JPanel panelForButton = new JPanel();
        panelForButton.setLayout(new BoxLayout(panelForButton, BoxLayout.X_AXIS));
        panelForButton.add(Box.createHorizontalGlue());
        panelForButton.add(sendMessageButton);
        panelForButton.add(Box.createHorizontalStrut(40));
        panelForButton.setPreferredSize(new Dimension(600, 30));
        add(panelForButton);
        setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    if(in.hasNext()){
                        String string = in.nextLine();
                        String substring;
                        if (string.startsWith("Echo: ")){
                            appendToPane(dialogField, Constants.YOU + "\n", Color.BLUE);
                            substring = string.substring(6);
                        }
                        else {
                            appendToPane(dialogField, Constants.SERVER_TEXT + "\n", Color.RED);
                            substring = string.substring(8);
                            System.out.println(Constants.SERVER_TEXT + substring);
                        }
                        appendToPane(dialogField, substring + "\n", Color.BLACK);
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

        setVisible(true);
    }

    public void sendMessage(){
        String string = messageField.getText();
        System.out.println(string);
        out.println(string);
        out.flush();
        messageField.setText("");
    }

    private void appendToPane(JTextPane textPane, String message, Color color) {
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        AttributeSet set = styleContext.addAttribute(
                SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
        set = styleContext.addAttribute(set, StyleConstants.FontFamily, "Lucida Console");
        set = styleContext.addAttribute(set, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int len = textPane.getDocument().getLength();
        textPane.setCaretPosition(len);
        textPane.setCharacterAttributes(set, false);
        textPane.setEditable(true);
        textPane.replaceSelection(message);
        textPane.setEditable(false);
    }
}


class WrapEditorKit extends StyledEditorKit {
    ViewFactory defaultFactory = new WrapColumnFactory();

    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}


class WrapColumnFactory implements ViewFactory {

    @Override
    public View create(Element element) {
        String kind = element.getName();
        if (kind != null) {

            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new WrapLabelView(element);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(element);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(element, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(element);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(element);
            }
        }
        return new LabelView(element);
    }
}


class WrapLabelView extends LabelView {

    WrapLabelView(Element element) {
        super(element);
    }

    public float getMinimumSpan(int axis) {
        switch (axis) {
            case View.X_AXIS:
                return 0;
            case View.Y_AXIS:
                return super.getMinimumSpan(axis);
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
}
