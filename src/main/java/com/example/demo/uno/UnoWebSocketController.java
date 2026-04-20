package com.example.demo.uno;

import com.example.demo.chess.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class UnoWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, UnoRoom> rooms = new ConcurrentHashMap<>();

    public UnoWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/api/uno/create")
    @ResponseBody
    public Map<String, Object> createRoom(@RequestBody Map<String, String> body) {
        String roomId = generateRoomId();
        UnoRoom room = new UnoRoom(roomId, body.get("playerName"));
        rooms.put(roomId, room);
        return Map.of("roomId", roomId, "playerIndex", 0);
    }

    @PostMapping("/api/uno/join")
    @ResponseBody
    public Map<String, Object> joinRoom(@RequestBody Map<String, String> body) {
        String roomId = body.get("roomId").toUpperCase();
        String playerName = body.get("playerName");

        UnoRoom room = rooms.get(roomId);
        if (room == null) return Map.of("error", "Room not found");
        if (room.players.size() >= 4) return Map.of("error", "Room is full");

        int playerIndex = room.players.size();
        room.addPlayer(playerName);

        // If 2 players joined, start automatically for this demo
        if (room.players.size() == 2 && room.status.equals("waiting")) {
            room.startGame();
        }

        broadcastState(room);
        return Map.of("roomId", roomId, "playerIndex", playerIndex);
    }

    @MessageMapping("/uno/{roomId}/play")
    public void playCard(@DestinationVariable String roomId, Map<String, Object> payload) {
        UnoRoom room = rooms.get(roomId);
        if (room == null) return;

        int pIdx = (int) payload.get("playerIndex");
        int cardId = (int) payload.get("cardId");
        String chosenColor = (String) payload.get("chosenColor");

        room.playCard(pIdx, cardId, chosenColor);
        broadcastState(room);
    }

    @MessageMapping("/uno/{roomId}/draw")
    public void drawCard(@DestinationVariable String roomId, Map<String, Object> payload) {
        UnoRoom room = rooms.get(roomId);
        if (room == null) return;

        int pIdx = (int) payload.get("playerIndex");
        room.drawCard(pIdx);
        broadcastState(room);
    }

    @MessageMapping("/uno/{roomId}/subscribe")
    public void subscribe(@DestinationVariable String roomId) {
        UnoRoom room = rooms.get(roomId);
        if (room != null) broadcastState(room);
    }

    private void broadcastState(UnoRoom room) {
        for (int i = 0; i < room.players.size(); i++) {
            messagingTemplate.convertAndSend("/topic/uno/" + room.roomId + "/player/" + i, (Object) room.toStateMap(i));
        }
        // Also send public state for observers or total counts if needed
        messagingTemplate.convertAndSend("/topic/uno/" + room.roomId, (Object) Map.of("status", room.status, "playerCount", room.players.size()));
    }

    private String generateRoomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        Random rnd = new Random();
        for (int i = 0; i < 6; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
