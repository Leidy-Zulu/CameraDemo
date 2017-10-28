package com.app.camerademo;

/**
 * Created by leidyzulu on 26/10/17.
 */


public enum TipoAdjunto {
    Ninguno (0, "Ninguno"),
    Gallery (1, "Gallery"),
    Camera (2, "Camera"),
    Audio (3, "Audio");

    private final int tipo;
    private final String tipoAdjunto;

    TipoAdjunto(int tipo, String tipoAdjunto) {
        this.tipo = tipo;
        this.tipoAdjunto = tipoAdjunto;
    }
}