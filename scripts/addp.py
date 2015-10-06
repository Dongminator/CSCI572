import sys

inf = open("weapons-seed-list.txt",'r')
outf = open("seed.txt",'w')
for line in inf:
	outf.write("http://"+line)
inf.close()
outf.close()

