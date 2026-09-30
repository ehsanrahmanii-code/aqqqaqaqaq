"""ARSAM boot helper for Chaquopy.

Starts the Flask analysis engine on the device loopback interface.
Persistent data is forced to /storage/emulated/0/AAA by arsam_engine.py.
"""
from __future__ import annotations

import os
import sys
import threading
import traceback

# Force AAA on Android before engine import side-effects.
os.environ.setdefault("TITAN_HOME", "/storage/emulated/0/AAA")
os.environ.setdefault("TITAN_HOST", "127.0.0.1")
os.environ.setdefault("TITAN_PORT", "8080")

_SERVER_STARTED = False
_LOCK = threading.Lock()


def start_server(port: int = 8080, host: str = "127.0.0.1") -> None:
    """Import engine and run Flask. Blocks the calling thread (service worker)."""
    global _SERVER_STARTED
    with _LOCK:
        if _SERVER_STARTED:
            return
        _SERVER_STARTED = True

    os.environ["TITAN_PORT"] = str(int(port))
    os.environ["TITAN_HOST"] = str(host)

    try:
        # Ensure AAA directories exist as early as possible.
        aaa = "/storage/emulated/0/AAA"
        for sub in ("", "data", "cache", "memory", "secrets"):
            path = aaa if not sub else f"{aaa}/{sub}"
            try:
                os.makedirs(path, exist_ok=True)
            except OSError:
                pass

        import arsam_engine as engine

        # Override host/port on the module if already bound at import time.
        try:
            engine.HOST = str(host)
            engine.PORT = int(port)
        except Exception:
            pass

        # run_titan already starts live engine + autonomous loops
        engine.run_titan(open_browser=False)
    except Exception:
        traceback.print_exc()
        raise


def ping() -> str:
    return "ARSAM_OK"
