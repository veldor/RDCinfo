package net.veldor.rdc_info.subclasses;

import java.util.ArrayList;
import java.util.List;

public class PriceInfo {
    // класс, ображающий информацию о ценах
    public ArrayList<Execution> executions = new ArrayList<>();
    public ArrayList<Contrast> contrasts;
    public ArrayList<Anesthesia> anaesthesia;
    public ArrayList<Complex> complexes;

    public static String[] discountSizes = new String[]{"Без скидки", "5%", "10%", "15%"};
}
