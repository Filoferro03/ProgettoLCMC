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
push 4
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
push 20
push 5
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
push -4
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
push 1
beq label0
lfp
push -6
add
lw
b label1
label0:
lfp
push -5
add
lw
label1:
lfp
lfp
push -7
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
push 0
stm
sra
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
push -2
add
lw
lfp
lw
push -2
add
lw
mult
stm
sra
pop
sfp
ltm
lra
js

function2:
cfp
lra
push 3
lfp
lw
push -2
add
lw
lfp
lw
push -2
add
lw
mult
mult
stm
sra
pop
sfp
ltm
lra
js