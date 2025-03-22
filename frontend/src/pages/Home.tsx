"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import CryptoPrices from "@/components/CryptoPrices"

const Home: React.FC = () => {
  const navigate = useNavigate()
  const [balance, setBalance] = useState<number>(0)

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
    fetchBalance()
  }, [])

  const handleReset = async () => {
    try {
      await fetch("/api/reset", { method: "POST" })
      alert("Account has been reset!")
      fetchBalance()
    } catch (error) {
      console.error("Error resetting account:", error)
    }
  }

  return (
    <div className="w-screen min-h-screen left-0 top-0 absolute">
      <div className="grid grid-cols-3 items-center w-full p-6 bg-gray-100 shadow-md">
        <div className="flex space-x-4 justify-start">
          <button
            onClick={() => navigate("/holdings")}
            className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md"
          >
            View Holdings
          </button>
          <button
            onClick={() => navigate("/transactions")}
            className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md"
          >
            Transaction History
          </button>
        </div>

        <h1 className="text-2xl font-bold text-black text-center">Crypto Trading Simulator</h1>

        <div className="flex space-x-4 justify-end">
          <div className="text-xl font-semibold">Balance: ${balance.toFixed(2)}</div>
          <button onClick={handleReset} className="px-6 py-3 bg-red-500 text-white rounded-lg shadow-md">
            Reset Account
          </button>
        </div>
      </div>

      <hr className="border-t border-gray-300 w-full mx-auto mb-6" />

      <div className="w-full px-6">
        <CryptoPrices setBalance={setBalance} />
      </div>
    </div>
  )
}

export default Home

