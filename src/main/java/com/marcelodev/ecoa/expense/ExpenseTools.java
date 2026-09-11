package com.marcelodev.ecoa.expense;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseTools {

    private final ExpenseRepository expenseRepository;
    private final ExpenseDraftRepository draftRepository;

    public ExpenseTools(ExpenseRepository expenseRepository, ExpenseDraftRepository draftRepository) {
        this.expenseRepository = expenseRepository;
        this.draftRepository = draftRepository;
    }

    @Tool(description = "Cria um rascunho de gasto. Use quando usuário informar gasto; nunca persiste gasto final.")
    @Transactional
    public String createExpenseDraft(
            @ToolParam(description = "Valor positivo do gasto em reais") BigDecimal amount,
            @ToolParam(description = "Descrição curta do gasto") String description,
            @ToolParam(description = "Categoria: ALIMENTACAO, TRANSPORTE, MORADIA, SAUDE, LAZER, EDUCACAO ou OUTROS") Category category) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }

        draftRepository.save(new ExpenseDraft(amount, description, category));
        return "Rascunho criado: " + amount + " reais em " + description + ", categoria " + category
                + ". Peça confirmação ao usuário.";
    }

    @Tool(description = "Confirma e registra o último rascunho de gasto. Use somente após confirmação explícita do usuário.")
    @Transactional
    public String confirmLatestExpense() {
        // ponytail: demo de usuário único; adicionar dono do rascunho quando houver autenticação.
        var draft = draftRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Não existe gasto aguardando confirmação"));
        var expense = expenseRepository.save(new Expense(draft.getAmount(), draft.getDescription(), draft.getCategory()));
        draftRepository.delete(draft);
        return "Gasto confirmado e registrado: " + expense.getAmount() + " reais em " + expense.getDescription();
    }

    @Tool(description = "Lista gastos já confirmados de uma categoria.")
    public List<ExpenseResponse> listExpensesByCategory(
            @ToolParam(description = "Categoria solicitada") Category category) {
        return expenseRepository.findAllByCategoryOrderByCreatedAtDesc(category).stream().map(ExpenseResponse::from).toList();
    }

    public List<ExpenseResponse> list(Category category) {
        var expenses = category == null ? expenseRepository.findAll() : expenseRepository.findAllByCategoryOrderByCreatedAtDesc(category);
        return expenses.stream().map(ExpenseResponse::from).toList();
    }
}
