package ru.bioresourceslab;

import java.awt.print.*;

public abstract class AbstractLabelRender implements Printable{
    private final static int MATERIAL_STANDARD = 0;
    private final static int MATERIAL_BENIGN = 1;
    private final static int MATERIAL_SARCOMA = 2;
    private final static int MATERIAL_CARCINOMA = 3;
    private final static int MATERIAL_P = 4;
    private final static int MATERIAL_AUTOPSY = 5;

    private final static int NORMA = 1;
    private final static int TUMOR = 2;
    private final static int METASTASIS = 3;
    private final static int LYMPHNODE = 4;
    private final static int LYMPHNODE_METASTASIS = 5;
    private final static int RECURRENT = 6;

    final private String label;

    public AbstractLabelRender(String label) {
        this.label = label;
    }

    public AbstractLabelRender(String code, int prefix, int type, int number) {
        // T3, T5, T6 - всегда Т; T4 - если не B4
        if ((type == METASTASIS) || ((type == LYMPHNODE) && (prefix != MATERIAL_BENIGN)) || (type == LYMPHNODE_METASTASIS) || (type == RECURRENT)) {
            label = code + "T" + type + "(" + number + ")";
            return;
        }
        // если аутопсия норма, то А, остальное Т
        if (prefix == MATERIAL_AUTOPSY) {
            if (type == NORMA) {
                label = code + "A" + "(" + number + ")";
            } else {
                label = code + "T" + type + "(" + number + ")";
            }
            return;
        }
        // если норма или опухоль и не аутопсия
        if (prefix == MATERIAL_STANDARD) {
            label = code+ "T" + type + "(" + number + ")";
            return;
        }
        if (prefix == MATERIAL_BENIGN) {
            label = code + "B" + type + "(" + number + ")";
            return;
        }
        if (prefix == MATERIAL_SARCOMA) {
            label = code + "S" + type + "(" + number + ")";
            return;
        }
        if (prefix == MATERIAL_CARCINOMA) {
            label = code + "C" + type + "(" + number + ")";
            return;
        }
        if (prefix == MATERIAL_P) {
            label = code + "P" + type + "(" + number + ")";
            return;
        }
        label = "invalid label";
    }

    public String getLabel() {
        return label;
    }

}
