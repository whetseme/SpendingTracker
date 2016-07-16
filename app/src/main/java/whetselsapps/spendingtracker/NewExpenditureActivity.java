package whetselsapps.spendingtracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * Created by Mikey on 6/3/2016.
 */
public class NewExpenditureActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expenditure);
        this.listener();
    }

    private void listener() {
        final DbConnector dbc =
                new DbConnector(this.getApplicationContext());
        final Spinner spinner = (Spinner) findViewById(R.id.act_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, dbc.getAllAccounts());
        spinner.setAdapter(adapter);

        Button submit = (Button) findViewById(R.id.new_ex_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = spinner.getSelectedItem().toString();

                dbc.setActId(account);

                EditText amount = (EditText) findViewById(R.id.amnt_entry);
                double amntEntry =
                        Double.parseDouble(amount.getText().toString());

                EditText description =
                        (EditText) findViewById(R.id.desc_entry);
                String descEntry = description.getText().toString();

                dbc.addNewTransaction(amntEntry, descEntry);
            }
        });
    }
}
