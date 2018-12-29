package vk.travel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void goToList(View v) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
    public void goToMap(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}