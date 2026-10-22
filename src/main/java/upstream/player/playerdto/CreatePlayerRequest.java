package upstream.player.playerdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import upstream.player.Color;

@Schema(description = "Request body to join a match as a player")
public record CreatePlayerRequest(
    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user joining the match", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer user,

    @NotNull(message = "Player color is required")
    @Schema(description = "Color selected by the player", example = "RED", requiredMode = Schema.RequiredMode.REQUIRED)
    Color color
) {}
