# this script extract image urls from the crawldb dump file
# the output is a url list file "image_urls"

# usage: python3 imgurl.py
# please put this script and the dump file "part-00000" in
# the same folder

import sys
import re

inf = open("part-00000",'r')
outf = open("image_urls",'w')
for line in inf:
	if line[0] == 'h':
		sample = line.split()
		if re.match(r"(.+)(jpg|JPG|png|PNG|bmp|BMP|gif|GIF|jpeg|JPEG)$",sample[0]):
			outf.write(sample[0]+"\n")
inf.close()
outf.close()
