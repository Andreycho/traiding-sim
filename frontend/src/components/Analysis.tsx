import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { ArrowLeft } from 'lucide-react'

type ProfitLossData = Record<string, number> | null

const Analysis = () => {
  const navigate = useNavigate()
  const [profitLossData, setProfitLossData] = useState<ProfitLossData>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [balance, setBalance] = useState<number>(0)

  useEffect(() => {
    const fetchProfitLoss = async () => {
      try {
        setLoading(true)
        const response = await fetch("/api/profit-loss")
        
        if (!response.ok) {
          throw new Error("Failed to fetch profit/loss data")
        }
        
        const data = await response.json()
        setProfitLossData(data)
      } catch (error) {
        console.error("Error fetching profit/loss data:", error)
        setError("Failed to load profit/loss data")
      } finally {
        setLoading(false)
      }
    }

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

    fetchProfitLoss()
    fetchBalance()
  }, [])

  if (loading) {
    return (
      <div className="w-screen min-h-screen flex items-center justify-center">
        <div className="text-center py-8">Loading profit/loss data...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="w-screen min-h-screen flex items-center justify-center">
        <div className="text-center py-8 text-red-500">{error}</div>
      </div>
    )
  }

  const isEmpty = !profitLossData || Object.keys(profitLossData).length === 0

  return (
    <div className="w-screen min-h-screen left-0 top-0 absolute bg-gray-50">
      <div className="w-full p-6 bg-gray-100 shadow-md flex justify-between items-center">
        <button
          onClick={() => navigate("/")}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white rounded-lg shadow-md"
        >
          <ArrowLeft size={18} />
          Back to Home
        </button>
        
        <h1 className="text-2xl font-bold text-black">Profit/Loss Analysis</h1>
        
        <div className="text-xl font-semibold">
          Balance: ${balance.toFixed(2)}
        </div>
      </div>

      <hr className="border-t border-gray-300 w-full mx-auto mb-6" />

      <div className="w-full flex justify-center px-6 py-8">
        <div className="w-full max-w-4xl bg-white rounded-lg shadow-md p-8">
          {isEmpty ? (
            <div className="text-center py-12 text-gray-600 text-xl">
              You have not made any purchases yet
            </div>
          ) : (
            <div>
              <p className="text-xl mb-6 text-gray-700">
                Here is your profit/loss based on your purchases:
              </p>
              <div className="space-y-6">
                {Object.entries(profitLossData).map(([pair, value]) => (
                  <div 
                    key={pair} 
                    className="flex justify-between items-center p-5 border rounded-lg bg-gray-50"
                  >
                    <span className="font-medium text-xl">{pair}</span>
                    <span className={`font-bold text-xl ${value >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {value >= 0 ? '+' : ''}{value.toFixed(2)} $
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Analysis