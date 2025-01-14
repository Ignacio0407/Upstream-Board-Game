package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private MatchService matchService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player player;
    private Match match;
    private MessageRequest messageRequest;

    @BeforeEach
    public void setUp() {
        player = new Player();
        player.setId(1);
        player.setName("player1");
        match = new Match();
        match.setId(1);
        match.setName("Match 1");
        messageRequest = new MessageRequest();
        messageRequest.setPlayerId(1);
        messageRequest.setMatchId(1);
        messageRequest.setContent("Hello, World!");
    }

    // Test createMessage()
    @Test
    void shouldCreateMessageSuccessfully() throws Exception {
        Message createdMessage = new Message(player, match, "Hello, World!");
        
        when(playerService.getById(1)).thenReturn(player);
        when(matchService.getById(1)).thenReturn(match);
        when(messageService.createMessage(player.getId(), match.getId(), "Hello, World!")).thenReturn(createdMessage);
        
        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    void shouldFailToCreateMessageWhenServiceThrowsException() throws Exception {
        when(playerService.getById(1)).thenReturn(player);
        when(matchService.getById(1)).thenReturn(match);
        when(messageService.createMessage(player.getId(), match.getId(), "Hello, World!")).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldFailToCreateMessageWithInvalidPlayerId() throws Exception {
        messageRequest.setPlayerId(999);

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToCreateMessageWithInvalidMatchId() throws Exception {
        messageRequest.setMatchId(999);

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)))
                .andExpect(status().isNotFound());
    }

    // Test getMatchMessages()
    @Test
    void shouldGetMatchMessagesSuccessfully() throws Exception {
        List<Message> messages = List.of(new Message(player, match, "Message 1"));
        when(messageService.getMatchMessages(1)).thenReturn(messages);
        when(matchService.getById(1)).thenReturn(new Match());

        mockMvc.perform(get("/api/v1/messages/match/1"))
                .andExpect(status().isOk()) // Esperamos que devuelva un 200 OK
                .andExpect(jsonPath("$[0].content").value("Message 1"));
    }

    @Test
    void shouldReturnEmptyListWhenNoMessagesForMatch() throws Exception {
        when(messageService.getMatchMessages(1)).thenReturn(new ArrayList<>()); // Simulamos que no hay mensajes para la partida
        when(matchService.getById(1)).thenReturn(new Match());

        mockMvc.perform(get("/api/v1/messages/match/1"))
                .andExpect(status().isOk()) // Esperamos que devuelva un 200 OK
                .andExpect(jsonPath("$").isEmpty()); 
    }

    @Test
    void shouldFailToGetMatchMessagesWhenMatchIdIsInvalid() throws Exception {
        when(messageService.getMatchMessages(999)).thenThrow(new RuntimeException("Invalid Match"));

        mockMvc.perform(get("/api/v1/messages/match/999"))
                .andExpect(status().isNotFound());
    }

    // Test getUserMessages()
    @Test
    void shouldGetUserMessagesSuccessfully() throws Exception {
        List<Message> messages = List.of(new Message(player, match, "User message"));
        when(messageService.getUserMessages(1)).thenReturn(messages);
        when(userService.findUser(1)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/messages/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("User message"));
    }

    @Test
    void shouldReturnEmptyListWhenNoMessagesForUser() throws Exception {
        when(messageService.getUserMessages(1)).thenReturn(new ArrayList<>());
        when(userService.findUser(1)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/messages/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldFailToGetUserMessagesWhenUserIdIsInvalid() throws Exception {
        when(messageService.getUserMessages(999)).thenThrow(new RuntimeException("Invalid User"));

        mockMvc.perform(get("/api/v1/messages/user/999"))
                .andExpect(status().isNotFound());
    }

    // Test deleteMessage()
    @Test
    void shouldDeleteMessageSuccessfully() throws Exception {
        Message messageToDelete = new Message(player, match, "Message to delete");
        messageToDelete.setId(1);

        when(messageService.findById(1)).thenReturn(messageToDelete);

        mockMvc.perform(delete("/api/v1/messages/1"))
                .andExpect(status().isNoContent());

        verify(messageService).deleteMessage(1);
    }

    @Test
    void shouldFailToDeleteMessageWhenMessageNotFound() throws Exception {
        when(messageService.findById(999)).thenReturn(null);

        mockMvc.perform(delete("/api/v1/messages/999"))
                .andExpect(status().isNotFound());
    }

    // Test getUserChats()
    @Test
    void shouldGetUserChatsSuccessfully() throws Exception {
        List<Match> chats = List.of(match);
        when(messageService.getUserChats(1)).thenReturn(chats);
        when(userService.findUser(1)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/messages/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenNoChatsForUser() throws Exception {
        when(messageService.getUserChats(1)).thenReturn(new ArrayList<>());
        when(userService.findUser(1)).thenReturn(new User());

        mockMvc.perform(get("/api/v1/messages/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldFailToGetUserChatsWhenUserIdIsInvalid() throws Exception {
        when(messageService.getUserChats(999)).thenThrow(new RuntimeException("Invalid User"));

        mockMvc.perform(get("/api/v1/messages/chats/999"))
                .andExpect(status().isNotFound());
    }
}