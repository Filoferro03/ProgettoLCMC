push 0
push 4
push 3
mult
push 10
lfp
push -3
add
lw
lfp
push -2
add
lw
bleq label0
push 0
b label1
label0:
push 1
label1:
lfp
push -2
add
lw
push 5
bleq label2
push 0
b label3
label2:
push 1
label3:
lfp
push -4
add
lw
push 0
beq label4
push 1
push 0
beq label4
push 1
b label5
label4:
push 0
label5:
lfp
push -6
add
lw
push 1
beq label6
lfp
push -5
add
lw
push 1
beq label6
push 0
b label7
label6:
push 1
label7:
print
halt