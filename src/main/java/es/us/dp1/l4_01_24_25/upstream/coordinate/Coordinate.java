package es.us.dp1.l4_01_24_25.upstream.coordinate;

import jakarta.persistence.Embeddable;

@Embeddable
public record Coordinate(Integer x, Integer y) {}