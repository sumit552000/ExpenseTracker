package com.example.expensetracker.repository

import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)
    fun getAllExpenses(): Flow<List<Expense>> = dao.getAllExpenses()
    fun getExpensesByMonth(month: String, year: String): Flow<List<Expense>> =
        dao.getExpensesByMonth(month, year)
}