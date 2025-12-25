import { useState } from "react";

function App() {
    const [url, setUrl] = useState("");
    const [selector, setSelector] = useState("");
    const [results, setResults] = useState([]);
    const [summary, setSummary] = useState("");
    const [loading, setLoading] = useState(false);
    const [summaryLoading, setSummaryLoading] = useState(false);
    const [error, setError] = useState("");

    // SCRAPE JSON DATA
    const scrape = async () => {
        setError("");
        setResults([]);
        setSummary("");
        setLoading(true);

        try {
            const response = await fetch("http://localhost:8081/api/scrape", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url, selector }),
            });

            if (!response.ok) throw new Error();

            const data = await response.json();
            setResults(data);
        } catch {
            setError("❌ Unable to scrape. Please check the URL and selector.");
        } finally {
            setLoading(false);
        }
    };

    // CSV DOWNLOAD
    const downloadCSV = async () => {
        try {
            const response = await fetch("http://localhost:8081/api/scrape/csv", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url, selector }),
            });

            if (!response.ok) throw new Error();

            const blob = await response.blob();
            const downloadUrl = window.URL.createObjectURL(blob);

            const a = document.createElement("a");
            a.href = downloadUrl;
            a.download = "scrape-results.csv";
            document.body.appendChild(a);
            a.click();
            a.remove();
        } catch {
            alert("❌ CSV download failed");
        }
    };

    // 🤖 AI SUMMARY
    const generateSummary = async () => {
        setSummary("");
        setSummaryLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8081/api/scrape/summary",
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ url, selector }),
                }
            );

            if (!response.ok) throw new Error();

            const text = await response.text();
            setSummary(text);
        } catch {
            setSummary("❌ Failed to generate AI summary.");
        } finally {
            setSummaryLoading(false);
        }
    };

    return (
        <div style={pageStyle}>
            <div style={cardStyle}>
                <h1 style={{ textAlign: "center" }}>Web Intelligence App</h1>

                <label>Website URL</label>
                <input
                    style={inputStyle}
                    placeholder="https://example.com"
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                />

                <label style={{ marginTop: 12 }}>CSS Selector</label>
                <input
                    style={inputStyle}
                    placeholder="p"
                    value={selector}
                    onChange={(e) => setSelector(e.target.value)}
                />

                <button onClick={scrape} style={primaryButton}>
                    Scrape Website
                </button>

                {loading && <p style={{ color: "#facc15" }}>⏳ Scraping…</p>}
                {error && <p style={{ color: "#ef4444" }}>{error}</p>}

                {results.length > 0 && (
                    <>
                        <table style={tableStyle}>
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Tag</th>
                                <th>Text</th>
                                <th>Page Title</th>
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
                        </table>

                        <button onClick={downloadCSV} style={csvButton}>
                            ⬇️ Download CSV
                        </button>

                        <button onClick={generateSummary} style={aiButton}>
                            🤖 Generate AI Summary
                        </button>

                        {summaryLoading && (
                            <p style={{ color: "#38bdf8" }}>
                                🤖 Generating summary…
                            </p>
                        )}

                        {summary && (
                            <div style={summaryBox}>
                                <h3>AI Summary</h3>
                                <pre style={{ whiteSpace: "pre-wrap" }}>
                                    {summary}
                                </pre>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

/* STYLES */

const pageStyle = {
    minHeight: "100vh",
    background: "#121212",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    color: "#fff",
};

const cardStyle = {
    width: "90%",
    maxWidth: 900,
    background: "#1e1e1e",
    padding: 30,
    borderRadius: 12,
};

const inputStyle = {
    width: "100%",
    padding: 10,
    marginTop: 6,
    marginBottom: 12,
    borderRadius: 6,
    border: "1px solid #333",
    background: "#000",
    color: "#fff",
};

const primaryButton = {
    width: "100%",
    padding: 12,
    background: "#fff",
    color: "#000",
    fontWeight: "bold",
    borderRadius: 8,
    cursor: "pointer",
};

const csvButton = {
    marginTop: 16,
    padding: 10,
    width: "100%",
    background: "#22c55e",
    borderRadius: 6,
    fontWeight: "bold",
};

const aiButton = {
    marginTop: 10,
    padding: 10,
    width: "100%",
    background: "#38bdf8",
    borderRadius: 6,
    fontWeight: "bold",
};

const tableStyle = {
    width: "100%",
    marginTop: 20,
    borderCollapse: "collapse",
};

const summaryBox = {
    marginTop: 16,
    padding: 16,
    background: "#0f172a",
    borderRadius: 8,
};

export default App;
