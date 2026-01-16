import { useState, useMemo } from "react";
import "./App.css";

const PAGE_SIZE = 25;

export default function App() {
    const [url, setUrl] = useState("");
    const [prompt, setPrompt] = useState("");
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);

    const [search, setSearch] = useState("");
    const [page, setPage] = useState(1);

    /* =========================
       SCRAPE
       ========================= */
    const scrape = async () => {
        if (!url || !prompt) {
            alert("Please enter both URL and extraction request");
            return;
        }

        setLoading(true);
        setResult(null);
        setSearch("");
        setPage(1);

        try {
            const response = await fetch(
                "http://localhost:8081/api/smart-scrape/preview",
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        targetUrl: url,
                        userPrompt: prompt,
                    }),
                }
            );

            const data = await response.json();
            setResult(data);
        } catch {
            alert("Failed to extract content");
        } finally {
            setLoading(false);
        }
    };

    /* =========================
       CSV DOWNLOAD
       ========================= */
    const downloadCsv = async () => {
        const response = await fetch(
            "http://localhost:8081/api/smart-scrape/export/csv",
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ targetUrl: url, userPrompt: prompt }),
            }
        );

        const blob = await response.blob();
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = "scraped_data.csv";
        link.click();
    };

    /* =========================
       FLATTEN DATA (MEMOIZED)
       ========================= */
    const rows = useMemo(() => {
        if (!result?.pages) return [];

        const all = [];

        result.pages.forEach((page) => {
            page.headings?.forEach((h) =>
                all.push({ page: page.pageUrl, type: "Heading", content: h })
            );
            page.paragraphs?.forEach((p) =>
                all.push({ page: page.pageUrl, type: "Paragraph", content: p })
            );
            page.links?.forEach((l) =>
                all.push({ page: page.pageUrl, type: "Link", content: l })
            );
            page.images?.forEach((i) =>
                all.push({ page: page.pageUrl, type: "Image", content: i })
            );
        });

        return all;
    }, [result]);

    /* =========================
       SEARCH (FAST)
       ========================= */
    const filtered = useMemo(() => {
        if (!search) return rows;
        const q = search.toLowerCase();
        return rows.filter(
            (r) =>
                r.content.toLowerCase().includes(q) ||
                r.type.toLowerCase().includes(q) ||
                r.page.toLowerCase().includes(q)
        );
    }, [rows, search]);

    /* =========================
       PAGINATION
       ========================= */
    const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));

    const paginated = useMemo(() => {
        const start = (page - 1) * PAGE_SIZE;
        return filtered.slice(start, start + PAGE_SIZE);
    }, [filtered, page]);

    /* =========================
       RENDER
       ========================= */
    return (
        <div className="page">
            <header className="header glass">
                <span className="tool-badge">WEB INTELLIGENCE</span>
                <h1>Smart Web Scraper</h1>
                <p>Extract structured web content using AI-assisted intent</p>
            </header>

            <div className="layout">
                {/* INPUT */}
                <section className="panel glass">
                    <label htmlFor="url">Website URL</label>
                    <input
                        id="url"
                        value={url}
                        onChange={(e) => setUrl(e.target.value)}
                        placeholder="https://example.com"
                    />

                    <label htmlFor="prompt">What do you want to extract?</label>
                    <textarea
                        id="prompt"
                        value={prompt}
                        onChange={(e) => setPrompt(e.target.value)}
                        placeholder="Extract main content, images and links"
                    />

                    <button
                        className="primary full"
                        onClick={scrape}
                        disabled={loading}
                    >
                        {loading ? "Analyzing…" : "Extract"}
                    </button>
                </section>

                {/* RESULTS */}
                {result && (
                    <section className="panel glass results-panel">
                        <h3>Extracted Data</h3>

                        {/* SEARCH */}
                        <input
                            className="full"
                            placeholder="Search across all extracted content…"
                            value={search}
                            onChange={(e) => {
                                setSearch(e.target.value);
                                setPage(1);
                            }}
                            aria-label="Search extracted data"
                        />

                        {/* TABLE CONTAINER (SCROLL SAFE) */}
                        <div style={{ overflowX: "auto", marginTop: 16 }}>
                            <table className="data-table" role="table">
                                <thead>
                                <tr>
                                    <th scope="col">Page</th>
                                    <th scope="col">Type</th>
                                    <th scope="col">Content</th>
                                </tr>
                                </thead>
                                <tbody>
                                {paginated.map((row, i) => (
                                    <tr key={i}>
                                        <td title={row.page}>{row.page}</td>
                                        <td>{row.type}</td>
                                        <td>
                                            {row.type === "Image" ? (
                                                <img
                                                    src={row.content}
                                                    alt="Scraped visual content"
                                                    className="table-image"
                                                    loading="lazy"
                                                />
                                            ) : row.type === "Link" ? (
                                                <a
                                                    href={row.content}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                >
                                                    {row.content}
                                                </a>
                                            ) : (
                                                <span>{row.content}</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {/* PAGINATION */}
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                marginTop: 16,
                            }}
                        >
                            <button
                                disabled={page === 1}
                                onClick={() => setPage((p) => p - 1)}
                            >
                                Previous
                            </button>

                            <span>
                                Page {page} of {totalPages}
                            </span>

                            <button
                                disabled={page === totalPages}
                                onClick={() => setPage((p) => p + 1)}
                            >
                                Next
                            </button>
                        </div>

                        <button className="primary full" onClick={downloadCsv}>
                            Download CSV
                        </button>

                        {/* AI SUMMARY — ALWAYS LAST */}
                        {result.summary && (
                            <div className="ai-summary" style={{ marginTop: 32 }}>
                                <h4>AI Summary</h4>
                                <p>{result.summary}</p>
                            </div>
                        )}
                    </section>
                )}
            </div>
        </div>
    );
}
