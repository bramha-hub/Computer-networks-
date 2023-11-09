import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientChecksumSimulator {
    private static JTextArea messageTextArea;
    private static JTextArea binaryTextArea;
    private static JTextArea checksumTextArea;
    private static JTextArea ackTextArea; // New JTextArea for acknowledgments
    private static JButton sendButton;
    private static Socket socket;
    private static OutputStream out;
    private static InputStream in;
    private static String message;
    private static String binaryData;
    private static int sequenceNumber = 0;
    private static int preSequenceNumber = 0;
    private static String checksum;
    private static Timer retransmissionTimer;
    private static String AckReceived = null;
    private static String dataToSend;
    private static boolean ackReceived = false;

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientChecksumSimulator::createMainFrame);
    }

    public static void createMainFrame() {
        JFrame mainFrame = new JFrame("TCP Client");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));

        messageTextArea = new JTextArea();
        binaryTextArea = new JTextArea();
        checksumTextArea = new JTextArea();
        ackTextArea = new JTextArea(); // Initialize acknowledgment JTextArea
        sendButton = new JButton("Send");

        panel.add(new JLabel("Enter Message:"));
        panel.add(messageTextArea);
        panel.add(new JLabel("Binary Data:"));
        panel.add(binaryTextArea);
        panel.add(new JLabel("Checksum:"));
        panel.add(checksumTextArea);
        panel.add(new JLabel("Acknowledgment:"));
        panel.add(ackTextArea); //acknowledgment
        panel.add(sendButton);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                message = messageTextArea.getText();
                binaryData = convertToBinary(message);
                Random random = new Random();
//                System.out.println(binaryData.length());
                int randomValue = random.nextInt(4);
//                String checksum = calculateChecksum(binaryData);
                if(randomValue ==  3){
                    checksum = "01010010";
                }
                else{
                    checksum = calculateChecksum(binaryData);
                }
//                checksum = calculateChecksum(binaryData);

                binaryTextArea.setText(binaryData);
                checksumTextArea.setText(checksum);
                ackTextArea.setText("");
                System.out.println("Sent: Seq No: " + sequenceNumber + ", Message: " + message +
                        ", Binary Data: " + binaryData + ", Checksum: " + checksum);
                dataToSend = binaryData + " " + checksum;
                // Simulate sending data to the server
                sendDataToServer(dataToSend);
                handleAcknowledgment();

//                try {
//
//                    AckReceived=null;
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//                    String ack = "NO ACK "+(preSequenceNumber);
////                    System.out.println(ack);
//                    AckReceived = reader.readLine();
//                    System.out.println(AckReceived);
//                    if (AckReceived.equals(ack)) {
//                        resendDataToServer();
//                        System.out.println("Retransmission");
//                        displayAcknowledgment(AckReceived);
////                        ackReceived = false;
//                    }
//                    else{
//                        System.out.println(AckReceived);
//                        displayAcknowledgment(AckReceived);
//                        sendButton.setEnabled(true);
//                    }
//
//                } catch (IOException w) {
//                        w.printStackTrace();
//                    }

                }
            });
        mainFrame.add(panel);
        mainFrame.setVisible(true);

        try {
                socket = new Socket("localhost", 4000); // Server IP and port
                out = socket.getOutputStream();
                in = socket.getInputStream(); // Initialize the InputStream
//            PrintWriter writer = new PrintWriter(out, false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static String convertToBinary(String message) {
            StringBuilder binary = new StringBuilder();
 
            for (char character : message.toCharArray()) {
                String charBinary = Integer.toBinaryString(character);
                while (charBinary.length() < 8) {
                    charBinary = "0" + charBinary;
                }
                binary.append(charBinary).append(" "); // Add space for separating characters
            }

            return binary.toString();
        }
        private static void resendDataToServer() {
            binaryData = convertToBinary(message);
            checksum=calculateChecksum(binaryData);
            String data=binaryData+" "+checksum;
            PrintWriter writer = new PrintWriter(out, true);
//        PrintWriter writer2 = new PrintWriter(out, true);
            writer.println(data);
//            /'writer2.println(sequenceNumber);
            writer.println(preSequenceNumber);
//            sequenceNumber++;
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
        private static void sendDataToServer(String message) {
            PrintWriter writer = new PrintWriter(out, true);
//        PrintWriter writer2 = new PrintWriter(out, true);
            preSequenceNumber=sequenceNumber;
            writer.println(message);
//        writer2.println(sequenceNumber);
            writer.println(sequenceNumber);
            sequenceNumber++;
        }
        public static void handleAcknowledgment() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String ack = "NO ACK " + preSequenceNumber;
                AckReceived = reader.readLine();

                if (AckReceived.equals(ack)) {
                    resendDataToServer();
                    System.out.println("Retransmission");
                    displayAcknowledgment(AckReceived);
                    ackTextArea.setText("");
                    handleAcknowledgment();

                } else {
                    System.out.println(AckReceived);
                    displayAcknowledgment(AckReceived);
                    // You can break the recursion here if needed.
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to display acknowledgment message in the GUI
        public static void displayAcknowledgment(String ackMessage) {
            ackTextArea.append(ackMessage + "\n");

        }
    }
    //
//    Socket clientSocket = new Socket("localhost", 4000);
//
//
//    InputStream input = clientSocket.getInputStream();
//    OutputStream output = clientSocket.getOutputStream();
//
//
//    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//    PrintWriter writer = new PrintWriter(output, true);
//
//
//            writer.println("Hello, server! This is the client.");
//                    writer.println("Hello, server! This is the Bramha.");
//                    writer.println("Hello, server! This is the friends .");
