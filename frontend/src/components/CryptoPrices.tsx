// CryptoPrices.tsx
import React, { useEffect, useState } from "react";
import { Table, TableBody, TableCaption, TableCell, TableHeader, TableRow, TableHead } from "@/components/ui/table";
import BuyCryptoForm from "./BuyCryptoForm";

interface Prices {
  [key: string]: number;
}

interface CryptoPricesProps {
  setBalance: React.Dispatch<React.SetStateAction<number>>;
}

const CryptoPrices: React.FC<CryptoPricesProps> = ({ setBalance }) => {
  const [prices, setPrices] = useState<Prices>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCrypto, setSelectedCrypto] = useState<string | null>(null);

  useEffect(() => {
    const fetchPrices = async () => {
      try {
        const response = await fetch("/api/prices");
        if (!response.ok) throw new Error("Network response was not ok");

        const data: Prices = await response.json();
        setPrices(data);
        setLoading(false);
      } catch (error) {
        setError((error as Error).message);
        setLoading(false);
      }
    };

    fetchPrices();
    const interval = setInterval(fetchPrices, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleBuy = async (crypto: string, amount: number) => {
    if (!amount || amount < 0.1) {
      alert("Please enter a valid amount (at least 0.1).");
      return;
    }

    try {
      const response = await fetch(`/api/buy?crypto=${crypto}&amount=${amount}`, {
        method: "POST",
      });
      if (!response.ok) throw new Error("Failed to buy cryptocurrency");

      const message = await response.text();
      alert(message);

      await fetchBalance();

      setSelectedCrypto(null);
    } catch (error) {
      alert(`Error buying cryptocurrency: ${(error as Error).message}`);
    }
  };

  const fetchBalance = async () => {
    try {
      const response = await fetch("/api/balance");
      if (!response.ok) throw new Error("Failed to fetch balance");

      const balanceData = await response.json();
      if (typeof balanceData === "number") {
        setBalance(balanceData);
      } else if (balanceData && balanceData.balance) {
        setBalance(balanceData.balance);
      } else {
        throw new Error("Invalid balance data format");
      }
    } catch (error) {
      console.error("Error fetching balance:", error);
    }
  };

  if (loading) {
    return <p>Loading prices...</p>;
  }

  if (error) {
    return <p>Error fetching prices: {error}</p>;
  }

  return (
    <div>
      <Table>
        <TableCaption>Real-Time Cryptocurrency Prices</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[100px]">Cryptocurrency</TableHead>
            <TableHead>Price (USD)</TableHead>
            <TableHead>Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {Object.entries(prices).map(([crypto, price]) => (
            <TableRow key={crypto}>
              <TableCell className="font-medium">{crypto}</TableCell>
              <TableCell>{`$${price.toFixed(2)}`}</TableCell>
              <TableCell>
                <button
                  onClick={() => setSelectedCrypto(crypto)}
                  className="bg-blue-500 text-white p-2 rounded"
                >
                  Buy
                </button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {selectedCrypto && (
        <div className="mt-4">
          <BuyCryptoForm
            crypto={selectedCrypto}
            onBuy={handleBuy}
            onCancel={() => setSelectedCrypto(null)}
          />
        </div>
      )}
    </div>
  );
};

export default CryptoPrices;
