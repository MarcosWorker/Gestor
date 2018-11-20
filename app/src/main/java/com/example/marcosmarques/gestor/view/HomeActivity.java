package com.example.marcosmarques.gestor.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.marcosmarques.gestor.R;
import com.example.marcosmarques.gestor.model.Divida;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance().getReference("mes");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_divida:
                addDivida();
                return true;
            case R.id.add_cartao:
                addCartao();
                return true;
            case R.id.del_divida:
                delDivida();
                return true;
            case R.id.del_cartao:
                delCartao();
                return true;
            case R.id.sair:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    private void addDivida() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_add_divida);

        final EditText textDivida = dialog.findViewById(R.id.edt_divida);
        final EditText textLocal = dialog.findViewById(R.id.edt_local);
        final EditText textvalor = dialog.findViewById(R.id.edt_valor);
        final DatePicker data = dialog.findViewById(R.id.data);
        final EditText textParcelas = dialog.findViewById(R.id.edt_parcelas);
        final CheckBox checkCartao = dialog.findViewById(R.id.check_cartao);
        final Spinner spinnerCartao = dialog.findViewById(R.id.spinner_cartao);


        Button salvarDivida = (Button) dialog.findViewById(R.id.salvar_divida);
        salvarDivida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Divida divida = new Divida();
                divida.setLocal(textLocal.getText().toString());
                divida.setTitulo(textDivida.getText().toString());
                divida.setValor(Double.valueOf(textvalor.getText().toString()));
                divida.setData(data.getDayOfMonth() + "/" + data.getMonth() + 1 + "/" + data.getYear());
                divida.setCartao(checkCartao.isChecked());


                dialog.dismiss();
            }
        });

        Button cancelarDivida = (Button) dialog.findViewById(R.id.cancelar_divida);
        cancelarDivida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void addCartao() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_add_cartao);

        EditText textCartao = (EditText) dialog.findViewById(R.id.edt_cartao);
        DatePicker vencimento = (DatePicker) dialog.findViewById(R.id.vencimento);
        EditText textLimite = (EditText) dialog.findViewById(R.id.edt_limite);

        Button salvarCartao = (Button) dialog.findViewById(R.id.salvar_cartao);
        salvarCartao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        Button cancelarCartao = (Button) dialog.findViewById(R.id.cancelar_cartao);
        cancelarCartao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void delDivida() {

    }

    private void delCartao() {

    }

    private boolean verificaDivida(Divida divida) {
        if (divida.getTitulo() == null || divida.getTitulo().equals("")) {
            Toast.makeText(this, "Preencha o titulo da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (divida.getData() == null || divida.getData().equals("")) {
            Toast.makeText(this, "Preencha a data da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (divida.getLocal() == null || divida.getLocal().equals("")) {
            Toast.makeText(this, "Preencha o local da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (divida.getValor() == null) {
            Toast.makeText(this, "Preencha o valor da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (divida.getNumeroParcelas() == null) {
            Toast.makeText(this, "Preencha o número de parcelas da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void salvarDividaFirebase(Divida divida, DatePicker data) {
        if (divida.getCartao()) {

        }
        mDatabase.child(divida.getUid()).setValue(divida);
        Toast.makeText(this, "Divida enviada com sucesso!", Toast.LENGTH_SHORT).show();
    }

}
