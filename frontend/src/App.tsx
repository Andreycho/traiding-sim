import React from "react";
import { Toaster } from "@/components/ui/sonner"
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import AccountHoldings from "./components/AccountHoldings";
import TransactionHistory from "./components/TransactionHistory";
import Analysis from "./components/Analysis";

const App: React.FC = () => {
  return (
    <>
      <Router>
      <div>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/holdings" element={<AccountHoldings />} />
          <Route path="/transactions" element={<TransactionHistory />} />
          <Route path="/analysis" element={<Analysis />} />
        </Routes>
      </div>
    </Router>
    <Toaster richColors position="top-right" />
    </>
  );
};

export default App;
