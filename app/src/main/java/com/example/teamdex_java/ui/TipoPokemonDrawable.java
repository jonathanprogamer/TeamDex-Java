package com.example.teamdex_java.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Locale;

public class TipoPokemonDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF circulo = new RectF();

    private final List<String> tipos;

    private final int colorBorde = Color.rgb(21, 31, 48);

    public TipoPokemonDrawable(List<String> tipos) {
        this.tipos = tipos;
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {

        float centroX = getBounds().exactCenterX();
        float centroY = getBounds().exactCenterY();

        float radio = Math.min(
                getBounds().width(),
                getBounds().height()
        ) / 2f;

        float grosorBorde = Math.max(2f, radio * 0.10f);

        circulo.set(
                centroX - radio + grosorBorde / 2f,
                centroY - radio + grosorBorde / 2f,
                centroX + radio - grosorBorde / 2f,
                centroY + radio - grosorBorde / 2f
        );

        if (tipos == null || tipos.isEmpty()) {
            paint.setColor(Color.GRAY);
            canvas.drawOval(circulo, paint);

        } else if (tipos.size() == 1) {

            paint.setColor(obtenerColor(tipos.get(0)));
            canvas.drawOval(circulo, paint);

        } else {

            // Mitad derecha.
            paint.setColor(obtenerColor(tipos.get(0)));

            canvas.drawArc(
                    circulo,
                    -90,
                    180,
                    true,
                    paint
            );

            // Mitad izquierda.
            paint.setColor(obtenerColor(tipos.get(1)));

            canvas.drawArc(
                    circulo,
                    90,
                    180,
                    true,
                    paint
            );
        }

        // Borde exterior.
        paint.setColor(colorBorde);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(grosorBorde);

        canvas.drawOval(circulo, paint);

        paint.setStyle(Paint.Style.FILL);
    }

    private int obtenerColor(String tipo) {

        switch (tipo.toLowerCase(Locale.ROOT)) {

            case "normal":
                return Color.rgb(168, 168, 120);

            case "fire":
                return Color.rgb(240, 128, 48);

            case "water":
                return Color.rgb(104, 144, 240);

            case "grass":
                return Color.rgb(120, 200, 80);

            case "electric":
                return Color.rgb(248, 208, 48);

            case "ice":
                return Color.rgb(152, 216, 216);

            case "fighting":
                return Color.rgb(192, 48, 40);

            case "poison":
                return Color.rgb(160, 64, 160);

            case "ground":
                return Color.rgb(224, 192, 104);

            case "flying":
                return Color.rgb(168, 144, 240);

            case "psychic":
                return Color.rgb(248, 88, 136);

            case "bug":
                return Color.rgb(168, 184, 32);

            case "rock":
                return Color.rgb(184, 160, 56);

            case "ghost":
                return Color.rgb(112, 88, 152);

            case "dragon":
                return Color.rgb(112, 56, 248);

            case "dark":
                return Color.rgb(112, 88, 72);

            case "steel":
                return Color.rgb(184, 184, 208);

            case "fairy":
                return Color.rgb(238, 153, 172);

            default:
                return Color.GRAY;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
