package upstream.matchtile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to rotate a match tile")
public record RotateMatchTileRequest(
    @NotNull(message = "Rotation is mandatory")
    @Schema(description = "Target rotation value", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer rotation
) {}
