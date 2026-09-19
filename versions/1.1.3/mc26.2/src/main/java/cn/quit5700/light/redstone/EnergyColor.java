package cn.quit5700.light.redstone;

import net.minecraft.util.StringRepresentable;

public enum EnergyColor implements StringRepresentable {
    NONE("none"), WHITE("white"), ORANGE("orange"), MAGENTA("magenta"), LIGHT_BLUE("light_blue"),
    YELLOW("yellow"), LIME("lime"), PINK("pink"), GRAY("gray"), LIGHT_GRAY("light_gray"),
    CYAN("cyan"), PURPLE("purple"), BLUE("blue"), BROWN("brown"), GREEN("green"), RED("red"), BLACK("black");

    private final String id;

    EnergyColor(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
