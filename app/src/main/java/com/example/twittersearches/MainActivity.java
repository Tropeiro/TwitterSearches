package com.example.twittersearches;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private EditText editPesquisa, editTag;
    private SharedPreferences preferenciasSalvas;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;
    private static final String SEARCHES = "searches";
    private ImageButton btnSalva;
    private ListView listaConsulta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPesquisa = findViewById(R.id.editPesquisa);
        editTag = findViewById(R.id.editTag);
        btnSalva = findViewById(R.id.btnSalva);
        listaConsulta = findViewById(R.id.listaConsulta);

        preferenciasSalvas = getSharedPreferences(SEARCHES, MODE_PRIVATE);
        tags = new ArrayList<String>(preferenciasSalvas.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, tags);
        listaConsulta.setAdapter(adapter);


        btnSalva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editPesquisa.getText().toString().isEmpty() && !editTag.getText().toString().isEmpty()){
                    String pesquisa = editPesquisa.getText().toString();
                    String tag = editTag.getText().toString();
                    adicionarConsulta(pesquisa, tag);
                    editPesquisa.getText().clear();
                    editTag.getText().clear();

                    //Ocultar teclado
                    ((InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editTag.getWindowToken(), 0);

                } else {
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //clique Curto
        listaConsulta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tag = ((TextView) view).getText().toString();
                String urlString = getString(R.string.searchURL) + Uri.encode(preferenciasSalvas.getString(tag, ""), "UTF-8");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                startActivity(webIntent);
            }
        });
        //clique Longo

        listaConsulta.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String tag = ((TextView) view).getText().toString();
                AlertDialog.Builder jan = new AlertDialog.Builder(MainActivity.this);
                jan.setTitle(R.string.app_name);

                jan.setItems(R.array.dialog_items, new DialogInterface.OnClickListener(){
                   public void onClick(DialogInterface dialogInterface, int i){
                       switch(i){
                           case 0:
                               compartilharBusca(tag);
                               break;
                           case 1:
                               editTag.setText(tag);
                               break;
                           case 2:
                               removerBusca(tag);
                               break;
                       }
                   }
                });
                jan.setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialogInterface, int i){
                        dialogInterface.cancel();
                    }
                });

                jan.create().show();
                return true;
            }
        });

        }

    public void adicionarConsulta(String query, String tag){
        SharedPreferences.Editor editor = preferenciasSalvas.edit();
        editor.putString(tag, query);
        editor.apply();
        if(!tags.contains(tag)){
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }
    }

    public void compartilharBusca(String tag){
        String urlString = getString(R.string.searchURL) + Uri.encode(preferenciasSalvas.getString(tag, ""));
        Intent sharerIntent = new Intent();
        sharerIntent.setAction(Intent.ACTION_SEND);
        sharerIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
        sharerIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, urlString));
        sharerIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharerIntent, getString(R.string.shareSearch)));
    }

    public void removerBusca(String tag){
        AlertDialog.Builder jan2 = new AlertDialog.Builder(this);
        jan2.setMessage(getString(R.string.confirmMessage, tag));
        jan2.setNegativeButton("Não", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i){
                tags.remove(tag);
                SharedPreferences.Editor preferencesEditor = preferenciasSalvas.edit();
                preferencesEditor.remove(tag);
                preferencesEditor.apply();
                adapter.notifyDataSetChanged();
            }
        });
        jan2.create().show();
    }
}