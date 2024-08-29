package com.example.zhixuanlai.ruler;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements RulerView.MoveDistanceCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //支持转动180
        RulerView rulerView = (RulerView) findViewById(R.id.ruler_view);
        rulerView.setRotation(180F);
        rulerView.setMoveDistanceCallBack(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            RulerView rulerView = (RulerView) findViewById(R.id.ruler_view);
            if (rulerView.getUnitType() == RulerView.Unit.INCH) {
                rulerView.setUnitType(RulerView.Unit.CM);
            } else {
                rulerView.setUnitType(RulerView.Unit.INCH);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void distanceCallBack(String distance) {
        TextView tvValue = (TextView) findViewById(R.id.tv_mine_title);
        tvValue.setText(distance);
    }
}
