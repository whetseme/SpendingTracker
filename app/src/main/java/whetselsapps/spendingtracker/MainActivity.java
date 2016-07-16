package whetselsapps.spendingtracker;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String  toDoItem = "To Do List",
                                balanceItem = "Balances",
                                expenditureListItem  = "View Expenditures";
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listItems.add(toDoItem);
        listItems.add(balanceItem);
        listItems.add(expenditureListItem);
        this.listeners();
    }

    private void listeners() {
        ListView listView = (ListView) findViewById(R.id.listView);

        this.adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, listItems);

        listView.setAdapter(this.adapter);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                        String item = String.valueOf(parent.getItemAtPosition(position));
                        Class c = null;

                        switch (item) {
                            case toDoItem:
                                c = ToDoActivity.class;
                                break;
                            case balanceItem:
                                c = BalanceActivity.class;
                                break;
                        }

                        startActivity(new Intent(MainActivity.this, c));
                    }
                }
        );

        ImageButton fab = (ImageButton) findViewById(R.id.myFAB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewExpenditureActivity.class);
                startActivity(intent);
            }
        });
    }
}
