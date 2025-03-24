"use client"

import type React from "react"
import { useState } from "react"
import { toast } from "sonner"

interface BuyCryptoFormProps {
  crypto: string
  onBuy: (crypto: string, amount: number) => void
  onCancel: () => void
}

const BuyCryptoForm: React.FC<BuyCryptoFormProps> = ({ crypto, onBuy, onCancel }) => {
  const [amount, setAmount] = useState<number | "">("")

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value

    if (inputValue === "") {
      setAmount("")
      return
    }

    let value = Number.parseFloat(inputValue)

    if (isNaN(value)) return
    if (value < 0.1) value = 0.1

    setAmount(value)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (amount === "" || (typeof amount === "number" && amount < 0.1)) {
      toast.error("Please enter at least 0.1.")
      return
    }

    onBuy(crypto, typeof amount === "number" ? amount : 0.1)
  }

  return (
    <div className="p-4 border rounded shadow-md">
      <h2 className="text-xl font-semibold mb-4">Buy {crypto}</h2>
      <form onSubmit={handleSubmit} className="flex flex-col space-y-4">
        <div>
          <label htmlFor="amount" className="block text-gray-700 text-sm font-bold mb-2">
            Amount to Buy:
          </label>
          <input
            type="number"
            id="amount"
            value={amount}
            onChange={handleChange}
            placeholder="Enter amount"
            step="0.1"
            min="0.1"
            inputMode="decimal"
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
          <button type="submit" className="bg-blue-500 text-white p-2 rounded-lg shadow-md hover:bg-blue-600">
            Buy
          </button>
        </div>
      </form>
    </div>
  )
}

export default BuyCryptoForm

