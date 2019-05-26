package net.veldor.rdc_info.subclasses;

import java.util.HashMap;

public class Execution {

    public static final String ATTR_NAME = "name";
    public static final String ATTR_PRICE = "price";
    public static final String ATTR_ID = "id";
    public static final int TYPE_SIMPLE = 1;
    public static final int TYPE_COMPLEX = 2;

    public int id;
    public String name;
    public String price;
    public String summ;
    public String summWithDiscount;
    public int type;
    public HashMap<String, Execution> innerExecutions;
}
