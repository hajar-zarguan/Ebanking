package com.example.onording;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Activity2 extends AppCompatActivity {
    Customer[] CustomerArray;
    ArrayList<Customer> RealCustomers=new ArrayList<>();
    Button btn;
    EditText editId;
    EditText editfirst;
    EditText editlast;
    EditText editemail;
    ListView listCustomer;
    ArrayList<String> CustomerData=null;
    ArrayAdapter<String> adapter=null;
    String newId =null;
    String newfirst =null;
    String newlast =null;
    String newemail =null;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = "http://192.168.43.166:8085/customers";
        GetAllCustomers();
        listCustomer=findViewById(R.id.listTasks);
        btn=findViewById(R.id.addButton);
        editId=findViewById(R.id.editId);
        editfirst = findViewById( R.id.editfirstName);
        editlast=findViewById(R.id.editlastName);
        editemail = findViewById( R.id.editemail);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newId=editId.getText().toString();
                newfirst=editfirst.getText().toString();
                newlast=editlast.getText().toString();
                newemail=editemail.getText().toString();
                InsertCustomer(newId);
                GetAllCustomers();
            }
        });


        listCustomer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String t=  parent.getAdapter().getItem(position).toString();
                Customer customer = RealCustomers.get(position);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Modifier nom");
                alert.setMessage("Modifier les information de client");
                final EditText input = new EditText(MainActivity.this);
                input.setText(customer.getId());



                alert.setView(input);
                alert.setPositiveButton("d'accord", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        customer.setId(value);
                        UpdateCustomer(customer,position);
                    }
                });
                alert.setNegativeButton("annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alert.show();
            }
        });


        listCustomer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Customer customer = RealCustomers.get(i);
                        DeleteCustomer(customer,i);
                    }
                });
                builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("confirmation");
                alertDialog.setMessage(" Etes vous sur de supprimer ce client" );
                alertDialog.show();

                return true;
            }
        });
    }
    public void GetAllCustomers(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {

            @Override
            public void run() {
                //do in background
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Gson gson=new Gson();
                                CustomerArray=gson.fromJson(response.toString(), Customer[].class);
                                CustomerData=new ArrayList<>();
                                for(Customer customer:CustomerArray)
                                {
                                    RealCustomers.add(customer);
                                    System.out.println(customer.toString());
                                    CustomerData.add(customer.getId() + ": "+customer.getFirstName()+" "+customer.getLastName()+"   |  "+customer.getEmail());
                                }
                                adapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1,CustomerData);
                                listCustomer.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("error response",error.toString());
                            }
                        }

                );

                requestQueue.add(jsonObjectRequest);
            }
        });}


    public void InsertCustomer(String newTask){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {

                //do in background
                MainActivity.this.runOnUiThread(()->{
                    Toast.makeText(MainActivity.this, "Insertion des données",Toast.LENGTH_SHORT).show();
                });
                JSONObject jsonobject = new JSONObject();
                try {
                    jsonobject.put("Customer", newTask);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonobject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Gson gson=new Gson();
                                Customer tas=gson.fromJson(response.toString(), Customer.class);

                                RealCustomers.add(tas);
                                CustomerData.add(tas.getId());
                                adapter.notifyDataSetChanged();
                                MainActivity.this.runOnUiThread(()->{
                                    Toast.makeText(MainActivity.this,"Client est ajouté avec succes",Toast.LENGTH_LONG).show();
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("erreur",error.toString());
                            }
                        }

                );

                requestQueue.add(jsonObjectRequest);
            }
        });

    }

    public void DeleteCustomer(Customer task, int i){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {

                //do in background
                MainActivity.this.runOnUiThread(()->{
                    Toast.makeText(MainActivity.this, "Supprission des données",Toast.LENGTH_SHORT).show();
                });
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        url+"/"+task.getId(),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                CustomerData.remove(task.getId());
                                RealCustomers.remove(i);
                                adapter.notifyDataSetChanged();
                                MainActivity.this.runOnUiThread(()->{
                                    Toast.makeText(MainActivity.this,"Client supprime avec succes",Toast.LENGTH_LONG).show();
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("erreur",error.toString());
                            }
                        }

                );

                requestQueue.add(jsonObjectRequest);
            }
        });
    }
    public void UpdateCustomer(Customer task, int i){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {

                //do in background
                MainActivity.this.runOnUiThread(()->{
                    Toast.makeText(MainActivity.this, "Modification des données",Toast.LENGTH_SHORT).show();
                });
                JSONObject jsonobject = new JSONObject();
                try {
                    jsonobject.put("id", task.getId());
                    jsonobject.put("firstname", task.getFirstName());
                    jsonobject.put("lastname", task.getLastName());
                    jsonobject.put("email", task.getEmail());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.PUT,
                        url+"/"+task.getId(),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                CustomerData.remove(RealCustomers.get(i).getId());
                                CustomerData.add(task.getId());
                                RealCustomers.remove(i);
                                adapter.notifyDataSetChanged();
                                MainActivity.this.runOnUiThread(()->{
                                    Toast.makeText(MainActivity.this,"Les informations sont modifier avec succes",Toast.LENGTH_LONG).show();
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("error response",error.toString());
                            }
                        }
                );
                requestQueue.add(jsonObjectRequest);
            }
        });

    }

}





