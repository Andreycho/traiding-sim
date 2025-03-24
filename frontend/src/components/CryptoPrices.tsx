"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { Table, TableBody, TableCaption, TableCell, TableHeader, TableRow, TableHead } from "@/components/ui/table"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import BuyCryptoForm from "./BuyCryptoForm"
import { toast } from "sonner"

interface Prices {
  [key: string]: number
}

interface CryptoPricesProps {
  setBalance: React.Dispatch<React.SetStateAction<number>>
}

const CryptoPrices: React.FC<CryptoPricesProps> = ({ setBalance }) => {
  const [prices, setPrices] = useState<Prices>({})
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedCrypto, setSelectedCrypto] = useState<string | null>(null)

  useEffect(() => {
    const fetchInitialPrices = async () => {
      try {
        const response = await fetch("/api/prices")
        if (!response.ok) throw new Error("Failed to fetch initial prices")
  
        const data = await response.json()
        console.log("Fetched initial prices:", data)
        setPrices(data)
        setLoading(false)
      } catch (error) {
        console.error("Error fetching initial prices:", error)
        setError("Failed to fetch initial prices.")
        setLoading(false)
      }
    }
  
    fetchInitialPrices()
  
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
        })
      },
      onStompError: (frame) => {
        console.error("STOMP error", frame)
        setError("Failed to connect to WebSocket.")
        setLoading(false)
      },
    })
  
    client.activate()
    return () => {
      client.deactivate()
    }
  }, [])

  const handleBuy = async (crypto: string, amount: number) => {
    if (!amount || amount < 0.1) {
      toast.error("Please enter a valid amount (at least 0.1).")
      return
    }

    try {
      const response = await fetch(`/api/buy?crypto=${crypto}&amount=${amount}`, {
        method: "POST",
      })
      if (!response.ok) throw new Error("Failed to buy cryptocurrency")

      const message = await response.text()
      toast.success(message)

      await fetchBalance()

      setSelectedCrypto(null)
    } catch (error) {
      toast.error(`Error buying cryptocurrency: ${(error as Error).message}`)
    }
  }

  const fetchBalance = async () => {
    try {
      const response = await fetch("/api/balance")
      if (!response.ok) throw new Error("Failed to fetch balance")

      const balanceData = await response.json()
      setBalance(balanceData.balance || balanceData)
    } catch (error) {
      console.error("Error fetching balance:", error)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center">
        <p>Loading prices...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex justify-center">
        <p>Error fetching prices: {error}</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center w-full">
      <div className="w-full max-w-3xl mx-auto border border-gray-300 rounded-lg overflow-hidden">
        <Table>
          <TableCaption>Real-Time Cryptocurrency Prices</TableCaption>
          <TableHeader>
            <TableRow className="border-b border-gray-300">
              <TableHead className="w-[100px] text-center">Cryptocurrency</TableHead>
              <TableHead className="text-center">Price (USD)</TableHead>
              <TableHead className="text-center">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Object.entries(prices).map(([crypto, price]) => (
              <TableRow key={crypto} className="border-b border-gray-200">
                <TableCell className="font-medium text-center">{crypto}</TableCell>
                <TableCell className="text-center">{`$${price.toFixed(2)}`}</TableCell>
                <TableCell className="text-center">
                  <button onClick={() => setSelectedCrypto(crypto)} className="bg-blue-500 text-white p-2 rounded">
                    Buy
                  </button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {selectedCrypto && (
        <div className="mt-4 w-full max-w-3xl mx-auto">
          <BuyCryptoForm crypto={selectedCrypto} onBuy={handleBuy} onCancel={() => setSelectedCrypto(null)} />
        </div>
      )}
    </div>
  )
}

export default CryptoPrices

