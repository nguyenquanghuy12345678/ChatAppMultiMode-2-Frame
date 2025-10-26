package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String roomName;
    private String creator;
    private Set<String> members;
    private int maxMembers;
    
    public ChatRoom(String roomName, String creator) {
        this.roomName = roomName;
        this.creator = creator;
        this.members = new HashSet<>();
        this.members.add(creator);
        this.maxMembers = 50; // Giới hạn mặc định
    }
    
    public boolean addMember(String username) {
        if (members.size() < maxMembers) {
            return members.add(username);
        }
        return false;
    }
    
    public boolean removeMember(String username) {
        return members.remove(username);
    }
    
    public boolean isMember(String username) {
        return members.contains(username);
    }
    
    // Getters and Setters
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public Set<String> getMembers() { return new HashSet<>(members); }
    
    public int getMemberCount() { return members.size(); }
    
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    
    @Override
    public String toString() {
        return roomName + " (" + members.size() + "/" + maxMembers + ") - Creator: " + creator;
    }
}
