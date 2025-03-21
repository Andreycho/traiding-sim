async function fetchPrices() {
    try {
        const response = await fetch('/api/prices');
        const prices = await response.json();

        const tableBody = document.querySelector('#prices-table tbody');
        tableBody.innerHTML = '';

        for (const [crypto, price] of Object.entries(prices)) {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${crypto}</td>
                <td>$${price.toFixed(2)}</td>
            `;
            tableBody.appendChild(row);
        }
    } catch (error) {
        console.error('Error fetching prices:', error);
    }
}

setInterval(fetchPrices, 5000);

fetchPrices();