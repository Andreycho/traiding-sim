"use client"

import type React from "react"
import { useState } from "react"

interface SellCryptoFormProps {
  crypto: string
  onSell: (crypto: string, amount: number) => void
  onCancel: () => void
}

const SellCryptoForm: React.FC<SellCryptoFormProps> = ({ crypto, onSell, onCancel }) => {
  const [amount, setAmount] = useState<number | "">("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (typeof amount === "number") {
      onSell(crypto, amount)
    } else {
      alert("Please enter a valid amount to sell.")
    }
  }

  return (
    <div className="p-4 border rounded shadow-md">
      <h2 className="text-xl font-semibold mb-4">Sell {crypto}</h2>
      <form onSubmit={handleSubmit} className="flex flex-col space-y-4">
        <div>
          <label htmlFor="amount" className="block text-gray-700 text-sm font-bold mb-2">
            Amount to Sell:
          </label>
          <input
            type="number"
            id="amount"
            placeholder="Enter amount"
            value={amount}
            onChange={(e) => setAmount(Number(e.target.value))}
            min="0"
            step="0.1"
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
          />
        </div>
        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={onCancel}
            className="bg-gray-400 text-white p-2 rounded-lg shadow-md hover:bg-gray-500"
          >
            Cancel
          </button>
          <button type="submit" className="bg-red-500 text-white p-2 rounded-lg shadow-md hover:bg-red-600">
            Sell
          </button>
        </div>
      </form>
    </div>
  )
}

export default SellCryptoForm

