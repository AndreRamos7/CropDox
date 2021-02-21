package com.cropdox.objetos;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Quadrilatero {
    private static final String GENIAL = "Quadrilatero";
    public static int quant_point;
    private Ponto[] pontos;
    private Ponto ponto_movel;
    public static Mat frame;

    public Quadrilatero(Mat frame) {
        Quadrilatero.frame = frame;
        Quadrilatero.quant_point = 0;
        pontos = new Ponto[4];
    }

    public void addPonto(Ponto ponto){
        if(Quadrilatero.quant_point <= 3) {
            pontos[Quadrilatero.quant_point] = ponto;
            Quadrilatero.quant_point++;
            Log.v(GENIAL, "Quadrilatero.quant_point = " + Quadrilatero.quant_point);
        }
    }

    public Ponto[] getPontos() {
        return pontos;
    }

    public Quadrilatero setPontos(Ponto[] pontos) {
        this.pontos = pontos;
        return this;
    }

    public Ponto getPonto_movel() {
        return ponto_movel;
    }

    public Quadrilatero setPonto_movel(Ponto ponto_movel) {
        this.ponto_movel = ponto_movel;
        return this;
    }

    public Ponto getPonto(int i) {
        return pontos[i];
    }
}
