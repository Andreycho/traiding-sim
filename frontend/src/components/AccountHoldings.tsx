"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import SellCryptoForm from "./SellCryptoForm"
import { toast } from "sonner"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"

interface Holding {
  crypto: string
  amount: number
  price: number
  totalValue: number
}

interface HoldingsResponse {
  [crypto: string]: number
}

interface PricesResponse {
  [crypto: string]: number
}

const AccountHoldings: React.FC = () => {
  const [holdings, setHoldings] = useState<Holding[]>([])
  const [error, setError] = useState<string | null>(null)
  const [selectedCrypto, setSelectedCrypto] = useState<string | null>(null)
  const [prices, setPrices] = useState<PricesResponse>({})
  const [balance, setBalance] = useState<number>(0)
  const navigate = useNavigate()

  const fetchBalance = async () => {
    try {
      const balanceResponse = await fetch("/api/balance")
      if (!balanceResponse.ok) throw new Error("Failed to fetch balance")

      const balanceData = await balanceResponse.json()
      console.log("Balance data:", balanceData)

      if (typeof balanceData === "number") {
        setBalance(balanceData)
      } else if (balanceData && balanceData.balance) {
        setBalance(balanceData.balance)
      } else {
        throw new Error("Invalid balance data format")
      }
    } catch (error) {
      setError("Failed to fetch balance")
      console.error("Error fetching balance:", error)
    }
  }

  useEffect(() => {
    const fetchHoldingsAndPrices = async () => {
      try {
        const holdingsResponse = await fetch("/api/holdings")
        if (!holdingsResponse.ok) throw new Error("Failed to fetch holdings")
  
        const holdingsData: HoldingsResponse = await holdingsResponse.json()
  
        const pricesResponse = await fetch("/api/prices")
        if (!pricesResponse.ok) throw new Error("Failed to fetch prices")
  
        const pricesData: PricesResponse = await pricesResponse.json()
  
        const transformedData = Object.entries(holdingsData).map(([crypto, amount]) => {
          const price = pricesData[crypto]
          return {
            crypto,
            amount,
            price: price || 0,
            totalValue: price ? price * amount : 0,
          }
        })
  
        setHoldings(transformedData)
        setPrices(pricesData)
        await fetchBalance()
      } catch (error) {
        setError("Failed to fetch holdings or prices")
        console.error(error)
      }
    }
  
    fetchHoldingsAndPrices()
  
    const socket = new SockJS("http://localhost:8080/ws")
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connected to WebSocket")
  
        client.subscribe("/topic/prices", (message) => {
          const data = JSON.parse(message.body)
          console.log(`📩 Received price update: ${data.symbol} -> $${data.price}`)
  
          setPrices((prevPrices) => ({
            ...prevPrices,
            [data.symbol]: data.price,
          }))
  
          setHoldings((prevHoldings) =>
            prevHoldings.map((holding) => {
              if (holding.crypto === data.symbol) {
                console.log(
                  `Updating holding: ${holding.crypto} | New Price: $${data.price.toFixed(2)}`
                )
  
                return {
                  ...holding,
                  price: data.price,
                  totalValue: data.price * holding.amount,
                }
              }
              return holding
            })
          )
        })
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame)
        setError("Failed to connect to WebSocket.")
      },
    })
  
    client.activate()
  
    return () => {
      client.deactivate()
    }
  }, [])

  const handleSell = async (crypto: string, amount: number) => {
    if (!amount || amount <= 0) {
      toast.error("Please enter a valid amount to sell.")
      return
    }

    try {
      const response = await fetch(`/api/sell?crypto=${crypto}&amount=${amount}`, {
        method: "POST",
      })

      if (!response.ok) {
        throw new Error("Failed to sell cryptocurrency")
      }

      const message = await response.text()
      toast.success(message)

      const updatedHoldingsResponse = await fetch("/api/holdings")
      const updatedHoldingsData: HoldingsResponse = await updatedHoldingsResponse.json()

      const updatedHoldings = Object.entries(updatedHoldingsData).map(([crypto, amount]) => {
        const price = prices[crypto] || 0
        return {
          crypto,
          amount,
          price,
          totalValue: price * amount,
        }
      })

      setHoldings(updatedHoldings)
      setSelectedCrypto(null)

      await fetchBalance()
    } catch (error) {
      toast.error(`Error selling cryptocurrency: ${(error as Error).message}`)
    }
  }

  return (
    <div className="w-screen min-h-screen left-0 top-0 absolute">
      {/* Top Bar with Grid Layout */}
      <div className="grid grid-cols-3 items-center w-full p-6 bg-gray-100 shadow-md">
        <div className="flex justify-start">
          <button onClick={() => navigate("/")} className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md">
            Back to Home
          </button>
        </div>

        <h1 className="text-2xl font-bold text-black text-center">Account Holdings</h1>

        <div className="flex justify-end">
          <div className="text-xl font-semibold">Balance: ${balance.toFixed(2)}</div>
        </div>
      </div>

      <hr className="border-t border-gray-300 w-full mx-auto mb-6" />

      {/* Main Content */}
      <div className="w-full px-6">
        {error && <p className="text-red-500 mb-4">{error}</p>}

        <div className="w-full bg-white rounded-lg shadow-lg p-6">
          <div className="overflow-x-auto w-full">
            {holdings.length > 0 ? (
              <table className="min-w-full table-auto border-collapse border border-gray-300">
                <thead>
                  <tr className="bg-gray-200 text-gray-800">
                    <th className="px-6 py-3 border-b">Cryptocurrency</th>
                    <th className="px-6 py-3 border-b">Amount</th>
                    <th className="px-6 py-3 border-b">Price (USD)</th>
                    <th className="px-6 py-3 border-b">Total Value (USD)</th>
                    <th className="px-6 py-3 border-b">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {holdings.map((holding) => (
                    <tr key={holding.crypto} className="border-b hover:bg-gray-50">
                      <td className="px-6 py-4 text-center">{holding.crypto}</td>
                      <td className="px-6 py-4 text-center">{holding.amount}</td>
                      <td className="px-6 py-4 text-center">${holding.price.toFixed(2)}</td>
                      <td className="px-6 py-4 text-center">${holding.totalValue.toFixed(2)}</td>
                      <td className="px-6 py-4 text-center">
                        <button
                          onClick={() => setSelectedCrypto(holding.crypto)}
                          className="bg-red-500 text-white p-2 rounded-lg shadow-md hover:bg-red-600"
                        >
                          Sell
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="text-center py-8 text-gray-600 text-lg font-medium">
                You have no holdings at the moment.
              </div>
            )}
          </div>

          {selectedCrypto && (
            <div className="mt-6">
              <SellCryptoForm
                crypto={selectedCrypto}
                onSell={(crypto, amount) => handleSell(crypto, amount)}
                onCancel={() => setSelectedCrypto(null)}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AccountHoldings

