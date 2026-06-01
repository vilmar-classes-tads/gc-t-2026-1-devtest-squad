package br.edu.ifpe.sistemaeditais.repository;

import br.edu.ifpe.sistemaeditais.model.Edital;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditalRepository {
    private static final List<Edital> editais = new ArrayList<>();

    public void salvar(Edital edital) {
        editais.add(edital);
    }

    public Edital buscarPorNumero(String numero) {
        for (Edital e : editais) {
            if (e.getNumero().equalsIgnoreCase(numero)) {
                return e;
            }
        }
        return null;
    }

    public List<Edital> listarTodos() {
        return Collections.unmodifiableList(editais);
    }
}