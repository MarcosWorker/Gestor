package com.example.marcosmarques.gestor.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.marcosmarques.gestor.R;
import com.example.marcosmarques.gestor.model.Cartao;
import com.example.marcosmarques.gestor.model.Divida;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DatabaseReference mDatabaseDivida;
    private DatabaseReference mDatabaseCartao;
    private List<Cartao> cartoes;
    private List<String> nomesCartao;
    private String nomeCartaoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabaseDivida = FirebaseDatabase.getInstance().getReference("divida");
        mDatabaseCartao = FirebaseDatabase.getInstance().getReference("cartao");

        carregarCartoes();

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

        if (cartoes != null) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, nomesCartao);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCartao.setAdapter(dataAdapter);
            spinnerCartao.setOnItemSelectedListener(this);
        }

        Button salvarDivida = dialog.findViewById(R.id.salvar_divida);
        salvarDivida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Divida divida = new Divida();
                divida.setLocal(textLocal.getText().toString());
                divida.setTitulo(textDivida.getText().toString());
                if (textvalor.getText().toString().equals("")) {
                    divida.setValor(1D);
                } else {
                    divida.setValor(Double.valueOf(textvalor.getText().toString()));
                }
                divida.setData(data.getDayOfMonth() + "/" + (data.getMonth() + 1) + "/" + data.getYear());
                divida.setCartao(checkCartao.isChecked());
                if (textParcelas.getText().toString().equals("")) {
                    divida.setNumeroParcelas(1L);
                } else {
                    divida.setNumeroParcelas(Long.valueOf(textParcelas.getText().toString()));
                }

                if (verificaDivida(divida)) {
                    salvarDividaFirebase(divida);
                }

                dialog.dismiss();
            }
        });

        Button cancelarDivida = dialog.findViewById(R.id.cancelar_divida);
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

        final EditText textCartao = dialog.findViewById(R.id.edt_cartao);
        final EditText textVencimento = dialog.findViewById(R.id.edt_vencimento);
        final EditText textLimite = dialog.findViewById(R.id.edt_limite);

        Button salvarCartao = dialog.findViewById(R.id.salvar_cartao);
        salvarCartao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cartao cartao = new Cartao();
                cartao.setNome(textCartao.getText().toString());
                cartao.setLimite(Double.valueOf(textLimite.getText().toString()));
                cartao.setVencimento(textVencimento.getText().toString());

                salvarCartaoFirebase(cartao);

                dialog.dismiss();
            }
        });

        Button cancelarCartao = dialog.findViewById(R.id.cancelar_cartao);
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

    private boolean verificarCartao(Cartao cartao) {
        if (cartao.getNome() == null || cartao.getNome().equals("")) {
            Toast.makeText(this, "Preencha o nome do cartão!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (verificaDuplicidadeCartao(cartao.getNome())) {
            Toast.makeText(this, "Esse cartão já existe!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (cartao.getLimite() == null) {
            Toast.makeText(this, "Preencha a limite do cartão!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (cartao.getVencimento() == null) {
            Toast.makeText(this, "Preencha o vencimento do cartão!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean verificaDuplicidadeCartao(String nome) {
        int achou = 0;
        for (Cartao cartao : cartoes) {
            if (nome.equals(cartao.getNome())) {
                achou = 1;
            }
        }
        return achou == 1;
    }

    private void salvarDividaFirebase(Divida divida) {

        divida.setUid(mDatabaseDivida.push().getKey());

        if (divida.getCartao()) {
            inserirDividaNoCartaoFirebase(divida);
        } else {
            mDatabaseDivida.child(divida.getUid()).setValue(divida);
        }
    }

    private void inserirDividaNoCartaoFirebase(Divida divida) {

        if (nomeCartaoSelecionado == null) {
            Toast.makeText(this, "Selecione um cartão", Toast.LENGTH_SHORT).show();
        } else {
            for (Cartao cartao : cartoes) {
                if (cartao.getNome().equals(nomeCartaoSelecionado)) {
                    if (cartao.getDividas() == null) {
                        List<Divida> dividas = new ArrayList<>();
                        dividas.add(divida);
                        cartao.setDividas(dividas);
                    } else {
                        cartao.getDividas().add(divida);
                    }
                    mDatabaseCartao.child(cartao.getUid()).setValue(cartao);
                    Log.i("Atualizar Cartão - ", "OK -");
                }
            }
        }

    }

    private void salvarCartaoFirebase(Cartao cartao) {
        if (verificarCartao(cartao)) {
            cartao.setUid(mDatabaseCartao.push().getKey());
            mDatabaseCartao.child(cartao.getUid()).setValue(cartao);
            Toast.makeText(this, "Cartão inserido com sucesso!", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarCartoes() {
        mDatabaseCartao.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cartoes = new ArrayList<>();
                nomesCartao = new ArrayList<>();
                cartoes.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Cartao cartao = dataSnapshot.getValue(Cartao.class);
                    cartoes.add(cartao);
                    nomesCartao.add(cartao.getNome());
                }

                Collections.reverse(cartoes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        nomeCartaoSelecionado = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
