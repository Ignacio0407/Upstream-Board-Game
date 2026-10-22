package upstream.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import upstream.validation.SafeText;

@Schema(description = "Request body to create a new match")
public record CreateMatchRequest(
    @NotBlank(message = "Match name is mandatory")
    @Size(min = 3, max = 50, message = "Match name must be between 3 and 50 characters")
    @SafeText
    @Schema(description = "Name of the match", example = "Salmon Run 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(description = "Optional password for private matches", example = "secret123")
    String password
) {
    public CreateMatchRequest {
        name = name != null ? name.trim() : null;
        password = password != null ? password.trim() : "";
    }
}
