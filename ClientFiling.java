import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClientFiling extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton, sendFileButton;

    private Socket Client_Socket;
    private PrintWriter Client_Writer;
    private BufferedReader Server_Reader;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    public ClientFiling() {
        setTitle("Client Chat");
        setSize(400, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendFileButton = new JButton("Send File");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(sendFileButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        setVisible(true);

        connectToServer();
        addListeners();
        startReadingThread();
    }

    private void connectToServer() {
        try {
            Client_Socket = new Socket("192.168.100.169", 8080);
            Client_Writer = new PrintWriter(Client_Socket.getOutputStream(), true);
            Server_Reader = new BufferedReader(new InputStreamReader(Client_Socket.getInputStream()));
            dataOut = new DataOutputStream(Client_Socket.getOutputStream());
            dataIn = new DataInputStream(Client_Socket.getInputStream());
            chatArea.append("Connected to server.\n");
        } catch (Exception e) {
            chatArea.append("Unable to connect to server.\n");
            e.printStackTrace();
        }
    }

    private void addListeners() {
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        sendFileButton.addActionListener(e -> chooseAndSendFile());
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("Me: " + message + "\n");
            Client_Writer.println(message);
            inputField.setText("");
        }
    }

    private void chooseAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            new Thread(() -> sendFile(file)).start();
        }
    }

    private void sendFile(File file) {
        try {
            String fileName = file.getName();
            long fileSize = file.length();

            Client_Writer.println("FILE:" + fileName + ":" + fileSize);

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) > 0) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
            fis.close();
            chatArea.append("File sent: " + fileName + "\n");
        } catch (IOException ex) {
            chatArea.append("File send failed.\n");
        }
    }

    private void startReadingThread() {
        Thread receiveThread = new Thread(() -> {
            try {
                String Server_MSG;
                while ((Server_MSG = Server_Reader.readLine()) != null) {
                    if (Server_MSG.startsWith("FILE:")) {
                        String[] parts = Server_MSG.split(":");
                        String fileName = parts[1];
                        long fileSize = Long.parseLong(parts[2]);
                        receiveFile(fileName, fileSize);
                    } else {
                        chatArea.append("Server: " + Server_MSG + "\n");
                    }
                }
            } catch (IOException e) {
                chatArea.append("Disconnected from server.\n");
            }
        });
        receiveThread.start();
    }

    private void receiveFile(String fileName, long fileSize) throws IOException {
        File dir = new File("ClientFiles");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long remaining = fileSize;

        while (remaining > 0 && (bytesRead = dataIn.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
        fos.close();
        chatArea.append("File received: " + fileName + "\n");
    }

    public static void main(String[] args) {
        new ClientFiling();
    }
}