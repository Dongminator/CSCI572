# deduplication in multiple directories recursively
# usage: python3 deduplicate.py <root_directory>

import sys
import os
import time
import hashlib

if not len(sys.argv)==2:
    print("usage: python3 deduplicate.py <root_directory>")
    exit()

start = time.time()
BLOCKSIZE = 65536
mydict = dict()
repeat = 0

rootdir = sys.argv[1]
for root, subdirs, files in os.walk(rootdir):
    for name in files:
        hasher = hashlib.sha1()
        with open(os.path.join(root, name), 'rb') as tempfile:
            buf = tempfile.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = tempfile.read(BLOCKSIZE)
        tempfile.close()
        hash_value = hasher.hexdigest()
        idx = mydict.get(hash_value,-1)
        if idx == -1:
            mydict[hash_value] = 1
        else:
            repeat += 1
            os.remove(os.path.join(root, name))
            print("removed duplicated file: "+ os.path.join(root, name))

print("done")
print("duplicated files: %d"%(repeat))
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))



