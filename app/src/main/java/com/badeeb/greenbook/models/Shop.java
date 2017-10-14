package com.badeeb.greenbook.models;

import java.util.ArrayList;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class Shop {

    private int id;
    private String name;
    private String description;
    private int rate;
    private ArrayList<WorkingDay> workingDays;
    private ShopLocation location;
    private String phoneNumber;
    private String mainPhotoURL;
    private ArrayList<String> photos;
}
