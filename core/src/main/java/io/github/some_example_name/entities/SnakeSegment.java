package io.github.some_example_name.entities;

/**
  Representa un segmento individual de la serpiente dentro de la cuadrícula del juego.
 */
public class SnakeSegment {

    /**
      Tipo de segmento que compone el cuerpo de la serpiente.
     */
    public enum Type {
        HEAD,
        BODY,
        TAIL
    }

    private int gridX;
    private int gridY;
    private Type type;
    private Direction direction;

    /**
     * Constructor para un segmento de serpiente.
     
         Posición X en casillas dentro de la cuadrícula.
         Posición Y en casillas dentro de la cuadrícula.
      	 type      Tipo de segmento (cabeza, cuerpo o cola).
      	 direction Dirección hacia la que está orientado el segmento.
     */
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
