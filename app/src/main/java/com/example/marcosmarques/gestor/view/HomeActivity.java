package com.example.marcosmarques.gestor.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marcosmarques.gestor.R;
import com.example.marcosmarques.gestor.model.Conta;
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
    private DatabaseReference mDatabaseConta;
    private List<Conta> contas;
    private List<String> nomesContas;
    private String nomeContaSelecionada;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        layout = findViewById(R.id.layout_home);

        mDatabaseDivida = FirebaseDatabase.getInstance().getReference("divida");
        mDatabaseConta = FirebaseDatabase.getInstance().getReference("conta");

        contas = new ArrayList<>();
        nomesContas = new ArrayList<>();

        carregarContas();

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
                if (contas.isEmpty()) {
                    Toast.makeText(this, "Cadastre uma conta primeiro.", Toast.LENGTH_SHORT).show();
                } else {
                    addDivida();
                }
                return true;
            case R.id.add_conta:
                addCartao();
                return true;
            case R.id.pg_conta:
                if (contas.isEmpty()) {
                    Toast.makeText(this, "Não existe conta para pagar", Toast.LENGTH_SHORT).show();
                } else {
                    pagouConta();
                }
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
        final Spinner spinnerCartao = dialog.findViewById(R.id.spinner_cartao);

        if (contas != null) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, nomesContas);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCartao.setAdapter(dataAdapter);
            spinnerCartao.setOnItemSelectedListener(this);
        }

        Button salvarDivida = dialog.findViewById(R.id.salvar_divida);
        salvarDivida.setOnClickListener(v -> {

            Divida divida = new Divida();
            divida.setLocal(textLocal.getText().toString());
            divida.setTitulo(textDivida.getText().toString());
            divida.setData(data.getDayOfMonth() + "/" + (data.getMonth() + 1) + "/" + data.getYear());
            if (textParcelas.getText().toString().equals("")) {
                divida.setNumeroParcelas(1L);
            } else {
                divida.setNumeroParcelas(Long.valueOf(textParcelas.getText().toString()));
            }
            if (textvalor.getText().toString().equals("")) {
                divida.setValorParcelas(1D);
            } else {
                divida.setValorParcelas((Double.valueOf(textvalor.getText().toString()) / divida.getNumeroParcelas()));
            }

            if (verificaDivida(divida)) {
                salvarDividaFirebase(divida);
            }

            dialog.dismiss();
        });

        Button cancelarDivida = dialog.findViewById(R.id.cancelar_divida);
        cancelarDivida.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }

    private void addCartao() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_add_conta);

        final EditText textCartao = dialog.findViewById(R.id.edt_cartao);
        final EditText textVencimento = dialog.findViewById(R.id.edt_vencimento);
        final EditText textLimite = dialog.findViewById(R.id.edt_limite);

        Button salvarCartao = dialog.findViewById(R.id.salvar_cartao);
        salvarCartao.setOnClickListener(v -> {

            Conta conta = new Conta();
            conta.setNome(textCartao.getText().toString());
            if (textLimite.getText().toString().equals("")) {
                conta.setLimite(0D);
            } else {
                conta.setLimite(Double.valueOf(textLimite.getText().toString()));
            }

            conta.setVencimento(textVencimento.getText().toString());

            salvarContaFirebase(conta);

            dialog.dismiss();
        });

        Button cancelarCartao = dialog.findViewById(R.id.cancelar_cartao);
        cancelarCartao.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

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
        } else if (divida.getValorParcelas() == null) {
            Toast.makeText(this, "Preencha o valor da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (divida.getNumeroParcelas() == null) {
            Toast.makeText(this, "Preencha o número de parcelas da dívida!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean verificarConta(Conta conta) {
        if (conta.getNome() == null || conta.getNome().equals("")) {
            Toast.makeText(this, "Preencha o nome da conta!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (verificaDuplicidadeConta(conta.getNome())) {
            Toast.makeText(this, "Essa conta já existe!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (conta.getLimite() == null) {
            Toast.makeText(this, "Preencha o limite da conta!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (conta.getVencimento() == null) {
            Toast.makeText(this, "Preencha o vencimento da conta!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean verificaDuplicidadeConta(String nome) {
        int achou = 0;
        for (Conta conta : contas) {
            if (nome.equals(conta.getNome())) {
                achou = 1;
            }
        }
        return achou == 1;
    }

    private void salvarDividaFirebase(Divida divida) {

        divida.setUid(mDatabaseDivida.push().getKey());

        inserirDividaNaContaFirebase(divida);
    }

    private void inserirDividaNaContaFirebase(Divida divida) {

        if (nomeContaSelecionada == null) {
            Toast.makeText(this, "Selecione uma conta", Toast.LENGTH_SHORT).show();
        } else {
            for (Conta conta : contas) {
                if (conta.getNome().equals(nomeContaSelecionada)) {
                    if (conta.getDividas() == null) {
                        List<Divida> dividas = new ArrayList<>();
                        dividas.add(divida);
                        conta.setDividas(dividas);
                    } else {
                        conta.getDividas().add(divida);
                    }
                    mDatabaseConta.child(conta.getUid()).setValue(conta);
                    Toast.makeText(this, "Dívida inserida com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarContas();
                }
            }
        }

    }

    private void salvarContaFirebase(Conta conta) {
        if (verificarConta(conta)) {
            conta.setUid(mDatabaseConta.push().getKey());
            mDatabaseConta.child(conta.getUid()).setValue(conta);
            Toast.makeText(this, "Conta inserida com sucesso!", Toast.LENGTH_SHORT).show();
            carregarContas();
        }
    }

    private void carregarContas() {
        mDatabaseConta.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                contas.clear();
                nomesContas.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Conta conta = dataSnapshot.getValue(Conta.class);
                    contas.add(conta);
                    assert conta != null;
                    nomesContas.add(conta.getNome());
                }

                Collections.reverse(contas);
                relatorio();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void relatorio() {

        layout.removeAllViews();
        Double total = 0.0;
        Double totalParcelado = 0.0;

        TextView tituloConta = new TextView(this);
        tituloConta.setText("Contas :");
        tituloConta.setTextSize(20);
        tituloConta.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        layout.addView(tituloConta);

        for (final Conta conta : contas) {
            TextView linha = new TextView(this);
            linha.setText(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            linha.setTextColor(getResources().getColor(R.color.colorDivisor, null));
            layout.addView(linha);

            TextView titulo = new TextView(this);
            titulo.setText(conta.getNome());
            titulo.setTextSize(20);
            titulo.setTextColor(getResources().getColor(R.color.colorTexto, null));
            layout.addView(titulo);

            TextView vencimento = new TextView(this);
            vencimento.setText("Dia do vencimento : " + conta.getVencimento());
            vencimento.setTextColor(getResources().getColor(R.color.colorTexto, null));
            layout.addView(vencimento);

            TextView limite = new TextView(this);
            limite.setText("Limite : R$ " + String.format("%.2f", (conta.getLimite() - fatorLimite(conta.getDividas()))));
            limite.setTextColor(getResources().getColor(R.color.colorTexto, null));
            layout.addView(limite);

            if (conta.getDividas() == null) {
                TextView semDividas = new TextView(this);
                semDividas.setText("Não existem débitos nessa conta.");
                layout.addView(semDividas);
            } else {
                for (Divida divida : conta.getDividas()) {

                    TextView linha2 = new TextView(this);
                    linha2.setText(" - - - - - - - - - - - - - - - - - - - ");
                    linha2.setTextColor(getResources().getColor(R.color.colorDivisor, null));
                    layout.addView(linha2);

                    TextView tituloDivida = new TextView(this);
                    tituloDivida.setText(divida.getTitulo());
                    layout.addView(tituloDivida);

                    TextView localDivida = new TextView(this);
                    localDivida.setText("Local : " + divida.getLocal());
                    layout.addView(localDivida);

                    TextView dataDivida = new TextView(this);
                    dataDivida.setText("Data : " + divida.getData());
                    layout.addView(dataDivida);

                    TextView valorParcela = new TextView(this);
                    valorParcela.setText("R$ " + String.format("%.2f", divida.getValorParcelas()));
                    layout.addView(valorParcela);

                    TextView parcelas = new TextView(this);
                    parcelas.setText("Faltam " + divida.getNumeroParcelas() + " Parcela(s)");
                    layout.addView(parcelas);

                    //interar totais

                    if (divida.getNumeroParcelas() > 1) {
                        totalParcelado = totalParcelado + divida.getValorParcelas();
                    } else {
                        total = total + divida.getValorParcelas();
                    }

                }
            }
        }

        TextView linhaTotal = new TextView(this);
        linhaTotal.setText(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        linhaTotal.setTextColor(getResources().getColor(R.color.colorDivisor, null));
        layout.addView(linhaTotal);

        TextView tituloTotal = new TextView(this);
        tituloTotal.setText("Total de contas :");
        tituloTotal.setTextSize(20);
        tituloTotal.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        layout.addView(tituloTotal);

        TextView valorTotal = new TextView(this);
        valorTotal.setText("R$ " + String.valueOf(String.format("%.2f", total)));
        layout.addView(valorTotal);

        TextView linhaTotalParcelado = new TextView(this);
        linhaTotalParcelado.setText(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        linhaTotalParcelado.setTextColor(getResources().getColor(R.color.colorDivisor, null));
        layout.addView(linhaTotalParcelado);

        TextView tituloTotalParcelado = new TextView(this);
        tituloTotalParcelado.setText("Total Parcelados :");
        tituloTotalParcelado.setTextSize(20);
        tituloTotalParcelado.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        layout.addView(tituloTotalParcelado);

        TextView valorTotalParcelado = new TextView(this);
        valorTotalParcelado.setText("R$ " + String.valueOf(String.format("%.2f", totalParcelado)));
        layout.addView(valorTotalParcelado);
    }

    private void pagouConta() {

        final CharSequence[] listContas = new CharSequence[contas.size()];
        final List<String> contasSelecionadas = new ArrayList<>();
        for (int i = 0; i < contas.size(); i++) {
            listContas[i] = contas.get(i).getNome();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Pagar Conta")
                .setMultiChoiceItems(listContas, null,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                contasSelecionadas.add(String.valueOf(listContas[which]));
                            } else if (contasSelecionadas.contains(String.valueOf(listContas[which]))) {
                                contasSelecionadas.remove(which);
                            }
                        })
                .setPositiveButton("Ok", (dialog, id) -> {

                    for (Conta conta : contas) {
                        if (contasSelecionadas.contains(conta.getNome())) {
                            List<Divida> dividasExcluidas = new ArrayList<>();
                            for (Divida divida : conta.getDividas()) {
                                if (divida.getNumeroParcelas() > 1) {
                                    divida.setNumeroParcelas((divida.getNumeroParcelas() - 1));
                                } else {
                                    dividasExcluidas.add(divida);
                                }
                            }
                            for (Divida dividaExculida : dividasExcluidas) {
                                conta.getDividas().remove(dividaExculida);
                            }
                            //atualiza conta no firebase
                            mDatabaseConta.child(conta.getUid()).setValue(conta);
                            dividasExcluidas.clear();
                        }
                    }

                    Toast.makeText(HomeActivity.this, "Contas atualizadas", Toast.LENGTH_SHORT).show();
                    carregarContas();
                })
                .setNegativeButton("Cancelar", (dialog, id) -> {

                });

        builder.create();
        builder.show();
    }

    private Double fatorLimite(List<Divida> dividas) {

        Double total = 0.0D;

        if (dividas != null) {
            for (Divida divida : dividas) {
                total = total + (divida.getValorParcelas() * divida.getNumeroParcelas());
            }
        }

        return total;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        nomeContaSelecionada = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
