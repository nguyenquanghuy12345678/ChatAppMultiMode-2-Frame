package server;

import common.ChatRoom;
import common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class ServerUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Color scheme - Modern Dark Theme
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 46);
    private static final Color PANEL_COLOR = new Color(40, 42, 54);
    private static final Color ACCENT_COLOR = new Color(139, 233, 253);
    private static final Color SUCCESS_COLOR = new Color(80, 250, 123);
    private static final Color ERROR_COLOR = new Color(255, 85, 85);
    private static final Color WARNING_COLOR = new Color(241, 250, 140);
    private static final Color TEXT_COLOR = new Color(248, 248, 242);
    private static final Color BORDER_COLOR = new Color(68, 71, 90);
    
    private ChatServer server;
    private JTextArea logArea;
    private JList<String> clientList;
    private JList<String> roomList;
    private DefaultListModel<String> clientListModel;
    private DefaultListModel<String> roomListModel;
    private JButton startButton;
    private JButton stopButton;
    private JTextField portField;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private JLabel roomCountLabel;
    private JLabel uptimeLabel;
    private JButton clearLogsButton;
    private JButton kickClientButton;
    private JButton deleteRoomButton;
    private long serverStartTime;
    private Timer uptimeTimer;
    
    public ServerUI() {
        initComponents();
        startUptimeTimer();
    }
    
    private void initComponents() {
        setTitle("Chat Server - Management Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        
        // Main panel with dark theme
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Top panel - Control
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Center panel - Statistics and Lists
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(BACKGROUND_COLOR);
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Lists panel
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        listsPanel.setBackground(BACKGROUND_COLOR);
        
        // Client list panel
        JPanel clientPanel = createClientListPanel();
        listsPanel.add(clientPanel);
        
        // Room list panel
        JPanel roomPanel = createRoomListPanel();
        listsPanel.add(roomPanel);
        
        centerPanel.add(listsPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Logs
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("SERVER CONTROL PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Main control panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlsPanel.setBackground(PANEL_COLOR);
        
        // Port input
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        portLabel.setForeground(TEXT_COLOR);
        controlsPanel.add(portLabel);
        
        portField = new JTextField("12345", 10);
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        portField.setBackground(BACKGROUND_COLOR);
        portField.setForeground(TEXT_COLOR);
        portField.setCaretColor(TEXT_COLOR);
        portField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        controlsPanel.add(portField);
        
        // Start button
        startButton = createStyledButton("Start Server", SUCCESS_COLOR);
        startButton.addActionListener(e -> startServer());
        controlsPanel.add(startButton);
        
        // Stop button
        stopButton = createStyledButton("Stop Server", ERROR_COLOR);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopServer());
        controlsPanel.add(stopButton);
        
        // Separator
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 30));
        separator.setForeground(BORDER_COLOR);
        controlsPanel.add(separator);
        
        // Status label
        statusLabel = new JLabel("STOPPED");
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        controlsPanel.add(statusLabel);
        
        // Uptime label
        uptimeLabel = new JLabel("Uptime: 00:00:00");
        uptimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uptimeLabel.setForeground(WARNING_COLOR);
        controlsPanel.add(uptimeLabel);
        
        panel.add(controlsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(5, 0, 10, 0));
        
        // Client count card
        clientCountLabel = createStatCard("", "Clients", "0", new Color(52, 152, 219));
        panel.add(clientCountLabel);
        
        // Room count card
        roomCountLabel = createStatCard("", "Rooms", "0", new Color(155, 89, 182));
        panel.add(roomCountLabel);
        
        return panel;
    }
    
    private JLabel createStatCard(String icon, String label, String value, Color accentColor) {
        JLabel card = new JLabel(String.format(
            "<html><div style='text-align: center; padding: 10px;'>" +
            "<span style='font-size: 36px;'>%s</span><br>" +
            "<span style='font-size: 14px; color: #aaa;'>%s</span><br>" +
            "<span style='font-size: 28px; font-weight: bold;'>%s</span>" +
            "</div></html>", icon, label, value
        ), JLabel.CENTER);
        card.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.setOpaque(true);
        card.setBackground(PANEL_COLOR);
        card.setForeground(accentColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accentColor, 2, true),
            new EmptyBorder(15, 10, 15, 10)
        ));
        return card;
    }
    
    private JPanel createClientListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel title = new JLabel("CONNECTED CLIENTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ACCENT_COLOR);
        panel.add(title, BorderLayout.NORTH);
        
        // List
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setFont(new Font("Consolas", Font.PLAIN, 12));
        clientList.setBackground(BACKGROUND_COLOR);
        clientList.setForeground(TEXT_COLOR);
        clientList.setSelectionBackground(ACCENT_COLOR);
        clientList.setSelectionForeground(BACKGROUND_COLOR);
        clientList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(clientList);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel clientButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        clientButtonPanel.setBackground(PANEL_COLOR);
        
        kickClientButton = createStyledButton("Kick Client", ERROR_COLOR);
        kickClientButton.setEnabled(false);
        kickClientButton.addActionListener(e -> kickSelectedClient());
        clientButtonPanel.add(kickClientButton);
        
        panel.add(clientButtonPanel, BorderLayout.SOUTH);
        
        // Enable kick button when client selected
        clientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                kickClientButton.setEnabled(clientList.getSelectedIndex() >= 0);
            }
        });
        
        return panel;
    }
    
    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel title = new JLabel("CHAT ROOMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ACCENT_COLOR);
        panel.add(title, BorderLayout.NORTH);
        
        // List
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("Consolas", Font.PLAIN, 12));
        roomList.setBackground(BACKGROUND_COLOR);
        roomList.setForeground(TEXT_COLOR);
        roomList.setSelectionBackground(ACCENT_COLOR);
        roomList.setSelectionForeground(BACKGROUND_COLOR);
        roomList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel roomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        roomButtonPanel.setBackground(PANEL_COLOR);
        
        deleteRoomButton = createStyledButton("Delete Room", WARNING_COLOR);
        deleteRoomButton.setEnabled(false);
        deleteRoomButton.addActionListener(e -> deleteSelectedRoom());
        roomButtonPanel.add(deleteRoomButton);
        
        panel.add(roomButtonPanel, BorderLayout.SOUTH);
        
        // Enable delete button when room selected
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteRoomButton.setEnabled(roomList.getSelectedIndex() >= 0);
            }
        });
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Title
        JLabel title = new JLabel("SERVER LOGS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ACCENT_COLOR);
        panel.add(title, BorderLayout.NORTH);
        
        // Log area
        logArea = new JTextArea(12, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(BACKGROUND_COLOR);
        logArea.setForeground(TEXT_COLOR);
        logArea.setCaretColor(TEXT_COLOR);
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.getViewport().setBackground(BACKGROUND_COLOR);
        panel.add(logScroll, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(PANEL_COLOR);
        
        clearLogsButton = createStyledButton("Clear Logs", WARNING_COLOR);
        clearLogsButton.addActionListener(e -> {
            logArea.setText("");
            appendLog("Logs cleared");
        });
        buttonPanel.add(clearLogsButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        
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
    
    private void kickSelectedClient() {
        String selected = clientList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to kick this client?\n" + selected,
                "Confirm Kick",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (server == null || !server.isRunning()) {
                    JOptionPane.showMessageDialog(this,
                        "Server is not running.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String username = selected.split("\\|")[0].trim();
                boolean ok = server.kickClient(username);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                        "Unable to kick: " + username,
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteSelectedRoom() {
        String selected = roomList.getSelectedValue();
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this room?\n" + selected,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (server == null || !server.isRunning()) {
                    JOptionPane.showMessageDialog(this,
                        "Server is not running.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String roomName = selected.split("\\|")[0].trim();
                boolean ok = server.deleteRoom(roomName);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                        "Only empty rooms can be deleted.",
                        "Delete Room", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    
    private void startUptimeTimer() {
        uptimeTimer = new Timer(1000, e -> updateUptime());
    }
    
    private void updateUptime() {
        if (server != null && server.isRunning()) {
            long uptime = System.currentTimeMillis() - serverStartTime;
            long hours = uptime / 3600000;
            long minutes = (uptime % 3600000) / 60000;
            long seconds = (uptime % 60000) / 1000;
            uptimeLabel.setText(String.format("Uptime: %02d:%02d:%02d", hours, minutes, seconds));
        } else {
            uptimeLabel.setText("Uptime: 00:00:00");
        }
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            
            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(this, 
                    "Port must be between 1024-65535!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            server = new ChatServer(port);
            server.setUI(this);
            server.start();
            
            serverStartTime = System.currentTimeMillis();
            uptimeTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
            kickClientButton.setEnabled(false);
            deleteRoomButton.setEnabled(false);
            
            statusLabel.setText("RUNNING");
            statusLabel.setForeground(SUCCESS_COLOR);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid port number!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
            
            uptimeTimer.stop();
            
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            portField.setEnabled(true);
            kickClientButton.setEnabled(false);
            deleteRoomButton.setEnabled(false);
            
            statusLabel.setText("STOPPED");
            statusLabel.setForeground(ERROR_COLOR);
            
            clientListModel.clear();
            roomListModel.clear();
            updateStatistics(0, 0);
            updateUptime();
        }
    }
    
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void updateStatistics(int clientCount, int roomCount) {
        SwingUtilities.invokeLater(() -> {
            clientCountLabel.setText(String.format(
                "<html><div style='text-align: center; padding: 10px;'>" +
                "<span style='font-size: 14px; color: #aaa;'>Clients</span><br>" +
                "<span style='font-size: 28px; font-weight: bold;'>%d</span>" +
                "</div></html>", clientCount
            ));
            
            roomCountLabel.setText(String.format(
                "<html><div style='text-align: center; padding: 10px;'>" +
                "<span style='font-size: 14px; color: #aaa;'>Rooms</span><br>" +
                "<span style='font-size: 28px; font-weight: bold;'>%d</span>" +
                "</div></html>", roomCount
            ));
        });
    }
    
    public void updateClientList(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear();
            for (User user : users) {
                String room = user.getCurrentRoom() != null ? 
                    " [Room: " + user.getCurrentRoom() + "]" : " [Online]";
                clientListModel.addElement(String.format("%-15s | %s:%d %s", 
                    user.getUsername(), 
                    user.getIpAddress(), 
                    user.getPort(),
                    room));
            }
        });
    }
    
    public void updateRoomList(List<ChatRoom> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomListModel.clear();
            for (ChatRoom room : rooms) {
                roomListModel.addElement(String.format("%-20s | Users %2d/%2d | Owner %s", 
                    room.getRoomName(),
                    room.getMemberCount(),
                    room.getMaxMembers(),
                    room.getCreator()));
            }
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI();
            ui.setVisible(true);
        });
    }
}
