import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class ServerChecksumSimulator {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static Timer acknowledgmentTimer;
    private static JTextArea receivedDataTextArea;
    private static JTextArea ackStatusTextArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createMainFrame());

        try {
            serverSocket = new ServerSocket(4000); // Server port

            while (true) {
                System.out.println("Waiting for a client to connect...");
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                while (true) {
                    String dataReceived = reader.readLine();
                    String SeqReceived = reader.readLine();

                    if (dataReceived == null || SeqReceived == null) {
                        // The client has closed the connection, you can break the loop or handle it accordingly
                        break;
                    }

                    int lastSpaceIndex = dataReceived.lastIndexOf(" ");

                    String binaryData = dataReceived.substring(0, lastSpaceIndex);
                    String receivedChecksum = dataReceived.substring(lastSpaceIndex + 1);

                    boolean isValid = validateChecksum(binaryData, receivedChecksum);

                    if (isValid) {
                        System.out.println("Received data: " + binaryData + " Sequence no: " + SeqReceived + " Successfully");
                        acknowledgeToClient(clientSocket, SeqReceived);
                        updateReceivedDataGUI("Received data: " + binaryData);
                        updateReceivedDataGUI("Sequence no: " + SeqReceived);
                        updateReceivedDataGUI("Received data successfully");
                        updateAckStatusGUI("ACK " + SeqReceived +" sent");
                    } else {
                        System.out.println("Received data " + binaryData + " with sequence no " + SeqReceived + " is Corrupted.");
                        updateReceivedDataGUI("Received data: " + binaryData + " Corrupted");

//                        updateAckStatusGUI("Sequence no: " + SeqReceived);
                        updateAckStatusGUI("ACK " + SeqReceived +" not sent");
//                        acknowledgeToClient(clientSocket, SeqReceived);
                        noAckToClient(clientSocket, SeqReceived);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean validateChecksum(String binaryData, String receivedChecksum) {
        String calculatedChecksum = calculateChecksum(binaryData);
        return receivedChecksum.equals(calculatedChecksum);
    }

    private static String calculateChecksum(String binaryData) {
        String[] data = binaryData.split(" "); // Split binary data by spaces

        int checksum = 0;
        for (String binary : data) {
            checksum ^= Integer.parseInt(binary, 2); // XOR operation
        }

        String checksumBinary = Integer.toBinaryString(checksum);
        while (checksumBinary.length() < 8) {
            checksumBinary = "0" + checksumBinary;
        }
        return checksumBinary;
    }

    private static void acknowledgeToClient(Socket clientSocket, String Seq) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("ACK " + Seq);
    }
    private static void noAckToClient(Socket clientSocket, String Seq) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("NO ACK " + Seq);
    }

    // Method to create the server-side GUI
    public static void createMainFrame() {
        JFrame mainFrame = new JFrame("TCP Server");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        receivedDataTextArea = new JTextArea();
        receivedDataTextArea.setEditable(false);
        ackStatusTextArea = new JTextArea();
        ackStatusTextArea.setEditable(false);

        JScrollPane receivedDataScrollPane = new JScrollPane(receivedDataTextArea);
        JScrollPane ackStatusScrollPane = new JScrollPane(ackStatusTextArea);

        panel.add(new JLabel("Server Status:"));
        panel.add(receivedDataScrollPane);
        panel.add(new JLabel("ACK Status:"));
        panel.add(ackStatusScrollPane);

        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }

    private static void updateReceivedDataGUI(String data) {
        receivedDataTextArea.append(data + "\n");
    }

    private static void updateAckStatusGUI(String ackStatus) {
        ackStatusTextArea.append(ackStatus + "\n");
    }
}
