package com.example.xingzhang.lecturefinder.Mapping;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by xingzhang on 26/08/2015.
 */
public class roomLocation {

    public String roomCode (String singleEvent){
        String[] words = singleEvent.split(" ");
        String code = null;
        for (int i = 0; i < words.length; i++){
            String s = words [i];
            if (s.contains("Room")){
                code = words[i+1];
            }
        }
        return code;
    }

    public LatLng codeLocation (String room){
        LatLng roomLocation = null;
        /*
            should be increased by having more room location
        */
        if (room.contains("MC3205")) {
            roomLocation = new LatLng(53.226624, -0.543874);
        }
        if (room.contains("MB1006")){
            roomLocation = new LatLng(53.228836, -0.538807);
        }
        if (room.contains("MT007")){
            roomLocation = new LatLng(53.226624, -0.543874);
        }
        if (room.contains("BL0101")){
            roomLocation = new LatLng(53.226556, -0.543826);
        }
        if (room.contains("BL1101")){
            roomLocation = new LatLng(53.226624, -0.543874);
        }
        if (room.contains("BL2102")){
            roomLocation = new LatLng(53.226556, -0.543826);
        }
        if (room.contains("BL1107")){
            roomLocation = new LatLng(53.226556, -0.543826);
        }
        return roomLocation;
    }
}
