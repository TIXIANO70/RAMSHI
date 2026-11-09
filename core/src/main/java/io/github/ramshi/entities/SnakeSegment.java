package io.github.ramshi.entities;


public class SnakeSegment {


    public enum Type {
        HEAD,
        BODY,
        TAIL
    }

    private int gridX;
    private int gridY;
    private Type type;
    private Direction direction;


    public SnakeSegment(int gridX, int gridY, Type type, Direction direction) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.type = type;
        this.direction = direction;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
