package io.github.some_example_name.screens;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Fonte 8x8 pixelada usada por todo o jogo.
 * O atlas é criado em memória para manter o visual pixel-art sem depender
 * de uma fonte externa ou de arquivos de sistema.
 */
public final class PixelFontFactory {

    private static final int CELL = 8;
    private static final int COLUMNS = 16;
    private static final int ROWS = 8;

    private PixelFontFactory() {
    }

    public static BitmapFont create() {
        Pixmap pixmap = new Pixmap(COLUMNS * CELL, ROWS * CELL, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        Map<Character, Integer> cells = new HashMap<>();

        int cellIndex = 0;
        for (char c = 32; c <= 126; c++) {
            cells.put(c, cellIndex++);
            drawGlyph(pixmap, cellIndex - 1, patternFor(c));
        }

        // Letras minúsculas usam as mesmas formas das maiúsculas.
        for (char c = 'a'; c <= 'z'; c++) {
            cells.put(c, cells.get(Character.toUpperCase(c)));
        }

        // Acentos comuns do português.
        char[] accented = {
                'Á','À','Â','Ã','Ä','É','Ê','Í','Ó','Ô','Õ','Ú','Ç',
                'á','à','â','ã','ä','é','ê','í','ó','ô','õ','ú','ç'
        };
        for (char c : accented) {
            cells.put(c, cellIndex);
            char base = removeAccent(c);
            String[] pattern = withAccent(patternFor(base), accentFor(c));
            drawGlyph(pixmap, cellIndex, pattern);
            cellIndex++;
        }

        // Símbolos usados nas telas.
        char[] aliases = {'—', '–', '•', '→', '←', '×'};
        char[] targets = {'-', '-', '*', '>', '<', 'x'};
        for (int i = 0; i < aliases.length; i++) {
            cells.put(aliases[i], cells.get(targets[i]));
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();

        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.lineHeight = 9f;
        data.capHeight = 8f;
        data.xHeight = 7f;
        data.ascent = 0f;
        data.descent = -2f;
        data.down = -9f;
        data.spaceXadvance = 5f;
        data.padTop = 0f;
        data.padRight = 0f;
        data.padBottom = 0f;
        data.padLeft = 0f;

        for (Map.Entry<Character, Integer> entry : cells.entrySet()) {
            char c = entry.getKey();
            int index = entry.getValue();

            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = c;
            glyph.srcX = (index % COLUMNS) * CELL;
            glyph.srcY = (index / COLUMNS) * CELL;
            glyph.width = CELL;
            glyph.height = CELL;
            glyph.xoffset = 0;
            glyph.yoffset = -CELL;
            glyph.xadvance = CELL;
            glyph.page = 0;
            glyph.fixedWidth = true;
            data.setGlyph(c, glyph);
        }

        BitmapFont font = new BitmapFont(data, new TextureRegion(texture), false);
        font.getData().setScale(1f);
        return font;
    }

    private static void drawGlyph(Pixmap pixmap, int index, String[] rows) {
        int originX = (index % COLUMNS) * CELL;
        int originY = (index / COLUMNS) * CELL;

        pixmap.setColor(1f, 1f, 1f, 1f);

        for (int y = 0; y < rows.length && y < CELL; y++) {
            String row = rows[y];
            for (int x = 0; x < row.length() && x < 5; x++) {
                if (row.charAt(x) == '1') {
                    pixmap.fillRectangle(originX + x + 1, originY + (CELL - 1 - y), 1, 1);
                }
            }
        }
    }

    private static String[] withAccent(String[] base, char accent) {
        String[] result = new String[8];
        result[0] = accent == '~' ? "00100" : "00000";
        result[1] = accent == '\'' ? "00100" : "00000";
        for (int i = 0; i < 7; i++) {
            result[i + 1] = i < base.length ? base[i] : "00000";
        }
        return result;
    }

    private static char removeAccent(char c) {
        switch (c) {
            case 'Á': case 'À': case 'Â': case 'Ã': case 'Ä': return 'A';
            case 'É': case 'Ê': return 'E';
            case 'Í': return 'I';
            case 'Ó': case 'Ô': case 'Õ': return 'O';
            case 'Ú': return 'U';
            case 'Ç': return 'C';
            case 'á': case 'à': case 'â': case 'ã': case 'ä': return 'A';
            case 'é': case 'ê': return 'E';
            case 'í': return 'I';
            case 'ó': case 'ô': case 'õ': return 'O';
            case 'ú': return 'U';
            case 'ç': return 'C';
            default: return c;
        }
    }

    private static char accentFor(char c) {
        switch (c) {
            case 'Ã': case 'ã': case 'Õ': case 'õ': return '~';
            default: return ''';
        }
    }

    private static String[] patternFor(char c) {
        switch (Character.toUpperCase(c)) {
            case 'A': return p("01110","10001","10001","11111","10001","10001","10001");
            case 'B': return p("11110","10001","10001","11110","10001","10001","11110");
            case 'C': return p("01111","10000","10000","10000","10000","10000","01111");
            case 'D': return p("11110","10001","10001","10001","10001","10001","11110");
            case 'E': return p("11111","10000","10000","11110","10000","10000","11111");
            case 'F': return p("11111","10000","10000","11110","10000","10000","10000");
            case 'G': return p("01111","10000","10000","10111","10001","10001","01111");
            case 'H': return p("10001","10001","10001","11111","10001","10001","10001");
            case 'I': return p("11111","00100","00100","00100","00100","00100","11111");
            case 'J': return p("00111","00010","00010","00010","10010","10010","01100");
            case 'K': return p("10001","10010","10100","11000","10100","10010","10001");
            case 'L': return p("10000","10000","10000","10000","10000","10000","11111");
            case 'M': return p("10001","11011","10101","10101","10001","10001","10001");
            case 'N': return p("10001","11001","10101","10011","10001","10001","10001");
            case 'O': return p("01110","10001","10001","10001","10001","10001","01110");
            case 'P': return p("11110","10001","10001","11110","10000","10000","10000");
            case 'Q': return p("01110","10001","10001","10001","10101","10010","01101");
            case 'R': return p("11110","10001","10001","11110","10100","10010","10001");
            case 'S': return p("01111","10000","10000","01110","00001","00001","11110");
            case 'T': return p("11111","00100","00100","00100","00100","00100","00100");
            case 'U': return p("10001","10001","10001","10001","10001","10001","01110");
            case 'V': return p("10001","10001","10001","10001","10001","01010","00100");
            case 'W': return p("10001","10001","10001","10101","10101","11011","10001");
            case 'X': return p("10001","10001","01010","00100","01010","10001","10001");
            case 'Y': return p("10001","10001","01010","00100","00100","00100","00100");
            case 'Z': return p("11111","00001","00010","00100","01000","10000","11111");
            case '0': return p("01110","10001","10011","10101","11001","10001","01110");
            case '1': return p("00100","01100","00100","00100","00100","00100","01110");
            case '2': return p("01110","10001","00001","00010","00100","01000","11111");
            case '3': return p("11110","00001","00001","01110","00001","00001","11110");
            case '4': return p("00010","00110","01010","10010","11111","00010","00010");
            case '5': return p("11111","10000","10000","11110","00001","00001","11110");
            case '6': return p("01110","10000","10000","11110","10001","10001","01110");
            case '7': return p("11111","00001","00010","00100","01000","01000","01000");
            case '8': return p("01110","10001","10001","01110","10001","10001","01110");
            case '9': return p("01110","10001","10001","01111","00001","00001","01110");
            case '.': return p("00000","00000","00000","00000","00000","01100","01100");
            case ',': return p("00000","00000","00000","00000","00000","01100","01000");
            case ':': return p("00000","01100","01100","00000","01100","01100","00000");
            case ';': return p("00000","01100","01100","00000","01100","01000","00000");
            case '!': return p("00100","00100","00100","00100","00100","00000","00100");
            case '?': return p("01110","10001","00001","00010","00100","00000","00100");
            case '/': return p("00001","00010","00100","01000","10000","00000","00000");
            case '\\': return p("10000","01000","00100","00010","00001","00000","00000");
            case '-': return p("00000","00000","00000","11111","00000","00000","00000");
            case '+': return p("00000","00100","00100","11111","00100","00100","00000");
            case '=': return p("00000","11111","00000","11111","00000","00000","00000");
            case '[': return p("01110","01000","01000","01000","01000","01000","01110");
            case ']': return p("01110","00010","00010","00010","00010","00010","01110");
            case '(': return p("00010","00100","01000","01000","01000","00100","00010");
            case ')': return p("01000","00100","00010","00010","00010","00100","01000");
            case '|': return p("00100","00100","00100","00100","00100","00100","00100");
            case '*': return p("00000","10101","01110","11111","01110","10101","00000");
            case '"': return p("01010","01010","00000","00000","00000","00000","00000");
            case '\'': return p("00100","00100","00000","00000","00000","00000","00000");
            case ' ': return p("00000","00000","00000","00000","00000","00000","00000");
            default: return p("11111","10001","10101","10101","10101","10001","11111");
        }
    }

    private static String[] p(String... rows) {
        return rows;
    }
}
