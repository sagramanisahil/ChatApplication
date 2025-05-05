import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ServerFiling extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton, sendFileButton;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    public ServerFiling() {
        setTitle("Server Chat");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupGUI();
        setVisible(true);
        startServer();
    }

    private void setupGUI() {
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

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        sendFileButton.addActionListener(e -> chooseAndSendFile());
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8080);
                appendMessage("Server started. Waiting for client...");
                socket = serverSocket.accept();
                appendMessage("Client connected.");

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("FILE:")) {
                        String[] parts = message.split(":");
                        String fileName = parts[1];
                        long fileSize = Long.parseLong(parts[2]);
                        receiveFile(fileName, fileSize);
                    } else {
                        appendMessage("Client: " + message);
                        if (message.equalsIgnoreCase("zzz")) break;
                    }
                }

                closeConnections();

            } catch (IOException e) {
                appendMessage("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            appendMessage("Server: " + message);
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

            writer.println("FILE:" + fileName + ":" + fileSize);

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) > 0) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
            fis.close();
            appendMessage("File sent: " + file.getName());
        } catch (IOException ex) {
            appendMessage("File send failed.");
        }
    }

    private void receiveFile(String fileName, long fileSize) throws IOException {
        File dir = new File("ServerFiles");
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
        appendMessage("File received: " + fileName);
    }

    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
    }

    private void closeConnections() throws IOException {
        if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (socket != null) socket.close();
        if (serverSocket != null) serverSocket.close();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerFiling());
    }
}
