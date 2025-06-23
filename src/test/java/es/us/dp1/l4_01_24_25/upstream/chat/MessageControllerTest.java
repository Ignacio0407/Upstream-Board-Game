package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.eq;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc
class MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @ParameterizedTest
    @CsvSource({
        "2, '[{\"playerId\":1,\"matchId\":1,\"content\":\"Hi\"},{\"playerId\":2,\"matchId\":1,\"content\":\"Hey\"}]', 2",
        "0, '[]', 0"
    })
    void testFindAll(int expectedSize, String expectedJsonResponse, int messageCount) throws Exception {
        List<MessageDTO> dtos = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            MessageDTO dto = new MessageDTO();
            dto.setPlayerId(i + 1);
            dto.setMatchId(1);
            dto.setContent(i == 0 ? "Hi" : "Hey");
            dtos.add(dto);
        }

        when(messageService.findAllAsDTO()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/messages"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJsonResponse));
    }

    @Test
    void testFindById() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("Hola");

        when(messageService.findByIdAsDTO(1)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/messages/1"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "playerId":1,
                    "matchId":1,
                    "content":"Hola"
                }
            """));
    }

    @Test
    void testSave() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(2);
        dto.setContent("New");

        when(messageService.saveAsDTO(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "playerId":1,
                        "matchId":2,
                        "content":"New"
                    }
                """))
            .andExpect(status().isCreated())
            .andExpect(content().json("""
                {
                    "playerId":1,
                    "matchId":2,
                    "content":"New"
                }
            """));
    }

    @Test
    void testUpdate() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(3);
        dto.setMatchId(4);
        dto.setContent("Updated");

        when(messageService.updateAsDTO(eq(1), any())).thenReturn(dto);

        mockMvc.perform(put("/api/v1/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "playerId":3,
                        "matchId":4,
                        "content":"Updated"
                    }
                """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "playerId":3,
                    "matchId":4,
                    "content":"Updated"
                }
            """));
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/messages/1"))
            .andExpect(status().isNoContent());

        verify(messageService).delete(1);
    }

    @Test
    void testSave_shouldSendMessageToWebSocketAndReturnDto() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(2);
        dto.setContent("Hello");

        when(messageService.saveAsDTO(any(MessageDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "playerId":1,
                        "matchId":2,
                        "content":"Hello"
                    }
                """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "playerId":1,
                    "matchId":2,
                    "content":"Hello"
                }
            """));

        verify(messagingTemplate).convertAndSend(eq("/topic/chat/2"), eq(dto));
    }

    @ParameterizedTest
    @CsvSource({
        "1, '[{\"playerId\":1,\"matchId\":1,\"content\":\"Hi\"}]'",
        "2, '[{\"playerId\":1,\"matchId\":2,\"content\":\"Hi\"},{\"playerId\":2,\"matchId\":2,\"content\":\"Hey\"}]'"
    })
    void testFindMatchMessages(int matchId, String expectedJson) throws Exception {
        List<MessageDTO> dtos = new ArrayList<>();
        if (matchId == 1) {
            MessageDTO dto = new MessageDTO();
            dto.setPlayerId(1);
            dto.setMatchId(1);
            dto.setContent("Hi");
            dtos.add(dto);
        } else {
            MessageDTO dto1 = new MessageDTO();
            dto1.setPlayerId(1);
            dto1.setMatchId(2);
            dto1.setContent("Hi");
            MessageDTO dto2 = new MessageDTO();
            dto2.setPlayerId(2);
            dto2.setMatchId(2);
            dto2.setContent("Hey");
            dtos = List.of(dto1, dto2);
        }

        when(messageService.findMatchMessagesAsDTO(matchId)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/messages/match/" + matchId))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void testFindUserMessages() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("Hola");

        when(messageService.findUserMessages(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/messages/user/1"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [{
                    "playerId":1,
                    "matchId":1,
                    "content":"Hola"
                }]
            """));
    }

    @Test
    void testFindUserChats() throws Exception {
        Match match = new Match();
        match.setId(1);
        match.setPassword("abc");

        when(messageService.findUserChats(1)).thenReturn(List.of(match));

        mockMvc.perform(get("/api/v1/messages/chats/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }
}