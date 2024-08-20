package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinal.ConnectionBD.ConnectionBD;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class LoginActivity extends AppCompatActivity {

    // Declaramos las variables a ocupar
    EditText usuario, clave;
    TextView lblregistrar;
    Button btningresar;
    Connection con;

    // Crear clase para llamar la conexión
    public LoginActivity() {
        ConnectionBD instanceConnection = new ConnectionBD();
        con = instanceConnection.connect();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        // Llamar las variables
        usuario = (EditText) findViewById(R.id.txtusuario);
        clave = (EditText) findViewById(R.id.txtclave);
        lblregistrar = (TextView) findViewById(R.id.lblregistrar);
        btningresar = (Button) findViewById(R.id.btningresar);

        btningresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtener los valores de los campos de texto
                String userText = usuario.getText().toString().trim();
                String claveText = clave.getText().toString().trim();

                // Validar que los campos no estén vacíos
                if (userText.isEmpty() || claveText.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Si los campos están llenos, ejecutar la tarea de inicio de sesión
                new LoginTask().execute(userText, claveText);
            }
        });

        lblregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg);
            }
        });
    }

    // Lógica para la consulta a la BD
    public class LoginTask extends AsyncTask<String, String, String> {
        String z = null;
        Boolean exito = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            String user = strings[0];
            String password = strings[1];

            if (con == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Verifique su conexión", Toast.LENGTH_SHORT).show();
                    }
                });
                z = "En conexión";
            } else {
                try {
                    String sql = "SELECT * FROM USUARIO WHERE usuario = '" + user + "' AND password = '" + password + "'";
                    Statement stm = con.createStatement();
                    ResultSet rs = stm.executeQuery(sql);


                    if (rs.next()) {
                        String userId = rs.getString("id");

                        SharedPreferences sharedPref = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Acceso Exitoso", Toast.LENGTH_SHORT).show();
                                Intent menu = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(menu);
                            }
                        });
                        usuario.setText("");
                        clave.setText("");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "No existe el usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        });
                        usuario.setText("");
                        clave.setText("");
                    }
                } catch (Exception e) {
                    exito = false;
                    Log.e("Error de conexión: ", e.getMessage());
                }
            }

            return z;
        }
    }
}
