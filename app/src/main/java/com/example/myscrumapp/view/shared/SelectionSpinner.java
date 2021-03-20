package com.example.myscrumapp.view.shared;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import com.example.myscrumapp.R;
import com.example.myscrumapp.model.entity.Item;
import com.example.myscrumapp.model.entity.Team;
import com.example.myscrumapp.model.entity.User;
import com.example.myscrumapp.utils.GlobalConstants;

import java.util.ArrayList;
import java.util.Arrays;

public class SelectionSpinner extends AppCompatSpinner implements
        DialogInterface.OnClickListener {

    ArrayList<Item> items = null;
    boolean[] selection = null;
    public ArrayAdapter<String> adapter;

    public SelectionSpinner(Context context) {
        super(context);

        adapter = new ArrayAdapter<>(context, R.layout.custom_spinner);
        super.setAdapter(adapter);
    }

    public SelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new ArrayAdapter<>(context,
                R.layout.custom_spinner);
        super.setAdapter(adapter);
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
            builder.setItems(itemNames,this);

            builder.setPositiveButton("OK", (arg0, arg1) -> {
                // Do nothing
            });

            builder.show();
        }

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (selection != null && which < selection.length) {
            Arrays.fill(selection, false);
            selection[which] = true;
            adapter.clear();
            adapter.add(items.get(which).getName());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
        selection = new boolean[this.items.size()];
        Arrays.fill(selection, false);
    }


    public Item getSelectedItem() {
        Item selectedItem = null;

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                return items.get(i);
            }
        }
        return selectedItem;
    }

    public void setSelectedItem(String id){
        int selectedIndex = -1;

        if(items != null && !id.equals(GlobalConstants.NON_EXISTENT_ID)) {
            Arrays.fill(selection, false);
            for (int i = 0; i < items.size(); i++) {
                if(items.get(i).getObj() instanceof Team){
                    if(((Team) items.get(i).getObj()).getTeamId().equals(id)){
                        selection[i] = true;
                        selectedIndex = i;
                    }
                }

                if(items.get(i).getObj() instanceof User){
                    if(((User) items.get(i).getObj()).userId.equals(id)){
                        selection[i] = true;
                        selectedIndex = i;
                    }
                }

            }
        }
        adapter.clear();
        if(selectedIndex > -1)
            adapter.add(items.get(selectedIndex).getName());
        else {
            if (selection != null) Arrays.fill(selection, false);
            adapter.add(" ");
        }


    }

    public void setSelectedItem(int status){
        Arrays.fill(selection, false);
        int selectedIndex = -1;

        if(items != null && status != GlobalConstants.INVALID_STATUS) {
            for (int i = 0; i < items.size(); i++) {
                if(((Integer) items.get(i).getObj()) == status){
                    selection[i] = true;
                    selectedIndex = i;
                }
            }

            }

        adapter.clear();
        if(selectedIndex > -1)
            adapter.add(items.get(selectedIndex).getName());
        else
            adapter.add(" ");

    }

}