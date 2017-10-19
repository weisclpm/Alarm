package com.example.weisc.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.weisc.alarm.R;

/**
 * Created by weisc on 17-10-16.
 */

public class RepeatSettings extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String[] items = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String REPEAT_DATE = "REPEAT_DATE";
    private int checked;

    private ListView listView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_listview);
        Intent intent=getIntent();
        checked = intent.getIntExtra(REPEAT_DATE, 0);

        listView = (ListView) findViewById(R.id.baseList);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items));
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //勾选重复日期
        int checked_copy = checked;
        int n = 0;
        while (checked_copy != 0 && n < 7) {
            if ((checked_copy & 0x01) != 0) {
                listView.setItemChecked(n, true);
            }
            checked_copy >>= 1;
            n++;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int n = 0x01 << position;
        if (listView.isItemChecked(position)) {
            checked = checked | n;
        } else if ((checked & n) != 0) {
            checked = checked & (~n & 0x7F);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuConfirm:
                Intent intent = new Intent();
                intent.putExtra(REPEAT_DATE, checked);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }
}
