package upstream.matchtile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import upstream.validation.ValidNumber;

@Schema(description = "Request body to place a match tile on the board")
public record PlaceTileRequest(
    @NotNull(message = "Coordinate x is mandatory")
    @ValidNumber(min = 0, max = 2)
    @Schema(description = "Column coordinate on grid (0 to 2)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer x,

    @NotNull(message = "Coordinate y is mandatory")
    @ValidNumber(min = 0, max = 5)
    @Schema(description = "Row coordinate on grid (0 to 5)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer y,

    @ValidNumber(min = 0, max = 5)
    @Schema(description = "Tile orientation (0 to 5 for hexagon rotation)", example = "0")
    Integer orientation
) {}
