import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import "./App.css";

export default function App() {
    const [url, setUrl] = useState("");
    const [selector, setSelector] = useState("");
    const [results, setResults] = useState([]);
    const [summary, setSummary] = useState("");
    const [loading, setLoading] = useState(false);
    const [summaryLoading, setSummaryLoading] = useState(false);

    const scrape = async () => {
        setResults([]);
        setSummary("");
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
        if (results.length === 0) return;

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

    const clearAll = () => {
        setResults([]);
        setSummary("");
    };

    const generateSummary = async () => {
        setSummary("");
        setSummaryLoading(true);

        try {
            const res = await fetch("http://localhost:8081/api/scrape/summary", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url, selector }),
            });

            const text = await res.text();
            setSummary(text);
        } catch {
            setSummary("Failed to generate summary.");
        } finally {
            setSummaryLoading(false);
        }
    };

    const presets = [
        { label: "Whole Page", value: "" },
        { label: "Headings", value: "h1, h2" },
        { label: "Paragraphs", value: "p" },
        { label: "Links", value: "a" },
        { label: "List Items", value: "li" },
    ];

    return (
        <div className="page">
            {/* HEADER */}
            <header className="header glass">
                <div>
                    <span className="tool-badge">WEB INTELLIGENCE</span>
                    <h1>Web Scraper</h1>
                    <p>Extract, analyze and summarize web content</p>
                </div>

                <div className="header-actions">
                    <button className="ghost" onClick={clearAll}>
                        Clear
                    </button>
                    <button
                        className="primary"
                        onClick={downloadCSV}
                        disabled={results.length === 0}
                    >
                        Export CSV
                    </button>
                </div>
            </header>

            {/* MAIN LAYOUT */}
            <div className="layout">
                {/* CONFIG */}
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
                        placeholder="Leave empty for full page"
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

                {/* RESULTS */}
                <motion.section
                    className="panel glass results-panel"
                    initial={{ opacity: 0, x: 30 }}
                    animate={{ opacity: 1, x: 0 }}
                >
                    <h3>Results</h3>

                    {loading && (
                        <div className="skeleton-list">
                            {[1, 2, 3, 4].map((i) => (
                                <div key={i} className="skeleton" />
                            ))}
                        </div>
                    )}

                    {!loading && results.length === 0 && (
                        <div className="empty">
                            <span>🔍</span>
                            <p>No results yet</p>
                        </div>
                    )}

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

                    {results.length > 0 && (
                        <>
                            <button className="ghost full" onClick={generateSummary}>
                                🤖 Generate AI Summary
                            </button>

                            {summaryLoading && (
                                <p className="ai-loading">Analyzing content…</p>
                            )}

                            {summary && (
                                <div className="ai-summary">
                                    <h4>AI Summary</h4>
                                    {summary.split("\n").map((line, i) => (
                                        <p key={i}>{line}</p>
                                    ))}
                                </div>
                            )}
                        </>
                    )}
                </motion.section>
            </div>
        </div>
    );
}
