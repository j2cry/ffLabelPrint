package ru.bioresourceslab;

import java.awt.*;
import java.awt.print.*;

public class LabelRender extends AbstractLabelRender {
    private final Font font;

//    public LabelRender(String code) {
//        super(code);
//    }

    public LabelRender(String code, Font font) {
        super(code);
        this.font = font;
    }

// зачем такой петух? много однотипных аргументов!!!
//    public LabelRender(String code, int prefix, int type, int number) {
//        super(code, prefix, type, number);
//    }

    // а это просто рукалицо...
    public LabelRender(String code, int prefix, int type, int number, Font font) {
        super(code, prefix, type, number);
        this.font = font;
    }

//    public void setFont(Font font) {
//        this.font = font;
//    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g2d.setFont(font);

        int stringWidth = g2d.getFontMetrics().stringWidth(this.getLabel());
        int stringHeight = g2d.getFontMetrics().getHeight();

        // центруем изображение
        // это работает
        // при настройке Reverse printing direction = true
        // rotate = false
        int x = (int)((pf.getImageableWidth() / 2) - (stringWidth / 2));
        int y = (int)((pf.getImageableHeight() / 2) + stringHeight / 2);

        g2d.drawString(this.getLabel(), x, y);

        return PAGE_EXISTS;
    }



}
