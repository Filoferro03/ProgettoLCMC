push 0
push 1
push 0
lfp
push -2
add
lw
push 1
beq label0
lfp
push -3
add
lw
push 1
beq label0
push 0
b label1
label0:
push 1
label1:
print
halt