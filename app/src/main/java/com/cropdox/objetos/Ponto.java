package com.cropdox.objetos;

public class Ponto {
    private int x;
    private int y;

    public Ponto(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public Ponto setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public Ponto setY(int y) {
        this.y = y;
        return this;
    }
}
