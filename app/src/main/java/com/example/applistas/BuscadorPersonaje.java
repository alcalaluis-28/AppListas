package com.example.applistas;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import org.json.JSONArray;

public class BuscadorPersonaje extends AppCompatActivity {

    RequestQueue requestQueue;
    ArrayList<String> listaTransformaciones;
    final String URL = "https://dragonball-api.com/api/characters/";

    //Java
    EditText edtIdPersonaje, edtNombre, edtKi, edtRaza, edtGenero;
    Button btnBuscarPersonaje;
    Button btnReiniciar;
    Button btnTransformaciones;
    ImageView imgPersonaje;

    private void loadUi() {
        //Vinculacion
        edtIdPersonaje = findViewById(R.id.edtIdPersonaje);
        btnBuscarPersonaje = findViewById(R.id.btnBuscarPersonaje);
        btnReiniciar = findViewById(R.id.btnReiniciar);
        imgPersonaje = findViewById(R.id.imgPersonaje);
        btnTransformaciones = findViewById(R.id.btnTransformaciones);
        edtNombre = findViewById(R.id.edtNombre);
        edtKi = findViewById(R.id.edtKi);
        edtRaza = findViewById(R.id.edtRaza);
        edtGenero = findViewById(R.id.edtGenero);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscador_personaje);

        this.loadUi();

        listaTransformaciones = new ArrayList<>();

        //Event
        btnBuscarPersonaje.setOnClickListener(view -> {
            getDataCharacter();
        });
        btnReiniciar.setOnClickListener(view -> {
            clearUI();
        });
        btnTransformaciones.setOnClickListener(view -> {
            showTransformations();
        });

    } //Oncreate
    private void clearUI(){
        edtIdPersonaje.setText("");
        edtNombre.setText("");
        edtKi.setText("");
        edtRaza.setText("");
        edtGenero.setText("");
        imgPersonaje.setImageDrawable(null);

        listaTransformaciones.clear();
        btnTransformaciones.setEnabled(false);
    }
    private void showTransformations(){
        if(listaTransformaciones.isEmpty()){
            Toast.makeText(
                    getApplicationContext(),
                    "No tiene transformaciones",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        StringBuilder transformations = new StringBuilder();
        for(String transformation :listaTransformaciones){
            transformations.append(transformation)
                    .append("\n");
        }
        Toast.makeText(
                getApplicationContext(),
                transformations.toString(),
                Toast.LENGTH_LONG
        ).show();
    }

    private void getDataCharacter() {
        //Comunicacion con la API de Dragon Ball
        if (edtIdPersonaje.getText().toString().isEmpty()) {
            edtIdPersonaje.setError("Escriba un ID");
            edtIdPersonaje.requestFocus();
            return;
        }

        String endPoint = URL + edtIdPersonaje.getText().toString(); //Se agrega el ID

        //Abrir canal de comunicacion
        requestQueue = Volley.newRequestQueue(this);

        //¿Que tipo de dato me devuelve el API?
        //Volley las solicitudes tienen 5 partes:
        //Verbo, URL, JSONEnviado, Resultado (JsonObject), Error
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                endPoint,
                null,
                this::showData,
                this::errorWS
        );

        //Enviamos la solicitud
        requestQueue.add(jsonObjectRequest);
    }

    //this::showData() se activa cuando el servicio retorna 2XX
    private void showData(JSONObject jsonObject) {
        Log.d("ResultadosWS", jsonObject.toString());

        //Java puede gestionar Json solo en entornos seguros
        try {
            edtNombre.setText(jsonObject.getString("name"));
            edtRaza.setText(jsonObject.getString("race"));
            edtGenero.setText(jsonObject.getString("gender"));
            edtKi.setText(jsonObject.getString("ki"));
            //Obtener imagen del personaje
            String urlImagen = jsonObject.getString("image");

            //Cargar imagen en ImageView
            Glide.with(this)
                    .load(urlImagen)
                    .into(imgPersonaje);

            //Leer transformaciones
            JSONArray transformations = jsonObject.getJSONArray("transformations");
            listaTransformaciones.clear();
            for (int i = 0; i < transformations.length(); i++){
                JSONObject transformation = transformations.getJSONObject(i);
                listaTransformaciones.add(
                        transformation.getString("name")
                );
            }
            if(listaTransformaciones.size() > 0) {
                btnTransformaciones.setEnabled(true);
            }else{
                btnTransformaciones.setEnabled(false);
            }

        } catch (Exception e) {
            Log.e("Error Json", e.toString());
        }
    }

    private void errorWS(VolleyError e) {
        //Log.e("ErrorWS", e.toString());

        //Para gestionar errores, necesitamos un objeto
        NetworkResponse response = e.networkResponse;

        //Si existe una respuesta (existe error)
        if (response != null && response.data != null) {
            //¿Cual es el codigo de error?
            int statuscode = response.statusCode;

            //No lo encontramos
            if (statuscode == 400) {
                String dataError = new String(response.data);
                try {
                    JSONObject jsonError = new JSONObject(dataError);
                    Toast.makeText(getApplicationContext(), jsonError.getString("message"), Toast.LENGTH_SHORT).show();
                    //Limpia la pantalla cuando el personaje no existe
                    clearUI();

                    Log.e("ErrorWS", dataError);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

} //BuscadorPersonaje