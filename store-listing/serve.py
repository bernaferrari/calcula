"""Serve the repository for capture and artwork previews on a dedicated local origin."""
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from pathlib import Path
import os
os.chdir(Path(__file__).resolve().parents[1])
ThreadingHTTPServer(('127.0.0.1', 8787), SimpleHTTPRequestHandler).serve_forever()
