package ru.garretech.garred.doramatv.tools;

import android.arch.persistence.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class ListConverter {
    @TypeConverter
    public String fromList(List<String> list) {
        return list.toString();
    }

    @TypeConverter
    public List<String> toList(String listAsString) {
        return Arrays.asList(listAsString.split("\\s*,\\s*"));
    }

}
