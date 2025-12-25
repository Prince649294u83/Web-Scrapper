import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import "./App.css";

export default function App() {
    const [url, setUrl] = useState("");
    const [selector, setSelector] = useState("");
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);

    const scrape = async () => {
        setResults([]);
        setLoading(true);

        try {
            const res = await fetch("http://localhost:8081/api/scrape", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url, selector }),
            });

            const data = await res.json();
            setResults(data);
        } catch {
            alert("Scraping failed");
        } finally {
            setLoading(false);
        }
    };

    const downloadCSV = async () => {
        const res = await fetch("http://localhost:8081/api/scrape/csv", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ url, selector }),
        });

        const blob = await res.blob();
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = "scrape-results.csv";
        link.click();
    };

    const presets = [
        { label: "Headlines (h1, h2)", value: "h1, h2" },
        { label: "All Links", value: "a" },
        { label: "Main Text", value: "p" },
        { label: "List Items", value: "li" },
        { label: "Product Titles", value: ".product-title, h2" },
        { label: "Product Prices", value: ".price, .amount" },
    ];

    return (
        <div className="page">
            {/* STICKY HEADER */}
            <header className="header">
                <div>
                    <span className="tool-badge">TOOLS</span>
                    <h1>Web Scraper</h1>
                    <p>Extract data from any website using CSS selectors.</p>
                </div>

                <div className="header-actions">
                    <button className="ghost" onClick={() => setResults([])}>Clear</button>
                    <button className="primary" onClick={downloadCSV}>Export CSV</button>
                </div>
            </header>

            {/* SPLIT LAYOUT */}
            <div className="layout full">
            {/* LEFT PANEL */}
                <motion.section
                    className="panel glass"
                    initial={{ opacity: 0, x: -30 }}
                    animate={{ opacity: 1, x: 0 }}
                >
                    <h3>Configuration</h3>

                    <label>Website URL</label>
                    <input
                        placeholder="https://example.com"
                        value={url}
                        onChange={(e) => setUrl(e.target.value)}
                    />

                    <label>CSS Selector</label>
                    <input
                        placeholder="h1, p, a"
                        value={selector}
                        onChange={(e) => setSelector(e.target.value)}
                    />

                    <button className="primary full" onClick={scrape}>
                        Start Scraping
                    </button>

                    <div className="presets">
                        {presets.map((p) => (
                            <button key={p.label} onClick={() => setSelector(p.value)}>
                                {p.label}
                            </button>
                        ))}
                    </div>
                </motion.section>

                {/* RIGHT PANEL */}
                <motion.section
                    className="panel glass"
                    initial={{ opacity: 0, x: 30 }}
                    animate={{ opacity: 1, x: 0 }}
                >
                    <h3>Results</h3>

                    {/* SKELETON LOADER */}
                    {loading && (
                        <div className="skeleton-list">
                            {[1, 2, 3, 4].map((i) => (
                                <div key={i} className="skeleton" />
                            ))}
                        </div>
                    )}

                    {/* RESULTS TABLE */}
                    <AnimatePresence>
                        {!loading && results.length > 0 && (
                            <motion.table
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 1 }}
                                exit={{ opacity: 0 }}
                            >
                                <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Tag</th>
                                    <th>Text</th>
                                    <th>Title</th>
                                </tr>
                                </thead>
                                <tbody>
                                {results.map((r) => (
                                    <tr key={r.index}>
                                        <td>{r.index}</td>
                                        <td>{r.tag}</td>
                                        <td>{r.text}</td>
                                        <td>{r.pageTitle}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </motion.table>
                        )}
                    </AnimatePresence>

                    {!loading && results.length === 0 && (
                        <div className="empty">
                            <span>🔍</span>
                            <p>No results to show</p>
                        </div>
                    )}
                </motion.section>
            </div>
        </div>
    );
}
