push 0
lhp
push function0
lhp
sw
lhp
push 1
add
shp
lhp
push function1
lhp
sw
lhp
push 1
add
shp
lhp
push function2
lhp
sw
lhp
push 1
add
shp
push 10
lhp
sw
lhp
push 1
add
shp
lfp
push-2
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
push 0
lhp
sw
lhp
push 1
add
shp
lfp
push-3
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
push 1
push 10
lhp
sw
lhp
push 1
add
shp
lhp
sw
lhp
push 1
add
shp
lfp
push-4
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
lfp
push 0
push 1
lfp
push -6
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
print
halt

function0:
cfp
lra
lfp
lw
push -1
add
lw
stm
sra
pop
pop
pop
sfp
ltm
lra
js

function1:
cfp
lra
lfp
lw
push -1
add
lw
stm
sra
pop
pop
pop
sfp
ltm
lra
js

function2:
cfp
lra
lfp
lw
push -2
add
lw
stm
sra
pop
sfp
ltm
lra
js