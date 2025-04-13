import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    private Socket Client_Socket;
    private PrintWriter Client_Writer;
    private BufferedReader Server_Reader;

    public ClientGUI() {
        setTitle("Client Chat");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        connectToServer();
        addListeners();
        startReadingThread();
    }

    private void connectToServer() {
        try {
            Client_Socket = new Socket("192.168.100.169", 8080); // Replace with your server IP
            Client_Writer = new PrintWriter(Client_Socket.getOutputStream(), true);
            Server_Reader = new BufferedReader(new InputStreamReader(Client_Socket.getInputStream()));
            chatArea.append("Connected to server.\n");
        } catch (Exception e) {
            chatArea.append("Unable to connect to server.\n");
            e.printStackTrace();
        }
    }

    private void addListeners() {
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("Me: " + message + "\n");
            Client_Writer.println(message);
            inputField.setText("");
        }
    }

    private void startReadingThread() {
        Thread receiveThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String Server_MSG;
                    while ((Server_MSG = Server_Reader.readLine()) != null) {
                        chatArea.append("Server: " + Server_MSG + "\n");
                        if (Server_MSG.equals("zzz")) {
                            break;
                        }
                    }
                    Client_Socket.close();
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }
        });
        receiveThread.start();
    }

    public static void main(String[] args) {
        new ClientGUI();
    }
}
