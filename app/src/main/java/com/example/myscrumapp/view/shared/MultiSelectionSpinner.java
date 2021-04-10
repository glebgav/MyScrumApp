package com.example.myscrumapp.view.shared;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;

import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.utils.GlobalConstants;

import java.util.ArrayList;
import java.util.Arrays;
/**
 *  Custom implementation for an  AppCompatSpinner as a multi selection spinner
 */
public class MultiSelectionSpinner extends AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

    ArrayList<Item> items = null;
    boolean[] selection = null;
    ArrayAdapter<String> adapter;

    public MultiSelectionSpinner(Context context) {
        super(context);

        adapter = new ArrayAdapter<>(context, R.layout.custom_spinner);
        super.setAdapter(adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new ArrayAdapter<>(context,
                R.layout.custom_spinner);
        super.setAdapter(adapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;

            adapter.clear();
            adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String[] itemNames;
        if(items != null) {
            itemNames = new String[items.size()];

            for (int i = 0; i < items.size(); i++) {
                itemNames[i] = items.get(i).getName();
            }


            builder.setMultiChoiceItems(itemNames, selection, this);

            builder.setPositiveButton("OK", (arg0, arg1) -> {
                // Do nothing
            });

            builder.show();
        }

        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
        selection = new boolean[this.items.size()];
        adapter.clear();
        adapter.add("");
        Arrays.fill(selection, false);
    }

    public void resetSelection() {
        if (items != null) {
            Arrays.fill(this.selection, false);
            adapter.clear();
            adapter.add(buildSelectedItemString());
        }
    }


    public void setSelection(ArrayList<Item> mySelection) {

        if(items != null){
            Arrays.fill(this.selection, false);
            for (Item sel : mySelection) {
                for (int j = 0; j < items.size(); ++j) {
                    if (items.get(j).getName().equals(sel.getName())) {
                        this.selection[j] = true;
                    }
                }
            }
            adapter.clear();
            adapter.add(buildSelectedItemString());
        }
    }


    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }

                foundOne = true;

                sb.append(items.get(i).getName());
            }
        }

        return sb.toString();
    }

    public ArrayList<Item> getSelectedItems() {
        ArrayList<Item> selectedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                selectedItems.add(items.get(i));
            }
        }

        return selectedItems;
    }
}