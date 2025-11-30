package client;

import common.ChatRoom;
import common.Message;
import common.User;
import common.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ClientUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // Color scheme - Modern Theme
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    private static final Color PANEL_COLOR = new Color(40, 42, 54);
    private static final Color ACCENT_COLOR = new Color(139, 233, 253);
    private static final Color SUCCESS_COLOR = new Color(80, 250, 123);
    private static final Color ERROR_COLOR = new Color(255, 85, 85);
    private static final Color TEXT_COLOR = new Color(248, 248, 242);
    private static final Color BORDER_COLOR = new Color(68, 71, 90);

    private ChatClient client;

    // Layout
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Components
    private JPanel loginPanel;
    private JTextField serverIPField, serverPortField, usernameField;
    private JButton connectButton;

    private JPanel chatPanel;
    private JTabbedPane chatTabs;

    // Broadcast
    private EmojiTextPane broadcastArea;
    private JTextField broadcastInput;
    private JButton broadcastEmojiButton, broadcastFileButton, broadcastSendButton;

    // Private
    private EmojiTextPane privateArea;
    private JTextField privateInput;
    private JButton privateEmojiButton, privateFileButton, privateSendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JTextField userSearchField;
    private List<User> allUsers = new ArrayList<>();

    // Room
    private EmojiTextPane roomArea;
    private JTextField roomInput;
    private JButton roomEmojiButton, roomFileButton, roomSendButton;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JButton joinRoomButton, leaveRoomButton, createRoomButton;
    private String currentRoom = null;

    // Status
    private JLabel statusLabel;
    private JLabel usernameLabel;

    public ClientUI() {
        client = new ChatClient();
        client.setUI(this);
        initComponents();
    }

    private void initComponents() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 750); // Tăng kích thước cửa sổ một chút
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        loginPanel = createLoginPanel();
        mainContainer.add(loginPanel, "LOGIN");

        chatPanel = createChatPanel();
        mainContainer.add(chatPanel, "CHAT");

        add(mainContainer);
        showLoginPanel();
    }

    // --- LOGIN PANEL ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ACCENT_COLOR, 2, true),
                new EmptyBorder(40, 50, 40, 50)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("CHAT APP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);

        // IP
        gbc.gridwidth = 1; gbc.gridy = 1;
        JLabel ipLabel = new JLabel("IP Address:"); 
        ipLabel.setForeground(TEXT_COLOR); ipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(ipLabel, gbc);
        
        gbc.gridx = 1;
        serverIPField = new JTextField("localhost", 20);
        styleTextField(serverIPField);
        formPanel.add(serverIPField, gbc);

        // Port
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel portLabel = new JLabel("Port:"); 
        portLabel.setForeground(TEXT_COLOR); portLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        serverPortField = new JTextField("12345", 20);
        styleTextField(serverPortField);
        formPanel.add(serverPortField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel userLabel = new JLabel("Username:"); 
        userLabel.setForeground(TEXT_COLOR); userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel passLabel = new JLabel("Password:"); 
        passLabel.setForeground(TEXT_COLOR); passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setBackground(BACKGROUND_COLOR);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(ACCENT_COLOR);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 12, 8, 12)));
        formPanel.add(passwordField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setBackground(PANEL_COLOR);

        connectButton = createStyledButton("Login", SUCCESS_COLOR);
        connectButton.addActionListener(e -> {
            String pass = new String(passwordField.getPassword());
            handleConnect(pass);
        });

        JButton registerButton = createStyledButton("Register", new Color(52, 152, 219));
        registerButton.addActionListener(e -> {
            String ip = serverIPField.getText().trim();
            try {
                int port = Integer.parseInt(serverPortField.getText().trim());
                String user = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword());
                if(user.isEmpty() || pass.isEmpty()) showError("Please enter username and password!");
                else client.register(ip, port, user, pass);
            } catch (Exception ex) { showError("Invalid Port!"); }
        });

        btnPanel.add(connectButton);
        btnPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel);
        return panel;
    }

    private void handleConnect(String password) {
        String serverIP = serverIPField.getText().trim();
        String portStr = serverPortField.getText().trim();
        String username = usernameField.getText().trim();

        if (serverIP.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            showError("Please fill in all fields!");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            if (client.connect(serverIP, port, username, password)) {
                showChatPanel();
                setTitle("Chat Client - " + username);
                usernameLabel.setText(username);
                statusLabel.setText("CONNECTED");
            }
        } catch (NumberFormatException e) {
            showError("Invalid port number!");
        }
    }

    // --- CHAT PANEL ---
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(PANEL_COLOR);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusPanel.setBackground(PANEL_COLOR);
        statusLabel = new JLabel("CONNECTED");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(SUCCESS_COLOR);
        
        usernameLabel = new JLabel("User");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(ACCENT_COLOR);
        
        statusPanel.add(statusLabel);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(usernameLabel);
        
        topPanel.add(statusPanel, BorderLayout.WEST);

        JButton disconnectButton = createStyledButton("Disconnect", ERROR_COLOR);
        disconnectButton.setPreferredSize(new Dimension(120, 35));
        disconnectButton.addActionListener(e -> handleDisconnect());
        topPanel.add(disconnectButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Tabs
        chatTabs = new JTabbedPane();
        chatTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Loại bỏ phần setUI phức tạp để tránh lỗi hiển thị
        chatTabs.setBackground(Color.WHITE); 
        
        chatTabs.addTab("Broadcast", createBroadcastTab());
        chatTabs.addTab("Private Chat", createPrivateTab());
        chatTabs.addTab("Rooms", createRoomTab());

        panel.add(chatTabs, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB CREATION HELPERS ---
    
    // 1. Broadcast Tab
    private JPanel createBroadcastTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        broadcastArea = createStyledEmojiPane();
        panel.add(createStyledScrollPane(broadcastArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);

        broadcastInput = new JTextField();
        styleTextField(broadcastInput);
        broadcastInput.addActionListener(e -> sendBroadcastMessage());

        JPanel btnPanel = createButtonPanel();
        
        // Setup Buttons for Broadcast
        broadcastEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        broadcastEmojiButton.setPreferredSize(new Dimension(50, 40));
        broadcastEmojiButton.addActionListener(e -> showEmojiPicker(broadcastInput));
        
        broadcastFileButton = createStyledButton("File", new Color(155, 89, 182));
        broadcastFileButton.setPreferredSize(new Dimension(80, 40)); // Tăng độ rộng
        broadcastFileButton.addActionListener(e -> sendFile("broadcast"));
        
        JButton screenBtn = createStyledButton("Screen", new Color(46, 204, 113));
        screenBtn.setPreferredSize(new Dimension(100, 40)); // Tăng độ rộng
        screenBtn.addActionListener(e -> sendScreenshot("broadcast"));
        
        broadcastSendButton = createStyledButton("Send", new Color(52, 152, 219));
        broadcastSendButton.setPreferredSize(new Dimension(80, 40)); // Tăng độ rộng
        broadcastSendButton.addActionListener(e -> sendBroadcastMessage());

        btnPanel.add(broadcastEmojiButton);
        btnPanel.add(broadcastFileButton);
        btnPanel.add(screenBtn);
        btnPanel.add(broadcastSendButton);

        inputPanel.add(broadcastInput, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    // 2. Private Chat Tab
    private JPanel createPrivateTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // LEFT: List User + Search
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(PANEL_COLOR);
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(PANEL_COLOR);
        JLabel lbl = new JLabel(" ONLINE USERS ");
        lbl.setForeground(ACCENT_COLOR);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchPanel.add(lbl, BorderLayout.NORTH);
        
        userSearchField = new JTextField();
        styleTextField(userSearchField);
        userSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterUserList(); }
            public void removeUpdate(DocumentEvent e) { filterUserList(); }
            public void changedUpdate(DocumentEvent e) { filterUserList(); }
        });
        searchPanel.add(userSearchField, BorderLayout.CENTER);
        
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = createStyledList(userListModel);
        leftPanel.add(createStyledScrollPane(userList), BorderLayout.CENTER);
        
        // Event chọn user
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    String username = selected.split(" \\(")[0];
                    privateArea.clearText();
                    client.requestChatHistory(username);
                }
            }
        });

        panel.add(leftPanel, BorderLayout.WEST);

        // RIGHT: Chat Area
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(BACKGROUND_COLOR);

        privateArea = createStyledEmojiPane();
        rightPanel.add(createStyledScrollPane(privateArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);

        privateInput = new JTextField();
        styleTextField(privateInput);
        privateInput.addActionListener(e -> sendPrivateMessage());

        JPanel btnPanel = createButtonPanel();
        
        privateEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        privateEmojiButton.setPreferredSize(new Dimension(50, 40));
        privateEmojiButton.addActionListener(e -> showEmojiPicker(privateInput));
        
        privateFileButton = createStyledButton("File", new Color(155, 89, 182));
        privateFileButton.setPreferredSize(new Dimension(80, 40));
        privateFileButton.addActionListener(e -> sendFile("private"));
        
        JButton screenBtn = createStyledButton("Screen", new Color(46, 204, 113));
        screenBtn.setPreferredSize(new Dimension(100, 40));
        screenBtn.addActionListener(e -> sendScreenshot("private"));
        
        JButton callBtn = createStyledButton("Call", new Color(231, 76, 60));
        callBtn.setPreferredSize(new Dimension(80, 40));
        callBtn.addActionListener(e -> startVideoCall());
        
        privateSendButton = createStyledButton("Send", SUCCESS_COLOR);
        privateSendButton.setPreferredSize(new Dimension(80, 40));
        privateSendButton.addActionListener(e -> sendPrivateMessage());

        btnPanel.add(privateEmojiButton);
        btnPanel.add(privateFileButton);
        btnPanel.add(screenBtn);
        btnPanel.add(callBtn);
        btnPanel.add(privateSendButton);

        inputPanel.add(privateInput, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }

    // 3. Room Tab
    private JPanel createRoomTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(PANEL_COLOR);
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel lbl = new JLabel(" CHAT ROOMS ");
        lbl.setForeground(ACCENT_COLOR); lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftPanel.add(lbl, BorderLayout.NORTH);

        roomListModel = new DefaultListModel<>();
        roomList = createStyledList(roomListModel);
        leftPanel.add(createStyledScrollPane(roomList), BorderLayout.CENTER);

        JPanel roomBtns = new JPanel(new GridLayout(3, 1, 0, 5));
        roomBtns.setBackground(PANEL_COLOR);
        
        joinRoomButton = createStyledButton("Join Room", SUCCESS_COLOR);
        joinRoomButton.addActionListener(e -> handleJoinRoom());
        
        leaveRoomButton = createStyledButton("Leave Room", new Color(230, 126, 34));
        leaveRoomButton.setEnabled(false);
        leaveRoomButton.addActionListener(e -> handleLeaveRoom());
        
        createRoomButton = createStyledButton("Create Room", new Color(52, 152, 219));
        createRoomButton.addActionListener(e -> handleCreateRoom());
        
        roomBtns.add(joinRoomButton); roomBtns.add(leaveRoomButton); roomBtns.add(createRoomButton);
        leftPanel.add(roomBtns, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right side
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(BACKGROUND_COLOR);

        roomArea = createStyledEmojiPane();
        rightPanel.add(createStyledScrollPane(roomArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);

        roomInput = new JTextField();
        styleTextField(roomInput);
        roomInput.setEnabled(false);
        roomInput.addActionListener(e -> sendRoomMessage());

        JPanel btnPanel = createButtonPanel();
        
        roomEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        roomEmojiButton.setPreferredSize(new Dimension(50, 40));
        roomEmojiButton.setEnabled(false);
        roomEmojiButton.addActionListener(e -> showEmojiPicker(roomInput));
        
        roomFileButton = createStyledButton("File", new Color(155, 89, 182));
        roomFileButton.setPreferredSize(new Dimension(80, 40));
        roomFileButton.setEnabled(false);
        roomFileButton.addActionListener(e -> sendFile("room"));
        
        JButton screenBtn = createStyledButton("Screen", new Color(46, 204, 113));
        screenBtn.setPreferredSize(new Dimension(100, 40));
        screenBtn.setEnabled(false);
        screenBtn.addActionListener(e -> sendScreenshot("room"));
        
        roomSendButton = createStyledButton("Send", SUCCESS_COLOR);
        roomSendButton.setPreferredSize(new Dimension(80, 40));
        roomSendButton.setEnabled(false);
        roomSendButton.addActionListener(e -> sendRoomMessage());

        btnPanel.add(roomEmojiButton);
        btnPanel.add(roomFileButton);
        btnPanel.add(screenBtn);
        btnPanel.add(roomSendButton);

        inputPanel.add(roomInput, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- ACTIONS ---
    private void handleDisconnect() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Disconnect", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            client.disconnect();
            showLoginPanel();
            setTitle("Chat App");
            statusLabel.setText("DISCONNECTED");
            broadcastArea.clearText();
            privateArea.clearText();
            roomArea.clearText();
            currentRoom = null;
            roomInput.setEnabled(false);
            roomSendButton.setEnabled(false);
            leaveRoomButton.setEnabled(false);
        }
    }

    private void sendBroadcastMessage() {
        String msg = broadcastInput.getText().trim();
        if (!msg.isEmpty()) { client.sendBroadcastMessage(msg); broadcastInput.setText(""); }
    }

    private void sendPrivateMessage() {
        String msg = privateInput.getText().trim();
        String selected = userList.getSelectedValue();
        if (msg.isEmpty()) return;
        if (selected == null) { showError("Select a user!"); return; }
        String receiver = selected.split(" \\(")[0];
        if (receiver.equals(client.getUsername())) { showError("Cannot msg yourself!"); return; }
        client.sendPrivateMessage(receiver, msg);
        privateInput.setText("");
    }

    private void sendRoomMessage() {
        String msg = roomInput.getText().trim();
        if (!msg.isEmpty() && currentRoom != null) { client.sendRoomMessage(currentRoom, msg); roomInput.setText(""); }
    }

    private void handleJoinRoom() {
        String selected = roomList.getSelectedValue();
        if (selected == null) { showError("Select a room!"); return; }
        String name = selected.split(" \\(")[0];
        if (!name.startsWith("#")) name = "#" + name;
        client.joinRoom(name);
        currentRoom = name;
        enableRoomControls(true);
        roomArea.appendText("=== Joined " + name + " ===");
    }

    private void handleLeaveRoom() {
        if (currentRoom != null) {
            client.leaveRoom(currentRoom);
            roomArea.appendText("=== Left " + currentRoom + " ===");
            currentRoom = null;
            enableRoomControls(false);
        }
    }
    
    private void enableRoomControls(boolean enable) {
        roomInput.setEnabled(enable);
        roomSendButton.setEnabled(enable);
        roomEmojiButton.setEnabled(enable);
        roomFileButton.setEnabled(enable);
        leaveRoomButton.setEnabled(enable);
    }

    private void handleCreateRoom() {
        String name = JOptionPane.showInputDialog(this, "Room Name:");
        if (name != null && !name.trim().isEmpty()) {
            client.createRoom(name.trim().replace("#", ""));
        }
    }

    private void showLoginPanel() { cardLayout.show(mainContainer, "LOGIN"); }
    private void showChatPanel() { cardLayout.show(mainContainer, "CHAT"); }

    // --- UPDATE UI ---
    public void updateUserList(List<User> users) {
        this.allUsers = users;
        filterUserList();
    }
    
    private void filterUserList() {
        SwingUtilities.invokeLater(() -> {
            if (allUsers == null) return;
            String q = userSearchField.getText().toLowerCase();
            userListModel.clear();
            for (User u : allUsers) {
                if (!u.getUsername().equals(client.getUsername())) {
                    if (u.getUsername().toLowerCase().contains(q)) {
                        userListModel.addElement(u.getUsername() + " (" + u.getIpAddress() + ")");
                    }
                }
            }
        });
    }

    public void updateRoomList(List<ChatRoom> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomListModel.clear();
            for (ChatRoom r : rooms) roomListModel.addElement(r.getDisplayName() + " (" + r.getMemberCount() + ")");
        });
    }

    public void displayBroadcastMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            String prefix = msg.getSender().equals(client.getUsername()) ? "You" : msg.getSender();
            broadcastArea.appendText(String.format("[%s] %s: %s", msg.getTimestamp(), prefix, msg.getContent()));
        });
    }

    public void displayPrivateMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            String sender = msg.getSender();
            String prefix = sender.equals(client.getUsername()) ? "You -> " + msg.getReceiver() : sender;
            privateArea.appendText(String.format("[%s] %s: %s", msg.getTimestamp(), prefix, msg.getContent()));
        });
    }

    public void displayRoomMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            roomArea.appendText(String.format("[%s] %s: %s", msg.getTimestamp(), msg.getSender(), msg.getContent()));
        });
    }

    public void displayFileInChat(String sender, String fileName, byte[] fileData, boolean isImage, String tabType) {
        SwingUtilities.invokeLater(() -> {
            EmojiTextPane targetArea;
            if (tabType.equals("broadcast")) targetArea = broadcastArea;
            else if (tabType.equals("room")) targetArea = roomArea;
            else targetArea = privateArea;
            
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String prefix = sender.equals(client.getUsername()) ? "You" : sender;
            String header = String.format("[%s] %s sent %s:", timestamp, prefix, isImage ? "a photo" : "a file");

            if (isImage && fileData != null) {
                try {
                     ImageIcon originalIcon = new ImageIcon(fileData);
                     // Resize ảnh nếu quá to
                     int maxWidth = 250;
                     if (originalIcon.getIconWidth() > maxWidth) {
                         Image scaled = originalIcon.getImage().getScaledInstance(maxWidth, -1, Image.SCALE_SMOOTH);
                         originalIcon = new ImageIcon(scaled);
                     }
                     targetArea.insertImage(originalIcon, header);
                } catch(Exception e) {}
            } else {
                // --- SỬA LỖI HIỂN THỊ NÚT FILE ---
                JButton fileButton = new JButton("📄 " + fileName + " (" + (fileData != null ? fileData.length/1024 : 0) + " KB)");
                
                // Style sửa lỗi bị trắng
                fileButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
                fileButton.setBackground(new Color(52, 152, 219)); // Màu xanh dương
                fileButton.setForeground(Color.WHITE);
                fileButton.setFocusPainted(false);
                fileButton.setBorderPainted(false); // Bỏ viền mặc định
                fileButton.setOpaque(true);         // Bắt buộc vẽ màu nền
                fileButton.setContentAreaFilled(true);
                fileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                fileButton.setMargin(new Insets(5, 10, 5, 10)); // Tạo khoảng cách nội dung
                
                // Nếu là file lịch sử (không có data thực), nút sẽ bị disable
                if (fileData == null) {
                    fileButton.setText("📄 " + fileName + " (File history)");
                    fileButton.setBackground(Color.GRAY);
                    fileButton.setEnabled(false);
                } else {
                    fileButton.addActionListener(e -> {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(fileName));
                        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                            try {
                                Files.write(fileChooser.getSelectedFile().toPath(), fileData);
                                JOptionPane.showMessageDialog(this, "Saved successfully!");
                            } catch (IOException ex) { showError("Error saving: " + ex.getMessage()); }
                        }
                    });
                }
                
                targetArea.insertComponent(fileButton, header);
            }
        });
    }

    // --- STYLING & HELPERS ---
    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(BACKGROUND_COLOR);
        tf.setForeground(TEXT_COLOR);
        tf.setCaretColor(ACCENT_COLOR);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 10, 8, 10)));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { if(b.isEnabled()) b.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { if(b.isEnabled()) b.setBackground(bg); }
        });
        return b;
    }

    private EmojiTextPane createStyledEmojiPane() {
        EmojiTextPane p = new EmojiTextPane();
        p.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.setBackground(PANEL_COLOR);
        p.setForeground(TEXT_COLOR);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JScrollPane createStyledScrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(PANEL_COLOR);
        return sp;
    }
    
    private <E> JList<E> createStyledList(DefaultListModel<E> m) {
        JList<E> l = new JList<>(m);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setBackground(PANEL_COLOR);
        l.setForeground(TEXT_COLOR);
        l.setSelectionBackground(ACCENT_COLOR);
        l.setSelectionForeground(BACKGROUND_COLOR);
        l.setFixedCellHeight(30);
        return l;
    }
    
    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        p.setBackground(BACKGROUND_COLOR);
        return p;
    }

    public void showError(String msg) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE)); }
    public void showInfo(String msg) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE)); }
    
    public void displayReaction(Message msg) { showInfo(msg.getSender() + " reacted " + msg.getReactionType()); }
    
    public void openVideoCallWindow(String user, String id, boolean video, boolean audio) {
        SwingUtilities.invokeLater(() -> {
            try {
                VideoCallWindow win = new VideoCallWindow(user, id, video, client);
                win.setVisible(true);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
    
 // 1. Logic Emoji
    private void showEmojiPicker(JTextField targetField) {
        JDialog emojiDialog = new JDialog(this, "Select Emoji", true);
        emojiDialog.setLayout(new BorderLayout());
        emojiDialog.setSize(600, 400);
        emojiDialog.setLocationRelativeTo(this);

        JPanel grid = new JPanel(new GridLayout(0, 10, 5, 5));
        grid.setBackground(PANEL_COLOR);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<String> emojis = IconManager.getAvailableEmojiIcons();
        if (emojis.isEmpty()) {
            grid.add(new JLabel("No icons found in resources/icons/"));
        } else {
            for (String iconName : emojis) {
                JButton btn = new JButton(IconManager.loadIcon(iconName, 32));
                btn.setPreferredSize(new Dimension(40, 40));
                btn.setBackground(PANEL_COLOR);
                btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
                btn.setFocusPainted(false);
                btn.addActionListener(e -> {
                    String code = IconManager.getEmojiCode(iconName);
                    targetField.setText(targetField.getText() + code);
                    emojiDialog.dispose();
                });
                grid.add(btn);
            }
        }
        
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        emojiDialog.add(scroll);
        emojiDialog.setVisible(true);
    }

    // 2. Logic Gửi File
    private void sendFile(String mode) {
        // Kiểm tra điều kiện trước
        String receiver = null;
        if (mode.equals("private")) {
            String selected = userList.getSelectedValue();
            if (selected == null) { showError("Select a user first!"); return; }
            receiver = selected.split(" \\(")[0];
        } else if (mode.equals("room")) {
            if (currentRoom == null) { showError("Join a room first!"); return; }
            receiver = currentRoom;
        }

        JFileChooser ch = new JFileChooser();
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            if (f.length() > 50 * 1024 * 1024) { // 50MB limit
                showError("File too large (>50MB)");
                return;
            }
            client.sendFile(f, receiver, mode);
        }
    }

    // 3. Logic Chụp màn hình (Screen)
    private void sendScreenshot(String mode) {
        String receiver = null;
        if (mode.equals("private")) {
            String selected = userList.getSelectedValue();
            if (selected == null) { showError("Select a user first!"); return; }
            receiver = selected.split(" \\(")[0];
        } else if (mode.equals("room")) {
            if (currentRoom == null) { showError("Join a room first!"); return; }
            receiver = currentRoom;
        }
        
        // Gọi hàm bên Client để xử lý chụp và gửi
        client.sendScreenshot(receiver, mode);
    }

    // 4. Logic Gọi điện (Call)
    private void startVideoCall() {
        String selected = userList.getSelectedValue();
        if (selected == null) { showError("Select a user to call!"); return; }
        
        String receiver = selected.split(" \\(")[0];
        if (receiver.equals(client.getUsername())) { showError("Cannot call yourself!"); return; }

        String[] options = { "Video Call", "Audio Call", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this, 
            "Call " + receiver + "?", "Start Call", 
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

        if (choice == 0) client.sendVideoCallRequest(receiver, true, true);      // Video
        else if (choice == 1) client.sendVideoCallRequest(receiver, false, true); // Audio
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}