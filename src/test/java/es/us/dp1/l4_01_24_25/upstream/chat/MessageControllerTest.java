package es.us.dp1.l4_01_24_25.upstream.chat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import es.us.dp1.l4_01_24_25.upstream.match.Match;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @ParameterizedTest
    @CsvSource({
        "1, '[{\"playerId\":1,\"matchId\":1,\"content\":\"Hi\"}]'",
        "2, '[{\"playerId\":1,\"matchId\":2,\"content\":\"Hi\"},{\"playerId\":2,\"matchId\":2,\"content\":\"Hey\"}]'"
    })
    @DisplayName("findMatchMessages - Should return messages for a match")
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

        when(messageService.findMatchMessages(matchId)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/messages/match/" + matchId))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    @DisplayName("findUserMessages - Should return messages for a user")
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
    @DisplayName("findUserChats - Should return chats for a user")
    void testFindUserChats() throws Exception {
        Match match = new Match();
        match.setId(1);
        match.setPassword("abc");

        when(messageService.findUserChats(1)).thenReturn(List.of(match));

        mockMvc.perform(get("/api/v1/messages/chats/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("saveAndSend - Should delegate to messageService.saveAsDTO")
    void testSaveAndSend() {
        MessageDTO dto = new MessageDTO();
        dto.setPlayerId(1);
        dto.setMatchId(1);
        dto.setContent("Hello World");

        MessageController controller = new MessageController(messageService);
        controller.saveAndSend(dto);

        verify(messageService).saveAsDTO(dto);
    }
}

