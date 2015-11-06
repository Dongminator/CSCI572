import sys

num_lines = sum(1 for line in open('input.txt'))
count = 0

inf = open("input.txt",'r')
outf = open("output.json",'w')

s = '[\n'
for line in inf:
    count += 1
    node = line.split()
    s += '{"name":"' + node[0] + '","imports":["'
    for idx,item in enumerate(node):
        if idx > 0 and idx < len(node) - 1:
            s += (item + '","') 
        elif idx == len(node) - 1:
            s += (item + '"]}')
    if count < num_lines:
        s += ',\n'
    else:
        s += '\n]'

outf.write(s)
    
inf.close()
outf.close()
