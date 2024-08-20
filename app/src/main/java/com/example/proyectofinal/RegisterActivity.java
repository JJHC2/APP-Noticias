package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyectofinal.ConnectionBD.ConnectionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class RegisterActivity extends AppCompatActivity {

    EditText nomapellidos, email, telefono, usuario, clave;
    Button registrar;
    TextView ingresar;

    Connection con;

    public RegisterActivity() {
        ConnectionBD instanceConnection = new ConnectionBD();
        con = instanceConnection.connect();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        nomapellidos = (EditText) findViewById(R.id.txtnomapellidos);
        email = (EditText) findViewById(R.id.txtemail);
        telefono = (EditText) findViewById(R.id.txttelefono);
        usuario = (EditText) findViewById(R.id.txtusuario);
        clave = (EditText) findViewById(R.id.txtclave);
        registrar = (Button) findViewById(R.id.btnregistrar);
        ingresar = (TextView) findViewById(R.id.lbliniciarsesion);

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrarUsuario();
            }
        });

        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
            }
        });
    }

    public void RegistrarUsuario() {
        try {
            if (con == null) {
                Toast.makeText(this, "Verifique su conexión a la BD", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener los valores de los campos
            String user = usuario.getText().toString().trim();
            String emailValue = email.getText().toString().trim();
            String telefonoValue = telefono.getText().toString().trim();
            String nomapellidosValue = nomapellidos.getText().toString().trim();
            String claveValue = clave.getText().toString().trim();

            // Validar que ningún campo esté vacío
            if (user.isEmpty() || emailValue.isEmpty() || telefonoValue.isEmpty() || nomapellidosValue.isEmpty() || claveValue.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben ser llenados", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar el formato del correo electrónico
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                Toast.makeText(this, "El correo electrónico no tiene un formato válido", Toast.LENGTH_SHORT).show();
                return;
            }

            //Validar el tamaño del usuario
            if (user.length() < 3 || user.length() > 15) {
                Toast.makeText(this, "El usuario debe tener entre 3 y 15 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }



            // Validar el número de teléfono
            if (telefonoValue.length() > 10) {
                Toast.makeText(this, "El teléfono debe tener un máximo de 10 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            //Que el telefono solo contenga digitos
            if (!telefonoValue.matches("\\d+")) {
                Toast.makeText(this, "El teléfono debe contener solo dígitos", Toast.LENGTH_SHORT).show();
                return;
            }


            // Validar la longitud de la contraseña
            if (claveValue.length() > 10) {
                Toast.makeText(this, "La clave no debe tener más de 15 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!claveValue.matches(".*[A-Z].*")) {
                Toast.makeText(this, "La clave debe tener una letra mayúscula", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!claveValue.matches(".*\\d.*")) {
                Toast.makeText(this, "La clave debe contener al menos un dígito", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!claveValue.matches(".*[!@#$%^&*()].*")) {
                Toast.makeText(this, "La clave debe tener al menos un carácter especial", Toast.LENGTH_SHORT).show();
                return;
            }


            // Verificar si el usuario ya existe
            PreparedStatement checkUserStmt = con.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE usuario = ?");
            checkUserStmt.setString(1, user);
            ResultSet rsUser = checkUserStmt.executeQuery();
            rsUser.next();
            int userCount = rsUser.getInt(1);
            rsUser.close();
            checkUserStmt.close();

            if (userCount > 0) {
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el correo ya existe
            PreparedStatement checkEmailStmt = con.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE correo = ?");
            checkEmailStmt.setString(1, emailValue);
            ResultSet rsEmail = checkEmailStmt.executeQuery();
            rsEmail.next();
            int emailCount = rsEmail.getInt(1);
            rsEmail.close();
            checkEmailStmt.close();

            if (emailCount > 0) {
                Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el teléfono ya existe
            PreparedStatement checkTelefonoStmt = con.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE telefono = ?");
            checkTelefonoStmt.setString(1, telefonoValue);
            ResultSet rsTelefono = checkTelefonoStmt.executeQuery();
            rsTelefono.next();
            int telefonoCount = rsTelefono.getInt(1);
            rsTelefono.close();
            checkTelefonoStmt.close();

            if (telefonoCount > 0) {
                Toast.makeText(this, "El teléfono ya está registrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el nombre ya existe
            PreparedStatement checkNombreStmt = con.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE nombre = ?");
            checkNombreStmt.setString(1, nomapellidosValue);
            ResultSet rsNombre = checkNombreStmt.executeQuery();
            rsNombre.next();
            int nombreCount = rsNombre.getInt(1);
            rsNombre.close();
            checkNombreStmt.close();

            if (nombreCount > 0) {
                Toast.makeText(this, "El nombre ya está registrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si ninguno de los campos está registrado, proceder a insertar
            PreparedStatement stm = con.prepareStatement("INSERT INTO USUARIO (nombre, usuario, correo, telefono, password) VALUES (?, ?, ?, ?, ?)");
            stm.setString(1, nomapellidosValue);
            stm.setString(2, user);
            stm.setString(3, emailValue);
            stm.setString(4, telefonoValue);
            stm.setString(5, claveValue);
            stm.executeUpdate();
            stm.close();

            Toast.makeText(this, "Registro Agregado con éxito", Toast.LENGTH_SHORT).show();

            // Limpiar los campos de texto
            nomapellidos.setText("");
            usuario.setText("");
            email.setText("");
            telefono.setText("");
            clave.setText("");
        } catch (Exception e) {
            Log.e("Error en la conexión a la BD", e.getMessage());
        }
    }
}