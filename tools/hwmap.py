#!/usr/bin/env python3
"""Highways between cities (city/Highways.java) recomputed from the seed.

    python3 tools/hwmap.py SEED RADIUS_REGIONS [--rails] [--old-layout]

Prints the region map (. wild, c civilian, C city, M military) and every
highway line near the origin: ('x', chunk row) or ('z', chunk column) with
its chunk range. Same rules as Highways.java: one line per region row /
column (offset 0 or 8 chunks), joining two city regions at most maxgap()
non city regions apart, plus an L link from every isolated city (no city
region within maxgap() + 1 straight east, west, north or south) to its
nearest diagonal city at most 2 regions away on both axes (link(), printed
as ('L', A, B, x first)). The sectors come from tools/citylayout.py: the
sized city layout of worlds made since v0.60.0 (maxgap() 5), or with
--old-layout the one of older worlds, where every sector rolls city on its
own (maxgap() 3). edgescan.py uses at() to skip highway chunks. With
--rails also the railway (city/Rails.java): every station (block position
of its track) and the chunk kinds. Rails.stationX also moves a station off
city rivers and out of airport blocks, which is not modelled here: stations
there can differ from the game's.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import citylayout as cl  # noqa: E402

M=(1<<64)-1
def s64(v):
    v&=M; return v-(1<<64) if v>>63 else v
def hl(seed,x,z):
    v=(seed ^ s64(x*0x9E3779B97F4A7C15) ^ s64(z*0xC2B2AE3D27D4EB4F))&M
    v^=v>>31; v=(v*0xBF58476D1CE4E5B9)&M; v^=v>>29; v=(v*0x94D049BB133111EB)&M; v^=v>>32
    return v & ((1<<63)-1)
def maxgap():
    """Highways.maxGap: cities of many sizes sit on a 5 sector grid, so longer highways there."""
    return 5 if cl.ON else 3
_cities={}
def city(seed,rx,rz):
    k=(seed,cl.EPOCH,rx,rz)
    if k not in _cities: _cities[k]=cl.region_sector(seed,rx,rz)==cl.CITY
    return _cities[k]
def line(seed,r,salt): return r*16+(hl(seed^salt,r,0)&1)*8
def spans(seed,r,o,ax):
    c=(lambda a:city(seed,a,o)) if ax else (lambda a:city(seed,o,a))
    g=maxgap(); lo,hi=r-1,r+1
    while lo>=r-g and not c(lo): lo-=1
    while hi<=r+g and not c(hi): hi+=1
    return c(lo) and c(hi) and hi-lo-1<=g
_links={}
def link(seed,ax,az):
    """Highways.link: (bx, bz, x_first) for an isolated city region, else None."""
    k=(seed,cl.EPOCH,ax,az)
    if k in _links: return _links[k]
    r=None
    if city(seed,ax,az) and not any(city(seed,ax+d,az) or city(seed,ax-d,az) or city(seed,ax,az+d) or city(seed,ax,az-d) for d in range(1,maxgap()+2)):
        best=None
        for dx in (-2,-1,1,2):
            for dz in (-2,-1,1,2):
                bx,bz=ax+dx,az+dz
                if best is not None and abs(dx)+abs(dz)>=best: continue
                if not city(seed,bx,bz): continue
                first=1 if clear(seed,ax,az,bx,bz,True) else 0 if clear(seed,ax,az,bx,bz,False) else -1
                if first>=0: r=(bx,bz,first==1); best=abs(dx)+abs(dz)
    _links[k]=r
    return r
def clear(seed,ax,az,bx,bz,xf):
    sx=1 if bx>ax else -1; sz=1 if bz>az else -1
    ps=[(x,az if xf else bz) for x in range(ax,bx+sx,sx)]+[(bx if xf else ax,z) for z in range(az,bz+sz,sz)]
    return all(not city(seed,*p) for p in ps if p!=(ax,az) and p!=(bx,bz))
def onL(seed,ax,az,bx,bz,xf,cx,cz,alongx):
    R=16
    row=line(seed,az if xf else bz,0x48575958); col=line(seed,bx if xf else ax,0x4857595A)
    if alongx:
        if cz!=row: return False
        lo=((ax+1)*R if bx>ax else col) if xf else (col if bx>ax else (bx+1)*R)
        hi=(col if bx>ax else ax*R-1) if xf else (bx*R-1 if bx>ax else col)
        return lo<=cx<=hi
    if cx!=col: return False
    lo=(row if bz>az else (bz+1)*R) if xf else ((az+1)*R if bz>az else row)
    hi=(bz*R-1 if bz>az else row) if xf else (row if bz>az else az*R-1)
    return lo<=cz<=hi
def onlink(seed,cx,cz,alongx):
    rx,rz=cx//16,cz//16
    for ax in range(rx-2,rx+3):
        for az in range(rz-2,rz+3):
            l=link(seed,ax,az)
            if l and onL(seed,ax,az,l[0],l[1],l[2],cx,cz,alongx): return True
    return False
def xh(seed,cx,cz):
    rz,rx=cz//16,cx//16
    return cz==line(seed,rz,0x48575958) and not city(seed,rx,rz) and spans(seed,rx,rz,True) or onlink(seed,cx,cz,True)
def zh(seed,cx,cz):
    rz,rx=cz//16,cx//16
    return cx==line(seed,rx,0x4857595A) and not city(seed,rx,rz) and spans(seed,rz,rx,False) or onlink(seed,cx,cz,False)
def at(seed, cx, cz):
    return xh(seed, cx, cz) or zh(seed, cx, cz)


# underground railway (city/Rails.java): same lines, every city region plus the highway gaps
RAIL=64-18
def xrail(seed,cx,cz):
    rz,rx=cz//16,cx//16
    return cz==line(seed,rz,0x48575958) and (city(seed,rx,rz) or spans(seed,rx,rz,True))
def zrail(seed,cx,cz):
    rz,rx=cz//16,cx//16
    return cx==line(seed,rx,0x4857595A) and (city(seed,rx,rz) or spans(seed,rz,rx,False))
def station_x(seed,rx,rz):
    return rx*16+4*(hl(seed^0x5354414E,rx,rz)%3)+2 if city(seed,rx,rz) else None
def rail_kind(seed,cx,cz):
    """Rails.kind: none, x, z, crossing, end west/east/north/south, station, platform."""
    x,z=xrail(seed,cx,cz),zrail(seed,cx,cz)
    if x and z:
        xa=xrail(seed,cx-1,cz) or xrail(seed,cx+1,cz); za=zrail(seed,cx,cz-1) or zrail(seed,cx,cz+1)
        if xa and za: return 'crossing'
        x,z=xa,not xa
    if x:
        sx=station_x(seed,cx//16,cz//16)
        if cx==sx: return 'station'
        if sx is not None and abs(cx-sx)==1: return 'platform'
        return 'end west' if not xrail(seed,cx-1,cz) else 'end east' if not xrail(seed,cx+1,cz) else 'x'
    if z:
        return 'end north' if not zrail(seed,cx,cz-1) else 'end south' if not zrail(seed,cx,cz+1) else 'z'
    return 'none'


if __name__=='__main__':
    a=[x for x in sys.argv[1:] if not x.startswith('--')]
    if '--old-layout' in sys.argv: cl.set_on(False)
    seed=int(a[0]); R=int(a[1])
    for rz in range(-R,R+1):
        print("%3d "%rz+"".join('.cCM'[cl.region_sector(seed,rx,rz)] for rx in range(-R,R+1)))
    segs=set()
    for cx in range(-R*16,R*16):
        for cz in range(-R*16,R*16):
            if xh(seed,cx,cz): segs.add(('x',cz))
            if zh(seed,cx,cz): segs.add(('z',cx))
    for rx in range(-R,R+1):
        for rz in range(-R,R+1):
            l=link(seed,rx,rz)
            if l: print(('L',(rx,rz),l[:2],'x first' if l[2] else 'z first'))
    if '--rails' in sys.argv:
        for rx in range(-R,R+1):
            for rz in range(-R,R+1):
                sx=station_x(seed,rx,rz)
                if sx is not None:
                    print('station', (sx*16+8, RAIL+1, line(seed,rz,0x48575958)*16+8))
        kinds={}
        for cx in range(-R*16,R*16):
            for cz in range(-R*16,R*16):
                k=rail_kind(seed,cx,cz)
                if k!='none': kinds.setdefault(k,[]).append((cx*16,cz*16))
        for k,v in sorted(kinds.items()): print('rail',k,len(v),v[:4])
    for s in sorted(segs):
        cs=[c for c in range(-R*16,R*16) if (xh(seed,c,s[1]) if s[0]=='x' else zh(seed,s[1],c))]
        print(s, 'chunks',min(cs),'..',max(cs),len(cs))
