"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { Table, TableBody, TableCell, TableHeader, TableRow, TableHead } from "@/components/ui/table" // Assuming you have a UI Table component

interface Transaction {
  crypto: string
  amount: number
  price: number
  total: number
  dateTime: string
  type: string
  id: number
}

const TransactionHistory: React.FC = () => {
  const navigate = useNavigate()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [balance, setBalance] = useState<number>(0)

  // Fetch the balance
  const fetchBalance = async () => {
    try {
      const response = await fetch("/api/balance")
      if (!response.ok) throw new Error("Failed to fetch balance")

      const balanceData = await response.json()
      if (typeof balanceData === "number") {
        setBalance(balanceData)
      } else if (balanceData && balanceData.balance) {
        setBalance(balanceData.balance)
      } else {
        throw new Error("Invalid balance data format")
      }
    } catch (error) {
      console.error("Error fetching balance:", error)
    }
  }

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const response = await fetch("/api/transactions")
        if (!response.ok) throw new Error("Failed to fetch transactions")

        const data: Transaction[] = await response.json()
        setTransactions(data)
        setLoading(false)
      } catch (error) {
        setError((error as Error).message)
        setLoading(false)
      }
    }

    fetchTransactions()
    fetchBalance()
  }, [])

  if (loading) {
    return (
      <div className="w-screen min-h-screen left-0 top-0 absolute flex items-center justify-center">
        <p className="text-xl">Loading transaction history...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="w-screen min-h-screen left-0 top-0 absolute flex items-center justify-center">
        <p className="text-xl text-red-500">Error fetching transaction history: {error}</p>
      </div>
    )
  }

  return (
    <div className="w-screen min-h-screen left-0 top-0 absolute">
      {/* Top Bar with Grid Layout */}
      <div className="grid grid-cols-3 items-center w-full p-6 bg-gray-100 shadow-md">
        <div className="flex justify-start">
          <button onClick={() => navigate("/")} className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md">
            Back
          </button>
        </div>

        <h1 className="text-2xl font-bold text-black text-center">Transaction History</h1>

        <div className="flex justify-end">
          <div className="text-xl font-semibold">Balance: ${balance.toFixed(2)}</div>
        </div>
      </div>

      <hr className="border-t border-gray-300 w-full mx-auto mb-6" />

      {/* Transaction History Table */}
      <div className="w-full px-6">
        {transactions.length > 0 ? (
          <Table className="w-full border border-gray-300">
            <TableHeader>
              <TableRow>
                <TableHead className="border border-gray-300">Crypto</TableHead>
                <TableHead className="border border-gray-300">Amount</TableHead>
                <TableHead className="border border-gray-300">Price (USD)</TableHead>
                <TableHead className="border border-gray-300">Total (USD)</TableHead>
                <TableHead className="border border-gray-300">Date & Time</TableHead>
                <TableHead className="border border-gray-300">Type</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transactions.map((transaction) => (
                <TableRow key={transaction.id}>
                  <TableCell className="border border-gray-300">{transaction.crypto}</TableCell>
                  <TableCell className="border border-gray-300">{transaction.amount}</TableCell>
                  <TableCell className="border border-gray-300">${transaction.price.toFixed(2)}</TableCell>
                  <TableCell className="border border-gray-300">${transaction.total.toFixed(2)}</TableCell>
                  <TableCell className="border border-gray-300">{transaction.dateTime}</TableCell>
                  <TableCell className="border border-gray-300">{transaction.type}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : (
          <div className="text-center py-8 bg-white rounded-lg shadow-md text-gray-600 text-lg font-medium">
            There are no transactions at the moment.
          </div>
        )}
      </div>
    </div>
  )
}

export default TransactionHistory

