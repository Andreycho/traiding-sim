import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import AccountHoldings from "./components/AccountHoldings";
import TransactionHistory from "./components/TransactionHistory";

const App: React.FC = () => {
  return (
    <Router>
      <div>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/holdings" element={<AccountHoldings />} />
          <Route path="/transactions" element={<TransactionHistory />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;
