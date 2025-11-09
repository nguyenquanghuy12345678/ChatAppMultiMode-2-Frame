package common;

import java.io.Serializable;
import java.util.*;

/**
 * Class quản lý reactions cho tin nhắn
 */
public class MessageReaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private Map<String, Set<String>> reactions; // reaction type -> set of usernames
    
    public MessageReaction(String messageId) {
        this.messageId = messageId;
        this.reactions = new HashMap<>();
    }
    
    /**
     * Thêm reaction từ user
     */
    public void addReaction(String username, String reactionType) {
        reactions.computeIfAbsent(reactionType, k -> new HashSet<>()).add(username);
    }
    
    /**
     * Xóa reaction từ user
     */
    public void removeReaction(String username, String reactionType) {
        Set<String> users = reactions.get(reactionType);
        if (users != null) {
            users.remove(username);
            if (users.isEmpty()) {
                reactions.remove(reactionType);
            }
        }
    }
    
    /**
     * Lấy số lượng reaction của một loại
     */
    public int getReactionCount(String reactionType) {
        Set<String> users = reactions.get(reactionType);
        return users != null ? users.size() : 0;
    }
    
    /**
     * Lấy tất cả users đã react với loại reaction
     */
    public Set<String> getUsersWithReaction(String reactionType) {
        return reactions.getOrDefault(reactionType, new HashSet<>());
    }
    
    /**
     * Lấy tất cả loại reactions
     */
    public Set<String> getAllReactionTypes() {
        return reactions.keySet();
    }
    
    /**
     * Kiểm tra user đã react với loại này chưa
     */
    public boolean hasUserReacted(String username, String reactionType) {
        Set<String> users = reactions.get(reactionType);
        return users != null && users.contains(username);
    }
    
    /**
     * Lấy tổng số reactions
     */
    public int getTotalReactions() {
        return reactions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
    
    // Getters
    public String getMessageId() { return messageId; }
    public Map<String, Set<String>> getReactions() { return reactions; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reactions for message ").append(messageId).append(":\n");
        reactions.forEach((type, users) -> 
            sb.append("  ").append(type).append(": ").append(users.size()).append(" users\n")
        );
        return sb.toString();
    }
}
