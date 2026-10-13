#!/usr/bin/env python3
"""Textures of deciworldgen:boom_barrier (BlockBoomArm): red / white stripes 4 px each with faint
noise. boom_barrier.png stripes across u (arms lying along x or z), _up.png
across v (a raised arm). Run from the project root; writes into dev/src/main/resources."""
import random
from PIL import Image

OUT = "dev/src/main/resources/assets/deciworldgen/textures/blocks/"
RED, WHITE = (176, 34, 30), (222, 220, 214)
random.seed(7)


def tone(c):
    n = random.randint(-7, 7)
    return tuple(max(0, min(255, v + n)) for v in c)


def make(name, across_u):
    im = Image.new("RGB", (16, 16))
    for v in range(16):
        for u in range(16):
            k = u if across_u else v
            im.putpixel((u, v), tone(RED if (k // 4) % 2 == 0 else WHITE))
    im.save(OUT + name)


make("boom_barrier.png", True)
make("boom_barrier_up.png", False)
print("written")
