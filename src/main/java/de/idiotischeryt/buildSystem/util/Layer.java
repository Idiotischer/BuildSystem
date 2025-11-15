package de.idiotischeryt.buildSystem.util;

import org.bukkit.Material;

public class Layer {
    final Material material;
    int rowPos;
    final int number;

    public Layer(Material material, int rowPos, int number) {
        this.material = material;
        this.rowPos = rowPos;
        this.number = number;
    }

    public void setRowPos(int pos) {
        this.rowPos = pos;
    }

    public Material getMaterial() {
        return material;
    }

    public int getRowPos() {
        return rowPos;
    }

    public int getNumber() {
        return number;
    }
}
