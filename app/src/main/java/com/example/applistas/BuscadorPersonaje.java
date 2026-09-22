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

public class BuscadorPersonaje extends AppCompatActivity {

    RequestQueue requestQueue;
    final String URL = "https://dragonball-api.com/api/characters/";

    //Java
    EditText edtIdPersonaje, edtNombre, edtKi, edtRaza, edtGenero;
    Button btnBuscarPersonaje;
    Button btnReiniciar;

    private void loadUi() {
        //Vinculacion
        edtIdPersonaje = findViewById(R.id.edtIdPersonaje);
        btnBuscarPersonaje = findViewById(R.id.btnBuscarPersonaje);
        btnReiniciar = findViewById(R.id.btnReiniciar);
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

        //Event
        btnBuscarPersonaje.setOnClickListener(view -> {
            getDataCharacter();
        });
        btnReiniciar.setOnClickListener(view -> {
            clearUI();
        });
    } //Oncreate
    private void clearUI(){
        edtIdPersonaje.setText("");
        edtNombre.setText("");
        edtKi.setText("");
        edtRaza.setText("");
        edtGenero.setText("");
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