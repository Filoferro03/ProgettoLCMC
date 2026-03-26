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
push function1
lhp
sw
lhp
push 1
add
shp
push function2
lhp
sw
lhp
push 1
add
shp
push 1
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
lhp
sw
lhp
push 1
add
shp
push 10000
push -2
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
stm
sra
pop
pop
sfp
ltm
lra
js

function1:
cfp
lra
push 10
push 20
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
push 10000
push -3
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
stm
sra
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
push -3
add
lw
stm
sra
pop
sfp
ltm
lra
js