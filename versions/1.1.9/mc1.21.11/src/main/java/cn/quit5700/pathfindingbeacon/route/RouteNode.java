package cn.quit5700.pathfindingbeacon.route;

import java.util.UUID;

public record RouteNode(int number, UUID placedBy, RoutePosition position, boolean active) {
}

