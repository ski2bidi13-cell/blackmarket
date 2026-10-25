"""Генерирует простые 16x16 текстуры без внешних библиотек."""
import os
import random
import struct
import zlib

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "blackmarket", "textures")
random.seed(7)


def write_png(path, px):
    raw = b"".join(b"\x00" + bytes(c for p in row for c in p) for row in px)

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw))
            + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(data)


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c) + (255,)


def make(mask, base):
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            m = mask(x, y)
            if not m:
                row.append((0, 0, 0, 0))
            else:
                k = 1.0 + (7 - y) * 0.03 + random.uniform(-0.08, 0.08)
                if m == 2:
                    k *= 0.7
                if m == 3:
                    k *= 1.25
                row.append(shade(base, k))
        rows.append(row)
    return rows


def circle(cx, cy, r):
    return lambda x, y: (x - cx) ** 2 + (y - cy) ** 2 <= r * r


def cash(x, y):
    if 2 <= x <= 13 and 4 <= y <= 11:
        if x in (2, 13) or y in (4, 11):
            return 2
        if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 5:
            return 3
        return 1
    return 0


def organ(x, y):
    a = circle(6, 8, 4)(x, y)
    b = circle(10, 7, 3.3)(x, y)
    c = circle(8, 11, 3)(x, y)
    if a or b or c:
        return 3 if (x + y) % 5 == 0 else 1
    return 0


def smoke(x, y):
    if circle(8, 9, 4)(x, y) or circle(5, 7, 3)(x, y) or circle(11, 6, 3)(x, y):
        return 3 if (x * y) % 7 == 0 else 1
    return 0


def dust(x, y):
    h = 12 - y
    if 3 <= y <= 13 and abs(x - 7.5) <= (y - 3) * 0.6 + 0.5:
        return 3 if (x + y) % 4 == 0 else 1
    return 0


def crystal(x, y):
    d = abs(x - 7.5) + abs(y - 7.5)
    if d <= 6:
        return 3 if x < 8 and y < 8 else (2 if d > 5 else 1)
    return 0


def bribe(x, y):
    if 2 <= x <= 13 and 4 <= y <= 12:
        if x in (2, 13) or y in (4, 12):
            return 2
        if y == 5 + int(abs(x - 7.5) * 0.6):
            return 2
        return 1
    return 0


write_png(os.path.join(OUT, "item", "dirty_cash.png"), make(cash, (70, 160, 70)))
write_png(os.path.join(OUT, "item", "organ.png"), make(organ, (190, 50, 70)))
write_png(os.path.join(OUT, "item", "green_smoke.png"), make(smoke, (90, 200, 110)))
write_png(os.path.join(OUT, "item", "red_dust.png"), make(dust, (210, 40, 40)))
write_png(os.path.join(OUT, "item", "blue_crystal.png"), make(crystal, (70, 120, 240)))
write_png(os.path.join(OUT, "item", "bribe.png"), make(bribe, (225, 215, 160)))

crate = []
for y in range(16):
    row = []
    for x in range(16):
        edge = x in (0, 15) or y in (0, 15) or x == y or x == 15 - y
        base = (40, 40, 44) if edge else (70, 52, 38)
        k = 1.0 + random.uniform(-0.08, 0.08)
        row.append(shade(base, k))
    crate.append(row)
write_png(os.path.join(OUT, "block", "market_crate.png"), crate)
print("textures ok")
