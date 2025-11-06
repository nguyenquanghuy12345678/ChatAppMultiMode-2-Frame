package client;

import common.ChatRoom;
import common.Message;
import common.User;
import common.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
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
    
    // Card layout
    private JPanel mainContainer;
    private CardLayout cardLayout;
    
    // Login components
    private JPanel loginPanel;
    private JTextField serverIPField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JButton connectButton;
    
    // Chat components
    private JPanel chatPanel;
    private JTabbedPane chatTabs;
    
    // Broadcast tab
    private EmojiTextPane broadcastArea;
    private JTextField broadcastInput;
    private JButton broadcastSendButton;
    private JButton broadcastEmojiButton;
    private JButton broadcastFileButton;
    
    // Private chat tab
    private EmojiTextPane privateArea;
    private JTextField privateInput;
    private JButton privateSendButton;
    private JButton privateEmojiButton;
    private JButton privateFileButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    // Room chat tab
    private EmojiTextPane roomArea;
    private JTextField roomInput;
    private JButton roomSendButton;
    private JButton roomEmojiButton;
    private JButton roomFileButton;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JButton joinRoomButton;
    private JButton leaveRoomButton;
    private JButton createRoomButton;
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
        // Set Look and Feel first
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Main container with CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // Login panel
        loginPanel = createLoginPanel();
        mainContainer.add(loginPanel, "LOGIN");
        
        // Chat panel
        chatPanel = createChatPanel();
        mainContainer.add(chatPanel, "CHAT");
        
        add(mainContainer);
        
        // Show login panel first
        showLoginPanel();
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_COLOR, 3, true),
            new EmptyBorder(40, 50, 40, 50)
        ));
        
        GridBagConstraints gbcTitle = new GridBagConstraints();
        gbcTitle.insets = new Insets(0, 0, 30, 0);
        gbcTitle.fill = GridBagConstraints.HORIZONTAL;
        gbcTitle.gridx = 0;
        gbcTitle.gridy = 0;
        gbcTitle.gridwidth = 2;
        
        // Title with icon
        JLabel titleLabel = new JLabel("Chat Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        formPanel.add(titleLabel, gbcTitle);
        
        GridBagConstraints gbcIpLabel = new GridBagConstraints();
        gbcIpLabel.insets = new Insets(10, 10, 10, 10);
        gbcIpLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcIpLabel.gridx = 0;
        gbcIpLabel.gridy = 1;
        
        // Server IP
        JLabel ipLabel = new JLabel("Server IP:");
        ipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ipLabel.setForeground(TEXT_COLOR);
        formPanel.add(ipLabel, gbcIpLabel);
        
        GridBagConstraints gbcIpField = new GridBagConstraints();
        gbcIpField.insets = new Insets(10, 10, 10, 10);
        gbcIpField.fill = GridBagConstraints.HORIZONTAL;
        gbcIpField.gridx = 1;
        gbcIpField.gridy = 1;
        
        serverIPField = new JTextField("localhost", 20);
        styleTextField(serverIPField);
        formPanel.add(serverIPField, gbcIpField);
        
        GridBagConstraints gbcPortLabel = new GridBagConstraints();
        gbcPortLabel.insets = new Insets(10, 10, 10, 10);
        gbcPortLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcPortLabel.gridx = 0;
        gbcPortLabel.gridy = 2;
        
        // Server Port
        JLabel portLabel = new JLabel("Server Port:");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        portLabel.setForeground(TEXT_COLOR);
        formPanel.add(portLabel, gbcPortLabel);
        
        GridBagConstraints gbcPortField = new GridBagConstraints();
        gbcPortField.insets = new Insets(10, 10, 10, 10);
        gbcPortField.fill = GridBagConstraints.HORIZONTAL;
        gbcPortField.gridx = 1;
        gbcPortField.gridy = 2;
        
        serverPortField = new JTextField("12345", 20);
        styleTextField(serverPortField);
        formPanel.add(serverPortField, gbcPortField);
        
        GridBagConstraints gbcUserLabel = new GridBagConstraints();
        gbcUserLabel.insets = new Insets(10, 10, 10, 10);
        gbcUserLabel.fill = GridBagConstraints.HORIZONTAL;
        gbcUserLabel.gridx = 0;
        gbcUserLabel.gridy = 3;
        
        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(TEXT_COLOR);
        formPanel.add(userLabel, gbcUserLabel);
        
        GridBagConstraints gbcUserField = new GridBagConstraints();
        gbcUserField.insets = new Insets(10, 10, 10, 10);
        gbcUserField.fill = GridBagConstraints.HORIZONTAL;
        gbcUserField.gridx = 1;
        gbcUserField.gridy = 3;
        
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        formPanel.add(usernameField, gbcUserField);
        
        GridBagConstraints gbcConnectBtn = new GridBagConstraints();
        gbcConnectBtn.insets = new Insets(20, 10, 10, 10);
        gbcConnectBtn.fill = GridBagConstraints.HORIZONTAL;
        gbcConnectBtn.gridx = 0;
        gbcConnectBtn.gridy = 4;
        gbcConnectBtn.gridwidth = 2;
        
        // Connect button
        connectButton = createStyledButton("Connect to Server", SUCCESS_COLOR);
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectButton.addActionListener(e -> handleConnect());
        formPanel.add(connectButton, gbcConnectBtn);
        
        panel.add(formPanel);
        return panel;
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Top panel - Status bar
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(PANEL_COLOR);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        // Left side - Status and username
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusPanel.setBackground(PANEL_COLOR);
        
        statusLabel = new JLabel("CONNECTED");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(SUCCESS_COLOR);
        statusPanel.add(statusLabel);
        
        usernameLabel = new JLabel("User");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(ACCENT_COLOR);
        statusPanel.add(usernameLabel);
        
        topPanel.add(statusPanel, BorderLayout.WEST);
        
        // Right side - Disconnect button
        JButton disconnectButton = createStyledButton("Disconnect", ERROR_COLOR);
        disconnectButton.addActionListener(e -> handleDisconnect());
        topPanel.add(disconnectButton, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center - Tabs
        chatTabs = new JTabbedPane();
        chatTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chatTabs.setBackground(PANEL_COLOR);
        chatTabs.setForeground(TEXT_COLOR);
        chatTabs.setOpaque(true);
        
        // Custom tab styling
        chatTabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(ACCENT_COLOR);
                } else {
                    g2d.setColor(PANEL_COLOR);
                }
                g2d.fillRoundRect(x, y, w, h, 8, 8);
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                g.setColor(isSelected ? BACKGROUND_COLOR : TEXT_COLOR);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // No border around content
            }
        });
        
        // Broadcast tab
        chatTabs.addTab("Broadcast (All)", createBroadcastTab());
        
        // Private chat tab
        chatTabs.addTab("Private Chat (1-1)", createPrivateTab());
        
        // Room chat tab
        chatTabs.addTab("Room Chat", createRoomTab());
        
        panel.add(chatTabs, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBroadcastTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Chat area with emoji support
        broadcastArea = createStyledEmojiPane();
        JScrollPane scrollPane = new JScrollPane(broadcastArea);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 2));
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        
        broadcastInput = new JTextField();
        styleTextField(broadcastInput);
        broadcastInput.addActionListener(e -> sendBroadcastMessage());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        broadcastEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        broadcastEmojiButton.setPreferredSize(new Dimension(50, 40));
        broadcastEmojiButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        broadcastEmojiButton.setText("😊");
        broadcastEmojiButton.addActionListener(e -> showEmojiPicker(broadcastInput));
        
        broadcastFileButton = createStyledButton("📁", new Color(155, 89, 182));
        broadcastFileButton.setPreferredSize(new Dimension(50, 40));
        broadcastFileButton.addActionListener(e -> sendFile("broadcast"));
        
        broadcastSendButton = createStyledButton("Send", new Color(52, 152, 219));
        broadcastSendButton.addActionListener(e -> sendBroadcastMessage());
        
        buttonPanel.add(broadcastEmojiButton);
        buttonPanel.add(broadcastFileButton);
        buttonPanel.add(broadcastSendButton);
        
        inputPanel.add(broadcastInput, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPrivateTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Left side - User list
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(PANEL_COLOR);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        
        JLabel userListLabel = new JLabel("ONLINE USERS");
        userListLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userListLabel.setForeground(ACCENT_COLOR);
        leftPanel.add(userListLabel, BorderLayout.NORTH);
        
        userListModel = new DefaultListModel<>();
        userList = createStyledList(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = createStyledScrollPane(userList);
        leftPanel.add(userScroll, BorderLayout.CENTER);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Chat
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        privateArea = createStyledEmojiPane();
        JScrollPane chatScroll = createStyledScrollPane(privateArea);
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        
        privateInput = new JTextField();
        styleTextField(privateInput);
        privateInput.addActionListener(e -> sendPrivateMessage());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        privateEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        privateEmojiButton.setPreferredSize(new Dimension(50, 40));
        privateEmojiButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        privateEmojiButton.setText("😊");
        privateEmojiButton.addActionListener(e -> showEmojiPicker(privateInput));
        
        privateFileButton = createStyledButton("📁", new Color(155, 89, 182));
        privateFileButton.setPreferredSize(new Dimension(50, 40));
        privateFileButton.addActionListener(e -> sendFile("private"));
        
        privateSendButton = createStyledButton("Send", SUCCESS_COLOR);
        privateSendButton.addActionListener(e -> sendPrivateMessage());
        
        buttonPanel.add(privateEmojiButton);
        buttonPanel.add(privateFileButton);
        buttonPanel.add(privateSendButton);
        
        inputPanel.add(privateInput, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRoomTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Left side - Room list
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(PANEL_COLOR);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        leftPanel.setPreferredSize(new Dimension(270, 0));
        
        JLabel roomListLabel = new JLabel("CHAT ROOMS");
        roomListLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        roomListLabel.setForeground(ACCENT_COLOR);
        leftPanel.add(roomListLabel, BorderLayout.NORTH);
        
        roomListModel = new DefaultListModel<>();
        roomList = createStyledList(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane roomScroll = createStyledScrollPane(roomList);
        leftPanel.add(roomScroll, BorderLayout.CENTER);
        
        // Room buttons
        JPanel roomButtonPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        roomButtonPanel.setBackground(PANEL_COLOR);
        roomButtonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        joinRoomButton = createStyledButton("Join Room", SUCCESS_COLOR);
        joinRoomButton.addActionListener(e -> handleJoinRoom());
        
        leaveRoomButton = createStyledButton("Leave Room", new Color(230, 126, 34));
        leaveRoomButton.setEnabled(false);
        leaveRoomButton.addActionListener(e -> handleLeaveRoom());
        
        createRoomButton = createStyledButton("Create Room", new Color(52, 152, 219));
        createRoomButton.addActionListener(e -> handleCreateRoom());
        
        roomButtonPanel.add(joinRoomButton);
        roomButtonPanel.add(leaveRoomButton);
        roomButtonPanel.add(createRoomButton);
        
        leftPanel.add(roomButtonPanel, BorderLayout.SOUTH);
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Chat
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        roomArea = createStyledEmojiPane();
        JScrollPane chatScroll = createStyledScrollPane(roomArea);
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        
        roomInput = new JTextField();
        styleTextField(roomInput);
        roomInput.setEnabled(false);
        roomInput.addActionListener(e -> sendRoomMessage());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        roomEmojiButton = createStyledButton("😊", new Color(241, 196, 15));
        roomEmojiButton.setPreferredSize(new Dimension(50, 40));
        roomEmojiButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roomEmojiButton.setText("😊");
        roomEmojiButton.setEnabled(false);
        roomEmojiButton.addActionListener(e -> showEmojiPicker(roomInput));
        
        roomFileButton = createStyledButton("📁", new Color(155, 89, 182));
        roomFileButton.setPreferredSize(new Dimension(50, 40));
        roomFileButton.setEnabled(false);
        roomFileButton.addActionListener(e -> sendFile("room"));
        
        roomSendButton = createStyledButton("Send", SUCCESS_COLOR);
        roomSendButton.setEnabled(false);
        roomSendButton.addActionListener(e -> sendRoomMessage());
        
        buttonPanel.add(roomEmojiButton);
        buttonPanel.add(roomFileButton);
        buttonPanel.add(roomSendButton);
        
        inputPanel.add(roomInput, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void handleConnect() {
        String serverIP = serverIPField.getText().trim();
        String portStr = serverPortField.getText().trim();
        String username = usernameField.getText().trim();
        
        if (serverIP.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all fields!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            
            if (client.connect(serverIP, port, username)) {
                showChatPanel();
                setTitle("Chat Client - " + username);
                usernameLabel.setText(username);
                statusLabel.setText("CONNECTED");
                statusLabel.setForeground(SUCCESS_COLOR);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid port number!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleDisconnect() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to disconnect?",
            "Confirm Disconnect",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.disconnect();
            showLoginPanel();
            setTitle("Chat Application");
            statusLabel.setText("DISCONNECTED");
            statusLabel.setForeground(ERROR_COLOR);
            
            // Clear chat areas
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
        String message = broadcastInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendBroadcastMessage(message);
            broadcastInput.setText("");
        }
    }
    
    private void sendPrivateMessage() {
        String message = privateInput.getText().trim();
        String selectedUser = userList.getSelectedValue();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a recipient!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract username (remove IP info)
        String receiver = selectedUser.split(" \\(")[0];
        
        if (receiver.equals(client.getUsername())) {
            JOptionPane.showMessageDialog(this, 
                "Cannot send message to yourself!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        client.sendPrivateMessage(receiver, message);
        privateInput.setText("");
    }
    
    private void sendRoomMessage() {
        String message = roomInput.getText().trim();
        
        if (message.isEmpty() || currentRoom == null) {
            return;
        }
        
        client.sendRoomMessage(currentRoom, message);
        roomInput.setText("");
    }
    
    private void handleJoinRoom() {
        String selectedRoom = roomList.getSelectedValue();
        
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a room!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract room name (without member count)
        String roomName = selectedRoom.split(" \\(")[0];
        
        // Add # prefix if not present
        if (!roomName.startsWith("#")) {
            roomName = "#" + roomName;
        }
        
        client.joinRoom(roomName);
        currentRoom = roomName;
        roomInput.setEnabled(true);
        roomSendButton.setEnabled(true);
        roomEmojiButton.setEnabled(true);
        roomFileButton.setEnabled(true);
        leaveRoomButton.setEnabled(true);
        roomArea.appendText("=== Joined room: " + roomName + " ===");
        roomArea.appendText("----------------------------------------");
    }
    
    private void handleLeaveRoom() {
        if (currentRoom != null) {
            client.leaveRoom(currentRoom);
            roomArea.appendText("========================================");
            roomArea.appendText("=== Left room: " + currentRoom + " ===");
            roomArea.appendText("");
            currentRoom = null;
            roomInput.setEnabled(false);
            roomSendButton.setEnabled(false);
            roomEmojiButton.setEnabled(false);
            roomFileButton.setEnabled(false);
            leaveRoomButton.setEnabled(false);
        }
    }
    
    private void handleCreateRoom() {
        String roomName = JOptionPane.showInputDialog(this, 
            "Enter room name (without #):", 
            "Create New Room", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (roomName != null && !roomName.trim().isEmpty()) {
            // Remove # if user added it
            roomName = roomName.trim().replace("#", "");
            
            // Validate room name
            if (roomName.matches("[a-zA-Z0-9_-]+")) {
                client.createRoom(roomName);
            } else {
                showError("Room name can only contain letters, numbers, _ and -");
            }
        }
    }
    
    private void showLoginPanel() {
        cardLayout.show(mainContainer, "LOGIN");
    }
    
    private void showChatPanel() {
        cardLayout.show(mainContainer, "CHAT");
    }
    
    public void updateUserList(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (User user : users) {
                if (!user.getUsername().equals(client.getUsername())) {
                    userListModel.addElement(user.getUsername() + " (" + 
                        user.getIpAddress() + ":" + user.getPort() + ")");
                }
            }
        });
    }
    
    public void updateRoomList(List<ChatRoom> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomListModel.clear();
            for (ChatRoom room : rooms) {
                roomListModel.addElement(room.getDisplayName() + " (" + 
                    room.getMemberCount() + "/" + room.getMaxMembers() + ")");
            }
        });
    }
    
    public void displayBroadcastMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            String prefix = msg.getSender().equals(client.getUsername()) ? "You" : msg.getSender();
            String text = String.format("[%s] %s: %s", 
                msg.getTimestamp(), prefix, msg.getContent());
            broadcastArea.appendText(text);
        });
    }
    
    public void displayPrivateMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            String sender = msg.getSender();
            String prefix;
            if (sender.equals(client.getUsername())) {
                prefix = "You -> " + msg.getReceiver();
            } else {
                prefix = sender;
            }
            String text = String.format("[%s] %s: %s", 
                msg.getTimestamp(), prefix, msg.getContent());
            privateArea.appendText(text);
        });
    }
    
    public void displayRoomMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            String text = String.format("[%s] %s: %s", 
                msg.getTimestamp(), msg.getSender(), msg.getContent());
            roomArea.appendText(text);
        });
    }
    
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void showInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    // Emoji Picker Dialog
    private void showEmojiPicker(JTextField targetField) {
        JDialog emojiDialog = new JDialog(this, "Select Emoji", true);
        emojiDialog.setLayout(new BorderLayout(10, 10));
        emojiDialog.setSize(600, 500);
        emojiDialog.setLocationRelativeTo(this);
        emojiDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("Select Emoji Icon");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        emojiDialog.add(titleLabel, BorderLayout.NORTH);
        
        // Emoji grid panel with scroll
        JPanel emojiGridPanel = new JPanel();
        
        // Get available emoji icons
        List<String> availableEmojis = IconManager.getAvailableEmojiIcons();
        
        if (availableEmojis.isEmpty()) {
            // No icons available, show message
            JLabel noIconsLabel = new JLabel("<html><center>No emoji icons found!<br><br>" +
                "Please add emoji icon files to:<br><b>resources/icons/</b><br><br>" +
                "Example: emoji_smile.png, emoji_heart.png, etc.</center></html>");
            noIconsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noIconsLabel.setForeground(TEXT_COLOR);
            noIconsLabel.setHorizontalAlignment(JLabel.CENTER);
            emojiDialog.add(noIconsLabel, BorderLayout.CENTER);
        } else {
            // Calculate grid size
            int cols = 10;
            int rows = (int) Math.ceil(availableEmojis.size() / (double) cols);
            
            emojiGridPanel.setLayout(new GridLayout(rows, cols, 5, 5));
            emojiGridPanel.setBackground(PANEL_COLOR);
            emojiGridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // Add emoji buttons
            for (String emojiIconName : availableEmojis) {
                JButton emojiBtn = new JButton();
                ImageIcon icon = IconManager.loadIcon(emojiIconName, 40);
                emojiBtn.setIcon(icon);
                emojiBtn.setToolTipText(emojiIconName.replace(".png", "").replace("emoji_", ""));
                emojiBtn.setPreferredSize(new Dimension(50, 50));
                emojiBtn.setBackground(PANEL_COLOR);
                emojiBtn.setFocusPainted(false);
                emojiBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
                emojiBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                emojiBtn.addActionListener(e -> {
                    // Insert emoji code into text field
                    String emojiCode = IconManager.getEmojiCode(emojiIconName);
                    String currentText = targetField.getText();
                    targetField.setText(currentText + emojiCode);
                    emojiDialog.dispose();
                });
                
                // Hover effect
                emojiBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        emojiBtn.setBackground(ACCENT_COLOR);
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        emojiBtn.setBackground(PANEL_COLOR);
                    }
                });
                
                emojiGridPanel.add(emojiBtn);
            }
            
            JScrollPane scrollPane = new JScrollPane(emojiGridPanel);
            scrollPane.setBorder(new LineBorder(BORDER_COLOR, 2));
            scrollPane.getViewport().setBackground(PANEL_COLOR);
            emojiDialog.add(scrollPane, BorderLayout.CENTER);
        }
        
        // Info label
        JLabel infoLabel = new JLabel("Click an emoji to insert into your message");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(TEXT_COLOR);
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        emojiDialog.add(infoLabel, BorderLayout.SOUTH);
        
        emojiDialog.setVisible(true);
    }
    
    // Send File
    private void sendFile(String mode) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Send (Max 5MB)");
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Check file size (limit to 5MB)
            if (selectedFile.length() > 5 * 1024 * 1024) {
                showError("File size exceeds 5MB limit!");
                return;
            }
            
            // Determine receiver based on mode
            String receiver = null;
            if (mode.equals("private")) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser == null) {
                    showError("Please select a recipient!");
                    return;
                }
                receiver = selectedUser.split(" \\(")[0];
                
                if (receiver.equals(client.getUsername())) {
                    showError("Cannot send file to yourself!");
                    return;
                }
            } else if (mode.equals("room")) {
                if (currentRoom == null) {
                    showError("Please join a room first!");
                    return;
                }
                receiver = currentRoom;
            }
            
            // Send file through client
            client.sendFile(selectedFile, receiver, mode);
        }
    }
    
    // Helper methods for styling
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(BACKGROUND_COLOR);
        textField.setForeground(TEXT_COLOR);
        textField.setCaretColor(ACCENT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.brighter());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });
        
        return button;
    }
    
    private JTextArea createStyledTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(PANEL_COLOR);
        area.setForeground(TEXT_COLOR);
        area.setCaretColor(TEXT_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }
    
    private EmojiTextPane createStyledEmojiPane() {
        EmojiTextPane pane = new EmojiTextPane();
        pane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pane.setBackground(PANEL_COLOR);
        pane.setForeground(TEXT_COLOR);
        pane.setCaretColor(TEXT_COLOR);
        pane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return pane;
    }
    
    private JScrollPane createStyledScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 2));
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        return scrollPane;
    }
    
    private <E> JList<E> createStyledList(DefaultListModel<E> model) {
        JList<E> list = new JList<>(model);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setBackground(PANEL_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setSelectionBackground(ACCENT_COLOR);
        list.setSelectionForeground(BACKGROUND_COLOR);
        list.setBorder(new EmptyBorder(5, 5, 5, 5));
        return list;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}
