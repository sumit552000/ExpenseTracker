package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE strftime('%m', date / 1000, 'unixepoch') = :month AND strftime('%Y', date / 1000, 'unixepoch') = :year")
    fun getExpensesByMonth(month: String, year: String): Flow<List<Expense>>
}