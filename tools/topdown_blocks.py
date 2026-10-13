"""Top down picture of a generated world from its region files (no game): the top block of every
column coloured by kind (grass green, road dark, iron bars red, BlockMetal_2 black, concertina wire
light red, hazard barriers orange, water blue, sidewalk slabs light grey, the rest grey).
usage: python3 tools/topdown_blocks.py WORLD X0 Z0 X1 Z1 OUT.png [PX_PER_BLOCK]   (needs Pillow)"""
import sys
sys.path.insert(0, __import__('os').path.dirname(__import__('os').path.abspath(__file__)))
from worldcheck import World
from PIL import Image
w = World(sys.argv[1]); x0, z0, x1, z1 = map(int, sys.argv[2:6]); out = sys.argv[6]; S = int(sys.argv[7]) if len(sys.argv) > 7 else 2
reg = w.registry(); name = {v: k for k, v in reg.items()}
col = {}
def c(n):
    if n is None: return (0, 0, 0)
    if n in col: return col[n]
    k = n.lower()
    r = (150, 150, 150)
    if 'grass' in k and 'tall' not in k: r = (90, 140, 60)
    elif 'blockroad' in k or 'road' in k: r = (50, 50, 55)
    elif 'iron_bars' in k: r = (220, 30, 30)
    elif 'metal_2' in k: r = (0, 0, 0)
    elif 'concertina' in k: r = (255, 80, 80)
    elif 'hazardbarrier' in k: r = (255, 160, 0)
    elif 'water' in k: r = (50, 90, 220)
    elif 'stone_slab' in k: r = (205, 205, 200)
    elif 'leaves' in k or 'log' in k: r = (40, 90, 40)
    elif 'dirt' in k or 'sand' in k or 'gravel' in k: r = (140, 120, 90)
    col[n] = r; return r
img = Image.new('RGB', ((x1 - x0) * S, (z1 - z0) * S))
px = img.load(); fence = 0
for x in range(x0, x1):
    for z in range(z0, z1):
        n = None
        for y in range(130, 30, -1):
            b = w.block(x, y, z)
            if b is None: break
            if b:
                n = name.get(b, str(b))
                if 'tallgrass' in n or 'double_plant' in n or 'flower' in n: continue
                break
        rgb = c(n)
        for i in range(S):
            for j in range(S): px[(x - x0) * S + i, (z - z0) * S + j] = rgb
img.save(out); print('saved', out)
