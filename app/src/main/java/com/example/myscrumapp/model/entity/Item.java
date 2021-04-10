package com.example.myscrumapp.model.entity;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model for Items in SelectionSpinner and  MultiSelectionSpinner View classes
 */
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Object obj;
    private String name;
    private Boolean value;

    @NonNull
    @Override
    public String toString(){
        return name;
    }
}
