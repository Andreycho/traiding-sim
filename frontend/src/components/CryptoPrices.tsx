"use client"

import type React from "react"
import { useEffect, useState } from "react"
import { Table, TableBody, TableCaption, TableCell, TableHeader, TableRow, TableHead } from "@/components/ui/table"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import BuyCryptoForm from "./BuyCryptoForm"
import { toast } from "sonner"
import { Search } from 'lucide-react'

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
  const [searchTerm, setSearchTerm] = useState<string>("")
  const [currentPage, setCurrentPage] = useState<number>(1)
  const itemsPerPage = 10

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
          console.log(`Received price update: ${data.symbol} -> $${data.price}`)

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

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || "Failed to buy cryptocurrency");
      }
      
      toast.success(data.message || "Cryptocurrency bought successfully!")
      await fetchBalance()
      setSelectedCrypto(null)
    } catch (error) {
      toast.error((error as Error).message)
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

  const filteredCryptos = Object.entries(prices).filter(([crypto]) => 
    crypto.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalPages = Math.ceil(filteredCryptos.length / itemsPerPage);
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentCryptos = filteredCryptos.slice(indexOfFirstItem, indexOfLastItem);

  if (loading) {
    return (
      <div className="flex justify-center p-8">
        <p className="text-lg">Loading prices...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex justify-center p-8">
        <p className="text-lg text-red-500">Error fetching prices: {error}</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center w-full pb-8">
      <div className="w-full max-w-4xl mx-auto mb-4 flex flex-col sm:flex-row justify-between items-center gap-4">
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search cryptocurrency..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">
            Page {currentPage} of {totalPages || 1}
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              className="px-3 py-1 bg-blue-500 text-white rounded-md disabled:opacity-50 hover:bg-blue-600 transition-colors"
            >
              Prev
            </button>
            <button
              onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              disabled={currentPage === totalPages || totalPages === 0}
              className="px-3 py-1 bg-blue-500 text-white rounded-md disabled:opacity-50 hover:bg-blue-600 transition-colors"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      <div className="w-full max-w-4xl mx-auto border border-gray-300 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <Table>
            <TableCaption>Real-Time Cryptocurrency Prices</TableCaption>
            <TableHeader>
              <TableRow className="border-b border-gray-300">
                <TableHead className="w-[180px] text-left pl-4">Cryptocurrency</TableHead>
                <TableHead className="text-right">Price (USD)</TableHead>
                <TableHead className="text-center w-[100px]">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {currentCryptos.length > 0 ? (
                currentCryptos.map(([crypto, price]) => (
                  <TableRow key={crypto} className="border-b border-gray-200 hover:bg-gray-50">
                    <TableCell className="font-medium pl-4">{crypto}</TableCell>
                    <TableCell className="text-right">{`$${price.toFixed(2)}`}</TableCell>
                    <TableCell className="text-center">
                      <button 
                        onClick={() => setSelectedCrypto(crypto)} 
                        className="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 transition-colors"
                      >
                        Buy
                      </button>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={3} className="text-center py-4">
                    {searchTerm ? "No cryptocurrencies match your search" : "No cryptocurrencies available"}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {selectedCrypto && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md overflow-hidden">
            <div className="p-6">
              <h3 className="text-xl font-bold mb-4">Buy {selectedCrypto}</h3>
              <BuyCryptoForm 
                crypto={selectedCrypto} 
                onBuy={handleBuy} 
                onCancel={() => setSelectedCrypto(null)} 
              />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default CryptoPrices