package an.xuan.tong.historycontact.call.receiver;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import an.xuan.tong.historycontact.R;

public class MainActivity extends AppCompatActivity {
    private Boolean onService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchService(view);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    private void switchService(View view) {
        if (onService) offService(view);
        else onService(view);
    }

    private void onService(View view) {
        Intent phoneCall = new Intent(getApplicationContext(), CallRecService.class);
        phoneCall.putExtra(ProcessingBase.IntentKey.INSTANCE.getPHONE_NUMBER(), "+79202162032");
        phoneCall.putExtra(ProcessingBase.IntentKey.INSTANCE.getTYPE_CALL(), ProcessingBase.TypeCall.INSTANCE.getINC());
        startService(phoneCall);
        Snackbar.make(view, "CallRecService started", Snackbar.LENGTH_SHORT).show();
        onService = true;
    }

    private void offService(View view) {
        stopService(new Intent(getApplicationContext(), CallRecService.class));
        Snackbar.make(view, "CallRecService stopped", Snackbar.LENGTH_SHORT).show();
        onService = false;
    }
}
