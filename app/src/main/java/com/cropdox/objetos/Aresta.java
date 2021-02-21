package com.cropdox.objetos;

import org.opencv.core.Point;

public class Aresta {
    private Point ponto1;
    private Point ponto2;

    public Aresta() {
    }

    public Aresta(Point ponto1, Point ponto2) {
        this.ponto1 = ponto1;
        this.ponto2 = ponto2;
    }

    public Point getPonto1() {
        return ponto1;
    }

    public Aresta setPonto1(Point ponto1) {
        this.ponto1 = ponto1;
        return this;
    }

    public Point getPonto2() {
        return ponto2;
    }

    public Aresta setPonto2(Point ponto2) {
        this.ponto2 = ponto2;
        return this;
    }
}
